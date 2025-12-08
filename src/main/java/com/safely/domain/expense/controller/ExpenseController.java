package com.safely.domain.expense.controller;

import com.safely.domain.expense.dto.ExpenseCreateRequest;
import com.safely.domain.expense.dto.ExpenseResponse;
import com.safely.domain.expense.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}/expenses")
@RequiredArgsConstructor
public class ExpenseController {
    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<Long> createExpense(
            @PathVariable Long groupId,
            @Valid @RequestBody ExpenseCreateRequest request) {
        Long expenseId = expenseService.createExpense(groupId, request);
        return ResponseEntity.ok(expenseId);
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> getExpenses(
            @PathVariable Long groupId) {
        return ResponseEntity.ok(expenseService.getExpenses(groupId));
    }

    @PutMapping("/{expenseId}")
    public ResponseEntity<Void> updateExpense(
            @PathVariable Long groupId,
            @PathVariable Long expenseId,
            @Valid @RequestBody ExpenseCreateRequest request) {
        expenseService.updateExpense(groupId, expenseId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<Void> deleteExpense(
            @PathVariable Long groupId,
            @PathVariable Long expenseId) {
        expenseService.deleteExpense(groupId, expenseId);
        return ResponseEntity.ok().build();
    }
}