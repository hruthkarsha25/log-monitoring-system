package com.project.logmonitoringsystem.controller;

import com.project.logmonitoringsystem.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
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

    @Operation(
            summary = "Export logs as CSV"
    )
    @GetMapping("/csv")
    public ResponseEntity<byte[]> exportCsv() {

        byte[] data = exportService.exportCsv();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=logs.csv")
                .contentType(MediaType.TEXT_PLAIN)
                .body(data);
    }

    @Operation(
            summary = "Export logs as Excel"
    )
    @GetMapping("/excel")
    public ResponseEntity<byte[]> exportExcel()
            throws IOException {

        byte[] data =
                exportService.exportExcel();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=logs.xlsx"
                )
                .contentType(
                        MediaType.APPLICATION_OCTET_STREAM
                )
                .body(data);
    }

}
