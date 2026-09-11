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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.wilsonmontenegro.odontologia.exception.BusinessException;
import com.wilsonmontenegro.odontologia.model.Inventario;
import com.wilsonmontenegro.odontologia.service.InventarioService;
import com.wilsonmontenegro.odontologia.service.ProductoImagenService;
import com.wilsonmontenegro.odontologia.service.ProveedorService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/inventario")
@PreAuthorize("hasAnyRole('ADMINISTRADOR','EMPLEADO')")
public class InventarioWebController {

    private final InventarioService inventarioService;
    private final ProveedorService proveedorService;
    private final ProductoImagenService productoImagenService;

    @GetMapping
    public String index(@RequestParam(required = false, defaultValue = "") String buscar,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!buscar.trim().isEmpty() && buscar.trim().matches("\\d+")) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Solo se puede buscar por nombre, estado o proveedor.");

            return "redirect:/inventario";
        }

        model.addAttribute("items", inventarioService.buscar(buscar));
        model.addAttribute("buscar", buscar);
        model.addAttribute("proveedores", proveedorService.listarTodos());

        return "inventario/index";
    }

    @PostMapping
    public String store(@RequestParam String nombre,
            @RequestParam Integer stock,
            @RequestParam BigDecimal precioUnitario,
            @RequestParam(required = false) String nombreProveedor,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) MultipartFile imagen,
            RedirectAttributes redirectAttributes) {

        try {
            Inventario datos = Inventario.builder()
                    .nombre(nombre).stock(stock).precioUnitario(precioUnitario)
                    .nombreProveedor(nombreProveedor).descripcion(descripcion)
                    .imagen(productoImagenService.guardar(imagen))
                    .build();

            inventarioService.crear(datos);
            redirectAttributes.addFlashAttribute("success", "Producto agregado al inventario.");

        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/inventario";
    }

    @PutMapping("/{id}")
    public String update(@PathVariable Long id,
            @RequestParam String nombre,
            @RequestParam Integer stock,
            @RequestParam BigDecimal precioUnitario,
            @RequestParam(required = false) String nombreProveedor,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) MultipartFile imagen,
            @RequestParam(required = false, defaultValue = "false") Boolean eliminarImagen,
            RedirectAttributes redirectAttributes) {

        try {
            // Si se solicita eliminar la imagen, no guardar ninguna
            String imagenUrl = null;
            if (eliminarImagen != null && eliminarImagen) {
                imagenUrl = null; // Eliminar imagen
            } else {
                imagenUrl = productoImagenService.guardar(imagen);
            }

            Inventario datos = Inventario.builder()
                    .nombre(nombre).stock(stock).precioUnitario(precioUnitario)
                    .nombreProveedor(nombreProveedor).descripcion(descripcion)
                    .imagen(imagenUrl)
                    .build();

            inventarioService.actualizar(id, datos, eliminarImagen);
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
    public String toggleEstado(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        inventarioService.toggleEstado(id);
        redirectAttributes.addFlashAttribute("success", "Estado del producto actualizado.");

        return "redirect:/inventario";
    }
}
