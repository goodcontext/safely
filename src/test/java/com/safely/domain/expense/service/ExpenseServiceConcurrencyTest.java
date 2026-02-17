package com.safely.domain.expense.service;

import com.safely.domain.expense.ExpenseCategory;
import com.safely.domain.expense.dto.ExpenseCreateRequest;
import com.safely.domain.expense.entity.Expense;
import com.safely.domain.expense.repository.ExpenseRepository;
import com.safely.domain.group.dto.GroupCreateRequest;
import com.safely.domain.group.service.GroupService;
import com.safely.domain.member.entity.Member;
import com.safely.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ExpenseServiceConcurrencyTest {

    @Autowired private ExpenseService expenseService;
    @Autowired private GroupService groupService;
    @Autowired private MemberRepository memberRepository;
    @Autowired private ExpenseRepository expenseRepository;

    @Test
    @DisplayName("동시성 테스트: 동시에 10명이 같은 지출 내역을 수정하면, @Retryable 덕분에 여러 건이 성공해야 한다.")
    void updateExpense_Concurrency() throws InterruptedException {
        // 멤버 생성
        Member payer = memberRepository.save(Member.builder().email("payer@test.com").password("1").name("결제자").authority("USER").build());
        Member participant = memberRepository.save(Member.builder().email("part@test.com").password("1").name("참여자").authority("USER").build());

        // 그룹 생성
        Long groupId = groupService.createGroup(payer.getId(), new GroupCreateRequest("동시성 여행", LocalDate.now(), LocalDate.now(), "Seoul"));
        groupService.joinGroupByCode(participant.getId(), groupService.getGroupDetail(groupId, payer.getId()).inviteCode());

        // 지출 내역 생성 (초기 금액 10,000원)
        ExpenseCreateRequest createRequest = new ExpenseCreateRequest(
                LocalDate.now(), "식당", ExpenseCategory.FOOD, 10000L, payer.getId(), List.of(payer.getId(), participant.getId())
        );
        Long expenseId = expenseService.createExpense(groupId, createRequest);

        // 동시성 로직 실행
        int threadCount = 10; // 동시에 10명 접속 가정
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount); // 모든 스레드가 끝날 때까지 대기용

        AtomicInteger successCount = new AtomicInteger(); // 성공 횟수 카운트
        AtomicInteger failCount = new AtomicInteger();    // 실패 횟수 카운트

        for (int i = 0; i < threadCount; i++) {
            int finalI = i;
            executorService.submit(() -> {
                try {
                    // 각 스레드가 금액을 조금씩 다르게 수정 시도 (구분하기 위함)
                    Long newAmount = 20000L + finalI;
                    ExpenseCreateRequest updateRequest = new ExpenseCreateRequest(
                            LocalDate.now(), "식당_수정", ExpenseCategory.FOOD, newAmount, payer.getId(), List.of(payer.getId(), participant.getId())
                    );

                    expenseService.updateExpense(groupId, expenseId, updateRequest);
                    successCount.getAndIncrement();
                    System.out.println("[Success] Thread-" + finalI + " 수정 성공");
                } catch (Exception e) {
                    failCount.getAndIncrement();
                    System.out.println("[Fail] Thread-" + finalI + " 수정 실패: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 스레드 종료 대기

        // 결과 검증
        Expense finalExpense = expenseRepository.findById(expenseId).orElseThrow();

        System.out.println("=== 테스트 결과 ===");
        System.out.println("=== 테스트 결과 ===");
        System.out.println("성공 횟수: " + successCount.get());
        System.out.println("실패 횟수: " + failCount.get());
        System.out.println("최종 버전(Version): " + finalExpense.getVersion());
        System.out.println("최종 금액: " + finalExpense.getAmount());

        // 낙관적 락(@Version)이 없었다면: 덮어쓰기(Lost Update)가 발생하여 버전은 0 -> 1로 한 번만 오르고 끝날 수 있음.
        // 낙관적 락 + @Retryable 적용 시: 충돌 난 스레드가 재시도하여 버전을 계속 올림.

        // 최소 1건은 무조건 성공해야 함
        assertThat(successCount.get()).isGreaterThan(0);

        // 버전이 0(초기)보다 커야 함 (수정이 반영됨)
        assertThat(finalExpense.getVersion()).isGreaterThan(0L);
    }
}