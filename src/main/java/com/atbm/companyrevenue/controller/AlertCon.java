package com.atbm.companyrevenue.controller;

import com.atbm.companyrevenue.dto.response.AlertRes;
import com.atbm.companyrevenue.dto.response.BenchMarksRes;
import com.atbm.companyrevenue.dto.response.PercentBranchAlertRes;
import com.atbm.companyrevenue.service.AlertSer;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/alert")
@CrossOrigin(origins = "${app.frontend.url}")
@RequiredArgsConstructor
public class AlertCon {
    private final AlertSer ser;

    @GetMapping("/handle")
    public List<AlertRes> handleAlerts() {
        ser.sendMail();
        return ser.handleAlerts();
    }

    @GetMapping("/per")
    public List<PercentBranchAlertRes> handlePer() {
        return ser.getListPerBranch();
    }

    @GetMapping("/avg")
    public List<BenchMarksRes> handleBenchMarks() {
        return ser.handleBenchMarks();
    }
}
