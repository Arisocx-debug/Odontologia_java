package com.wilsonmontenegro.odontologia.controller;

import com.wilsonmontenegro.odontologia.exception.BusinessException;
import com.wilsonmontenegro.odontologia.model.enums.Rol;
import com.wilsonmontenegro.odontologia.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Gestion de usuarios (crear administradores, empleados y clientes).
 * Equivalente a AdminUsuarioController.php.
 */
@Controller
@RequestMapping("/admin/usuarios")
@RequiredArgsConstructor
public class UsuarioWebController {

    private final UsuarioService usuarioService;

    @GetMapping
    public String index(@RequestParam(required = false, defaultValue = "") String search, Model model) {
        model.addAttribute("usuarios", usuarioService.buscar(search));
        model.addAttribute("search", search);
        model.addAttribute("roles", Rol.values());
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
}
