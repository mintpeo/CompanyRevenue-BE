package com.atbm.companyrevenue.dto.response;

import lombok.Data;

@Data
public class BenchMarksRes {
    private String businessId;
    private double averageRevenue;
    private double averageCancellationRate;
    private double averageComplaints;
}