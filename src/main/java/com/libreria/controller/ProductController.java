package com.libreria.controller;

import com.libreria.model.Producto;
import com.libreria.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/productos")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public Page<Producto> getAllProductos(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String busqueda) {
        Pageable pageable = PageRequest.of(page, size);
        if (busqueda != null && !busqueda.isEmpty()) {
            return productService.searchProductos(busqueda, pageable);
        }
        return productService.getAllProductos(pageable);
    }

    @PostMapping
    public Producto createProducto(@RequestBody Producto producto) {
        return productService.createProducto(producto);
    }

    @GetMapping("/{id}")
    public Optional<Producto> getProductoById(@PathVariable Long id) {
        return productService.getProductoById(id);
    }

    @PutMapping("/{id}")
    public Producto updateProducto(@PathVariable Long id, @RequestBody Producto producto) {
        return productService.updateProducto(id, producto);
    }

    @DeleteMapping("/{id}")
    public void deleteProducto(@PathVariable Long id) {
        productService.deleteProducto(id);
    }
}
