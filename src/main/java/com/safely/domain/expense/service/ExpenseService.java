package com.safely.domain.expense.service;

import com.safely.domain.expense.dto.ExpenseCreateRequest;
import com.safely.domain.expense.dto.ExpenseResponse;
import com.safely.domain.expense.entity.Expense;
import com.safely.domain.expense.entity.ExpenseParticipant;
import com.safely.domain.expense.repository.ExpenseRepository;
import com.safely.domain.group.entity.Group;
import com.safely.domain.group.repository.GroupRepository;
import com.safely.domain.member.entity.Member;
import com.safely.domain.member.repository.MemberRepository;
import com.safely.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Long createExpense(Long groupId, ExpenseCreateRequest request) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(NotFoundException::new);

        Member payer = memberRepository.findById(request.payerId())
                .orElseThrow(NotFoundException::new);

        // 지출 엔티티 생성
        Expense expense = Expense.builder()
                .group(group)
                .payer(payer)
                .amount(request.amount())
                .location(request.location())
                .category(request.category())
                .spentDate(request.spentDate())
                .build();

        // 1/N 분배 로직 실행 및 참여자 저장
        distributeAmountAndSaveParticipants(expense, request.amount(), request.participantMemberIds());

        expenseRepository.save(expense);
        return expense.getId();
    }

    // 2. 지출 내역 목록 조회
    public List<ExpenseResponse> getExpenses(Long groupId) {
        return expenseRepository. findAllByGroupId(groupId).stream()
                .map(ExpenseResponse::from)
                .toList();
    }

    // 3. 지출 내역 수정
    @Transactional
    public void updateExpense(Long groupId, Long expenseId, ExpenseCreateRequest request) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(NotFoundException::new);

        validateGroupAccess(expense, groupId);

        Member newPayer = memberRepository.findById(request.payerId())
                .orElseThrow(NotFoundException::new);

        // 기본 정보 업데이트
        expense.update(newPayer, request.amount(), request.location(), request.category(), request.spentDate());

        // 기존 참여자 목록 삭제 후 재생성 (가장 깔끔한 갱신 방법)
        expense.clearParticipants();
        distributeAmountAndSaveParticipants(expense, request.amount(), request.participantMemberIds());
    }

    // 4. 지출 내역 삭제
    @Transactional
    public void deleteExpense(Long groupId, Long expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(NotFoundException::new);

        validateGroupAccess(expense, groupId);

        expenseRepository.delete(expense);
    }

    // 금액 분배 및 참여자 생성 로직 (1/N + 나머지 처리)
    private void distributeAmountAndSaveParticipants(Expense expense, Long totalAmount, List<Long> participantIds) {
        if (participantIds == null || participantIds.isEmpty()) {
            throw new IllegalArgumentException("참여 인원은 최소 1명 이상이어야 합니다.");
        }

        List<Member> members = memberRepository.findAllById(participantIds);
        if (members.size() != participantIds.size()) {
            throw new IllegalArgumentException("존재하지 않는 멤버가 포함되어 있습니다.");
        }

        int count = members.size();
        long splitAmount = totalAmount / count; // 몫
        long remainder = totalAmount % count;   // 나머지

        for (int i = 0; i < count; i++) {
            long individualAmount = splitAmount;

            // 나머지는 첫 번째 사람에게 부과 (예: 10,000원 / 3명 -> 3,334 / 3,333 / 3,333)
            if (i == 0) {
                individualAmount += remainder;
            }

            ExpenseParticipant participant = ExpenseParticipant.builder()
                    .expense(expense)
                    .member(members.get(i))
                    .amount(individualAmount)
                    .build();

            expense.addParticipant(participant);
        }
    }

    private void validateGroupAccess(Expense expense, Long groupId) {
        if (!expense.getGroup().getId().equals(groupId)) {
            throw new IllegalArgumentException("해당 그룹의 지출 내역이 아닙니다.");
        }
    }
}
