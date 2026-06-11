package com.subpilot.module.bill.controller;

import com.subpilot.common.response.ApiResponse;
import com.subpilot.module.bill.dto.BillCreateRequest;
import com.subpilot.module.bill.service.BillService;
import com.subpilot.module.bill.vo.BillPageVO;
import com.subpilot.module.bill.vo.BillVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Tag(name = "Bills")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping
public class BillController {

    private final BillService billService;

    @Operation(summary = "Create a bill")
    @PostMapping("/api/bills")
    public ApiResponse<BillVO> create(@Valid @RequestBody BillCreateRequest request) {
        return ApiResponse.success(billService.create(request));
    }

    @Operation(summary = "List bills")
    @GetMapping("/api/bills")
    public ApiResponse<BillPageVO> list(
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) long size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ApiResponse.success(billService.list(page, size, status, keyword, startDate, endDate));
    }

    @Operation(summary = "Get bill detail")
    @GetMapping("/api/bills/{id}")
    public ApiResponse<BillVO> detail(@PathVariable Long id) {
        return ApiResponse.success(billService.detail(id));
    }

    @Operation(summary = "Mark bill as paid")
    @PutMapping("/api/bills/{id}/paid")
    public ApiResponse<BillVO> markPaid(@PathVariable Long id) {
        return ApiResponse.success(billService.markPaid(id));
    }

    @Operation(summary = "Mark bill as unpaid")
    @PutMapping("/api/bills/{id}/unpaid")
    public ApiResponse<BillVO> markUnpaid(@PathVariable Long id) {
        return ApiResponse.success(billService.markUnpaid(id));
    }

    @Operation(summary = "List bills by subscription")
    @GetMapping("/api/subscriptions/{subscriptionId}/bills")
    public ApiResponse<BillPageVO> listBySubscription(
            @PathVariable Long subscriptionId,
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) long size
    ) {
        return ApiResponse.success(billService.listBySubscription(subscriptionId, page, size));
    }
}
