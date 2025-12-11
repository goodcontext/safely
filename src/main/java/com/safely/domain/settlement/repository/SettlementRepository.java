package com.safely.domain.settlement.repository;

import com.safely.domain.settlement.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    // 그룹 내 특정 멤버의 정산 기록 조회 (Upsert용(Upsert = Update + Insert))
    Optional<Settlement> findByGroupIdAndMemberId(Long groupId, Long memberId);

    // 그룹의 정산 내역 전체 조회 (화면 표시용)
    // N+1 문제 방지를 위해 Member fetch join
    @Query("SELECT s FROM Settlement s JOIN FETCH s.member WHERE s.group.id = :groupId")
    List<Settlement> findAllByGroupId(@Param("groupId") Long groupId);
}