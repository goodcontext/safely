package com.safely.domain.group.dto;

import com.safely.domain.group.entity.Group;
import com.safely.domain.group.entity.GroupMember;
import java.time.LocalDate;
import java.util.List;

public record GroupDetailResponse(
        Long id,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        String destination,
        String inviteCode,
        List<GroupMemberDto> members
) {
    public static GroupDetailResponse of(Group group, List<GroupMember> members) {
        List<GroupMemberDto> memberDtos = members.stream()
                .map(GroupMemberDto::from)
                .toList();

        return new GroupDetailResponse(
                group.getId(),
                group.getName(),
                group.getStartDate(),
                group.getEndDate(),
                group.getDestination(),
                group.getInviteCode(),
                memberDtos
        );
    }

    // 내부 클래스 또는 별도 파일로 분리 가능
    public record GroupMemberDto(Long memberId, String name, String profileImage, String role) {
        public static GroupMemberDto from(GroupMember gm) {
            return new GroupMemberDto(
                    gm.getMember().getId(),
                    gm.getMember().getName(),
                    gm.getMember().getProfileImage(),
                    gm.getRole().name()
            );
        }
    }
}