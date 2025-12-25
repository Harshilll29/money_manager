package com.example.moneymanager.service;

import com.example.moneymanager.entity.IncomeEntity;
import com.example.moneymanager.entity.ProfileEntity;
import com.example.moneymanager.repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncomeExcelService {

    private final IncomeRepository incomeRepository;

    public byte[] generate(ProfileEntity profile) {

        List<IncomeEntity> incomes =
                incomeRepository.findByProfileId(profile.getId());

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Incomes");

            CellStyle headerStyle = createHeaderStyle(workbook);

            Row headerRow = sheet.createRow(0);
            String[] columns = {
                    "ID", "Name", "Category", "Amount", "Date", "Created At"
            };

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

            for (IncomeEntity income : incomes) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(income.getId());
                row.createCell(1).setCellValue(income.getName());
                row.createCell(2).setCellValue(
                        income.getCategory() != null
                                ? income.getCategory().getName()
                                : ""
                );
                row.createCell(3).setCellValue(income.getAmount().doubleValue());
                row.createCell(4).setCellValue(income.getDate().format(formatter));
                row.createCell(5).setCellValue(
                        income.getCreatedAt() != null
                                ? income.getCreatedAt().toString()
                                : ""
                );
            }

            autoSize(sheet, columns.length);

            workbook.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate income Excel", e);
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
