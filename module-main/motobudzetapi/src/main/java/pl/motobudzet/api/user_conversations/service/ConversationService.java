package pl.motobudzet.api.user_conversations.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.motobudzet.api.advertisement.entity.Advertisement;
import pl.motobudzet.api.advertisement.service.PublicAdvertisementService;
import pl.motobudzet.api.user.entity.AppUser;
import pl.motobudzet.api.user.service.AppUserCustomService;
import pl.motobudzet.api.user_conversations.dto.ConversationDTO;
import pl.motobudzet.api.user_conversations.entity.Conversation;
import pl.motobudzet.api.user_conversations.repository.ConversationRepository;

import java.util.List;
import java.util.UUID;

import static pl.motobudzet.api.utils.ConversationMapper.mapConversationToDTO;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final PublicAdvertisementService advertisementService;
    private final AppUserCustomService userCustomService;
    private final ConversationRepository conversationRepository;

//    public Conversation getAllConversationsForAdvertisement(String advertisementId, String userOwnerName) {
//
//        AppUser user = userCustomService.getByName(userOwnerName);
//        return conversationRepository.findByAdvertisementIdAndUserOwnerId(UUID.fromString(advertisementId), user.getId());
//
//    }

    public Long createConversation(String advertisementId, String loggedUser) {

        AppUser userClient = userCustomService.getByName(loggedUser);
        Advertisement advertisement = advertisementService.getAdvertisement(advertisementId);

        if(advertisement.getUser().getUsername().equals(loggedUser)){
            return null;
        }

        else {
            Conversation conversation = Conversation.builder()
                    .advertisement(advertisement)
                    .userOwner(advertisement.getUser())
                    .userClient(userClient)
                    .build();
            return conversationRepository.saveAndFlush(conversation).getId();
        }
    }


    public Conversation findConversationById(Long conversationId) {
        return conversationRepository
                .findById(conversationId).orElse(null);
    }

//    public Conversation findConversation(String advertisementId,Long userOwnerId,Long userClientId){
//        return conversationRepository
//                .findConversationByAdvertisement_IdAndUserOwner_IdAndUserClient_Id(UUID.fromString(advertisementId),userOwnerId,userClientId)
//                .orElseThrow(() -> new RuntimeException("conversation didnt found !"));
//    }

    public List<ConversationDTO> getAllUserSellerConversations(String ownerName) {
        AppUser user = userCustomService.getByName(ownerName);
        List<Conversation> conversationList = conversationRepository.findAllByUserOwnerId(user.getId());
        return conversationList.stream().map(conversation -> mapConversationToDTO(conversation, ownerName, advertisementService)).toList();
    }

    public List<ConversationDTO> getAllUserBuyerConversations(String ownerName) {
        AppUser user = userCustomService.getByName(ownerName);
        List<Conversation> conversationList = conversationRepository.findAllByUserClientId(user.getId());
        return conversationList.stream().map(conversation -> mapConversationToDTO(conversation, ownerName, advertisementService)).toList();
    }

    public List<ConversationDTO> getAllConversations(String userName) {
        AppUser user = userCustomService.getByName(userName);
        List<Conversation> conversationList = conversationRepository.findAllConversationsByUserId(user.getId());
        return conversationList.stream().map(conversation -> mapConversationToDTO(conversation, userName, advertisementService)).toList();
    }

    public Long findConversationIdByAdvIdAndSender(String advertisementId, String name) {
        return conversationRepository.findByUserClientIdAndAdvertisementId(UUID.fromString(advertisementId), name).orElse( -1L);
    }
}
