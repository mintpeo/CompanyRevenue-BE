package com.atbm.companyrevenue.repository;

import com.atbm.companyrevenue.model.Daily;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DailyRep extends JpaRepository<Daily, Long> {
}
