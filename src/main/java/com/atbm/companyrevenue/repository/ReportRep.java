package com.atbm.companyrevenue.repository;

import com.atbm.companyrevenue.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRep extends JpaRepository<Report, Long> {
}
