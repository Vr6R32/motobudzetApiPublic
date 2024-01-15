package pl.motobudzet.api.user_account.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegistrationRequest {
    @NotEmpty(message = "Nazwa użytkownika nie może być pusta !")
    @NotNull(message = "Nazwa użytkownika nie może być pusta !")
    String userName;
    @NotEmpty(message = "Hasło nie może być puste !")
    @NotNull(message = "Hasło nie może być puste !")
    String password;
    @NotEmpty(message = "Adres email nie może być pusty !")
    @NotNull(message = "Adres email nie może być pusty !")
    @Email(message = "Podaj prawidłowy adres email !")
    String email;
}