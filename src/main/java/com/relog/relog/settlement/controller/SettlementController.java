package com.relog.relog.settlement.controller;

import com.relog.relog.common.ApiResponse;
import com.relog.relog.settlement.dto.MonthlySettlementResponse;
import com.relog.relog.settlement.dto.QuarterlySettlementResponse;
import com.relog.relog.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<MonthlySettlementResponse>> getMonthlySettlement(
            @AuthenticationPrincipal Long memberId,
            @RequestParam int year,
            @RequestParam int month) {
        MonthlySettlementResponse response = settlementService.getMonthlySettlement(memberId, year, month);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/quarterly")
    public ResponseEntity<ApiResponse<QuarterlySettlementResponse>> getQuarterlySettlement(
            @AuthenticationPrincipal Long memberId,
            @RequestParam int year,
            @RequestParam int quarter) {
        QuarterlySettlementResponse response = settlementService.getQuarterlySettlement(memberId, year, quarter);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
