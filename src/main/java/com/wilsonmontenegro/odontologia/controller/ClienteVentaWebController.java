package com.wilsonmontenegro.odontologia.controller;

import com.wilsonmontenegro.odontologia.exception.BusinessException;
import com.wilsonmontenegro.odontologia.model.Venta;
import com.wilsonmontenegro.odontologia.service.ExcelService;
import com.wilsonmontenegro.odontologia.service.InventarioService;
import com.wilsonmontenegro.odontologia.service.PdfService;
import com.wilsonmontenegro.odontologia.service.VentaService;
import com.wilsonmontenegro.odontologia.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Portal de compras del cliente. Equivalente a ClienteVentaController.php.
 * El cliente ve el catalogo de inventario activo y puede comprar productos.
 */
@Controller
@RequestMapping("/cliente")
@RequiredArgsConstructor
public class ClienteVentaWebController {

    private final InventarioService inventarioService;
    private final VentaService ventaService;
    private final PdfService pdfService;
    private final ExcelService excelService;

    @GetMapping("/inventario")
    public String catalogo(Model model) {
        model.addAttribute("items", inventarioService.listarTodos().stream()
                .filter(i -> i.getEstado().name().equals("ACTIVO") && i.getStock() > 0)
                .toList());
        return "cliente/inventario";
    }

    @PostMapping("/compras")
    public String comprar(@RequestParam Long idInventario,
                           @RequestParam Integer cantidad,
                           RedirectAttributes redirectAttributes) {
        try {
            String responsable = AuthUtil.usuarioActual() != null ? AuthUtil.usuarioActual().getName() : "Cliente";
            Venta venta = ventaService.registrarVenta(idInventario, cantidad, java.math.BigDecimal.ZERO, responsable,
                    "COMPRA_CLIENTE", AuthUtil.idUsuarioActual());
            redirectAttributes.addFlashAttribute("success", "Compra realizada correctamente. Factura #" + venta.getIdVenta());
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cliente/inventario";
    }

    @GetMapping("/compras/{id}/pdf")
    public ResponseEntity<byte[]> facturaPdf(@PathVariable Long id) {
        Venta venta = ventaService.obtenerPorId(id);
        ventaService.validarPropietario(venta, AuthUtil.idUsuarioActual());
        byte[] pdf = pdfService.generarPdfVenta(venta);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=factura_" + id + ".pdf")
                .body(pdf);
    }

    @GetMapping("/compras/{id}/excel")
    public ResponseEntity<byte[]> facturaExcel(@PathVariable Long id) {
        Venta venta = ventaService.obtenerPorId(id);
        ventaService.validarPropietario(venta, AuthUtil.idUsuarioActual());
        byte[] excel = excelService.generarExcelVenta(venta);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Factura_" + id + ".xlsx")
                .body(excel);
    }
}
