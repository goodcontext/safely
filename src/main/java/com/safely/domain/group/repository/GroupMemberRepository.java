package com.safely.domain.group.repository;

import com.safely.domain.group.entity.Group;
import com.safely.domain.group.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    // JPA는 리스트를 반환할 때 null 값을 주지 않고, 빈 리스트를 반환하므로 Optional을 사용할 필요가 없음.
    // 그리고, Optional<List>는 안티패턴임. 값을 이중으로 확인해야 하기 때문임.
    @Query("SELECT gm FROM GroupMember gm JOIN FETCH gm.group WHERE gm.member.id = :memberId")
    List<GroupMember> findAllByMemberIdWithGroup(@Param("memberId") Long memberId);

    @Query("SELECT gm FROM GroupMember gm JOIN FETCH gm.member WHERE gm.group.id = :groupId")
    List<GroupMember> findAllByGroupIdWithMember(@Param("groupId") Long groupId);

    // List가 아니고 1건 조회이므로 Optional로 가져옴.
    Optional<GroupMember> findByGroupIdAndMemberId(Long groupId, Long memberId);

    // 그룹 ID와 멤버 ID로 존재 여부만 딱 확인 (SELECT 1 ... LIMIT 1)
    boolean existsByGroupIdAndMemberId(Long groupId, Long memberId);

    List<GroupMember> findByGroup(Group group);
}
