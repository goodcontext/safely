package com.safely.domain.group.dto;

import com.safely.domain.group.entity.Group;
import java.time.LocalDate;

public record GroupResponse(
        Long id,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        String destination,
        String inviteCode
) {
    public static GroupResponse from(Group group) {
        return new GroupResponse(
                group.getId(),
                group.getName(),
                group.getStartDate(),
                group.getEndDate(),
                group.getDestination(),
                group.getInviteCode()
        );
    }
}