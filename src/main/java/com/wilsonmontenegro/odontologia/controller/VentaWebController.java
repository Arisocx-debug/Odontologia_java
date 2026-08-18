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

import java.math.BigDecimal;

/**
 * Registro de ventas desde el panel de Administrador/Empleado. Equivalente a VentaController.php.
 */
@Controller
@RequiredArgsConstructor
public class VentaWebController {

    private final VentaService ventaService;
    private final InventarioService inventarioService;
    private final PdfService pdfService;
    private final ExcelService excelService;

    @GetMapping({"/admin/ventas", "/empleado/ventas"})
    public String index(Model model) {
        model.addAttribute("ventas", ventaService.listarTodas());
        model.addAttribute("productos", inventarioService.listarTodos());
        return "ventas/index";
    }

    @PostMapping({"/admin/ventas", "/empleado/ventas"})
    public String store(@RequestParam Long idInventario,
                         @RequestParam Integer cantidad,
                         @RequestParam(required = false, defaultValue = "0") BigDecimal descuento,
                         RedirectAttributes redirectAttributes) {
        try {
            String responsable = AuthUtil.usuarioActual() != null ? AuthUtil.usuarioActual().getName() : "N/A";
            ventaService.registrarVenta(idInventario, cantidad, descuento, responsable, "VENTA_INTERNA", null);
            redirectAttributes.addFlashAttribute("success", "Venta registrada correctamente.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/ventas";
    }

    @DeleteMapping({"/admin/ventas/{id}", "/empleado/ventas/{id}"})
    public String destroy(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            ventaService.eliminar(id);
            redirectAttributes.addFlashAttribute("success", "Venta anulada y stock restaurado correctamente.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/ventas";
    }

    @GetMapping({"/admin/ventas/{id}/pdf", "/empleado/ventas/{id}/pdf"})
    public ResponseEntity<byte[]> generarPdf(@PathVariable Long id) {
        Venta venta = ventaService.obtenerPorId(id);
        byte[] pdf = pdfService.generarPdfVenta(venta);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=venta_" + id + ".pdf")
                .body(pdf);
    }

    @GetMapping({"/admin/ventas/{id}/excel", "/empleado/ventas/{id}/excel"})
    public ResponseEntity<byte[]> generarExcel(@PathVariable Long id) {
        Venta venta = ventaService.obtenerPorId(id);
        byte[] excel = excelService.generarExcelVenta(venta);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Venta_" + id + ".xlsx")
                .body(excel);
    }
}
