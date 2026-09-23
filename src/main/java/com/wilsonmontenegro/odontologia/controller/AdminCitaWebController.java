package com.wilsonmontenegro.odontologia.controller;

import com.wilsonmontenegro.odontologia.exception.BusinessException;
import com.wilsonmontenegro.odontologia.model.Cita;
import com.wilsonmontenegro.odontologia.model.enums.EstadoCita;
import com.wilsonmontenegro.odontologia.service.CitaService;
import com.wilsonmontenegro.odontologia.service.ClienteConsultaService;
import com.wilsonmontenegro.odontologia.service.ExcelService;
import com.wilsonmontenegro.odontologia.service.PdfService;
import com.wilsonmontenegro.odontologia.service.ReporteService;
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
 * CRUD completo de citas para el rol Administrador. Equivalente a
 * AdminCitaController.php.
 */
@Controller
@RequestMapping("/admin/citas")
@RequiredArgsConstructor
public class AdminCitaWebController {

    private final CitaService citaService;
    private final ServicioService servicioService;
    private final ClienteConsultaService clienteConsultaService;
    private final PdfService pdfService;
    private final ExcelService excelService;
    private final ReporteService reporteService;

    @GetMapping
    public String index(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) Long servicioId,
            @RequestParam(required = false) String fechaDesde,
            @RequestParam(required = false) String fechaHasta,
            @RequestParam(required = false) EstadoCita estado,
            Model model) {

        try {

            model.addAttribute("citas", citaService.buscarCitasActivas(clienteId, servicioId,
                    fechaDesde, fechaHasta, estado, search));

        } catch (BusinessException e) {

            model.addAttribute("error", e.getMessage());

            model.addAttribute(
                    "citas",
                    citaService.listarCitasActivas());
        }

        model.addAttribute("search", search);
        model.addAttribute("clienteId", clienteId);
        model.addAttribute("servicioId", servicioId);
        model.addAttribute("fechaDesde", fechaDesde);
        model.addAttribute("fechaHasta", fechaHasta);
        model.addAttribute("estado", estado);

        model.addAttribute(
                "clientes",
                clienteConsultaService.listarClientesConUsuario());

        model.addAttribute(
                "servicios",
                servicioService.listarTodos());

        return "admin/citas";
    }

    @GetMapping("/reporte/{formato}")
    public ResponseEntity<byte[]> reporte(@PathVariable String formato,
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false) Long clienteId, @RequestParam(required = false) Long servicioId,
            @RequestParam(required = false) String fechaDesde, @RequestParam(required = false) String fechaHasta,
            @RequestParam(required = false) EstadoCita estado) {
        var citas = citaService.buscarCitasActivas(clienteId, servicioId, fechaDesde, fechaHasta, estado, search);
        String[] encabezados = {"ID", "Paciente", "Servicio", "Entrada", "Estado"};
        var filas = citas.stream().map(c -> new String[]{String.valueOf(c.getIdCita()),
                c.getCliente().getUsuario().getName(), c.getServicio().getNombre(),
                String.valueOf(c.getFechaEntrada()), String.valueOf(c.getEstado())}).toList();
        var criterios = ReporteService.filtros("Búsqueda", search, "Cliente", clienteId, "Servicio", servicioId,
                "Desde", fechaDesde, "Hasta", fechaHasta, "Estado", estado);
        return respuestaReporte(formato, "Reporte de citas", encabezados, filas, criterios);
    }

    private ResponseEntity<byte[]> respuestaReporte(String formato, String titulo, String[] encabezados,
            java.util.List<String[]> filas, java.util.Map<String, String> criterios) {
        boolean pdf = "pdf".equalsIgnoreCase(formato);
        byte[] contenido = pdf ? reporteService.generarPdf(titulo, encabezados, filas, criterios)
                : reporteService.generarExcel(titulo, encabezados, filas, criterios);
        String extension = pdf ? "pdf" : "xlsx";
        MediaType tipo = pdf ? MediaType.APPLICATION_PDF : MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=citas." + extension).contentType(tipo).body(contenido);
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
        return "redirect:/admin/citas";
    }

    @GetMapping("/{id}/editar")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("citas", citaService.listarTodas());
        model.addAttribute("clientes", clienteConsultaService.listarClientesConUsuario());
        model.addAttribute("servicios", servicioService.listarTodos());
        model.addAttribute("citaEditar", citaService.obtenerPorId(id));
        model.addAttribute("search", "");
        return "admin/citas";
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
        return "redirect:/admin/citas";
    }

    @DeleteMapping("/{id}")
    public String destroy(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        citaService.eliminar(id);
        redirectAttributes.addFlashAttribute("success", "Cita eliminada correctamente.");
        return "redirect:/admin/citas";
    }

    @GetMapping("/pdf/{id}")
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
                .contentType(
                        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Factura_" + id + ".xlsx")
                .body(excel);
    }
}
