package com.wilsonmontenegro.odontologia.controller;

import com.wilsonmontenegro.odontologia.exception.BusinessException;
import com.wilsonmontenegro.odontologia.model.enums.Rol;
import com.wilsonmontenegro.odontologia.model.enums.EstadoUsuario;
import com.wilsonmontenegro.odontologia.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
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
