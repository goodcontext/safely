package com.safely.domain.group.repository;

import com.safely.domain.group.entity.Group;
import com.safely.domain.group.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    @Query("SELECT gm FROM GroupMember gm JOIN FETCH gm.group WHERE gm.member.id = :memberId")
    List<GroupMember> findAllByMemberIdWithGroup(@Param("memberId") Long memberId);

    @Query("SELECT gm FROM GroupMember gm JOIN FETCH gm.member WHERE gm.group.id = :groupId")
    List<GroupMember> findAllByGroupIdWithMember(@Param("groupId") Long groupId);

    Optional<GroupMember> findByGroupIdAndMemberId(Long groupId, Long memberId);

    boolean existsByGroupIdAndMemberId(Long groupId, Long memberId);

    List<GroupMember> findByGroup(Group group);
}
