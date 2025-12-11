package com.safely.domain.expense.dto;

import com.safely.domain.expense.entity.Expense;
import java.time.LocalDate;

// 목록 조회용 DTO
public record ExpenseResponse(
        Long expenseId,
        String location,
        Long amount,
        String payerName,
        LocalDate spentDate,
        String category,
        int participantCount // "루크(결제) 외 N명" 표기를 위함
) {
    public static ExpenseResponse from(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getLocation(),
                expense.getAmount(),
                expense.getPayer().getName(), // 실제로는 GroupMember의 닉네임을 가져오면 더 좋음
                expense.getSpentDate(),
                expense.getCategory().getDescription(),
                expense.getParticipants().size()
        );
    }
}