package com.wilsonmontenegro.odontologia.controller;

import com.wilsonmontenegro.odontologia.model.Cita;
import com.wilsonmontenegro.odontologia.model.enums.EstadoCita;
import com.wilsonmontenegro.odontologia.service.CitaService;
import com.wilsonmontenegro.odontologia.service.ReporteService;
import com.wilsonmontenegro.odontologia.util.AuthUtil;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/cliente/historial-citas")
@RequiredArgsConstructor
public class ClienteHistorialCitaController {

    private final CitaService citaService;
    private final ReporteService reporteService;

    @GetMapping
    public String index(
            @RequestParam(required = false) String fechaDesde,
            @RequestParam(required = false) String fechaHasta,
            @RequestParam(required = false) EstadoCita estado,
            @RequestParam(required = false, defaultValue = "") String search,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (search != null && search.matches(".*\\d.*")) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Solo se puede buscar por servicio.");

            return "redirect:/cliente/historial-citas";
        }

        Long usuarioId = AuthUtil.idUsuarioActual();

        model.addAttribute(
                "citas",
                citaService.buscarHistorialPorUsuario(
                        usuarioId,
                        fechaDesde,
                        fechaHasta,
                        estado,
                        search));

        model.addAttribute("fechaDesde", fechaDesde);
        model.addAttribute("fechaHasta", fechaHasta);
        model.addAttribute("estado", estado);
        model.addAttribute("search", search);

        return "cliente/historial-citas";
    }

    @GetMapping("/reporte/{formato}")
    public ResponseEntity<byte[]> reporte(@PathVariable String formato,
            @RequestParam(required = false) String fechaDesde,
            @RequestParam(required = false) String fechaHasta,
            @RequestParam(required = false) EstadoCita estado,
            @RequestParam(required = false, defaultValue = "") String search) {
        var citas = citaService.buscarHistorialPorUsuario(AuthUtil.idUsuarioActual(), fechaDesde, fechaHasta, estado, search);
        String[] encabezados = {"ID", "Servicio", "Entrada", "Salida", "Estado"};
        var filas = citas.stream().map(this::filaHistorial).toList();
        var criterios = ReporteService.filtros("Búsqueda", search, "Desde", fechaDesde, "Hasta", fechaHasta, "Estado", estado);
        boolean pdf = "pdf".equalsIgnoreCase(formato);
        byte[] contenido = pdf
                ? reporteService.generarPdf("Historial de mis citas", encabezados, filas, criterios)
                : reporteService.generarExcel("Historial de mis citas", encabezados, filas, criterios);
        String extension = pdf ? "pdf" : "xlsx";
        MediaType tipo = pdf ? MediaType.APPLICATION_PDF
                : MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=historial-citas." + extension)
                .contentType(tipo)
                .body(contenido);
    }

    private String[] filaHistorial(Cita cita) {
        String servicio = cita.getServicio() != null ? cita.getServicio().getNombre() : "-";
        return new String[]{
                String.valueOf(cita.getIdCita()), servicio,
                String.valueOf(cita.getFechaEntrada()),
                cita.getFechaSalida() == null ? "-" : String.valueOf(cita.getFechaSalida()),
                String.valueOf(cita.getEstado())
        };
    }
}