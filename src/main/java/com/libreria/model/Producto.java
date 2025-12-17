package com.libreria.model;

import jakarta.persistence.*;

@Entity
@Table(name = "productos")
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @org.hibernate.annotations.Formula("(COALESCE((SELECT SUM(ii.cantidad) FROM ingreso_inventario ii WHERE ii.producto_id = id), 0) - COALESCE((SELECT SUM(dv.cantidad) FROM detalle_ventas dv WHERE dv.id_producto_fk = id), 0))")
    private Integer stockActual;

    @Column(name = "precio_unitario", nullable = false)
    private Double precioUnitario;

    @Column(name = "categoria")
    private String categoria;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Integer getStockActual() {
        return stockActual;
    }

    // Setter removed as stock is calculated

    public Double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(Double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }
}
