package com.project.logmonitoringsystem.service;

import com.project.logmonitoringsystem.model.LogEvent;
import com.project.logmonitoringsystem.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final LogRepository logRepository;

    public byte[] exportCsv() {

        List<LogEvent> logs = logRepository.findAll();

        StringBuilder csv = new StringBuilder();

        csv.append("ID,,Level,Service,Message,CreatedAt\n");

        for (LogEvent log : logs) {

            csv.append(log.getId()).append(",");
            csv.append(log.getLevel()).append(",");
            csv.append(log.getServiceName()).append(",");
            csv.append(log.getMessage()).append(",");
            csv.append(log.getCreatedAt()).append("\n");
        }

        return csv.toString().getBytes();

    }


    public byte[] exportExcel() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Logs");

        Row headerRow = sheet.createRow(0);

        headerRow.createCell(0).setCellValue("Id");
        headerRow.createCell(1).setCellValue("Level");
        headerRow.createCell(2).setCellValue("Service");
        headerRow.createCell(3).setCellValue("Message");
        headerRow.createCell(4).setCellValue("Created At");

        List<LogEvent> logs = logRepository.findAll();

        int rowNum = 1;

        for (LogEvent log : logs) {

            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(log.getId());
            row.createCell(1).setCellValue(log.getLevel().toString());
            row.createCell(2).setCellValue(log.getServiceName());
            row.createCell(3).setCellValue(log.getMessage());
            row.createCell(4).setCellValue(
                    log.getCreatedAt().toString()
            );
        }

        for (int i = 0; i < 5; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        workbook.write(outputStream);

        workbook.close();

        return outputStream.toByteArray();
    }
}
