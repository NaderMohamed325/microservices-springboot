package com.neo.customerservice.dto.user.input;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for user login")
public class LoginUserInputDto {

    @Schema(description = "Username for authentication", example = "johndoe", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Username is mandatory")
    private String username;

    @Schema(description = "Password for authentication", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password is mandatory")
    private String password;
}
