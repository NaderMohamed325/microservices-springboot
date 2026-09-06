package com.neo.accountservice.dto.user.events;

import lombok.*;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserCreatedEvent {
    Long userId;
    String email;
    String username;
}
