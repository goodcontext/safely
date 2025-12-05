package com.safely.domain.group.service;

import com.safely.domain.group.GroupRole;
import com.safely.domain.group.dto.*;
import com.safely.domain.group.entity.Group;
import com.safely.domain.group.entity.GroupMember;
import com.safely.domain.group.repository.GroupMemberRepository;
import com.safely.domain.group.repository.GroupRepository;
import com.safely.domain.member.entity.Member;
import com.safely.domain.member.repository.MemberRepository;
import com.safely.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupService {
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final MemberRepository memberRepository;

    // 1. 그룹 생성
    @Transactional
    public Long createGroup(Long memberId, GroupCreateRequest request) {
        Member member = getMember(memberId);
        Group savedGroup = groupRepository.save(request.toEntity());

        // 생성자를 MANAGER로 등록
        GroupMember manager = GroupMember.builder()
                .group(savedGroup)
                .member(member)
                .role(GroupRole.MANAGER)
                .memberName(member.getName())
                .build();
        groupMemberRepository.save(manager);

        return savedGroup.getId();
    }

    // 2. 내 그룹 목록 조회
    public List<GroupResponse> getMyGroups(Long memberId) {
        // 내가 속한 GroupMember 리스트를 통해 Group을 가져옴 (N+1 문제 주의: fetch join 필요하지만 일단 기능 구현 우선)
        // 실제로는 repository에 findGroupsByMemberId 같은 메서드를 만드는 것이 좋음.
        // 현재 로직: GroupMember -> Group 접근
        return groupMemberRepository.findAll().stream() // 임시: 전체 조회 후 필터링 (성능 최적화 필요)
                .filter(gm -> gm.getMember().getId().equals(memberId))
                .map(gm -> GroupResponse.from(gm.getGroup()))
                .toList();
    }

    // 3. 그룹 상세 조회
    public GroupDetailResponse getGroupDetail(Long groupId, Long memberId) {
        Group group = getGroup(groupId);
        validateMemberInGroup(groupId, memberId); // 멤버인지 확인

        List<GroupMember> members = group.getGroupMembers();
        return GroupDetailResponse.of(group, members);
    }

    // 4. 그룹 정보 수정 (관리자만 가능)
    @Transactional
    public void updateGroup(Long groupId, Long memberId, GroupUpdateRequest request) {
        Group group = getGroup(groupId);
        validateManager(groupId, memberId); // 권한 체크

        group.update(
                request.name(),
                request.startDate(),
                request.endDate(),
                request.destination()
        );
    }

    // 5. 그룹 삭제 (관리자만 가능)
    @Transactional
    public void deleteGroup(Long groupId, Long memberId) {
        Group group = getGroup(groupId);
        validateManager(groupId, memberId);

        groupRepository.delete(group); // Cascade 설정으로 GroupMember도 삭제됨
    }

    // 6. 초대 코드로 그룹 가입
    @Transactional
    public void joinGroupByCode(Long memberId, String inviteCode) {
        Member member = getMember(memberId);

        // 초대 코드로 그룹 찾기 (Repository에 findByInviteCode 추가 필요)
        // 여기서는 임시로 전체 검색 (성능상 나중에 QueryDSL이나 메서드 쿼리로 변경해야 함)
        Group group = groupRepository.findAll().stream()
                .filter(g -> inviteCode.equals(g.getInviteCode()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 초대 코드입니다."));

        // 이미 가입된 멤버인지 확인
        boolean isAlreadyMember = group.getGroupMembers().stream()
                .anyMatch(gm -> gm.getMember().getId().equals(memberId));
        if (isAlreadyMember) {
            throw new IllegalArgumentException("이미 가입된 그룹입니다.");
        }

        GroupMember newMember = GroupMember.builder()
                .group(group)
                .member(member)
                .role(GroupRole.MEMBER)
                .memberName(member.getName())
                .build();
        groupMemberRepository.save(newMember);
    }

    // --- 검증 및 조회 헬퍼 메서드 ---
    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(NotFoundException::new);
    }

    private Group getGroup(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(NotFoundException::new);
    }

    private void validateMemberInGroup(Long groupId, Long memberId) {
        boolean isMember = groupMemberRepository.findAll().stream()
                .anyMatch(gm -> gm.getGroup().getId().equals(groupId) && gm.getMember().getId().equals(memberId));
        if (!isMember) throw new IllegalArgumentException("해당 그룹의 멤버가 아닙니다.");
    }

    private void validateManager(Long groupId, Long memberId) {
        GroupMember gm = groupMemberRepository.findAll().stream()
                .filter(m -> m.getGroup().getId().equals(groupId) && m.getMember().getId().equals(memberId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당 그룹의 멤버가 아닙니다."));

        if (gm.getRole() != GroupRole.MANAGER) {
            throw new IllegalArgumentException("그룹 관리자만 수행할 수 있습니다.");
        }
    }
}