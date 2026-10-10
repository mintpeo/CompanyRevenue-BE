package com.atbm.companyrevenue.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "daily_data")
@Data
@NoArgsConstructor
public class Daily {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id")
    private Business business;

    private LocalDate date;
    private String branch;
    private Integer revenue;
    private Integer orders;
    private Integer customer;
    private Integer expense;
    private Integer staff;
    private Integer complaint;
    private Integer cancelled;

    public Daily(Business business, LocalDate date, String branch, Integer revenue, Integer orders, Integer customer, Integer expense, Integer staff, Integer complaint, Integer cancelled) {
        this.business = business;
        this.date = date;
        this.branch = branch;
        this.revenue = revenue;
        this.orders = orders;
        this.customer = customer;
        this.expense = expense;
        this.staff = staff;
        this.complaint = complaint;
        this.cancelled = cancelled;
    }
}