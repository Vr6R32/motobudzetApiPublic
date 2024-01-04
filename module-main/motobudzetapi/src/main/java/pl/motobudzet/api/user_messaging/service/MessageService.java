package pl.motobudzet.api.user_messaging.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.motobudzet.api.emailSender.SpringMailSenderService;
import pl.motobudzet.api.emailSender.EmailMessageRequest;
import pl.motobudzet.api.emailSender.service.KafkaServiceInterface;
import pl.motobudzet.api.user_account.entity.AppUser;
import pl.motobudzet.api.user_account.service.AppUserCustomService;
import pl.motobudzet.api.user_messaging.dto.ConversationMessageDTO;
import pl.motobudzet.api.user_messaging.entity.Conversation;
import pl.motobudzet.api.user_messaging.entity.ConversationMessage;
import pl.motobudzet.api.user_messaging.repository.ConversationMessagesRepository;
import pl.motobudzet.api.utils.MessageMapper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static pl.motobudzet.api.z_configuration.security.UserAuthorization.authorizeMessageGetAccess;
import static pl.motobudzet.api.z_configuration.security.UserAuthorization.authorizeMessagePostAccess;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final AppUserCustomService userCustomService;
    private final ConversationService conversationService;
    private final ConversationMessagesRepository messagesRepository;
    private final KafkaServiceInterface kafkaService;
    private final SpringMailSenderService springMailSenderService;

    public String sendMessage(String message,Long conversationId, String messageSenderName) {

        Conversation conversation = conversationService.findConversationById(conversationId);
        AppUser conversationUserClient = conversation.getUserClient();
        AppUser conversationUserOwner = conversation.getUserOwner();

        AppUser emailNotificationReceiver = conversationUserClient.getUsername().equals(messageSenderName) ? conversationUserOwner : conversationUserClient;
        AppUser messageSender = userCustomService.getUserByName(messageSenderName);

        String lastMessageUserName = conversation.getLastMessage().getMessageSender().getUsername();

        if (authorizeMessagePostAccess(messageSenderName, conversationUserOwner.getUsername(), conversationUserClient.getUsername())) {
            ConversationMessage newMessage = ConversationMessage.builder()
                    .conversation(conversation)
                    .message(message)
                    .messageSendDateTime(LocalDateTime.now())
                    .messageSender(messageSender)
                    .build();

            updateConversation(conversation, newMessage);
            messagesRepository.save(newMessage);
        }



        if(!Objects.equals(lastMessageUserName, messageSenderName)){
            sendEmailMessageNotificationAsync(message,emailNotificationReceiver,messageSender,conversation);
        }

        return "Message Sent!";
    }

    private void updateConversation(Conversation conversation, ConversationMessage newMessage) {
        if (conversation.getConversationMessages() == null) {
            conversation.setConversationMessages(List.of(newMessage));
        } else {
            List<ConversationMessage> conversationMessageList = conversation.getConversationMessages();
            conversationMessageList.add(newMessage);
            conversation.setConversationMessages(conversationMessageList);
        }
        conversation.setLastMessage(newMessage);
    }

    private void sendEmailMessageNotificationToKafka(String message,AppUser emailNotificationReceiver,AppUser messageSender,Conversation conversation) {
        EmailMessageRequest emailMessageRequest = EmailMessageRequest.builder()
                .message(message)
                .senderName(messageSender.getUsername())
                .receiverEmail(emailNotificationReceiver.getEmail())
                .advertisementTitle(conversation.getAdvertisement().getName())
                .advertisementId(String.valueOf(conversation.getAdvertisement().getId()))
                .build();
        kafkaService.sendMessageNotification(emailMessageRequest);
    }

    private void sendEmailMessageNotificationAsync(String message,AppUser emailNotificationReceiver,AppUser messageSender,Conversation conversation) {
        EmailMessageRequest emailMessageRequest = EmailMessageRequest.builder()
                .message(message)
                .senderName(messageSender.getUsername())
                .receiverEmail(emailNotificationReceiver.getEmail())
                .advertisementTitle(conversation.getAdvertisement().getName())
                .advertisementId(String.valueOf(conversation.getAdvertisement().getId()))
                .build();
        springMailSenderService.sendMessageNotificationHtml(emailMessageRequest);
    }


    public List<ConversationMessageDTO> getAllMessages(Long conversationId, String loggedUser) {
        List<ConversationMessage> conversationMessagesList = messagesRepository.getAllMessages(conversationId);
        if (conversationMessagesList.isEmpty()) return Collections.emptyList();

        if (authorizeMessageGetAccess(conversationMessagesList, loggedUser)) {
            updateMessagesRead(loggedUser, conversationMessagesList);
            return conversationMessagesList
                    .stream().map(MessageMapper::mapToConversationMessageDTO).collect(Collectors.toList());
        }
        return null;
    }

    private void updateMessagesRead(String loggedUser, List<ConversationMessage> conversationMessagesList) {

        for (ConversationMessage conversationMessage : conversationMessagesList) {
            String messageSenderUsername = conversationMessage.getMessageSender().getUsername();
            if (!loggedUser.equals(messageSenderUsername)) {
                if (conversationMessage.getMessageReadDateTime() == null) {
                    conversationMessage.setMessageReadDateTime(LocalDateTime.now(ZoneId.of("Europe/Warsaw")));
                }
            }
        }
        messagesRepository.saveAll(conversationMessagesList);
    }
}