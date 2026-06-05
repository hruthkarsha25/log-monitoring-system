package com.project.logmonitoringsystem.controller;

import com.project.logmonitoringsystem.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/export")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;
    private static final Logger log = LoggerFactory.getLogger(ExportController.class);

    @Operation(
            summary = "Export logs as CSV"
    )
    @GetMapping("/csv")
    public ResponseEntity<byte[]> exportCsv() {
        log.info("API_LOG endpoint=/export/csv method=GET format=CSV");
        try {
            byte[] data = exportService.exportCsv();
            log.info("EXPORT_SUCCESS format=CSV filesize={} bytes", data.length);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=logs.csv")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(data);
        } catch (Exception e) {
            log.error("EXPORT_FAILED format=CSV exception={}", e.getMessage());
            throw e;
        }
    }

    @Operation(
            summary = "Export logs as Excel"
    )
    @GetMapping("/excel")
    public ResponseEntity<byte[]> exportExcel()
            throws IOException {
        log.info("API_LOG endpoint=/export/excel method=GET format=EXCEL");
        try {
            byte[] data =
                    exportService.exportExcel();
            log.info("EXPORT_SUCCESS format=EXCEL filesize={} bytes", data.length);
            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=logs.xlsx"
                    )
                    .contentType(
                            MediaType.APPLICATION_OCTET_STREAM
                    )
                    .body(data);
        } catch (Exception e) {
            log.error("EXPORT_FAILED format=EXCEL exception={}", e.getMessage());
            throw e;
        }
    }

}
