package com.atbm.companyrevenue.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailyDataTodayRes {
    private String businessId;
    private Integer totalRevenue;
    private Integer totalOrders;
    private Integer totalCustomers;
    private Integer totalExpense;
    private Integer totalStaff;
    private Integer totalComplaints;
    private Integer totalCancelled;
    private LocalDate date;
}