package com.example.moneymanager.controller;

import com.example.moneymanager.entity.ProfileEntity;
import com.example.moneymanager.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
public class EmailController {

    private final ExcelService excelService;
    private final IncomeService incomeService;
    private final ExpenseService expenseService;
    private final BrevoApiEmailService emailService;
    private final ProfileService profileService;

//    @GetMapping("/income-excel")
//    public ResponseEntity<Void> emailIncomeExcel() throws IOException {
//        ProfileEntity profile = profileService.getCurrentProfile();
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        excelService.writeIncomesToExcel(baos, incomeService.getCurrentMonthIncomesForCurrentUser());
//        emailService.sendEmail
//    }
@GetMapping("/income-excel")
public ResponseEntity<String> emailIncomeExcel() throws IOException {
    ProfileEntity profile = profileService.getCurrentProfile();

    // Generate Excel file
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    excelService.writeIncomesToExcel(baos, incomeService.getCurrentMonthIncomesForCurrentUser());
    byte[] excelBytes = baos.toByteArray();

    // Generate filename with current date
    String filename = "Income_Report_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".xlsx";

    // Email content
    String subject = "Your Income Report - Money Manager";
    String htmlBody = String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #4CAF50;">Income Report</h2>
                    <p>Hi %s,</p>
                    <p>Please find attached your complete income report from Money Manager.</p>
                    <p>The report includes all your income transactions with details like:</p>
                    <ul>
                        <li>Income name</li>
                        <li>Amount</li>
                        <li>Category</li>
                        <li>Date</li>
                    </ul>
                    <p style="margin-top: 30px;">Best regards,<br/>
                    <strong>Money Manager Team</strong></p>
                    <hr style="border: 1px solid #eee; margin: 30px 0;">
                    <p style="font-size: 12px; color: #999;">
                        This is an automated email from Money Manager. Please do not reply to this email.
                    </p>
                </div>
            </body>
            </html>
            """, profile.getFullName());

    // Send email with attachment
    emailService.sendEmailWithAttachment(
            profile.getEmail(),
            subject,
            htmlBody,
            filename,
            excelBytes
    );

    return ResponseEntity.ok("Income report sent successfully to " + profile.getEmail());
}

    @GetMapping("/expense-excel")
    public ResponseEntity<String> emailExpenseExcel() throws IOException {
        ProfileEntity profile = profileService.getCurrentProfile();

        // Generate Excel file
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        excelService.writeExpenseToExcel(baos, expenseService.getCurrentMonthExpensesForCurrentUser());
        byte[] excelBytes = baos.toByteArray();

        // Generate filename with current date
        String filename = "Expense_Report_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".xlsx";

        // Email content
        String subject = "Your Expense Report - Money Manager";
        String htmlBody = String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #f44336;">Expense Report</h2>
                    <p>Hi %s,</p>
                    <p>Please find attached your complete expense report from Money Manager.</p>
                    <p>The report includes all your expense transactions with details like:</p>
                    <ul>
                        <li>Expense name</li>
                        <li>Amount</li>
                        <li>Category</li>
                        <li>Date</li>
                    </ul>
                    <p style="margin-top: 30px;">Best regards,<br/>
                    <strong>Money Manager Team</strong></p>
                    <hr style="border: 1px solid #eee; margin: 30px 0;">
                    <p style="font-size: 12px; color: #999;">
                        This is an automated email from Money Manager. Please do not reply to this email.
                    </p>
                </div>
            </body>
            </html>
            """, profile.getFullName());

        // Send email with attachment
        emailService.sendEmailWithAttachment(
                profile.getEmail(),
                subject,
                htmlBody,
                filename,
                excelBytes
        );

        return ResponseEntity.ok("Expense report sent successfully to " + profile.getEmail());
    }

}
