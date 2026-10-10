package com.atbm.companyrevenue.controller;

import com.atbm.companyrevenue.dto.response.DailyDataRes;
import com.atbm.companyrevenue.dto.response.DailyDataTodayRes;
import com.atbm.companyrevenue.service.DailySer;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/daily")
@CrossOrigin(origins = "${app.frontend.url}")
@RequiredArgsConstructor
public class DailyCon {
    private final DailySer ser;

    @GetMapping("/total-branch-today")
    public List<DailyDataTodayRes> totalToday() {
        return ser.calculateSummaryByBusiness();
    }

    @GetMapping("/each-branch-today")
    public List<DailyDataRes> getToday() {
        return ser.getDailyToday();
    }
}
