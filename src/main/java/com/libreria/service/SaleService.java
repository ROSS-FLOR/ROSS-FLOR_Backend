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

            // 3. Update Stock
            producto.setStockActual(producto.getStockActual() - itemDto.getCantidad());
            productoRepository.save(producto);

            // 4. Create Detail
            DetalleVenta detalle = new DetalleVenta();
            detalle.setProducto(producto);
            detalle.setCantidad(itemDto.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecioUnitario());
            double subtotal = producto.getPrecioUnitario() * itemDto.getCantidad();
            detalle.setSubtotal(subtotal);

            // Link to Header
            cabecera.addDetalle(detalle);

            totalFinal += subtotal;
        }

        cabecera.setTotalFinal(totalFinal);

        // 5. Save all (Cascade should save details)
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
