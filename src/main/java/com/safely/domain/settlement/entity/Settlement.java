package com.safely.domain.settlement.entity;

import com.safely.domain.common.entity.BaseEntity;
import com.safely.domain.group.entity.Group;
import com.safely.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "settlements")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Settlement extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "settlement_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 최종 정산 금액 (양수: 받을 돈 / 음수: 보낼 돈)
    @Column(name = "net_amount", nullable = false)
    private Long netAmount;

    @Column(name = "is_settled", nullable = false)
    private boolean isSettled;

    // 정산 확정 일시 (시간 포함)
    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    @Builder
    public Settlement(Group group, Member member, Long netAmount, boolean isSettled) {
        this.group = group;
        this.member = member;
        this.netAmount = netAmount;
        this.isSettled = isSettled;
        if (isSettled) {
            this.settledAt = LocalDateTime.now();
        }
    }

    // 정산 결과 업데이트 (재정산 또는 취소 시)
    public void update(Long netAmount, boolean isSettled) {
        this.netAmount = netAmount;
        this.isSettled = isSettled;
        // 정산 완료(true)면 현재 시간, 취소(false)면 null
        this.settledAt = isSettled ? LocalDateTime.now() : null;
    }
}