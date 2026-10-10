package com.atbm.companyrevenue.repository;

import com.atbm.companyrevenue.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertRep extends JpaRepository<Alert, Long> {
}
