package com.libreria.dto;

import java.util.List;

public class SaleRequest {
    private String modoPago;
    private List<SaleItemDto> items;

    // Getters and Setters
    public String getModoPago() {
        return modoPago;
    }

    public void setModoPago(String modoPago) {
        this.modoPago = modoPago;
    }

    public List<SaleItemDto> getItems() {
        return items;
    }

    public void setItems(List<SaleItemDto> items) {
        this.items = items;
    }
}
