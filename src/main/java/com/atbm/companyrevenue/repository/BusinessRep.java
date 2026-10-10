package com.atbm.companyrevenue.repository;

import com.atbm.companyrevenue.model.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessRep extends JpaRepository<Business, String> {
}
