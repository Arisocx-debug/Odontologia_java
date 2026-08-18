package com.wilsonmontenegro.odontologia.controller;

import com.wilsonmontenegro.odontologia.exception.BusinessException;
import com.wilsonmontenegro.odontologia.model.Proveedor;
import com.wilsonmontenegro.odontologia.service.ProveedorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * CRUD de proveedores. Equivalente a ProveedorController.php.
 * Accesible por Administrador (ruta /admin/proveedores).
 */
@Controller
@RequestMapping("/admin/proveedores")
@RequiredArgsConstructor
public class ProveedorWebController {

    private final ProveedorService proveedorService;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("proveedores", proveedorService.listarTodos());
        return "proveedores/index";
    }

    @PostMapping
    public String store(@RequestParam String nombre,
                         @RequestParam(required = false) String contacto,
                         @RequestParam(required = false) String telefono,
                         @RequestParam(required = false) String email,
                         @RequestParam(required = false) String direccion,
                         RedirectAttributes redirectAttributes) {
        try {
            Proveedor datos = Proveedor.builder()
                    .nombre(nombre).contacto(contacto).telefono(telefono)
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
            Proveedor datos = Proveedor.builder()
                    .nombre(nombre).contacto(contacto).telefono(telefono)
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
