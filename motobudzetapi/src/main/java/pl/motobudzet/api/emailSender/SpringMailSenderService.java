package pl.motobudzet.api.emailSender;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pl.motobudzet.api.advertisement.entity.Advertisement;
import pl.motobudzet.api.file_manager.PathsConfig;
import pl.motobudzet.api.user_account.entity.AppUser;

import java.io.File;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SpringMailSenderService {

    public static final String BACKGROUND_IMAGE_URL = "https://motobudzet.pl/api/static/background_email";

    private final JavaMailSender mailSender;
    private final PathsConfig pathsConfig;


    private static final String NEW_CONVERSATION_MESSAGE_TITLE = "Dostałeś nową wiadomość ";
    private static final String REGISTRATION_ACTIVATION_TITLE = "Link aktywacyjny";
    private static final String ADVERTISEMENT_ACTIVATION_TITLE = "Aktywacja ogłoszenia";
    private static final String ADVERTISEMENT_TO_ACTIVATE = "Nowe ogłoszenie do zweryfikowania";
    private static final String ADVERTISEMENT_URL_LINK = "/advertisement?id=";
    private static final String RESET_PASSWORD_TITLE = "Resetowanie hasła";

    @Async
    public void sendMessageNotificationHtml(EmailMessageRequest request) {
        sendEmail(NEW_CONVERSATION_MESSAGE_TITLE, createHtmlStringMessageNotification(request), request.getReceiverEmail());
    }

    @Async
    public void sendRegisterActivationNotificationHtml(AppUser user) {
        sendEmail(REGISTRATION_ACTIVATION_TITLE, createHtmlStringRegisterActivation(user), user.getEmail());
    }

    @Async
    public void sendResetPasswordNotificationCodeLink(AppUser user) {
        sendEmail(RESET_PASSWORD_TITLE, createHtmlStringResetPassword(user), user.getEmail());
    }

    @Async
    public void sendAdvertisementActivationConfirmNotification(AppUser user, Advertisement advertisement) {
        sendEmail(ADVERTISEMENT_ACTIVATION_TITLE, createHtmlStringActivationConfirmation(user, advertisement), user.getEmail());
    }

    @Async
    public void sendEmailNotificationToManagement(List<String> emailList, UUID id) {
        String[] to = emailList.toArray(new String[1]);
        sendEmail(ADVERTISEMENT_TO_ACTIVATE, createHtmlNewAdvertisementToActivate(id), to);
    }


    private void sendEmail(String subject, String htmlContent, String... to) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(pathsConfig.getInfoEmailAddressPath());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            addStaticResources(helper);
            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email.", e);
        }
    }


    private void addStaticResources(MimeMessageHelper helper) throws MessagingException {
        String logoPath = pathsConfig.getPrivateFilePath() + "logo.png";
        FileSystemResource logo = new FileSystemResource(new File(logoPath));
        helper.addInline("image001", logo);
    }

    private String createHtmlStringMessageNotification(EmailMessageRequest request) {

        return "<center>" +
                "<table width='100%' style='text-align: center; padding: 20px; background-image: url(" + BACKGROUND_IMAGE_URL + "); background-size: cover;'>" +
                "<tr><td>" +
                "<img src='cid:image001'/><br>" +
                "<font color='darkgoldenrod'>Wiadomość od użytkownika</font><br>" +
                "<b><font color='moccasin' size='+3'>" + request.getSenderName() + "</font></b><br>" +
                "<font color='darkgoldenrod'>Odnośnie ogłoszenia</font><br>" +
                "<b><font color='moccasin' size='+3'>" + request.getAdvertisementTitle() + "</font></b><br>" +
                "<hr>" +
                "<font color='darkgoldenrod' size='+2'>" + request.getMessage() + "</font>" +
                "</td></tr></table>" +
                "</center>";
    }

    private String createHtmlStringRegisterActivation(AppUser user) {
        return "<center>" +
                "<table width='100%' style='text-align: center; padding: 20px; background-image: url(" + BACKGROUND_IMAGE_URL + "); background-size: cover;'>" +
                "<tr><td>" +
                "<img src='cid:image001'/><br>" +
                "<b><font color='moccasin' size='+3'>" + "Witaj " + user.getUsername() + "</font></b><br>" +
                "<font color='darkgoldenrod'>Twój link aktywacyjny :</font><br>" +
                "<b><font color='moccasin' size='+3'>" + "<a href='" + pathsConfig.getSiteUrlPath() + "/api/user/confirm?activationCode=" + user.getRegisterCode() + "'>Kliknij tutaj</a>" + "</font></b><br>" +
                "<hr>" +
                "<font color='darkgoldenrod' size='+2'>" + "Kliknij aby aktywowac swoje konto oraz dokończyć rejerstracje" + "</font>" +
                "</td></tr></table>" +
                "</center>";
    }

    private String createHtmlStringResetPassword(AppUser user) {
        return "<center>" +
                "<table width='100%' style='text-align: center; padding: 20px; background-image: url(" + BACKGROUND_IMAGE_URL + "); background-size: cover;'>" +
                "<tr><td>" +
                "<img src='cid:image001'/><br>" +
                "<b><font color='moccasin' size='+3'>" + "Witaj " + user.getUsername() + "</font></b><br>" +
                "<font color='darkgoldenrod'>Twój link resetujący hasło :</font><br>" +
                "<b><font color='moccasin' size='+3'>" + "<a href='" + pathsConfig.getSiteUrlPath() + "/reset?code=" + user.getResetPasswordCode() + "'>Kliknij tutaj</a>" + "</font></b><br>" +
                "<hr>" +
                "<font color='darkgoldenrod' size='+2'>" + "Kliknij aby zresetować swoje hasło." + "</font><br>" +
                "<font color='darkgoldenrod' size='+2'>" + "Jeśli nie prosiłeś o zmianę hasła, zignoruj tą wiadomość." + "</font>" +
                "</td></tr></table>" +
                "</center>";
    }

    private String createHtmlStringActivationConfirmation(AppUser user, Advertisement advertisement) {
        return "<center>" +
                "<table width='100%' style='text-align: center; padding: 20px; background-image: url(" + BACKGROUND_IMAGE_URL + "); background-size: cover;'>" +
                "<tr><td>" +
                "<img src='cid:image001'/><br>" +
                "<b><font color='moccasin' size='+3'>" + "Witaj " + user.getUsername() + "</font></b><br>" +
                "<font color='darkgoldenrod'>Twoje ogłoszenie <br>" + advertisement.getBrand().getName() + advertisement.getModel().getName() + "<br>"
                + advertisement.getName() + "<br> jest już widoczne !</font><br>" +
                "<b><font color='moccasin' size='+3'>" + "<a href='" + pathsConfig.getSiteUrlPath() + ADVERTISEMENT_URL_LINK + advertisement.getId() + "'>Kliknij tutaj</a>" + "</font></b><br>" +
                "<hr>" +
                "<font color='darkgoldenrod' size='+2'>" + "Kliknij aby przejść do swojego ogłoszenia." + "</font>" +
                "</td></tr></table>" +
                "</center>";
    }

    private String createHtmlNewAdvertisementToActivate(UUID id) {
        return "<center>" +
                "<table width='100%' style='text-align: center; padding: 20px; background-image: url(" + BACKGROUND_IMAGE_URL + "); background-size: cover;'>" +
                "<tr><td>" +
                "<img src='cid:image001'/><br>" +
                "<b><font color='moccasin' size='+3'>" + "Witaj, pojawilo sie nowe ogłoszenie do aktywacji. " + "</font></b><br>" +
                "<font color='darkgoldenrod'>Wykonaj proszę weryfikacje ogłoszenia. <br>" + "<br></font><br>" +
                "<b><font color='moccasin' size='+3'>" + "<a href='" + pathsConfig.getSiteUrlPath() + ADVERTISEMENT_URL_LINK + id + "'>Kliknij tutaj</a>" + "</font></b><br>" +
                "<hr>" +
                "<font color='darkgoldenrod' size='+2'>" + "Kliknij aby przejść do ogłoszenia." + "</font>" +
                "</td></tr></table>" +
                "</center>";
    }

}
