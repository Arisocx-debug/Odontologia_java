package com.wilsonmontenegro.odontologia.controller;

import com.wilsonmontenegro.odontologia.exception.BusinessException;
import com.wilsonmontenegro.odontologia.model.enums.Rol;
import com.wilsonmontenegro.odontologia.model.enums.EstadoUsuario;
import com.wilsonmontenegro.odontologia.service.UsuarioService;
import com.wilsonmontenegro.odontologia.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Gestion de usuarios (crear administradores, empleados y clientes). */

@Controller
@RequestMapping("/admin/usuarios")
@RequiredArgsConstructor
public class UsuarioWebController {

    private final UsuarioService usuarioService;
    private final ReporteService reporteService;

    @GetMapping
    public String index(@RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false) Rol rol,
            @RequestParam(required = false) EstadoUsuario estado,
            @RequestParam(required = false) String fechaDesde,
            @RequestParam(required = false) String fechaHasta,
            Model model,
            RedirectAttributes redirectAttributes) {

        model.addAttribute("usuarios", usuarioService.buscarConFiltros(search, rol, estado, fechaDesde, fechaHasta));
        model.addAttribute("search", search);
        model.addAttribute("roles", Rol.values());
        model.addAttribute("rol", rol);
        model.addAttribute("estado", estado);
        model.addAttribute("fechaDesde", fechaDesde);
        model.addAttribute("fechaHasta", fechaHasta);

        return "usuarios/index";
    }

    @GetMapping("/reporte/{formato}")
    public ResponseEntity<byte[]> reporte(@PathVariable String formato, @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false) Rol rol, @RequestParam(required = false) EstadoUsuario estado,
            @RequestParam(required = false) String fechaDesde, @RequestParam(required = false) String fechaHasta) {
        var usuarios = usuarioService.buscarConFiltros(search, rol, estado, fechaDesde, fechaHasta);
        String[] encabezados = {"ID", "Nombre", "Correo", "Teléfono", "Rol", "Estado"};
        var filas = usuarios.stream().map(u -> new String[]{String.valueOf(u.getId()), u.getName(), u.getEmail(), u.getTelefono(), String.valueOf(u.getRol()), String.valueOf(u.getEstado())}).toList();
        var criterios = ReporteService.filtros("Búsqueda", search, "Rol", rol, "Estado", estado,
                "Desde", fechaDesde, "Hasta", fechaHasta);
        boolean pdf = "pdf".equalsIgnoreCase(formato);
        byte[] contenido = pdf ? reporteService.generarPdf("Reporte de usuarios", encabezados, filas, criterios)
                : reporteService.generarExcel("Reporte de usuarios", encabezados, filas, criterios);
        String extension = pdf ? "pdf" : "xlsx";
        MediaType tipo = pdf ? MediaType.APPLICATION_PDF : MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=usuarios." + extension).contentType(tipo).body(contenido);
    }

    @PostMapping
    public String store(@RequestParam String nombre,
            @RequestParam String email,
            @RequestParam String telefono,
            @RequestParam Rol rol,
            @RequestParam String password,
            RedirectAttributes redirectAttributes) {
        try {
            usuarioService.crear(nombre, email, telefono, rol, password);
            redirectAttributes.addFlashAttribute("success", "Usuario creado correctamente.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    @PutMapping("/{id}")
    public String update(@PathVariable Long id,
            @RequestParam String nombre,
            @RequestParam String email,
            @RequestParam String telefono,
            @RequestParam Rol rol,
            @RequestParam(required = false) String password,
            RedirectAttributes redirectAttributes) {
        try {
            usuarioService.actualizar(id, nombre, email, telefono, rol, password);
            redirectAttributes.addFlashAttribute("success", "Usuario actualizado correctamente.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    @DeleteMapping("/{id}")
    public String destroy(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.eliminar(id);
            redirectAttributes.addFlashAttribute("success", "Usuario eliminado correctamente.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    @PatchMapping("/{id}/estado")
    public String toggleEstado(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.toggleEstado(id);
            redirectAttributes.addFlashAttribute("success", "Estado del usuario actualizado.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }
}
