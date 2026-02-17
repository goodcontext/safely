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

    @GetMapping("/preview")
    public ResponseEntity<List<SettlementResponse>> previewSettlement(@PathVariable Long groupId) {
        return ResponseEntity.ok(settlementService.getSettlementPreview(groupId));
    }

    @PostMapping("/complete")
    public ResponseEntity<Void> completeSettlement(@PathVariable Long groupId) {
        settlementService.completeSettlement(groupId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cancel")
    public ResponseEntity<Void> cancelSettlement(@PathVariable Long groupId) {
        settlementService.cancelSettlement(groupId);
        return ResponseEntity.ok().build();
    }
}