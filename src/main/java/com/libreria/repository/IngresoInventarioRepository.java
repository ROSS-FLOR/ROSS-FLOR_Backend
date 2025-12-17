package com.libreria.repository;

import com.libreria.model.IngresoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IngresoInventarioRepository extends JpaRepository<IngresoInventario, Long> {
    List<IngresoInventario> findByProductoId(Long productoId);
}
