package com.safely.domain.expense.dto;

import com.safely.domain.expense.ExpenseCategory;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record ExpenseCreateRequest(
        @NotNull(message = "결제일은 필수입니다.") LocalDate spentDate,
        @NotNull(message = "장소는 필수입니다.") String location,
        @NotNull(message = "항목은 필수입니다.") ExpenseCategory category,
        @NotNull(message = "금액은 필수입니다.") Long amount,
        @NotNull(message = "결제자는 필수입니다.") Long payerId, // 결제한 멤버의 ID
        @NotNull(message = "참여 인원은 최소 1명 이상이어야 합니다.") List<Long> participantMemberIds // 함께한 멤버 ID 리스트
) {}