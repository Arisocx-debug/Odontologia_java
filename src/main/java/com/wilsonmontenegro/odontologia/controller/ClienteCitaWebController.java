package com.wilsonmontenegro.odontologia.controller;

import com.wilsonmontenegro.odontologia.exception.BusinessException;
import com.wilsonmontenegro.odontologia.model.Cita;
import com.wilsonmontenegro.odontologia.service.CitaService;
import com.wilsonmontenegro.odontologia.service.ExcelService;
import com.wilsonmontenegro.odontologia.service.PdfService;
import com.wilsonmontenegro.odontologia.service.ServicioService;
import com.wilsonmontenegro.odontologia.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

/**
 * Portal del cliente: solo puede ver, agendar, editar y cancelar SUS PROPIAS citas.
 * Equivalente a ClienteCitaController.php.
 */
@Controller
@RequestMapping("/cliente/citas")
@RequiredArgsConstructor
public class ClienteCitaWebController {

    private final CitaService citaService;
    private final ServicioService servicioService;
    private final PdfService pdfService;
    private final ExcelService excelService;

    @GetMapping
    public String index(Model model) {
        Long usuarioId = AuthUtil.idUsuarioActual();
        model.addAttribute("citas", citaService.listarPorUsuario(usuarioId));
        model.addAttribute("servicios", servicioService.listarTodos());
        return "cliente/citas";
    }

    @PostMapping
    public String store(@RequestParam LocalDateTime fechaEntrada,
                         @RequestParam Long idservicio,
                         RedirectAttributes redirectAttributes) {
        try {
            Long usuarioId = AuthUtil.idUsuarioActual();
            citaService.agendarComoCliente(fechaEntrada, idservicio, usuarioId);
            redirectAttributes.addFlashAttribute("success", "Tu cita fue agendada correctamente.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cliente/citas";
    }

    @GetMapping("/{id}/editar")
    public String edit(@PathVariable Long id, Model model) {
        Cita cita = citaService.obtenerPorId(id);
        citaService.validarPropietario(cita, AuthUtil.idUsuarioActual());

        model.addAttribute("citas", citaService.listarPorUsuario(AuthUtil.idUsuarioActual()));
        model.addAttribute("servicios", servicioService.listarTodos());
        model.addAttribute("citaEditar", cita);
        return "cliente/citas";
    }

    @PutMapping("/{id}")
    public String update(@PathVariable Long id,
                          @RequestParam LocalDateTime fechaEntrada,
                          @RequestParam Long idservicio,
                          RedirectAttributes redirectAttributes) {
        try {
            Long usuarioId = AuthUtil.idUsuarioActual();
            citaService.actualizarComoCliente(id, fechaEntrada, idservicio, usuarioId);
            redirectAttributes.addFlashAttribute("success", "Tu cita fue actualizada correctamente.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cliente/citas";
    }

    @DeleteMapping("/{id}")
    public String destroy(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Cita cita = citaService.obtenerPorId(id);
        citaService.validarPropietario(cita, AuthUtil.idUsuarioActual());
        citaService.eliminar(id);
        redirectAttributes.addFlashAttribute("success", "Tu cita fue cancelada.");
        return "redirect:/cliente/citas";
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> generarPdf(@PathVariable Long id) {
        Cita cita = citaService.obtenerPorId(id);
        citaService.validarPropietario(cita, AuthUtil.idUsuarioActual());
        byte[] pdf = pdfService.generarPdfCita(cita);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=cita_" + id + ".pdf")
                .body(pdf);
    }

    @GetMapping("/{id}/excel")
    public ResponseEntity<byte[]> generarExcel(@PathVariable Long id) {
        Cita cita = citaService.obtenerPorId(id);
        citaService.validarPropietario(cita, AuthUtil.idUsuarioActual());
        byte[] excel = excelService.generarExcelFactura(cita);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Factura_" + id + ".xlsx")
                .body(excel);
    }
}
