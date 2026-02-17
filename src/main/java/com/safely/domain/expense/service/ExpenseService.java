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
import com.safely.global.exception.BusinessException;
import com.safely.global.exception.ErrorCode;
import com.safely.global.exception.common.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
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
                .orElseThrow(EntityNotFoundException::new);

        Member payer = memberRepository.findById(request.payerId())
                .orElseThrow(EntityNotFoundException::new);

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

        log.info("[+] 지출 내역 생성: ExpenseID={}, GroupID={}, Amount={}", expense.getId(), groupId, request.amount());
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
    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000)
    )
    public void updateExpense(Long groupId, Long expenseId, ExpenseCreateRequest request) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> {
                    log.warn("[!] 지출 수정 실패: 존재하지 않는 내역. ExpenseID={}", expenseId);
                    return new EntityNotFoundException();
                });

        validateGroupAccess(expense, groupId);

        Member newPayer = memberRepository.findById(request.payerId())
                .orElseThrow(EntityNotFoundException::new);

        // 기본 정보 업데이트
        expense.update(newPayer, request.amount(), request.location(), request.category(), request.spentDate());

        // 기존 참여자 목록 삭제 후 재생성 (가장 깔끔한 갱신 방법)
        expense.clearParticipants();
        distributeAmountAndSaveParticipants(expense, request.amount(), request.participantMemberIds());

        log.info("[*] 지출 내역 수정 완료: ExpenseID={}, ModifierID={}", expenseId, request.payerId());

        // 트랜잭션이 끝날 때 버전 체크가 일어납니다. 실패하면 @Retryable이 잡아줍니다.
    }

    // 4. 지출 내역 삭제
    @Transactional
    public void deleteExpense(Long groupId, Long expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(EntityNotFoundException::new);

        validateGroupAccess(expense, groupId);

        expenseRepository.delete(expense);
        log.info("[-] 지출 내역 삭제 완료: ExpenseID={}, GroupID={}", expenseId, groupId);
    }

    // 금액 분배 및 참여자 생성 로직 (1/N + 나머지 처리)
    private void distributeAmountAndSaveParticipants(Expense expense, Long totalAmount, List<Long> participantIds) {
        if (participantIds == null || participantIds.isEmpty()) {
            log.warn("[!] 지출 생성 실패: 참여자 목록 비어있음.");
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        List<Member> members = memberRepository.findAllById(participantIds);
        if (members.size() != participantIds.size()) {
            log.warn("[!] 지출 생성 실패: 요청한 참여자와 DB 조회 결과 불일치. ReqSize={}, DBSize={}",
                    participantIds.size(), members.size());
            throw new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND);
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
            log.warn("[!] 권한 거부: 해당 그룹의 지출 내역이 아님. ReqGroupID={}, ExpenseGroupID={}",
                    groupId, expense.getGroup().getId());
            throw new BusinessException(ErrorCode.EXPENSE_NOT_FOUND);
        }
    }
}
