package com.safely.domain.expense.entity;

import com.safely.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "expense_participants")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExpenseParticipant {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expense_participant_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", nullable = false)
    private Expense expense;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member; // 비용을 분담해야 할 사람

    @Column(nullable = false)
    private Long amount; // 이 사람이 내야 할 금액 (1/N 된 금액)

    @Builder
    public ExpenseParticipant(Expense expense, Member member, Long amount) {
        this.expense = expense;
        this.member = member;
        this.amount = amount;
    }
}