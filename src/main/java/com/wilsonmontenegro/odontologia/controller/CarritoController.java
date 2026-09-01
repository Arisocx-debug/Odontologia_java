package com.wilsonmontenegro.odontologia.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wilsonmontenegro.odontologia.dto.VentaDTO; // ✅ Importa tu DTO
import com.wilsonmontenegro.odontologia.service.InventarioService;
import com.wilsonmontenegro.odontologia.service.VentaService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/cliente")
@RequiredArgsConstructor
public class CarritoController {

    private final InventarioService inventarioService;
    private final VentaService ventaService; // ✅ Inyecta correctamente el servicio

    @GetMapping("/cart")
    public String mostrarCarrito() {
        return "cliente/cart";
    }

    @PostMapping("/actualizar-stock")
    @ResponseBody
    public void actualizarStock(@RequestParam Long idInventario, @RequestParam int cantidad) {
        inventarioService.actualizarStock(idInventario, cantidad);
    }

    @GetMapping("/checkout")
    public String mostrarCheckout() {
        return "cliente/checkout"; // ✅ apunta a templates/cliente/checkout.html
    }

    @PostMapping("/registrar-venta")
    @ResponseBody
    public void registrarVenta(@RequestBody VentaDTO venta) {
        ventaService.registrarVenta(venta); // ✅ usa la instancia, no la clase
    }
}


