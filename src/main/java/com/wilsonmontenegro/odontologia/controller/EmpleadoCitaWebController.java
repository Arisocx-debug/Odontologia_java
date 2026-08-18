package com.wilsonmontenegro.odontologia.controller;

import com.wilsonmontenegro.odontologia.exception.BusinessException;
import com.wilsonmontenegro.odontologia.model.Cita;
import com.wilsonmontenegro.odontologia.model.enums.EstadoCita;
import com.wilsonmontenegro.odontologia.service.CitaService;
import com.wilsonmontenegro.odontologia.service.ClienteConsultaService;
import com.wilsonmontenegro.odontologia.service.ExcelService;
import com.wilsonmontenegro.odontologia.service.PdfService;
import com.wilsonmontenegro.odontologia.service.ServicioService;
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
 * CRUD completo de citas para el rol Empleado. Equivalente a EmpleadoCitaController.php
 * (misma logica que el panel de Administrador, pero bajo el prefijo /empleado).
 */
@Controller
@RequestMapping("/empleado/citas")
@RequiredArgsConstructor
public class EmpleadoCitaWebController {

    private final CitaService citaService;
    private final ServicioService servicioService;
    private final ClienteConsultaService clienteConsultaService;
    private final PdfService pdfService;
    private final ExcelService excelService;

    @GetMapping
    public String index(@RequestParam(required = false, defaultValue = "") String search, Model model) {
        try {
            model.addAttribute("citas", citaService.buscar(search));
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("citas", citaService.listarTodas());
        }
        model.addAttribute("search", search);
        model.addAttribute("clientes", clienteConsultaService.listarClientesConUsuario());
        model.addAttribute("servicios", servicioService.listarTodos());
        return "empleado/citas";
    }

    @PostMapping
    public String store(@RequestParam LocalDateTime fechaEntrada,
                         @RequestParam Long idservicio,
                         @RequestParam Long idcliente,
                         @RequestParam EstadoCita estado,
                         RedirectAttributes redirectAttributes) {
        try {
            citaService.agendar(fechaEntrada, idservicio, idcliente, estado);
            redirectAttributes.addFlashAttribute("success", "Cita agendada correctamente.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/empleado/citas";
    }

    @GetMapping("/{id}/editar")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("citas", citaService.listarTodas());
        model.addAttribute("clientes", clienteConsultaService.listarClientesConUsuario());
        model.addAttribute("servicios", servicioService.listarTodos());
        model.addAttribute("citaEditar", citaService.obtenerPorId(id));
        model.addAttribute("search", "");
        return "empleado/citas";
    }

    @PutMapping("/{id}")
    public String update(@PathVariable Long id,
                          @RequestParam LocalDateTime fechaEntrada,
                          @RequestParam Long idservicio,
                          @RequestParam Long idcliente,
                          @RequestParam EstadoCita estado,
                          RedirectAttributes redirectAttributes) {
        try {
            citaService.actualizar(id, fechaEntrada, idservicio, idcliente, estado);
            redirectAttributes.addFlashAttribute("success", "Cita actualizada correctamente.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/empleado/citas";
    }

    @DeleteMapping("/{id}")
    public String destroy(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        citaService.eliminar(id);
        redirectAttributes.addFlashAttribute("success", "Cita eliminada correctamente.");
        return "redirect:/empleado/citas";
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> generarPdf(@PathVariable Long id) {
        Cita cita = citaService.obtenerPorId(id);
        byte[] pdf = pdfService.generarPdfCita(cita);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=cita_" + id + ".pdf")
                .body(pdf);
    }

    @GetMapping("/{id}/excel")
    public ResponseEntity<byte[]> generarExcel(@PathVariable Long id) {
        Cita cita = citaService.obtenerPorId(id);
        byte[] excel = excelService.generarExcelFactura(cita);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Factura_" + id + ".xlsx")
                .body(excel);
    }
}
