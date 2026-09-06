package com.neo.customerservice.dto.user.events;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Setter
@Getter
public class UserDeletedEvent {
    Long userId;
}
