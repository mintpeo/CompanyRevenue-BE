package com.atbm.companyrevenue.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "alert_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id")
    @JsonIgnore
    private Business business;

    @Enumerated(EnumType.STRING)
    private AlertRule alert;

    @Enumerated(EnumType.STRING)
    private AlertSeverity severity;

    private int threshold;
    private boolean enabled;

    public Alert(Business business, AlertRule alert, AlertSeverity severity, int threshold, boolean enabled) {
        this.business = business;
        this.alert = alert;
        this.severity = severity;
        this.threshold = threshold;
        this.enabled = enabled;
    }
}
