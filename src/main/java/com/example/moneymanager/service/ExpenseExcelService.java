package com.example.moneymanager.service;

import com.example.moneymanager.entity.ExpenseEntity;
import com.example.moneymanager.entity.ProfileEntity;
import com.example.moneymanager.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseExcelService {

    private final ExpenseRepository expenseRepository;

    public byte[] generate(ProfileEntity profile) {

        List<ExpenseEntity> expenses =
                expenseRepository.findByProfileId(profile.getId());

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Expenses");

            CellStyle headerStyle = createHeaderStyle(workbook);

            Row headerRow = sheet.createRow(0);
            String[] columns = {
                    "ID", "Title", "Category", "Amount", "Date", "Created At"
            };

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

            for (ExpenseEntity expense : expenses) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(expense.getId());
                row.createCell(1).setCellValue(expense.getName());
                row.createCell(2).setCellValue(
                        expense.getCategory() != null
                                ? expense.getCategory().getName()
                                : ""
                );
                row.createCell(3).setCellValue(expense.getAmount().doubleValue());
                row.createCell(4).setCellValue(expense.getDate().format(formatter));
                row.createCell(5).setCellValue(
                        expense.getCreatedAt() != null
                                ? expense.getCreatedAt().toString()
                                : ""
                );
            }

            autoSize(sheet, columns.length);

            workbook.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate expense Excel", e);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private void autoSize(Sheet sheet, int columns) {
        for (int i = 0; i < columns; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}
