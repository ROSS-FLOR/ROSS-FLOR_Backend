package com.libreria.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.libreria.model.CabeceraVenta;
import com.libreria.model.DetalleVenta;
import com.libreria.repository.CabeceraVentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private CabeceraVentaRepository cabeceraVentaRepository;

    // Existing Management Report (Date Range)
    // Existing Management Report (Date Range)
    public ByteArrayInputStream generateSalesReport(LocalDateTime start, LocalDateTime end) {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Reporte de Ventas General", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // Date Range Header
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            String dateRangeText;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            if (start != null && end != null) {
                dateRangeText = "Periodo del Reporte: " + start.format(formatter) + " - " + end.format(formatter);
            } else {
                dateRangeText = "Periodo del Reporte: Histórico Completo";
            }

            Paragraph period = new Paragraph(dateRangeText, subtitleFont);
            period.setAlignment(Element.ALIGN_CENTER);
            period.setSpacingAfter(10);
            document.add(period);

            document.add(new Paragraph(" "));

            // Fetch Data
            List<CabeceraVenta> ventas = cabeceraVentaRepository
                    .findByFechaHoraBetween(
                            start != null ? start : LocalDateTime.of(2000, 1, 1, 0, 0),
                            end != null ? end : LocalDateTime.now(),
                            org.springframework.data.domain.Pageable.unpaged())
                    .getContent();

            // Calculate Total
            double totalSales = ventas.stream().mapToDouble(CabeceraVenta::getTotalFinal).sum();

            // Total Summary (Top Right or just below Period)
            Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Paragraph totalSummary = new Paragraph(
                    "Venta Total en el Periodo: S/. " + String.format("%,.2f", totalSales), totalFont);
            totalSummary.setAlignment(Element.ALIGN_RIGHT);
            totalSummary.setSpacingAfter(20);
            document.add(totalSummary);

            // Table
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new int[] { 1, 3, 2, 4, 2 });
            addTableHeader(table, "ID");
            addTableHeader(table, "Fecha");
            addTableHeader(table, "Modo Pago");
            addTableHeader(table, "Items");
            addTableHeader(table, "Total");

            for (CabeceraVenta venta : ventas) {
                table.addCell(String.valueOf(venta.getId()));
                table.addCell(venta.getFechaHora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                table.addCell(venta.getModoPago());
                String itemsSummary = venta.getDetalles().stream()
                        .map(d -> d.getProducto().getNombre() + " x" + d.getCantidad())
                        .collect(Collectors.joining(", "));
                table.addCell(itemsSummary);
                table.addCell("S/. " + String.format("%,.2f", venta.getTotalFinal()));
            }
            document.add(table);
            document.close();
        } catch (DocumentException ex) {
            throw new RuntimeException("Error generating PDF", ex);
        }
        return new ByteArrayInputStream(out.toByteArray());
    }

    // NEW Single Boleta Report
    public ByteArrayInputStream generateBoletaPdf(Long id) {
        CabeceraVenta venta = cabeceraVentaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));

        // Format for a thermal-printer-like width (approx 80mm ~ 226 points)
        // new Rectangle(width, height) - height is arbitrary long, can be dynamic but
        // fixed is easier
        Rectangle pageSize = new Rectangle(226, 800);
        Document document = new Document(pageSize, 10, 10, 10, 10); // Custom margins
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Font Styles
            Font fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font fontSmall = FontFactory.getFont(FontFactory.HELVETICA, 8);

            // --- HEADER (Ross&Flor) ---
            Paragraph companyName = new Paragraph("ROSS & FLOR", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16));
            companyName.setAlignment(Element.ALIGN_CENTER);
            document.add(companyName);

            Paragraph companyInfo = new Paragraph(
                    "RUC: 20601234567\n" +
                            "Dirección: Av. Las Flores 123, Lima\n" +
                            "Telf: 987 654 321 | Correo: contacto@rossflor.com",
                    fontSmall);
            companyInfo.setAlignment(Element.ALIGN_CENTER);
            document.add(companyInfo);

            document.add(new Paragraph("------------------------------------------------------------------"));

            Paragraph boletaTitle = new Paragraph("BOLETA DE VENTA ELECTRÓNICA", fontBold);
            boletaTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(boletaTitle);

            Paragraph boletaId = new Paragraph("B001 - " + String.format("%08d", venta.getId()), fontBold);
            boletaId.setAlignment(Element.ALIGN_CENTER);
            document.add(boletaId);

            document.add(new Paragraph(" ")); // Spacer

            // --- CLIENT INFO (Placeholder) ---
            Paragraph clientInfo = new Paragraph(
                    "CLIENTE: CLIENTE GENERICO\n" +
                            "DNI: 00000000\n" +
                            "FECHA: " + venta.getFechaHora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
                    fontNormal);
            document.add(clientInfo);

            document.add(new Paragraph(" "));

            // --- ITEMS TABLE ---
            PdfPTable table = new PdfPTable(4); // Cant, Desc, P.Unit, Total
            table.setWidthPercentage(100);
            table.setWidths(new float[] { 1, 4, 2, 2 });

            // Table Headers
            PdfPCell c1 = new PdfPCell(new Phrase("CANT", fontBold));
            c1.setBorder(Rectangle.BOTTOM);
            table.addCell(c1);
            PdfPCell c2 = new PdfPCell(new Phrase("DESCRIPCION", fontBold));
            c2.setBorder(Rectangle.BOTTOM);
            table.addCell(c2);
            PdfPCell c3 = new PdfPCell(new Phrase("P.UNIT", fontBold));
            c3.setBorder(Rectangle.BOTTOM);
            table.addCell(c3);
            PdfPCell c4 = new PdfPCell(new Phrase("TOTAL", fontBold));
            c4.setBorder(Rectangle.BOTTOM);
            table.addCell(c4);

            // Table Body
            for (DetalleVenta det : venta.getDetalles()) {
                PdfPCell cell;

                cell = new PdfPCell(new Phrase(String.valueOf(det.getCantidad()), fontNormal));
                cell.setBorder(Rectangle.NO_BORDER);
                table.addCell(cell);
                cell = new PdfPCell(new Phrase(det.getProducto().getNombre(), fontNormal));
                cell.setBorder(Rectangle.NO_BORDER);
                table.addCell(cell);
                cell = new PdfPCell(new Phrase(String.format("%.2f", det.getPrecioUnitario()), fontNormal));
                cell.setBorder(Rectangle.NO_BORDER);
                table.addCell(cell);
                cell = new PdfPCell(new Phrase(String.format("%.2f", det.getSubtotal()), fontNormal));
                cell.setBorder(Rectangle.NO_BORDER);
                table.addCell(cell);
            }
            document.add(table);

            document.add(new Paragraph("------------------------------------------------------------------"));

            // --- TOTALS ---
            PdfPTable totalsTable = new PdfPTable(2);
            totalsTable.setWidthPercentage(40);
            totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

            totalsTable.addCell(new PdfPCell(new Phrase("OP. GRAVADA:", fontNormal)));
            totalsTable.addCell(
                    new PdfPCell(new Phrase("S/ " + String.format("%.2f", venta.getTotalFinal() / 1.18), fontNormal)));

            totalsTable.addCell(new PdfPCell(new Phrase("I.G.V. (18%):", fontNormal)));
            totalsTable.addCell(new PdfPCell(
                    new Phrase("S/ " + String.format("%.2f", venta.getTotalFinal() - (venta.getTotalFinal() / 1.18)),
                            fontNormal)));

            PdfPCell totalLabel = new PdfPCell(new Phrase("TOTAL A PAGAR:", fontBold));
            totalLabel.setBorder(Rectangle.TOP);
            totalsTable.addCell(totalLabel);

            PdfPCell totalValue = new PdfPCell(
                    new Phrase("S/ " + String.format("%.2f", venta.getTotalFinal()), fontBold));
            totalValue.setBorder(Rectangle.TOP);
            totalsTable.addCell(totalValue);

            document.add(totalsTable);

            // Footer
            document.add(new Paragraph(" "));
            Paragraph footer = new Paragraph("GRACIAS POR SU COMPRA\nForma de Pago: " + venta.getModoPago(), fontSmall);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
        } catch (DocumentException ex) {
            throw new RuntimeException("Error generating Boleta PDF", ex);
        }
        return new ByteArrayInputStream(out.toByteArray());
    }

    private void addTableHeader(PdfPTable table, String headerTitle) {
        PdfPCell header = new PdfPCell();
        header.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
        header.setPhrase(new Phrase(headerTitle));
        table.addCell(header);
    }

    public ByteArrayInputStream generateSalesExcel(LocalDateTime start, LocalDateTime end) {
        try (org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Ventas");

            // Header Style
            org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Data Style (Date and Currency)
            org.apache.poi.ss.usermodel.CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(workbook.createDataFormat().getFormat("dd/MM/yyyy HH:mm"));

            org.apache.poi.ss.usermodel.CellStyle currencyStyle = workbook.createCellStyle();
            currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("S/#,##0.00"));

            // Headers
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            String[] headers = { "ID", "Fecha", "Cliente", "Total", "Método de Pago" };
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data
            List<CabeceraVenta> ventas = cabeceraVentaRepository.findByFechaHoraBetween(
                    start != null ? start : LocalDateTime.of(2000, 1, 1, 0, 0),
                    end != null ? end : LocalDateTime.now(),
                    org.springframework.data.domain.Pageable.unpaged()).getContent();

            int rowIdx = 1;
            for (CabeceraVenta venta : ventas) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(venta.getId());

                org.apache.poi.ss.usermodel.Cell dateCell = row.createCell(1);
                dateCell.setCellValue(venta.getFechaHora());
                dateCell.setCellStyle(dateStyle);

                row.createCell(2).setCellValue("N/A"); // Cliente not available in entity

                org.apache.poi.ss.usermodel.Cell totalCell = row.createCell(3);
                totalCell.setCellValue(venta.getTotalFinal());
                totalCell.setCellStyle(currencyStyle);

                row.createCell(4).setCellValue(venta.getModoPago());
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Error generating Excel report", e);
        }
    }
}
