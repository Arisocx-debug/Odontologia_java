package com.wilsonmontenegro.odontologia.controller;

import com.wilsonmontenegro.odontologia.exception.BusinessException;
import com.wilsonmontenegro.odontologia.model.Cita;
import com.wilsonmontenegro.odontologia.model.enums.EstadoCita;
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
 * 
 * Portal del cliente:
 *
 * El cliente solamente puede:
 * * Ver sus propias citas.
 * * Agendar sus propias citas.
 * * Editar sus propias citas.
 * * Cancelar sus propias citas.
 * * Generar PDF de sus propias citas.
 * * Generar Excel de sus propias citas.
 */
@Controller
@RequestMapping("/cliente/citas")
@RequiredArgsConstructor
public class ClienteCitaWebController {

    private final CitaService citaService;
    private final ServicioService servicioService;
    private final PdfService pdfService;
    private final ExcelService excelService;

    // ============================================================
    // LISTAR CITAS
    // ============================================================

    @GetMapping
    public String index(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false) Long servicioId,
            @RequestParam(required = false) String fechaDesde,
            @RequestParam(required = false) String fechaHasta,
            @RequestParam(required = false) EstadoCita estado,
            Model model) {

        Long usuarioId = AuthUtil.idUsuarioActual();

        model.addAttribute(
                "citas",
                citaService.buscarCitasActivasPorUsuario(
                        usuarioId, servicioId, fechaDesde, fechaHasta, estado, search));

        model.addAttribute("search", search);
        model.addAttribute("servicioId", servicioId);
        model.addAttribute("fechaDesde", fechaDesde);
        model.addAttribute("fechaHasta", fechaHasta);
        model.addAttribute("estado", estado);

        model.addAttribute(
                "servicios",
                servicioService.listarTodos());

        return "cliente/citas";
    }

    // ============================================================
    // AGENDAR CITA
    // ============================================================

    @PostMapping
    public String store(
            @RequestParam LocalDateTime fechaEntrada,
            @RequestParam Long idservicio,
            RedirectAttributes redirectAttributes) {

        try {

            Long usuarioId = AuthUtil.idUsuarioActual();

            System.out.println("=================================");
            System.out.println(
                    "USUARIO ID ACTUAL: " + usuarioId);
            System.out.println(
                    "SERVICIO ID: " + idservicio);
            System.out.println(
                    "FECHA: " + fechaEntrada);
            System.out.println("=================================");

            citaService.agendarComoCliente(
                    fechaEntrada,
                    idservicio,
                    usuarioId);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Tu cita fue agendada correctamente.");

        } catch (BusinessException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage());
        }

        return "redirect:/cliente/citas";
    }

    // ============================================================
    // EDITAR CITA
    // ============================================================

    @GetMapping("/{id}/editar")
    public String edit(
            @PathVariable Long id,
            Model model) {

        Long usuarioId = AuthUtil.idUsuarioActual();

        Cita cita = citaService.obtenerPorId(id);

        // SEGURIDAD:
        // Solo puede editar una cita propia.
        citaService.validarPropietario(
                cita,
                usuarioId);

        model.addAttribute(
                "citas",
                citaService.listarCitasActivasPorUsuario(
                        usuarioId));

        model.addAttribute(
                "servicios",
                servicioService.listarTodos());

        model.addAttribute(
                "citaEditar",
                cita);

        return "cliente/citas";

    }

    // ============================================================
    // ACTUALIZAR CITA
    // ============================================================

    @PutMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @RequestParam LocalDateTime fechaEntrada,
            @RequestParam Long idservicio,
            RedirectAttributes redirectAttributes) {

        try {

            Long usuarioId = AuthUtil.idUsuarioActual();

            citaService.actualizarComoCliente(
                    id,
                    fechaEntrada,
                    idservicio,
                    usuarioId);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Tu cita fue actualizada correctamente.");

        } catch (BusinessException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage());
        }

        return "redirect:/cliente/citas";

    }

    // ============================================================
    // CANCELAR CITA
    // ============================================================

    @DeleteMapping("/{id}")
    public String destroy(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {

            Long usuarioId = AuthUtil.idUsuarioActual();

            citaService.cancelarComoCliente(
                    id,
                    usuarioId);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "La cita fue cancelada correctamente.");

        } catch (BusinessException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage());
        }

        return "redirect:/cliente/citas";
    }

    // ============================================================
    // GENERAR PDF
    // ============================================================

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> generarPdf(
            @PathVariable Long id) {

        Long usuarioId = AuthUtil.idUsuarioActual();

        Cita cita = citaService.obtenerPorId(id);

        // SEGURIDAD:
        // Solo puede generar el PDF de una cita propia.
        citaService.validarPropietario(
                cita,
                usuarioId);

        byte[] pdf = pdfService.generarPdfCita(cita);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=cita_" + id + ".pdf")
                .body(pdf);

    }

    // ============================================================
    // GENERAR EXCEL
    // ============================================================

    @GetMapping("/{id}/excel")
    public ResponseEntity<byte[]> generarExcel(
            @PathVariable Long id) {

        Long usuarioId = AuthUtil.idUsuarioActual();

        Cita cita = citaService.obtenerPorId(id);

        // SEGURIDAD:
        // Solo puede generar el Excel de una cita propia.
        citaService.validarPropietario(
                cita,
                usuarioId);

        byte[] excel = excelService.generarExcelFactura(cita);

        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=Factura_" + id + ".xlsx")
                .body(excel);

    }
}
