package com.wilsonmontenegro.odontologia.service;

import com.wilsonmontenegro.odontologia.exception.BusinessException;
import com.wilsonmontenegro.odontologia.model.Inventario;
import com.wilsonmontenegro.odontologia.model.Usuario;
import com.wilsonmontenegro.odontologia.model.Venta;
import com.wilsonmontenegro.odontologia.model.enums.EstadoInventario;
import com.wilsonmontenegro.odontologia.repository.InventarioRepository;
import com.wilsonmontenegro.odontologia.repository.UsuarioRepository;
import com.wilsonmontenegro.odontologia.repository.VentaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class VentaServiceTest {
    private InventarioRepository inventarioRepository;
    private VentaService ventaService;

    @BeforeEach
    void setUp() {
        inventarioRepository = mock(InventarioRepository.class);
        ventaService = new VentaService(mock(VentaRepository.class), inventarioRepository,
                mock(MovimientoStockService.class), mock(UsuarioRepository.class));
    }

    @Test
    void rechazaCantidadNoPositiva() {
        assertThrows(BusinessException.class,
                () -> ventaService.registrarVenta(1L, 0, BigDecimal.ZERO, "Admin", "VENTA", null));
        verifyNoInteractions(inventarioRepository);
    }

    @Test
    void rechazaProductoInactivo() {
        Inventario item = Inventario.builder().idInventario(1L).stock(10)
                .estado(EstadoInventario.INACTIVO).precioUnitario(BigDecimal.TEN).build();
        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(BusinessException.class,
                () -> ventaService.registrarVenta(1L, 1, BigDecimal.ZERO, "Admin", "VENTA", null));
    }

    @Test
    void soloElCompradorPuedeConsultarSuVenta() {
        Usuario comprador = Usuario.builder().id(10L).build();
        Venta venta = Venta.builder().comprador(comprador).build();

        assertDoesNotThrow(() -> ventaService.validarPropietario(venta, 10L));
        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> ventaService.validarPropietario(venta, 11L));
    }
}
