package com.safely.domain.group.service;

import com.safely.domain.group.GroupRole;
import com.safely.domain.group.dto.GroupCreateRequest;
import com.safely.domain.group.entity.Group;
import com.safely.domain.group.entity.GroupMember;
import com.safely.domain.group.repository.GroupMemberRepository;
import com.safely.domain.group.repository.GroupRepository;
import com.safely.domain.member.entity.Member;
import com.safely.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupService {
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Long createGroup(Long memberId, GroupCreateRequest request) {
        // 1. 회원 조회 (없으면 예외 발생)
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 2. 그룹 엔티티 생성 및 저장
        Group group = request.toEntity();
        Group savedGroup = groupRepository.save(group);

        // 3. 생성자를 관리자로 등록 (GroupMember 생성)
        GroupMember manager = GroupMember.builder()
                .group(savedGroup)
                .member(member)
                .role(GroupRole.MANAGER)
                .memberName(member.getName())
                .build();

        groupMemberRepository.save(manager);

        return savedGroup.getId();
    }
}
