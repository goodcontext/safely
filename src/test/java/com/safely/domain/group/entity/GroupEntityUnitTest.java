package com.safely.domain.group.entity;

import com.safely.domain.group.GroupRole;
import com.safely.domain.member.entity.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class GroupEntityUnitTest {
    @Test
    @DisplayName("Group Entity가 Builder로 정상 생성된다.")
    void groupBuilderTest() {
        Group group = Group.builder()
                .name("단위테스트 여행")
                .startDate(LocalDate.of(2025,12,01))
                .endDate(LocalDate.of(2025,12,05))
                .destination("Seoul")
                .build();

        assertThat(group.getName()).isEqualTo("단위테스트 여행");
        assertThat(group.getGroupMembers()).isNotNull(); // 리스트 초기화 확인 (ArrayList);
        assertThat(group.getGroupMembers()).isEmpty();
    }

    @Test
    @DisplayName("GroupMember Entity 생성 확인")
    void groupMemberBuilderTest() {
        Group group = Group.builder().name("Group").build();
        Member member = Member.builder().name("Member").build();

        GroupMember groupMember = GroupMember.builder()
                .group(group)
                .member(member)
                .role(GroupRole.MANAGER)
                .memberName("닉네임")
                .build();

        assertThat(groupMember.getGroup()).isEqualTo(group);
        assertThat(groupMember.getMember()).isEqualTo(member);
        assertThat(groupMember.getRole()).isEqualTo(GroupRole.MANAGER);
    }
}