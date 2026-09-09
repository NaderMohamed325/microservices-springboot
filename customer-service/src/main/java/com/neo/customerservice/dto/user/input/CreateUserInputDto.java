package com.neo.customerservice.dto.user.input;

import com.neo.customerservice.enums.CustomerType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for creating a new user account")
public class CreateUserInputDto {

    @Schema(description = "User's email address", example = "john.doe@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @Email(message = "Email should be valid")
    private String email;

    @Schema(description = "Unique username for the account", example = "johndoe", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Username is mandatory")
    private String username;

    @Schema(description = "User password (8-30 characters)", example = "password123", minLength = 8, maxLength = 30, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password is mandatory")
    @Length(min = 8, max = 30, message = "Password must be between 8 and 30 characters")
    private String password;

    @Schema(description = "Customer type", example = "RETAIL", allowableValues = {"RETAIL", "CORPORATE", "INVESTMENT"})
    private CustomerType type;
}
