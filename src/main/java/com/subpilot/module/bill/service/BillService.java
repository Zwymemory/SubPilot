package com.subpilot.module.bill.service;

import com.subpilot.module.bill.dto.BillCreateRequest;
import com.subpilot.module.bill.vo.BillPageVO;
import com.subpilot.module.bill.vo.BillVO;

import java.time.LocalDate;

public interface BillService {

    BillVO create(BillCreateRequest request);

    BillPageVO list(long page, long size, String status, String keyword, LocalDate startDate, LocalDate endDate);

    BillVO detail(Long id);

    BillVO markPaid(Long id);

    BillVO markUnpaid(Long id);

    BillPageVO listBySubscription(Long subscriptionId, long page, long size);
}
