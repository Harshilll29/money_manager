package com.example.moneymanager.controller;

import com.example.moneymanager.entity.ProfileEntity;
import com.example.moneymanager.service.ExpenseExcelService;
import com.example.moneymanager.service.IncomeExcelService;
import com.example.moneymanager.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ExcelController {


    private final IncomeExcelService incomeExcelService;
    private final ExpenseExcelService expenseExcelService;
    private final ProfileService profileService;

    @GetMapping("/excel/download/income")
    public ResponseEntity<byte[]> downloadIncomeExcel() {
        ProfileEntity profile = profileService.getCurrentProfile();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=income-report.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(incomeExcelService.generate(profile));
    }

    @GetMapping("/excel/download/expense")
    public ResponseEntity<byte[]> downloadExpenseExcel() {
        ProfileEntity profile = profileService.getCurrentProfile();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=expense-report.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(expenseExcelService.generate(profile));
    }
}
