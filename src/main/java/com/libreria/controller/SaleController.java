package com.libreria.controller;

import com.libreria.dto.SaleRequest;
import com.libreria.model.CabeceraVenta;
import com.libreria.model.DetalleVenta;
import com.libreria.service.ReportService; // Added ReportService import
import com.libreria.service.SaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders; // Added HttpHeaders
import org.springframework.http.MediaType; // Added MediaType
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream; // Added ByteArrayInputStream
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/ventas")
public class SaleController {

    @Autowired
    private SaleService saleService;

    @Autowired
    private ReportService reportService; // Inject ReportService

    @PostMapping("/emitir-boleta")
    public ResponseEntity<CabeceraVenta> emitirBoleta(@RequestBody SaleRequest request) {
        CabeceraVenta nuevaVenta = saleService.emitirBoleta(request);
        return ResponseEntity.ok(nuevaVenta);
    }

    @GetMapping
    public ResponseEntity<Page<CabeceraVenta>> getVentas(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {

        // Adjust for 1-based indexing
        System.out.println("Request getVentas - Page: " + page + ", Size: " + size);
        int pageNo = (page < 1) ? 0 : page - 1;
        Pageable pageable = PageRequest.of(pageNo, size, Sort.by("fechaHora").descending());

        Page<CabeceraVenta> ventas = saleService.getVentas(fechaInicio, fechaFin, pageable);
        System.out.println("Response getVentas - Page: " + ventas.getNumber() +
                ", TotalPages: " + ventas.getTotalPages() +
                ", TotalElements: " + ventas.getTotalElements() +
                ", ContentSize: " + ventas.getContent().size());
        return ResponseEntity.ok(ventas);
    }

    @GetMapping("/{id}/detalles")
    public List<DetalleVenta> getDetalles(@PathVariable Long id) {
        return saleService.getDetalles(id);
    }

    @GetMapping("/{id}/boleta")
    public ResponseEntity<InputStreamResource> generarBoleta(@PathVariable Long id) {
        ByteArrayInputStream bis = reportService.generateBoletaPdf(id);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=boleta_" + id + ".pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }

    @GetMapping("/reporte-excel")
    public ResponseEntity<InputStreamResource> generarReporteExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {

        ByteArrayInputStream bis = reportService.generateSalesExcel(fechaInicio, fechaFin);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=reporte_ventas.xlsx");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(
                        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(bis));
    }
}
