package com.neo.customerservice.dto.user.output;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for user information response")
public class UserOutputDto {

    @Schema(description = "Unique user identifier", example = "1")
    private Long id;

    @Schema(description = "User's email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Unique username", example = "johndoe")
    private String username;
}
