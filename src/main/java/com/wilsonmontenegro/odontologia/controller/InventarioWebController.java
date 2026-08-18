package com.wilsonmontenegro.odontologia.controller;

import java.math.BigDecimal;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.wilsonmontenegro.odontologia.exception.BusinessException;
import com.wilsonmontenegro.odontologia.model.Inventario;
import com.wilsonmontenegro.odontologia.service.InventarioService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/inventario")
@PreAuthorize("hasAnyRole('ADMINISTRADOR','EMPLEADO')")
public class InventarioWebController {

    private final InventarioService inventarioService;

    @GetMapping
    public String index(@RequestParam(required = false, defaultValue = "") String buscar, Model model) {
        model.addAttribute("items", inventarioService.buscar(buscar));
        model.addAttribute("buscar", buscar);
        return "inventario/index";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','EMPLEADO')")
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
        return "redirect:/inventario";
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','EMPLEADO')")
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

        return "redirect:/inventario";
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','EMPLEADO')")
    public String destroy(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        try {
            inventarioService.eliminar(id);
            redirectAttributes.addFlashAttribute("success", "Producto eliminado correctamente.");

        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/inventario";
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','EMPLEADO')")
    public String toggleEstado(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        inventarioService.toggleEstado(id);
        redirectAttributes.addFlashAttribute("success", "Estado del producto actualizado.");

        return "redirect:/inventario";
    }
}

