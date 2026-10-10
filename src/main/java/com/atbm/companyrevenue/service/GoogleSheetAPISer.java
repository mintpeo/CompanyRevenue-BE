package com.atbm.companyrevenue.service;

import com.atbm.companyrevenue.model.*;
import com.atbm.companyrevenue.repository.AlertRep;
import com.atbm.companyrevenue.repository.BusinessRep;
import com.atbm.companyrevenue.repository.DailyRep;
import com.atbm.companyrevenue.repository.ReportRep;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoogleSheetAPISer {
    private static final String APPLICATION_NAME = "Spring Boot Sheet Reader";
    private final DailyRep dailyRep;
    private final BusinessRep businessRep;
    private final AlertRep alertRep;
    private final ReportRep reportRep;

    // Read sheet report and save to data
    public int sheetToReport(String url, String range) {
        List<List<Object>> sheetRows = readSheetData(url, range);
        if  (sheetRows == null || sheetRows.isEmpty()) return 0;

        Map<String, Business> mapBusiness = mapBusinesses();
        List<Report> reportList = new ArrayList<>();
        for (int i = 1; i < sheetRows.size(); i++) { // Skip row 0 => data column table
            List<Object> row = sheetRows.get(i);
            if (row == null || row.isEmpty()) continue;

            String businessId = getStringValue(row, 0);
            if (businessId.isBlank()) continue;
            Business business = mapBusiness.get(businessId);

            String getDailyReport = getStringValue(row, 1);
            boolean dailyReport = getDailyReport.equals("TRUE");
            String getReportTime = getStringValue(row, 2);
            LocalTime reportTime = LocalTime.parse(getReportTime);
            String getIncludeRevenue = getStringValue(row, 3);
            boolean includeRevenue = getIncludeRevenue.equals("TRUE");
            String getIncludeOrders = getStringValue(row, 4);
            boolean includeOrders = getIncludeOrders.equals("TRUE");
            String getIncludeCustomers = getStringValue(row, 5);
            boolean includeCustomers = getIncludeCustomers.equals("TRUE");
            String getIncludeExpense = getStringValue(row, 6);
            boolean includeExpense = getIncludeExpense.equals("TRUE");
            String getIncludeAlerts = getStringValue(row, 7);
            boolean includeAlerts = getIncludeAlerts.equals("TRUE");

            Report b = new Report(business, dailyReport, reportTime, includeRevenue,
                    includeOrders, includeCustomers, includeExpense, includeAlerts);
            reportList.add(b);
        }
        reportRep.saveAll(reportList);
        return reportList.size();
    }

    // Read sheet alert and save to data
    public int sheetToAlert(String url, String range) {
        List<List<Object>> sheetRows = readSheetData(url, range);
        if  (sheetRows == null || sheetRows.isEmpty()) return 0;

        Map<String, Business> mapBusiness = mapBusinesses();
        List<Alert> alertList = new ArrayList<>();
        for (int i = 1; i < sheetRows.size(); i++) { // Skip row 0 => data column table
            List<Object> row = sheetRows.get(i);
            if (row == null || row.isEmpty()) continue;

            String businessId = getStringValue(row, 0);
            if (businessId.isBlank()) continue;
            Business business = mapBusiness.get(businessId);

            String getRule = getStringValue(row, 1);
            AlertRule alert = AlertRule.valueOf(getRule);

            Integer threshold = parseInteger(getStringValue(row, 2));

            String getSeverity = getStringValue(row, 3);
            AlertSeverity severity = AlertSeverity.valueOf(getSeverity);

            String getEnabled = getStringValue(row, 4);
            boolean enabled = getEnabled.equals("TRUE");

            Alert b = new Alert(business, alert, severity, threshold, enabled);
            alertList.add(b);
        }
        alertRep.saveAll(alertList);
        return alertList.size();
    }

    // Read Sheet Daily and Save to Data
    public int sheetToDatabase(String url, String range) {
        List<List<Object>> sheetRows = readSheetData(url, range);
        if  (sheetRows == null || sheetRows.isEmpty()) return 0;

        Map<String, Business> mapBusiness = mapBusinesses();
        List<Daily> dailyList = new ArrayList<>();
        for (int i = 1; i < sheetRows.size(); i++) { // Skip row 0 => data column table
            List<Object> row = sheetRows.get(i);
            if (row == null || row.isEmpty()) continue;

            String getBusinessId = getStringValue(row, 0);
            if (getBusinessId.isBlank()) continue;

            Business business = mapBusiness.get(getBusinessId);
            LocalDate date = parseDate(getStringValue(row, 1));
            String branch = getStringValue(row, 2);
            Integer revenue = parseInteger(getStringValue(row, 3));
            Integer order = parseInteger(getStringValue(row, 4));
            Integer customer = parseInteger(getStringValue(row, 5));
            Integer expense = parseInteger(getStringValue(row, 6));
            Integer staff = parseInteger(getStringValue(row, 7));
            Integer complaint = parseInteger(getStringValue(row, 8));
            Integer cancelled = parseInteger(getStringValue(row, 9));

            Daily daily = new Daily(business, date, branch,
                    revenue, order, customer, expense, staff, complaint, cancelled);
            dailyList.add(daily);
        }
        dailyRep.saveAll(dailyList);
        return dailyList.size();
    }

    // Read sheet business and save to data
    public int sheetToBusiness(String url, String range) {
        List<List<Object>> sheetRows = readSheetData(url, range);
        if  (sheetRows == null || sheetRows.isEmpty()) return 0;

        List<Business> businessList = new ArrayList<>();
        for (int i = 1; i < sheetRows.size(); i++) { // Skip row 0 => data column table
            List<Object> row = sheetRows.get(i);
            if (row == null || row.isEmpty()) continue;

            String businessId = getStringValue(row, 0);
            if (businessId.isBlank()) continue;

            String businessName = getStringValue(row, 1);
            String industry = getStringValue(row, 2);
            String ownerEmail = getStringValue(row, 3);
            // Report Time
            String getReportTime = getStringValue(row, 4);
            LocalTime reportTime = LocalTime.parse(getReportTime);

            String timezone = getStringValue(row, 5);
            String getStatus = getStringValue(row, 6);
            boolean status = getStatus.equals("active");

            Business b = new Business(businessId, businessName, industry,
                    ownerEmail, reportTime, timezone, status);
            businessList.add(b);
        }
        businessRep.saveAll(businessList);
        return businessList.size();
    }
    private Map<String, Business> mapBusinesses() {
        List<Business> businessList = businessRep.findAll();
        return businessList.stream()
                .collect(Collectors.toMap(
                        Business::getBusinessId,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));
    }

    private String getStringValue(List<Object> row, int index) {
        if (index < row.size() && row.get(index) != null) return String.valueOf(row.get(index)).trim();
        return "";
    }

    private Integer parseInteger(String value) {
        if (value.isBlank()) return 0;
        try {
            String cleanValue = value.replaceAll("[^0-9-]", "");
            return cleanValue.isEmpty() ? 0 : Integer.parseInt(cleanValue);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private LocalDate parseDate(String value) {
        if (value.isBlank()) return null;
        try {
            return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value.isBlank()) {
            return BigDecimal.ZERO;
        }
        try {
            String cleanValue = value.replace(",", "").replace(".", "");
            return new BigDecimal(cleanValue);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    // Khởi tạo Sheets client từ Service Account
    private Sheets getSheetsClient() throws GeneralSecurityException, IOException {
        GoogleCredentials credentials = GoogleCredentials.fromStream(
                new ClassPathResource("credentials.json").getInputStream()
        ).createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS_READONLY));

        return new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials)
        ).setApplicationName(APPLICATION_NAME).build();
    }

    // Trích xuất spreadsheetId từ link Google Sheets
    public String extractSpreadsheetId(String url) {
        Pattern pattern = Pattern.compile("/d/([a-zA-Z0-9-_]+)");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException("Đường dẫn Google Sheet không hợp lệ!");
    }

    // Đọc dữ liệu theo phạm vi (range) ví dụ: "Sheet1!A1:Z"
    public List<List<Object>> readSheetData(String sheetUrl, String range) {
        try {
            String spreadsheetId = extractSpreadsheetId(sheetUrl);
            Sheets service = getSheetsClient();

            // Gọi API lấy dữ liệu theo khoảng
            ValueRange response = service.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();

            return response.getValues();
        } catch (Exception e) {
            throw new RuntimeException("Không thể đọc dữ liệu từ Google Sheets API: " + e.getMessage(), e);
        }
    }
}