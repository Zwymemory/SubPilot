package com.subpilot.module.bill.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Bill page response")
public record BillPageVO(
        long page,
        long size,
        long total,
        long pages,
        List<BillVO> records
) {
}
