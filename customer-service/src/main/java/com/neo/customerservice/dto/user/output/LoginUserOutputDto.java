package com.neo.customerservice.dto.user.output;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for login response containing JWT token")
public class LoginUserOutputDto {

    @Schema(description = "JWT access token for authentication", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VybmFtZSIsImlhdCI6MTcx...")
    private String accessToken;
}
