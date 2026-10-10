package com.atbm.companyrevenue.dto.response;

import lombok.Data;

@Data
public class AlertRes {
    private String businessId;
    private String branch;
    private String type;
    private String severity;
    private double value;
    private String message;
}
