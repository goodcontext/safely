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

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expense_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_id", nullable = false)
    private Member payer; // 결제자 (실제 돈을 낸 사람)

    @Column(nullable = false)
    private Long amount; // 총 지출 금액

    @Column(nullable = false)
    private String location; // 장소

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseCategory category; // 항목 (식비, 교통 등)

    @Column(nullable = false)
    private LocalDate spentDate; // 결제일

    // CascadeType.ALL + orphanRemoval = true : 지출 삭제 시 분담 내역도 함께 삭제
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

    // 수정 메서드
    public void update(Member payer, Long amount, String location, ExpenseCategory category, LocalDate spentDate) {
        this.payer = payer;
        this.amount = amount;
        this.location = location;
        this.category = category;
        this.spentDate = spentDate;
    }

    // 연관관계 편의 메서드
    public void addParticipant(ExpenseParticipant participant) {
        this.participants.add(participant);
    }

    // 참여자 목록 초기화 (수정 시 사용)
    // @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    // 원래는 ArrayList<>()의 clear() 메서드 사용하면 메모리에서만 삭제되어야 하지만, 엔티티 클래스 안에서 orphanRemoval = true
    // 옵션 주면 DB까지 같이 삭제됨.
    //
    // JPA에서의 특별한 의미 (중요!) (출처: 제미나이 3.0 pro)
    // 그냥 자바 ArrayList에서는 clear()를 하면 "메모리에서만" 데이터가 사라지지만, JPA 엔티티 안에서 사용할 때는 DB 데이터 삭제로
    // 이어지는 강력한 기능을 합니다.
    // 설정: @OneToMany(..., orphanRemoval = true)
    // 동작: this.participants.clear()가 호출되면, JPA는 "어? 리스트에서 객체들이 없어졌네? 고아(Orphan)가 되었으니 DB에서도 지워야지!"
    // 라고 판단하고 DELETE 쿼리를 날려줍니다.
    public void clearParticipants() {
        this.participants.clear();
    }
}