package com.safely.domain.settlement.controller;

import com.safely.domain.settlement.dto.SettlementResponse;
import com.safely.domain.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}/settlements")
@RequiredArgsConstructor
public class SettlementController {
    private final SettlementService settlementService;

    // 1. 정산 프리뷰 (단순 조회)
    @GetMapping("/preview")
    public ResponseEntity<List<SettlementResponse>> previewSettlement(@PathVariable Long groupId) {
        return ResponseEntity.ok(settlementService.getSettlementPreview(groupId));
    }

    // 2. 정산 완료 (확정 저장)
    @PostMapping("/complete")
    public ResponseEntity<Void> completeSettlement(@PathVariable Long groupId) {
        settlementService.completeSettlement(groupId);
        return ResponseEntity.ok().build();
    }

    // 3. 정산 취소 (초기화)
    @PostMapping("/cancel")
    public ResponseEntity<Void> cancelSettlement(@PathVariable Long groupId) {
        settlementService.cancelSettlement(groupId);
        return ResponseEntity.ok().build();
    }
}