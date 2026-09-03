package com.neo.customerservice.dto.user.output;

import com.neo.customerservice.enums.UserRoles;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserOutputDto {
    private Long id;
    private String email;
    private String username;
}
