package com.libreria.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cabecera_ventas")
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class CabeceraVenta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "modo_pago", nullable = false)
    private String modoPago;

    @Column(name = "total_final", nullable = false)
    private Double totalFinal;

    @Column(name = "cadena_qr", length = 500)
    private String cadenaQR;

    @Column(name = "valor_resumen")
    private String valorResumen;

    @OneToMany(mappedBy = "cabeceraVenta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleVenta> detalles = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        fechaHora = LocalDateTime.now();
    }

    public void addDetalle(DetalleVenta detalle) {
        detalles.add(detalle);
        detalle.setCabeceraVenta(this);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getModoPago() {
        return modoPago;
    }

    public void setModoPago(String modoPago) {
        this.modoPago = modoPago;
    }

    public Double getTotalFinal() {
        return totalFinal;
    }

    public void setTotalFinal(Double totalFinal) {
        this.totalFinal = totalFinal;
    }

    public String getCadenaQR() {
        return cadenaQR;
    }

    public void setCadenaQR(String cadenaQR) {
        this.cadenaQR = cadenaQR;
    }

    public String getValorResumen() {
        return valorResumen;
    }

    public void setValorResumen(String valorResumen) {
        this.valorResumen = valorResumen;
    }

    public List<DetalleVenta> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleVenta> detalles) {
        this.detalles = detalles;
    }
}
