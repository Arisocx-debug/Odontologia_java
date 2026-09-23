package com.wilsonmontenegro.odontologia.controller;

import com.wilsonmontenegro.odontologia.exception.BusinessException;
import com.wilsonmontenegro.odontologia.service.ServicioService;
import com.wilsonmontenegro.odontologia.service.ReporteService;
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
 * CRUD de servicios odontologicos. Equivalente a ServicioController.php.
 * Accesible por Administrador y Empleado (segun SecurityConfig).
 */
@Controller
@RequestMapping("/servicios")
@RequiredArgsConstructor
public class ServicioWebController {

    private final ServicioService servicioService;
    private final ReporteService reporteService;

    @GetMapping
    public String index(@RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false) BigDecimal costoMinimo,
            @RequestParam(required = false) BigDecimal costoMaximo,
            Model model) {
        model.addAttribute("servicios", servicioService.buscarConFiltros(search, costoMinimo, costoMaximo));
        model.addAttribute("search", search);
        model.addAttribute("costoMinimo", costoMinimo);
        model.addAttribute("costoMaximo", costoMaximo);
        return "servicios/index";
    }

    @GetMapping("/reporte/{formato}")
    public ResponseEntity<byte[]> reporte(@PathVariable String formato,
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false) BigDecimal costoMinimo,
            @RequestParam(required = false) BigDecimal costoMaximo) {
        var servicios = servicioService.buscarConFiltros(search, costoMinimo, costoMaximo);
        String[] encabezados = {"ID", "Nombre", "Descripcion", "Costo"};
        var filas = servicios.stream().map(servicio -> new String[]{
                String.valueOf(servicio.getIdServicio()), servicio.getNombre(),
                servicio.getDescripcion(), String.valueOf(servicio.getCosto())
        }).toList();
        var criterios = ReporteService.filtros("Búsqueda", search, "Costo mínimo", costoMinimo, "Costo máximo", costoMaximo);
        return respuestaReporte(formato, "Reporte de servicios", "servicios", encabezados, filas, criterios);
    }

    private ResponseEntity<byte[]> respuestaReporte(String formato, String titulo, String archivo,
            String[] encabezados, java.util.List<String[]> filas, java.util.Map<String, String> criterios) {
        boolean pdf = "pdf".equalsIgnoreCase(formato);
        byte[] contenido = pdf ? reporteService.generarPdf(titulo, encabezados, filas, criterios)
                : reporteService.generarExcel(titulo, encabezados, filas, criterios);
        String extension = pdf ? "pdf" : "xlsx";
        MediaType tipo = pdf ? MediaType.APPLICATION_PDF
                : MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + archivo + "." + extension)
                .contentType(tipo)
                .body(contenido);
    }

    @GetMapping("/publicos")
    public String serviciosPublicos() {
        return "servicios/publicos";
    }

    @PostMapping
    public String store(@RequestParam String nombre,
            @RequestParam String descripcion,
            @RequestParam BigDecimal costo,
            RedirectAttributes redirectAttributes) {
        servicioService.crear(nombre, descripcion, costo);
        redirectAttributes.addFlashAttribute("success", "Servicio creado correctamente.");
        return "redirect:/servicios";
    }

    @PutMapping("/{id}")
    public String update(@PathVariable Long id,
            @RequestParam String nombre,
            @RequestParam String descripcion,
            @RequestParam BigDecimal costo,
            RedirectAttributes redirectAttributes) {
        try {
            servicioService.actualizar(id, nombre, descripcion, costo);
            redirectAttributes.addFlashAttribute("success", "Servicio actualizado correctamente.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/servicios";
    }

    @DeleteMapping("/{id}")
    public String destroy(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        try {

            servicioService.eliminar(id);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Servicio eliminado correctamente.");

        } catch (BusinessException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage());
        }

        return "redirect:/servicios";
    }
}
