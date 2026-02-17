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
        int participantCount
) {
    public static ExpenseResponse from(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getLocation(),
                expense.getAmount(),
                expense.getPayer().getName(),
                expense.getSpentDate(),
                expense.getCategory().getDescription(),
                expense.getParticipants().size()
        );
    }
}