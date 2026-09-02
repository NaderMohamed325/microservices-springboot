package com.neo.customerservice.dto.user.output;


import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginUserOutputDto {
    private String accessToken;
}
