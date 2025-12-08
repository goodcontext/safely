package com.safely.domain.expense;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExpenseCategory {
    FOOD("식비"),
    TRANSPORT("교통"),
    ACCOMMODATION("숙박"),
    SHOPPING("쇼핑"),
    ACTIVITY("액티비티"),
    ETC("기타");

    private final String description;
}