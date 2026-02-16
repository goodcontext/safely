package com.safely.domain.group.service;

import com.safely.domain.group.GroupRole;
import com.safely.domain.group.dto.GroupCreateRequest;
import com.safely.domain.group.entity.Group;
import com.safely.domain.group.entity.GroupMember;
import com.safely.domain.group.repository.GroupMemberRepository;
import com.safely.domain.group.repository.GroupRepository;
import com.safely.domain.member.entity.Member;
import com.safely.domain.member.repository.MemberRepository;
import com.safely.global.exception.NotFoundException;
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
        // 1. Given (준비)
        Long memberId = 1L;
        Member member = Member.builder().id(memberId).name("작성자").build();

        // DTO 준비
        GroupCreateRequest request = new GroupCreateRequest();
        ReflectionTestUtils.setField(request, "name", "테스트 여행");
        ReflectionTestUtils.setField(request, "startDate", LocalDate.now());
        ReflectionTestUtils.setField(request, "endDate", LocalDate.now());

        // Mocking: 리포지토리가 어떻게 행동할지 정의
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

        Group savedGroup = request.toEntity();
        ReflectionTestUtils.setField(savedGroup, "id", 100L);
        given(groupRepository.save(any(Group.class))).willReturn(savedGroup);

        // 2. when (실행)
        Long resultGroupId = groupService.createGroup(memberId, request);

        // 3. then (검증)
        assertThat(resultGroupId).isEqualTo(100L);

        // Verify: groupRepository.save()가 한 번 호출되었는지 확인
        verify(groupRepository, times(1)).save(any(Group.class));

        //Verify: groupMemberRepository.save()가 호출될 때 ,들어간 데이터가 MANAGER인지 확인 (ArgumentCaptor 사용)
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
        // 1. Given
        Long invalidMemberId = 900L;
        GroupCreateRequest request = new GroupCreateRequest();

        // Mocking: 회원을 찾으면 빈값(Optional.empty)을 제공
        given(memberRepository.findById(invalidMemberId)).willReturn(Optional.empty());

        // 2. When & Then (예외 발생 확인)
        assertThatThrownBy(() -> groupService.createGroup(invalidMemberId, request))
                .isInstanceOf(NotFoundException.class); // [수정] IllegalArgumentException -> NotFoundException
        // .hasMessage("존재하지 않는 회원입니다."); // [삭제] NotFoundException은 별도 메시지가 없으므로 제거

        // Verify: 예외가 발생했으므로 그룹 저장은 호출되면 안 됨.
        verify(groupRepository, times(0)).save(any());
    }
}