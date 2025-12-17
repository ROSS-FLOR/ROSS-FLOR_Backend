package com.libreria.service;

import com.libreria.dto.SaleItemDto;
import com.libreria.dto.SaleRequest;
import com.libreria.model.CabeceraVenta;
import com.libreria.model.DetalleVenta;
import com.libreria.model.Producto;
import com.libreria.repository.CabeceraVentaRepository;
import com.libreria.repository.DetalleVentaRepository;
import com.libreria.repository.ProductoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
public class SaleService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CabeceraVentaRepository cabeceraVentaRepository;

    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

    @Transactional
    public CabeceraVenta emitirBoleta(SaleRequest request) {
        // 1. Create Header
        CabeceraVenta cabecera = new CabeceraVenta();
        cabecera.setModoPago(request.getModoPago());

        double totalFinal = 0.0;

        // 2. Process Items
        for (SaleItemDto itemDto : request.getItems()) {
            Producto producto = productoRepository.findById(itemDto.getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + itemDto.getIdProducto()));

            if (producto.getStockActual() < itemDto.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre());
            }

            // 3. Update Stock - REMOVED (Stock is calculated by Formula)
            // productoRepository.save(producto); // No need to save product if only stock
            // changed

            // 4. Create Detail
            DetalleVenta detalle = new DetalleVenta();
            detalle.setProducto(producto);
            detalle.setCantidad(itemDto.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecioUnitario());
            double subtotal = producto.getPrecioUnitario() * itemDto.getCantidad();
            detalle.setSubtotal(subtotal);

            // ... (Existing calculation loop) ...
            cabecera.addDetalle(detalle);
            totalFinal += subtotal;
        }

        cabecera.setTotalFinal(totalFinal);

        // --- SUNAT QR & Hash Logic ---
        // 1. Generate QR String (Pipe separated)
        // Format:
        // RUC|TIPO_DOC|SERIE|NUMERO|MTO_IGV|MTO_TOTAL|FECHA|TIPO_DOC_CLIENTE|NUM_DOC_CLIENTE
        // Note: Using dummy RUC and IDs for this demo as we don't have them in
        // request/db
        String rucEmisor = "20601234567";
        String tipoDoc = "03"; // Boleta
        String serie = "B001";
        // Convert ID to number (ID is generated AFTER save, but we need it here?
        // Actually, we can't have the ID before saving if it's Identity.
        // Option 1: Save first, then update. Option 2: Use timestamp or UUID.
        // For compliance simulation, we will save first to get ID, then update QR.
        cabecera = cabeceraVentaRepository.save(cabecera);

        String numero = String.format("%08d", cabecera.getId());
        double mtoIgv = totalFinal - (totalFinal / 1.18);
        String fecha = cabecera.getFechaHora().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        String cadenaQR = String.format("%s|%s|%s|%s|%.2f|%.2f|%s|%s|%s",
                rucEmisor, tipoDoc, serie, numero, mtoIgv, totalFinal, fecha,
                "1", "00000000"); // Dummy client data

        cabecera.setCadenaQR(cadenaQR);

        // 2. Generate Hash (Valor Resumen) - SHA-256 of cadenaQR
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(cadenaQR.getBytes(StandardCharsets.UTF_8));
            String valorResumen = Base64.getEncoder().encodeToString(hash);
            cabecera.setValorResumen(valorResumen);
        } catch (Exception e) {
            throw new RuntimeException("Error generating hash", e);
        }

        // Save again with QR and Hash
        return cabeceraVentaRepository.save(cabecera);
    }

    public Page<CabeceraVenta> getVentas(LocalDateTime inicio, LocalDateTime fin, Pageable pageable) {
        if (inicio != null && fin != null) {
            return cabeceraVentaRepository.findByFechaHoraBetween(inicio, fin, pageable);
        }
        return cabeceraVentaRepository.findAll(pageable);
    }

    public List<DetalleVenta> getDetalles(Long idVenta) {
        return detalleVentaRepository.findByCabeceraVentaId(idVenta);
    }
}
