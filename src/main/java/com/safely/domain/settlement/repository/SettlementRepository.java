package com.safely.domain.settlement.repository;

import com.safely.domain.settlement.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    Optional<Settlement> findByGroupIdAndMemberId(Long groupId, Long memberId);

    @Query("SELECT s FROM Settlement s JOIN FETCH s.member WHERE s.group.id = :groupId")
    List<Settlement> findAllByGroupId(@Param("groupId") Long groupId);
}