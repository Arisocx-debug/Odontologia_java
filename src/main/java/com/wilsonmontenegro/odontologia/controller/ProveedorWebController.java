package com.wilsonmontenegro.odontologia.controller;

import com.wilsonmontenegro.odontologia.exception.BusinessException;
import com.wilsonmontenegro.odontologia.model.Proveedor;
import com.wilsonmontenegro.odontologia.service.ProveedorService;
import com.wilsonmontenegro.odontologia.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

/**
 * CRUD de proveedores. Equivalente a ProveedorController.php.
 * Accesible por Administrador (ruta /admin/proveedores).
 */
@Controller
@RequestMapping("/admin/proveedores")
@RequiredArgsConstructor
public class ProveedorWebController {

    private final ProveedorService proveedorService;
    private final ReporteService reporteService;

    @GetMapping
    public String index(@RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false) LocalDate fechaDesde,
            @RequestParam(required = false) LocalDate fechaHasta,
            Model model) {
        model.addAttribute("proveedores", proveedorService.buscarConFiltros(search, fechaDesde, fechaHasta));
        model.addAttribute("search", search);
        model.addAttribute("fechaDesde", fechaDesde);
        model.addAttribute("fechaHasta", fechaHasta);
        return "proveedores/index";
    }

    @GetMapping("/reporte/{formato}")
    public ResponseEntity<byte[]> reporte(@PathVariable String formato,
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false) LocalDate fechaDesde,
            @RequestParam(required = false) LocalDate fechaHasta) {
        var proveedores = proveedorService.buscarConFiltros(search, fechaDesde, fechaHasta);
        String[] encabezados = {"ID", "Nombre", "Contacto", "Telefono", "Correo", "Direccion", "Registro"};
        var filas = proveedores.stream().map(proveedor -> new String[]{
                String.valueOf(proveedor.getId()), proveedor.getNombre(), proveedor.getContacto(),
                proveedor.getTelefono(), proveedor.getEmail(), proveedor.getDireccion(),
                proveedor.getCreatedAt() == null ? "" : proveedor.getCreatedAt().toLocalDate().toString()
        }).toList();
        boolean pdf = "pdf".equalsIgnoreCase(formato);
        byte[] contenido = pdf ? reporteService.generarPdf("Reporte de proveedores", encabezados, filas)
                : reporteService.generarExcel("Reporte de proveedores", encabezados, filas);
        String extension = pdf ? "pdf" : "xlsx";
        MediaType tipo = pdf ? MediaType.APPLICATION_PDF
                : MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=proveedores." + extension)
                .contentType(tipo)
                .body(contenido);
    }

    @PostMapping
    public String store(@RequestParam String nombre,
                         @RequestParam(required = false) String contacto,
                         @RequestParam(required = false) String telefono,
                         @RequestParam(required = false) String email,
                         @RequestParam(required = false) String direccion,
                         RedirectAttributes redirectAttributes) {
        try {
            if (contacto == null || !contacto.matches("[A-Za-zÀ-ÿ\\s]{2,}")) {
                throw new BusinessException("El contacto debe ser un nombre valido (solo letras).");
            }
            Proveedor datos = Proveedor.builder()
                    .nombre(nombre).contacto(contacto.trim()).telefono(telefono)
                    .email(email).direccion(direccion).build();
            proveedorService.crear(datos);
            redirectAttributes.addFlashAttribute("success", "Proveedor creado correctamente.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/proveedores";
    }

    @PutMapping("/{id}")
    public String update(@PathVariable Long id,
                          @RequestParam String nombre,
                          @RequestParam(required = false) String contacto,
                          @RequestParam(required = false) String telefono,
                          @RequestParam(required = false) String email,
                          @RequestParam(required = false) String direccion,
                          RedirectAttributes redirectAttributes) {
        try {
            if (contacto == null || !contacto.matches("[A-Za-zÀ-ÿ\\s]{2,}")) {
                throw new BusinessException("El contacto debe ser un nombre valido (solo letras).");
            }
            Proveedor datos = Proveedor.builder()
                    .nombre(nombre).contacto(contacto.trim()).telefono(telefono)
                    .email(email).direccion(direccion).build();
            proveedorService.actualizar(id, datos);
            redirectAttributes.addFlashAttribute("success", "Proveedor actualizado correctamente.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/proveedores";
    }

    @DeleteMapping("/{id}")
    public String destroy(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        proveedorService.eliminar(id);
        redirectAttributes.addFlashAttribute("success", "Proveedor eliminado correctamente.");
        return "redirect:/admin/proveedores";
    }
}
