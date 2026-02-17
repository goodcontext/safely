package com.safely.domain.group.service;

import com.safely.domain.group.GroupRole;
import com.safely.domain.group.dto.GroupCreateRequest;
import com.safely.domain.group.entity.Group;
import com.safely.domain.group.entity.GroupMember;
import com.safely.domain.group.repository.GroupMemberRepository;
import com.safely.domain.group.repository.GroupRepository;
import com.safely.domain.member.entity.Member;
import com.safely.domain.member.repository.MemberRepository;
import com.safely.global.exception.common.EntityNotFoundException;
import com.safely.global.exception.group.AlreadyJoinedGroupException;
import com.safely.global.exception.group.InvalidInviteCodeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class GroupServiceUnitTest {
    @InjectMocks // 가짜 리포지토리들을 주입받을 진짜 서비스
    GroupService groupService;

    @Mock // 가짜 리포지토리 생성
    GroupRepository groupRepository;
    @Mock
    GroupMemberRepository groupMemberRepository;
    @Mock
    MemberRepository memberRepository;

    @Test
    @DisplayName("성공: 그룹 생성 시 Repository들이 정상적으로 호출되고 MANAGER가 설정된다.")
    void createGroup_Success() {
        // Given
        Long memberId = 1L;
        Member member = Member.builder().id(memberId).name("작성자").build();

        // DTO 준비
        GroupCreateRequest request = new GroupCreateRequest(
                "테스트 여행",
                LocalDate.now(),
                LocalDate.now(),
                "서울"
        );

        // Mocking
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

        // Entity 준비
        Group savedGroup = request.toEntity();
        ReflectionTestUtils.setField(savedGroup, "id", 100L);
        given(groupRepository.save(any(Group.class))).willReturn(savedGroup);

        // when
        Long resultGroupId = groupService.createGroup(memberId, request);

        // then
        assertThat(resultGroupId).isEqualTo(100L);
        verify(groupRepository, times(1)).save(any(Group.class));

        ArgumentCaptor<GroupMember> captor = ArgumentCaptor.forClass(GroupMember.class);
        verify(groupMemberRepository, times(1)).save(captor.capture());

        GroupMember capturedGroupMember = captor.getValue();
        assertThat(capturedGroupMember.getRole()).isEqualTo(GroupRole.MANAGER);
        assertThat(capturedGroupMember.getMember()).isEqualTo(member);
        assertThat(capturedGroupMember.getGroup()).isEqualTo(savedGroup);
    }

    @Test
    @DisplayName("실패: 존재하지 않는 회원이면 예외가 발생한다.")
    void createGroup_Fail_MemberNotFound() {
        // Given
        Long invalidMemberId = 900L;

        GroupCreateRequest request = new GroupCreateRequest(
                "실패 테스트용 여행",
                LocalDate.now(),
                LocalDate.now(),
                "어디든지"
        );

        given(memberRepository.findById(invalidMemberId)).willReturn(Optional.empty());

        // When & Then (예외 발생 확인)
        assertThatThrownBy(() -> groupService.createGroup(invalidMemberId, request))
                .isInstanceOf(EntityNotFoundException.class);

        // Verify: 예외가 발생했으므로 그룹 저장은 호출되면 안 됨.
        verify(groupRepository, never()).save(any());
    }

    @Test
    @DisplayName("그룹 가입 성공: 초대 코드가 일치하면 멤버로 추가된다.")
    void joinGroupByCode_Success() {
        // Given
        Long memberId = 1L;
        String inviteCode = "AB12CD34";

        Member member = Member.builder().id(memberId).name("참여자").build();
        // 이미 멤버가 있는지 확인할 때 사용될 빈 리스트
        Group group = Group.builder()
                .name("테스트 여행")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(3))
                .build();

        // Reflection을 사용하여 private 필드인 id에 강제로 값을 넣음.
        ReflectionTestUtils.setField(group, "id", 100L);

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(groupRepository.findByInviteCode(inviteCode)).willReturn(Optional.of(group));

        // When
        groupService.joinGroupByCode(memberId, inviteCode);

        // Then
        // 그룹 멤버로 저장(save)이 일어났는지 확인
        ArgumentCaptor<GroupMember> captor = ArgumentCaptor.forClass(GroupMember.class);
        verify(groupMemberRepository).save(captor.capture());

        assertThat(captor.getValue().getRole()).isEqualTo(GroupRole.MEMBER); // 일반 멤버인지 확인
        assertThat(captor.getValue().getGroup()).isEqualTo(group);
    }

    @Test
    @DisplayName("그룹 가입 실패: 이미 가입된 유저는 예외 발생")
    void joinGroupByCode_Fail_AlreadyJoined() {
        // Given
        Long memberId = 1L;
        String inviteCode = "CODE1234";
        Member member = Member.builder().id(memberId).build();

        // 이미 가입된 상태 시뮬레이션
        Group group = Group.builder()
                .name("테스트 여행")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(3))
                .build();

        // Reflection을 사용하여 private 필드인 id에 강제로 값을 넣음.
        ReflectionTestUtils.setField(group, "id", 100L);

        GroupMember existingMember = GroupMember.builder().group(group).member(member).build();
        group.getGroupMembers().add(existingMember); // 그룹에 멤버 추가해둠

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(groupRepository.findByInviteCode(inviteCode)).willReturn(Optional.of(group));

        // When & Then
        assertThatThrownBy(() -> groupService.joinGroupByCode(memberId, inviteCode))
                .isInstanceOf(AlreadyJoinedGroupException.class);
    }

    @Test
    @DisplayName("그룹 가입 실패: 초대 코드가 틀리면 예외 발생")
    void joinGroupByCode_Fail_InvalidCode() {
        // Given
        String invalidCode = "WRONG";
        given(memberRepository.findById(any())).willReturn(Optional.of(Member.builder().build()));
        given(groupRepository.findByInviteCode(invalidCode)).willReturn(Optional.empty()); // 못 찾음

        // When & Then
        assertThatThrownBy(() -> groupService.joinGroupByCode(1L, invalidCode))
                .isInstanceOf(InvalidInviteCodeException.class);
    }
}