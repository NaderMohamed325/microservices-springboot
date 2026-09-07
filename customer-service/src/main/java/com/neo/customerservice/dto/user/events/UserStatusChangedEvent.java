package com.neo.customerservice.dto.user.events;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Setter
@Getter
public class UserStatusChangedEvent {
    Long userId;
    boolean enabled;
}
