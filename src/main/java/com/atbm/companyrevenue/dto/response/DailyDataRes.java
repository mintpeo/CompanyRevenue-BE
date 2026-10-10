package com.atbm.companyrevenue.dto.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DailyDataRes {
    private Long id;
    private String businessId;
    private LocalDate date;
    private String branch;
    private Integer revenue;
    private Integer orders;
    private Integer customer;
    private Integer expense;
    private Integer staff;
    private Integer complaints;
    private Integer cancelled;
}
