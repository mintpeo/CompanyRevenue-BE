package com.atbm.companyrevenue.controller;

import com.atbm.companyrevenue.service.GoogleSheetAPISer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sheets")
@RequiredArgsConstructor
public class GoogleSheetAPICon {
    private final GoogleSheetAPISer googleSheetAPISer;

    @PostMapping("/sync-report")
    public ResponseEntity<Map<String, Object>> syncSheetReport(
            @RequestParam("url") String url,
            @RequestParam(value = "range", defaultValue = "Sheet1") String range) {

        int savedCount = googleSheetAPISer.sheetToReport(url, range);

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Đã lưu dữ liệu từ Google Sheet vào MySQL thành công!",
                "totalRowsSaved", savedCount
        ));
    }

    @PostMapping("/sync-alert")
    public ResponseEntity<Map<String, Object>> syncSheetAlert(
            @RequestParam("url") String url,
            @RequestParam(value = "range", defaultValue = "Sheet1") String range) {

        int savedCount = googleSheetAPISer.sheetToAlert(url, range);

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Đã lưu dữ liệu từ Google Sheet vào MySQL thành công!",
                "totalRowsSaved", savedCount
        ));
    }

    @PostMapping("/sync-business")
    public ResponseEntity<Map<String, Object>> syncSheetBusiness(
            @RequestParam("url") String url,
            @RequestParam(value = "range", defaultValue = "Sheet1") String range) {

        int savedCount = googleSheetAPISer.sheetToBusiness(url, range);

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Đã lưu dữ liệu từ Google Sheet vào MySQL thành công!",
                "totalRowsSaved", savedCount
        ));
    }

    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> syncSheetData(
            @RequestParam("url") String url,
            @RequestParam(value = "range", defaultValue = "Sheet1") String range) {

        int savedCount = googleSheetAPISer.sheetToDatabase(url, range);

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Đã lưu dữ liệu từ Google Sheet vào MySQL thành công!",
                "totalRowsSaved", savedCount
        ));
    }

    @GetMapping("/read")
    public ResponseEntity<List<List<Object>>> readSheetData(
            @RequestParam("url") String url,
            @RequestParam(value = "range", defaultValue = "Sheet1!A1:Z100") String range) {

        List<List<Object>> sheetData = googleSheetAPISer.readSheetData(url, range);
        return ResponseEntity.ok(sheetData);
    }
}
