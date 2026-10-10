package com.atbm.companyrevenue.service;

import com.atbm.companyrevenue.dto.response.DailyDataRes;
import com.atbm.companyrevenue.dto.response.DailyDataTodayRes;
import com.atbm.companyrevenue.model.Daily;
import com.atbm.companyrevenue.repository.DailyRep;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DailySer {
    private final DailyRep rep;

    public List<DailyDataTodayRes> calculateSummaryByBusiness() {
        List<DailyDataRes> dailyList = getDailyToday();
        Map<String, List<DailyDataRes>> groupedByBusiness = dailyList.stream()
                .collect(Collectors.groupingBy(DailyDataRes::getBusinessId));

        return groupedByBusiness.entrySet().stream()
                .map(entry -> {
                    String businessId = entry.getKey();
                    List<DailyDataRes> list = entry.getValue();

                    Integer totalRevenue = list.stream().mapToInt(d -> d.getRevenue() != null ? d.getRevenue() : 0).sum();
                    Integer totalExpense = list.stream().mapToInt(d -> d.getExpense() != null ? d.getExpense() : 0).sum();
                    Integer totalOrders = list.stream().mapToInt(d -> d.getOrders() != null ? d.getOrders() : 0).sum();
                    Integer totalCustomers = list.stream().mapToInt(d -> d.getCustomer() != null ? d.getCustomer() : 0).sum();
                    Integer totalCancelled = list.stream().mapToInt(d -> d.getCancelled() != null ? d.getCancelled() : 0).sum();
                    Integer totalComplaints = list.stream().mapToInt(d -> d.getComplaints() != null ? d.getComplaints() : 0).sum();
                    Integer totalStaff = list.stream().mapToInt(d -> d.getStaff() != null ? d.getStaff() : 0).sum();

                    return new DailyDataTodayRes(businessId, totalRevenue, totalOrders,
                            totalCustomers, totalExpense, totalStaff,
                            totalComplaints, totalCancelled, list.get(0).getDate()
                    );
                })
                .collect(Collectors.toList());
    }

    // Get data daily today
    public List<DailyDataRes> getDailyToday() {
//        LocalDate today = LocalDate.now();
        LocalDate today = LocalDate.of(2026, 10, 4);
        List<Daily> list = rep.findAll();
        List<Daily> d = list.stream().filter(dai -> dai.getDate().equals(today)).toList();
        List<DailyDataRes> res = new ArrayList<>();
        for (Daily daily : d) {
            DailyDataRes dailyDataRes = new DailyDataRes();
            dailyDataRes.setId(daily.getId());
            dailyDataRes.setBusinessId(daily.getBusiness().getBusinessId());
            dailyDataRes.setDate(daily.getDate());
            dailyDataRes.setBranch(daily.getBranch());
            dailyDataRes.setRevenue(daily.getRevenue());
            dailyDataRes.setOrders(daily.getOrders());
            dailyDataRes.setCustomer(daily.getCustomer());
            dailyDataRes.setExpense(daily.getExpense());
            dailyDataRes.setStaff(daily.getStaff());
            dailyDataRes.setComplaints(daily.getComplaint());
            dailyDataRes.setCancelled(daily.getCancelled());
            res.add(dailyDataRes);
        }
        return res;
    }
}
