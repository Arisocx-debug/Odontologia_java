package com.wilsonmontenegro.odontologia.controller;

import com.wilsonmontenegro.odontologia.exception.BusinessException;
import com.wilsonmontenegro.odontologia.model.Inventario;
import com.wilsonmontenegro.odontologia.service.InventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

/**
 * CRUD de inventario. Equivalente a InventarioController.php.
 * Se expone bajo /admin/inventario y /empleado/inventario (misma logica, distinto rol).
 */
@Controller
@RequiredArgsConstructor
public class InventarioWebController {

    private final InventarioService inventarioService;

    @GetMapping({"/admin/inventario", "/empleado/inventario"})
    public String index(@RequestParam(required = false, defaultValue = "") String buscar, Model model) {
        model.addAttribute("items", inventarioService.buscar(buscar));
        model.addAttribute("buscar", buscar);
        return "inventario/index";
    }

    @PostMapping({"/admin/inventario", "/empleado/inventario"})
    public String store(@RequestParam String nombre,
                         @RequestParam Integer stock,
                         @RequestParam BigDecimal precioUnitario,
                         @RequestParam(required = false) String nombreProveedor,
                         @RequestParam(required = false) String descripcion,
                         RedirectAttributes redirectAttributes) {
        Inventario datos = Inventario.builder()
                .nombre(nombre).stock(stock).precioUnitario(precioUnitario)
                .nombreProveedor(nombreProveedor).descripcion(descripcion)
                .build();
        inventarioService.crear(datos);
        redirectAttributes.addFlashAttribute("success", "Producto agregado al inventario.");
        return "redirect:/admin/inventario";
    }

    @PutMapping({"/admin/inventario/{id}", "/empleado/inventario/{id}"})
    public String update(@PathVariable Long id,
                          @RequestParam String nombre,
                          @RequestParam Integer stock,
                          @RequestParam BigDecimal precioUnitario,
                          @RequestParam(required = false) String nombreProveedor,
                          @RequestParam(required = false) String descripcion,
                          RedirectAttributes redirectAttributes) {
        try {
            Inventario datos = Inventario.builder()
                    .nombre(nombre).stock(stock).precioUnitario(precioUnitario)
                    .nombreProveedor(nombreProveedor).descripcion(descripcion)
                    .build();
            inventarioService.actualizar(id, datos);
            redirectAttributes.addFlashAttribute("success", "Producto actualizado correctamente.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/inventario";
    }

    @DeleteMapping({"/admin/inventario/{id}", "/empleado/inventario/{id}"})
    public String destroy(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            inventarioService.eliminar(id);
            redirectAttributes.addFlashAttribute("success", "Producto eliminado correctamente.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/inventario";
    }

    @PatchMapping({"/admin/inventario/{id}/estado", "/empleado/inventario/{id}/estado"})
    public String toggleEstado(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        inventarioService.toggleEstado(id);
        redirectAttributes.addFlashAttribute("success", "Estado del producto actualizado.");
        return "redirect:/admin/inventario";
    }
}
