package com.wilsonmontenegro.odontologia.service;

import com.wilsonmontenegro.odontologia.model.MovimientoStock;
import com.wilsonmontenegro.odontologia.model.enums.TipoMovimiento;
import com.wilsonmontenegro.odontologia.repository.MovimientoStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovimientoStockService {

    private final MovimientoStockRepository movimientoStockRepository;

    public List<MovimientoStock> listarTodos() {
        return movimientoStockRepository.findAllByOrderByFechaDesc();
    }

    @Transactional
    public MovimientoStock registrar(Long productoId, String nombreProducto, TipoMovimiento tipo,
                                      int cantidad, String descripcion, String responsable) {
        MovimientoStock movimiento = MovimientoStock.builder()
                .fecha(LocalDate.now())
                .producto(nombreProducto)
                .tipo(tipo)
                .cantidad(cantidad)
                .responsable(responsable)
                .productoId(productoId)
                .descripcion(descripcion)
                .build();
        return movimientoStockRepository.save(movimiento);
    }
}
