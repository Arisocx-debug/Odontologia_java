package com.wilsonmontenegro.odontologia.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wilsonmontenegro.odontologia.dto.VentaDTO;
import com.wilsonmontenegro.odontologia.model.Inventario;
import com.wilsonmontenegro.odontologia.model.Usuario;
import com.wilsonmontenegro.odontologia.model.Venta;
import com.wilsonmontenegro.odontologia.service.ExcelService;
import com.wilsonmontenegro.odontologia.service.InventarioService;
import com.wilsonmontenegro.odontologia.service.PdfService;
import com.wilsonmontenegro.odontologia.service.VentaService;
import com.wilsonmontenegro.odontologia.util.AuthUtil;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/cliente")
@RequiredArgsConstructor
public class ClienteVentaWebController {

    private final VentaService ventaService;
    private final InventarioService inventarioService;
    private final PdfService pdfService;
    private final ExcelService excelService;

    /**
     * Muestra los productos disponibles para el cliente.
     */
    @GetMapping("/inventario")
    public String mostrarInventario(Model model) {

        List<Inventario> items = inventarioService.listarTodos()
                .stream()
                .filter(item -> item.getEstado() != null
                        && item.getEstado().name().equals("ACTIVO"))
                .filter(item -> item.getStock() != null
                        && item.getStock() > 0)
                .toList();

        model.addAttribute("items", items);

        return "cliente/inventario";
    }

    /**
     * Registra una compra individual.
     */
    @PostMapping("/compras")
    @ResponseBody
    public ResponseEntity<?> registrarCompra(
            @RequestBody VentaDTO dto) {

        try {
            Usuario usuario = AuthUtil.usuarioActual();

            if (usuario == null) {
                return ResponseEntity.status(401).body(
                        Map.of(
                                "success", false,
                                "message", "No hay un usuario autenticado."
                        )
                );
            }

            if (dto == null) {
                return ResponseEntity.badRequest().body(
                        Map.of(
                                "success", false,
                                "message", "Los datos de la compra son obligatorios."
                        )
                );
            }

            if (dto.getProductoId() == null) {
                return ResponseEntity.badRequest().body(
                        Map.of(
                                "success", false,
                                "message", "Debe seleccionar un producto."
                        )
                );
            }

            if (dto.getCantidad() <= 0) {
                return ResponseEntity.badRequest().body(
                        Map.of(
                                "success", false,
                                "message", "La cantidad debe ser mayor a cero."
                        )
                );
            }

            Venta venta = ventaService.registrarVenta(
                    dto.getProductoId(),
                    dto.getCantidad(),
                    java.math.BigDecimal.ZERO,
                    usuario.getName(),
                    "COMPRA_CLIENTE",
                    usuario.getId()
            );

            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "message", "Compra registrada correctamente.",
                            "ventaId", venta.getIdVenta()
                    )
            );

        } catch (Exception e) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "message", e.getMessage() != null
                                    ? e.getMessage()
                                    : "No fue posible registrar la compra."
                    )
            );
        }
    }

    /**
     * Registra todos los productos del carrito.
     *
     * Este es el único endpoint encargado de procesar
     * la compra completa del carrito.
     */
    @PostMapping(
            value = "/registrar-venta",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<?> completarCheckout(
            @RequestBody List<VentaDTO> items) {

        try {
            Usuario usuario = AuthUtil.usuarioActual();

            if (usuario == null) {
                return ResponseEntity.status(401).body(
                        Map.of(
                                "success", false,
                                "message", "No hay un usuario autenticado."
                        )
                );
            }

            if (items == null || items.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        Map.of(
                                "success", false,
                                "message", "El carrito está vacío."
                        )
                );
            }

            for (VentaDTO item : items) {

                if (item == null) {
                    return ResponseEntity.badRequest().body(
                            Map.of(
                                    "success", false,
                                    "message", "El carrito contiene un producto inválido."
                            )
                    );
                }

                if (item.getProductoId() == null) {
                    return ResponseEntity.badRequest().body(
                            Map.of(
                                    "success", false,
                                    "message", "Uno de los productos no tiene un ID válido."
                            )
                    );
                }

                if (item.getCantidad() <= 0) {
                    return ResponseEntity.badRequest().body(
                            Map.of(
                                    "success", false,
                                    "message", "La cantidad debe ser mayor a cero."
                            )
                    );
                }
            }

            List<Venta> ventas = ventaService.registrarVentasCliente(
                    items,
                    usuario.getId(),
                    usuario.getName()
            );

            List<Long> ventaIds = ventas.stream()
                    .map(Venta::getIdVenta)
                    .toList();

            Map<String, Object> respuesta = new HashMap<>();

            respuesta.put("success", true);
            respuesta.put(
                    "message",
                    "Compra registrada correctamente."
            );
            respuesta.put("ventaIds", ventaIds);

            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "message", e.getMessage() != null
                                    ? e.getMessage()
                                    : "No fue posible registrar la compra."
                    )
            );
        }
    }

    /**
     * Genera el PDF de una compra.
     */
    @GetMapping("/compras/{id}/pdf")
    public ResponseEntity<?> descargarPdf(
            @PathVariable Long id) {

        try {
            Usuario usuario = AuthUtil.usuarioActual();

            if (usuario == null) {
                return ResponseEntity.status(401).build();
            }

            Venta venta = ventaService.obtenerPorId(id);

            ventaService.validarPropietario(
                    venta,
                    usuario.getId()
            );

            byte[] pdf = pdfService.generarPdfVenta(venta);

            return ResponseEntity.ok()
                    .header(
                            "Content-Disposition",
                            "attachment; filename=venta-" + id + ".pdf"
                    )
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);

        } catch (Exception e) {

            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Genera el Excel de una compra.
     */
    @GetMapping("/compras/{id}/excel")
    public ResponseEntity<?> descargarExcel(
            @PathVariable Long id) {

        try {
            Usuario usuario = AuthUtil.usuarioActual();

            if (usuario == null) {
                return ResponseEntity.status(401).build();
            }

            Venta venta = ventaService.obtenerPorId(id);

            ventaService.validarPropietario(
                    venta,
                    usuario.getId()
            );

            byte[] excel = excelService.generarExcelVenta(venta);

            return ResponseEntity.ok()
                    .header(
                            "Content-Disposition",
                            "attachment; filename=venta-" + id + ".xlsx"
                    )
                    .contentType(
                            MediaType.parseMediaType(
                                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                            )
                    )
                    .body(excel);

        } catch (Exception e) {

            return ResponseEntity.badRequest().build();
        }
    }
}
