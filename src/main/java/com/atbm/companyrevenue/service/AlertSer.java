package com.atbm.companyrevenue.service;

import com.atbm.companyrevenue.dto.response.*;
import com.atbm.companyrevenue.model.Alert;
import com.atbm.companyrevenue.model.AlertRule;
import com.atbm.companyrevenue.model.AlertSeverity;
import com.atbm.companyrevenue.model.Business;
import com.atbm.companyrevenue.repository.AlertRep;
import com.atbm.companyrevenue.repository.BusinessRep;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertSer {
    private final AlertRep rep;
    private final DailySer dailySer;
    private final MailSer mailSer;
    private final BusinessRep businessRep;

    // Send Mail
    public void sendMail() {
        String subject = "BÁO CÁO KINH DOANH HẰNG NGÀY";
        String text;

        List<Business> businesses = businessRep.findAll();
        List<DailyDataTodayRes> calculateSummaryByBusiness = dailySer.calculateSummaryByBusiness();
        List<AlertRes> handleAlerts = handleAlerts();

        for (Business business : businesses) {
            // Total
            DailyDataTodayRes today = calculateSummaryByBusiness.stream()
                    .filter(c -> c.getBusinessId().equals(business.getBusinessId()))
                    .findFirst().orElseThrow(() -> new RuntimeException("Business Not Found"));
            String total = "TỔNG QUAN\n" +
                    "Báo cáo tổng quan hoạt động ngày 2026-10-09 của White Bakery:\n" +
                    "CHỈ SỐ KINH DOANH\n" +
                    "• Doanh thu: " + today.getTotalRevenue() +
                    "\n" +
                    "• Số đơn: " + today.getTotalOrders() +
                    "\n" +
                    "• Khách hàng: " + today.getTotalStaff() +
                    "\n" +
                    "• Chi phí: " + today.getTotalExpense() + "đ";
            // Alert
            List<AlertRes> alerts = handleAlerts.stream()
                    .filter(h -> h.getBusinessId().equals(business.getBusinessId())).toList();
            if (alerts.isEmpty()) {
                text = total;
                mailSer.sendMail(business.getOwnerEmail(), subject, text);
                continue;
            }
            StringBuilder sb = new StringBuilder("CẢNH BÁO\n");
            for (AlertRes ar : alerts) {
                sb.append("• ")
                        .append(ar.getBranch())
                        .append(": ")
                        .append(ar.getMessage())
                        .append("\n");
            }
            String alertsString = sb.toString();
            text = total + "\n" + alertsString;
            mailSer.sendMail(business.getOwnerEmail(), subject, text);
        }
    }

    // Handle Alert for each branch
    public List<AlertRes> handleAlerts() {
        List<BenchMarksRes> benchList = handleBenchMarks(); // avg branch
        List<PercentBranchAlertRes> perList = getListPerBranch(); // each branch
        List<Alert> alerts = rep.findAll(); // rule

        // List filter
        List<Alert> revenueRules = alerts.stream().filter(a -> a.getAlert().equals(AlertRule.LOW_REVENUE)).toList();
        List<Alert> cancellationRules = alerts.stream().filter(a -> a.getAlert().equals(AlertRule.HIGH_CANCEL)).toList();
        List<Alert> complaintRules = alerts.stream().filter(a -> a.getAlert().equals(AlertRule.HIGH_COMPLAINT)).toList();

        // Text
        String rText1 = "Doanh thu thấp hơn ";
        String rText2 = "% so với trung bình các chi nhánh.";

        String cText1 = "Tỷ lệ hủy đơn từ ";
        String cText2 = "% trở lên và cao hơn đáng kể so với các chi nhánh khác.";
        String cText3 = "% trở lên.";

        String cpText1 = "Số lượng khiếu nại từ ";
        String cpText2 = " trở lên và cao hơn mức trung bình.";

        List<AlertRes> res = new ArrayList<>();
        for (PercentBranchAlertRes per : perList) {
            BenchMarksRes benchMarksRes = benchList.stream().filter(b -> b.getBusinessId().equals(per.getBusinessId())).findFirst().orElseThrow(() -> new RuntimeException("Business id not found"));
            // Revenue
            AlertRes r = null;
            for (Alert revenueRule : revenueRules) {
                if (per.getRevenuePer() <= -(revenueRule.getThreshold())) {
                    r = handleAddList(revenueRule, per, rText1, rText2, per.getRevenuePer());
                }
            }
            if (r != null) res.add(r);

            // Cancellation
            // Can xem lai thu tu uu tien => Order Index
            AlertRes c = null;
            for (Alert cancellationRule : cancellationRules) {
                if (AlertSeverity.HIGH.equals(cancellationRule.getSeverity())) {
                    if (per.getCancellationPer() >= cancellationRule.getThreshold() &&
                            per.getCancellationPer() >= benchMarksRes.getAverageCancellationRate() * 1.5) {
                        c = handleAddList(cancellationRule, per, cText1, cText2, per.getCancellationPer());
                    }
                } else if (per.getCancellationPer() >= cancellationRule.getThreshold()) {
                    c = handleAddList(cancellationRule, per, cText1, cText3, per.getCancellationPer());
                }
            }
            if (c != null) res.add(c);

            // Complaint
            AlertRes cp = null;
            for (Alert complaintRule : complaintRules) {
                if (per.getComplaint() >= complaintRule.getThreshold() &&
                        per.getComplaint() > benchMarksRes.getAverageComplaints()) {
                    cp = handleAddList(complaintRule, per, cpText1, cpText2, per.getComplaint());
                }
            }
            if (cp != null) res.add(cp);
        }
        return res;
    }

    // Handle list
    private AlertRes handleAddList(Alert alert, PercentBranchAlertRes per, String text1, String text2, double value) {
        AlertRes r = new AlertRes();
        r.setBranch(per.getBranch());
        r.setBusinessId(per.getBusinessId());
        r.setType(alert.getAlert().toString());
        r.setSeverity(alert.getSeverity().toString());
        r.setValue(roundToTwoDecimals(value));
        r.setMessage(text1 + alert.getThreshold() + text2);
        return r;
    }

    // Get List Per Branch
    public List<PercentBranchAlertRes> getListPerBranch() {
        List<DailyDataRes> dataToday = dailySer.getDailyToday(); // List each branch today
        List<BenchMarksRes> benchMarks = handleBenchMarks(); // avg
        List<PercentBranchAlertRes> res = new ArrayList<>();
        for (DailyDataRes d : dataToday) {
            PercentBranchAlertRes p = new PercentBranchAlertRes();
            BenchMarksRes benchMarksRes = benchMarks.stream().filter(b -> b.getBusinessId().equals(d.getBusinessId())).findFirst().orElseThrow(() -> new RuntimeException("BenchMarks not found"));
            double r1 = d.getRevenue() - benchMarksRes.getAverageRevenue();
            double r2 = r1 / benchMarksRes.getAverageRevenue();
            double revenueVsAveragePercent = r2 * 100;

            double c1 = (double) d.getCancelled() / d.getOrders();
            double cancellationRate = c1 * 100;

            // Set
            p.setBusinessId(d.getBusinessId());
            p.setBranch(d.getBranch());
            p.setRevenuePer(revenueVsAveragePercent);
            p.setCancellationPer(cancellationRate);
            p.setComplaint(d.getComplaints());
            res.add(p);
        }
        return res;
    }

    // Handle bench marks
    public List<BenchMarksRes> handleBenchMarks() {
        List<DailyDataTodayRes> summary = dailySer.calculateSummaryByBusiness();
        List<BenchMarksRes> res = new ArrayList<>();
        for (DailyDataTodayRes d : summary) {
            BenchMarksRes b = new BenchMarksRes();
            int totalBranch = countBranchForBusiness(d.getBusinessId());
            double avgRevenue = (double) d.getTotalRevenue() / totalBranch;
            double avgCancellationRate = (double) d.getTotalCancelled() / totalBranch;
            double avgComplaints = (double) d.getTotalComplaints() / totalBranch;

            // Set
            b.setBusinessId(d.getBusinessId());
            b.setAverageRevenue(avgRevenue);
            b.setAverageCancellationRate(avgCancellationRate);
            b.setAverageComplaints(avgComplaints);
            res.add(b);
        }
        return res;
    }

    // Total list each branch for business
    private int countBranchForBusiness(String businessId) {
        int count = 0;
        List<DailyDataRes> data = dailySer.getDailyToday();
        for (DailyDataRes d : data) {
            if (d.getBusinessId().equals(businessId)) count++;
        }
        return count;
    }

    // Round
    private double roundToTwoDecimals(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    //    public List<AlertRes> handleAlerts() {
//        List<BenchMarksRes> benchList = handleBenchMarks(); // avg branch
//        List<PercentBranchAlertRes> perList = getListPerBranch(); // each branch
//        List<Alert> alerts = rep.findAll(); // rule
//
//        List<AlertRes> res = new ArrayList<>();
//        for (PercentBranchAlertRes per : perList) {
//            BenchMarksRes benchMarksRes = benchList.stream().filter(b -> b.getBusinessId().equals(per.getBusinessId())).findFirst().orElseThrow(() -> new RuntimeException("Business id not found"));
//            // Revenue
//            AlertRes r = new AlertRes();
//            List<Alert> revenueRules = alerts.stream().filter(a -> a.getAlert().equals(AlertRule.LOW_REVENUE)).toList();
//            for (Alert revenueRule : revenueRules) {
//                r.setBranch(per.getBranch());
//                r.setBusinessId(per.getBusinessId());
//
//                if (per.getRevenuePer() <= -(revenueRule.getThreshold())) {
//                    r.setType(revenueRule.getAlert().toString());
//                    r.setSeverity(revenueRule.getSeverity().toString());
//                    r.setValue(roundToTwoDecimals(per.getRevenuePer())); // Round
//                    r.setMessage("Doanh thu thấp hơn " + revenueRule.getThreshold() + "% so với trung bình các chi nhánh.");
//                }
//            }
//            if (r.getMessage() != null && !r.getMessage().isEmpty()) res.add(r);
//
//            // Cancellation
//            AlertRes c = new AlertRes();
//            List<Alert> cancellationRules = alerts.stream().filter(a -> a.getAlert().equals(AlertRule.HIGH_CANCEL)).toList();
//            for (Alert cancellationRule : cancellationRules) {
//                c.setBranch(per.getBranch());
//                c.setBusinessId(per.getBusinessId());
//
//                if (cancellationRule.getSeverity().equals(AlertSeverity.HIGH)) {
//                    if (per.getCancellationPer() >= cancellationRule.getThreshold() &&
//                            per.getCancellationPer() >= benchMarksRes.getAverageCancellationRate() * 1.5) {
//                        c.setType(cancellationRule.getAlert().toString());
//                        c.setSeverity(cancellationRule.getSeverity().toString());
//                        c.setValue(roundToTwoDecimals(per.getCancellationPer())); // Round
//                        c.setMessage("Tỷ lệ hủy đơn từ " + cancellationRule.getThreshold() + "% trở lên và cao hơn đáng kể so với các chi nhánh khác.");
//                    }
//                } else if (per.getCancellationPer() >= cancellationRule.getThreshold()) {
//                    c.setType(cancellationRule.getAlert().toString());
//                    c.setSeverity(cancellationRule.getSeverity().toString());
//                    c.setValue(roundToTwoDecimals(per.getCancellationPer())); // Round
//                    c.setMessage("Tỷ lệ hủy đơn từ " + cancellationRule.getThreshold() + "% trở lên.");
//                }
//            }
//            if (c.getMessage() != null && !c.getMessage().isEmpty()) res.add(c);
//
//            // Complaint
//            AlertRes cp = new AlertRes();
//            List<Alert> complaintRules = alerts.stream().filter(a -> a.getAlert().equals(AlertRule.HIGH_COMPLAINT)).toList();
//            for (Alert complaintRule : complaintRules) {
//                cp.setBranch(per.getBranch());
//                cp.setBusinessId(per.getBusinessId());
//
//                if (per.getComplaint() >= complaintRule.getThreshold() &&
//                    per.getComplaint() > benchMarksRes.getAverageComplaints()) {
//                    cp.setType(complaintRule.getAlert().toString());
//                    cp.setSeverity(complaintRule.getSeverity().toString());
//                    cp.setValue(per.getComplaint());
//                    cp.setMessage("Số lượng khiếu nại từ " + complaintRule.getThreshold() + " trở lên và cao hơn mức trung bình.");
//                }
//            }
//            if (cp.getMessage() != null && !cp.getMessage().isEmpty()) res.add(cp);
//        }
//        return res;
//    }
}