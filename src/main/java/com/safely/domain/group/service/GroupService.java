package com.safely.domain.group.service;

import com.safely.domain.group.GroupRole;
import com.safely.domain.group.dto.*;
import com.safely.domain.group.entity.Group;
import com.safely.domain.group.entity.GroupMember;
import com.safely.domain.group.repository.GroupMemberRepository;
import com.safely.domain.group.repository.GroupRepository;
import com.safely.domain.member.entity.Member;
import com.safely.domain.member.repository.MemberRepository;
import com.safely.global.exception.common.EntityNotFoundException;
import com.safely.global.exception.group.AlreadyJoinedGroupException;
import com.safely.global.exception.group.GroupPermissionDeniedException;
import com.safely.global.exception.group.InvalidInviteCodeException;
import com.safely.global.exception.group.NotGroupMemberException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
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

        log.info("[+] 그룹 생성 완료: GroupID={}, GroupName={}, ManagerID={}",
                savedGroup.getId(), savedGroup.getName(), memberId);
        return savedGroup.getId();
    }

    // 2. 내 그룹 목록 조회
    @Transactional(readOnly = true) // readOnly = true는 조회 전용이므로 성능 향상
    public List<GroupResponse> getMyGroups(Long memberId) {
        List<GroupMember> myGroupMembers = groupMemberRepository.findAllByMemberIdWithGroup(memberId);

        // stream()은 빈 리스트를 받으면 아무일도 하지 않고 빈 리스트를 반환함. 따라서 null값 체크할 필요 없음.
        return myGroupMembers.stream()
                .map(gm -> GroupResponse.from(gm.getGroup()))
                .toList();
    }

    // 3. 그룹 상세 조회
    public GroupDetailResponse getGroupDetail(Long groupId, Long memberId) {
        Group group = getGroup(groupId);
        validateMemberInGroup(groupId, memberId); // 멤버인지 확인

        List<GroupMember> members = group.getGroupMembers();
        log.info("[*] 그룹 상세 조회: GroupID={}, RequesterID={}", groupId, memberId);
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
        log.info("[*] 그룹 정보 수정 완료: GroupID={}, ModifierID={}", groupId, memberId);
    }

    // 5. 그룹 삭제 (관리자만 가능)
    @Transactional
    public void deleteGroup(Long groupId, Long memberId) {
        Group group = getGroup(groupId);
        validateManager(groupId, memberId);

        groupRepository.delete(group); // Cascade 설정으로 GroupMember도 삭제됨
        log.info("[-] 그룹 삭제 완료: GroupID={}, DeletedBy={}", groupId, memberId);
    }

    // 6. 초대 코드로 그룹 가입
    @Transactional
    public void joinGroupByCode(Long memberId, String inviteCode) {
        Member member = getMember(memberId);

        Group group = groupRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> {
                    log.warn("[!] 그룹 가입 실패: 유효하지 않은 초대 코드. MemberID={}, Code={}", memberId, inviteCode);
                    return new InvalidInviteCodeException();
                });

        // 이미 가입된 멤버인지 확인
        boolean isAlreadyMember = group.getGroupMembers().stream()
                .anyMatch(gm -> gm.getMember().getId().equals(memberId));
        if (isAlreadyMember) {
            log.warn("[!] 그룹 가입 실패: 이미 가입된 그룹. MemberID={}, GroupID={}", memberId, group.getId());
            throw new AlreadyJoinedGroupException();
        }

        GroupMember newMember = GroupMember.builder()
                .group(group)
                .member(member)
                .role(GroupRole.MEMBER)
                .memberName(member.getName())
                .build();
        groupMemberRepository.save(newMember);
        log.info("[+] 그룹 가입 완료: MemberID={}, GroupID={}", memberId, group.getId());
    }

    // --- 검증 및 조회 헬퍼 메서드 ---
    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(EntityNotFoundException::new);
    }

    private Group getGroup(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(EntityNotFoundException::new);
    }

    private void validateMemberInGroup(Long groupId, Long memberId) {
        // existsBy 메서드를 사용하므로 O(log N)의 속도로 DB에서 인덱스를 타고 존재 여부만 0.01초 만에 확인
        // 참고로 findAll로 찾으면 O(N)의 속도임. O(N) -> 데이터가 많아지면 그만큼 시간도 늘어남.
        // O(1) : 1건 조회 (가장 빠름)
        // O(Log N) 이분검색으로 조회, 시간 약간 늘어남. 인덱스 조회일 때 걸리는 시간임. (두 번째로 빠름)
        // O(N) : 100만 건 검색 시 100만 번 조회. Full Table Scan 시 걸리는 시간임. (느림)
        // O(N^2) : for문 이중 루프 시 걸리는 시간 (최악)
        if (!groupMemberRepository.existsByGroupIdAndMemberId(groupId, memberId)) {
            log.warn("[!] 접근 거부: 그룹 멤버가 아님. GroupID={}, MemberID={}", groupId, memberId);
            throw new NotGroupMemberException();
        }
    }

    private void validateManager(Long groupId, Long memberId) {
        GroupMember gm = groupMemberRepository.findByGroupIdAndMemberId(groupId, memberId)
                .orElseThrow(() -> {
                    log.warn("[!] 권한 없음: 그룹 멤버 정보 조회 불가. GroupID: {}, MemberID: {}", groupId, memberId);
                    return new NotGroupMemberException();
                });

        // 관리자 권한 체크
        if (gm.getRole() != GroupRole.MANAGER) {
            log.warn("[!] 권한 거부: 관리자 권한 필요. GroupID={}, MemberID={}", groupId, memberId);
            throw new GroupPermissionDeniedException();
        }
    }
}