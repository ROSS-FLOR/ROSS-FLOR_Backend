package com.libreria.repository;

import com.libreria.model.CabeceraVenta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface CabeceraVentaRepository extends JpaRepository<CabeceraVenta, Long> {
    Page<CabeceraVenta> findByFechaHoraBetween(LocalDateTime inicio, LocalDateTime fin, Pageable pageable);
}
