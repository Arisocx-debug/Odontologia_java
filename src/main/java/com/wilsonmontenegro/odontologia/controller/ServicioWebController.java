package com.wilsonmontenegro.odontologia.controller;

import com.wilsonmontenegro.odontologia.exception.BusinessException;
import com.wilsonmontenegro.odontologia.service.ServicioService;
import lombok.RequiredArgsConstructor;
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

    @GetMapping
    public String index(Model model) {
        model.addAttribute("servicios", servicioService.listarTodos());
        return "servicios/index";
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
        servicioService.eliminar(id);
        redirectAttributes.addFlashAttribute("success", "Servicio eliminado correctamente.");
        return "redirect:/servicios";
    }
}
