package com.neo.customerservice.dto.user.input;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
public class CreateUserInputDto {

    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Username is mandatory")
    private String username;

    @NotBlank(message = "Password is mandatory")
    @Length(min = 8, max = 30, message = "Password must be between 8 and 30 characters")
    private String password;
}
