package com.atbm.companyrevenue.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Table(name = "report_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id")
    @JsonIgnore
    private Business business;

    private boolean dailyReport;
    private LocalTime reportTime;
    private boolean includeRevenue;
    private boolean includeOrders;
    private boolean includeCustomers;
    private boolean includeExpense;
    private boolean includeAlerts;

    public Report(Business business, boolean dailyReport, LocalTime reportTime, boolean includeRevenue, boolean includeOrders, boolean includeCustomers, boolean includeExpense, boolean includeAlerts) {
        this.business = business;
        this.dailyReport = dailyReport;
        this.reportTime = reportTime;
        this.includeRevenue = includeRevenue;
        this.includeOrders = includeOrders;
        this.includeCustomers = includeCustomers;
        this.includeExpense = includeExpense;
        this.includeAlerts = includeAlerts;
    }
}
