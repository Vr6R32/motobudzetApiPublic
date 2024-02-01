package pl.motobudzet.api.domain.messaging;


import lombok.RequiredArgsConstructor;
import pl.motobudzet.api.adapter.facade.AdvertisementFacade;
import pl.motobudzet.api.domain.advertisement.entity.Advertisement;
import pl.motobudzet.api.domain.user.entity.AppUser;
import pl.motobudzet.api.infrastructure.mailing.EmailManagerFacade;
import pl.motobudzet.api.infrastructure.mailing.EmailMessageRequest;
import pl.motobudzet.api.infrastructure.mapper.MessageMapper;
import pl.motobudzet.api.persistance.ConversationRepository;
import pl.motobudzet.api.persistance.MessageRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static pl.motobudzet.api.infrastructure.mapper.ConversationMapper.mapConversationToDTO;

@RequiredArgsConstructor
class MessagingServiceImpl implements MessagingService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final EmailManagerFacade mailService;
    private final AdvertisementFacade advertisementFacade;

    public Conversation createConversation(UUID advertisementId, AppUser loggedUser) {

        Advertisement advertisement = advertisementFacade.getAdvertisementEntity(advertisementId);

        Conversation conversation = Conversation.builder()
                .advertisement(advertisement)
                .userOwner(advertisement.getUser())
                .userClient(loggedUser)
                .build();

        conversationRepository.saveAndFlush(conversation);

        return conversation;
    }

    public List<ConversationDTO> getAllUserConversations(AppUser loggedUser) {
        List<Conversation> conversationList = conversationRepository.findAllConversationsByUserId(loggedUser.getId());
        return conversationList.stream().map(conversation -> mapConversationToDTO(conversation, loggedUser.getUsername())).toList();
    }

    public Long findConversationIdByAdvIdAndSender(UUID advertisementId, String name) {
        return conversationRepository.findByUserClientIdAndAdvertisementId(advertisementId, name).orElse(-1L);
    }
    public Conversation findConversationByAdvertisementIdAndUserSender(UUID advertisementId, AppUser messageSender) {
        return conversationRepository
                .findByAdvertisementIdAndUserClientId(messageSender,advertisementId).orElse(null);
    }

    public String sendMessage(String message, UUID advertisementId, AppUser user) {

        Conversation conversation = findConversationOrCreateNew(advertisementId, user);

        String messageSenderName = user.getUsername();
        AppUser conversationUserClient = conversation.getUserClient();
        AppUser conversationUserOwner = conversation.getUserOwner();
        String lastMessageUserName = getLastMessageUserName(conversation);

        if (authorizeMessagePostAccess(messageSenderName, conversationUserOwner.getUsername(), conversationUserClient.getUsername())) {
            Message newMessage = Message.builder()
                    .conversation(conversation)
                    .message(message)
                    .messageSendDateTime(LocalDateTime.now())
                    .messageSender(user)
                    .build();

            updateConversation(conversation, newMessage);
            messageRepository.save(newMessage);

            AppUser emailNotificationReceiver = conversationUserClient.getUsername().equals(messageSenderName) ? conversationUserOwner : conversationUserClient;
            if (lastMessageUserName == null || !Objects.equals(lastMessageUserName, messageSenderName)) {
                sendEmailMessageNotificationAsync(message, emailNotificationReceiver, user, conversation);
            }
            return "Message Sent!";
        } else {
            return "Error while sending a message!";
        }
    }
    public List<MessageDTO> getAllMessages(Long conversationId, String loggedUser) {
        List<Message> messagesList = messageRepository.getAllMessages(conversationId);
        if (messagesList.isEmpty()) return Collections.emptyList();
        if (authorizeMessageGetAccess(messagesList, loggedUser)) {
            updateMessagesRead(loggedUser, messagesList);
            return messagesList
                    .stream().map(MessageMapper::mapToMessageDTO).toList();
        }
        return Collections.emptyList();
    }



    private String getLastMessageUserName(Conversation conversation) {
        String lastMessageUserName = null;
        if (conversation.getLastMessage() != null) {
            lastMessageUserName = conversation.getLastMessage().getMessageSender().getUsername();
        }
        return lastMessageUserName;
    }

    private Conversation findConversationOrCreateNew(UUID advertisementId, AppUser user) {
        Conversation conversation = findConversationByAdvertisementIdAndUserSender(advertisementId, user);

        if(conversation==null){
            conversation = createConversation(advertisementId, user);
        }
        return conversation;
    }

    private void updateConversation(Conversation conversation, Message newMessage) {
        if (conversation.getMessages() == null) {
            conversation.setMessages(List.of(newMessage));
        } else {
            List<Message> messageList = conversation.getMessages();
            messageList.add(newMessage);
            conversation.setMessages(messageList);
        }
        conversation.setLastMessage(newMessage);
    }

    private void sendEmailMessageNotificationAsync(String message, AppUser emailNotificationReceiver, AppUser messageSender, Conversation conversation) {
        EmailMessageRequest emailMessageRequest = EmailMessageRequest.builder()
                .message(message)
                .senderName(messageSender.getUsername())
                .receiverEmail(emailNotificationReceiver.getEmail())
                .advertisementTitle(conversation.getAdvertisement().getName())
                .advertisementId(String.valueOf(conversation.getAdvertisement().getId()))
                .build();
        mailService.sendMessageNotificationHtml(emailMessageRequest);
    }


    private void updateMessagesRead(String loggedUser, List<Message> messagesList) {

        for (Message message : messagesList) {
            String messageSenderUsername = message.getMessageSender().getUsername();
            if (!loggedUser.equals(messageSenderUsername) && (message.getMessageReadDateTime() == null)) {
                message.setMessageReadDateTime(LocalDateTime.now());
            }
        }
        messageRepository.saveAll(messagesList);
    }

    private boolean authorizeMessageGetAccess(List<Message> messageList, String loggedUser) {
        return loggedUser.equals(messageList.stream().findFirst().orElseThrow(() -> new RuntimeException("No messages found!")).getConversation().getUserOwner().getUsername()) ||
                loggedUser.equals(messageList.stream().findFirst().orElseThrow(() -> new RuntimeException("No messages found!")).getConversation().getUserClient().getUsername());
    }

    private boolean authorizeMessagePostAccess(String loggedUser, String userNameOwner, String userNameClient) {
        return loggedUser.equals(userNameClient) || loggedUser.equals(userNameOwner);
    }
}