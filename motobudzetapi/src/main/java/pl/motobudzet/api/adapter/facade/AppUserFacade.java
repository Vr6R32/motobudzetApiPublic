package pl.motobudzet.api.adapter.facade;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import pl.motobudzet.api.domain.user.AppUser;
import pl.motobudzet.api.domain.user.service.RegistrationService;
import pl.motobudzet.api.domain.user.service.UserService;
import pl.motobudzet.api.dto.*;

@AllArgsConstructor
public class AppUserFacade {

    private final UserService userService;
    private final RegistrationService registrationService;


    public AppUserDTO getUserDetails(String userName){
        return userService.getUserDetails(userName);
    }

    public int changeUserPassword(NewPasswordRequest request){
        return userService.changeUserPassword(request);
    }

    public int generatePasswordResetCode(ResetPasswordRequest request){
        return userService.generatePasswordResetCode(request);
    }

    public ResponseEntity<String> register(RegistrationRequest request){
        return registrationService.register(request);
    }

    @Transactional
    public ResponseEntity<?> updateFirstUserDetails(UserDetailsRequest request, AppUser loggedUser, HttpServletResponse response, HttpServletRequest httpServletRequest){
        return userService.updateFirstUserDetails(request, loggedUser, response, httpServletRequest);
    }

    public void confirmEmail(String registrationCode, HttpServletResponse response, HttpServletRequest request){
        registrationService.confirmEmail(registrationCode,response,request);
    }


}
