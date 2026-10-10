package com.atbm.companyrevenue.dto.response;

import lombok.Data;

@Data
public class PercentBranchAlertRes {
    private String businessId;
    private String branch;
    private double revenuePer;
    private double cancellationPer;
    private Integer complaint;
}
