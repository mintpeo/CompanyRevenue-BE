package com.atbm.companyrevenue.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Table(name = "business_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Business {
    @Id
    private String businessId;

    private String businessName;
    private String industry;
    private String ownerEmail;
    private LocalTime reportTime;
    private String timeZone; // OffsetTime nowInVietnam = OffsetTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
    private boolean status;
}