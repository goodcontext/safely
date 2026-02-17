package com.safely.domain.expense.entity;

import com.safely.domain.common.entity.BaseEntity;
import com.safely.domain.expense.ExpenseCategory;
import com.safely.domain.group.entity.Group;
import com.safely.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "expenses")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Expense extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expense_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_id", nullable = false)
    private Member payer; // 결제자 (실제 돈을 낸 사람)

    @Version
    private Long version; // @Transactional로는 동시 수정 충돌을 감지할 수 없으므로 낙관적 락(@Version) 기능을 추가함.

    @Column(nullable = false)
    private Long amount; // 총 지출 금액

    @Column(nullable = false)
    private String location; // 장소

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseCategory category; // 항목 (식비, 교통 등)

    @Column(nullable = false)
    private LocalDate spentDate; // 결제일

    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExpenseParticipant> participants = new ArrayList<>();

    @Builder
    public Expense(Group group, Member payer, Long amount, String location, ExpenseCategory category, LocalDate spentDate) {
        this.group = group;
        this.payer = payer;
        this.amount = amount;
        this.location = location;
        this.category = category;
        this.spentDate = spentDate;
    }

    public void update(Member payer, Long amount, String location, ExpenseCategory category, LocalDate spentDate) {
        this.payer = payer;
        this.amount = amount;
        this.location = location;
        this.category = category;
        this.spentDate = spentDate;
    }

    public void addParticipant(ExpenseParticipant participant) {
        this.participants.add(participant);
    }

    public void clearParticipants() {
        this.participants.clear();
    }
}