package com.safely.domain.expense.repository;

import com.safely.domain.expense.entity.Expense;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    @Query("SELECT e FROM Expense e JOIN FETCH e.payer WHERE e.group.id = :groupId ORDER BY e.spentDate DESC, e.createdAt DESC")
    List<Expense> findAllByGroupId(@Param("groupId") Long groupId);
}
