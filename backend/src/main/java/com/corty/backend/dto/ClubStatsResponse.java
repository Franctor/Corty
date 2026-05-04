package com.corty.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class ClubStatsResponse {

    private BigDecimal totalRevenue;
    private BigDecimal totalPenalties;
    private Map<String, BigDecimal> revenueByMonth;
    private Map<String, BigDecimal> penaltiesByMonth;
    private Map<String, BigDecimal> penaltiesByReason;
    private List<ClubBalanceEntryResponse> balanceEntries;
}
