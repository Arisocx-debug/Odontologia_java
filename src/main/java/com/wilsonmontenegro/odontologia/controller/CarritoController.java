package com.wilsonmontenegro.odontologia.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/cliente")
@RequiredArgsConstructor
public class CarritoController {

    /**
     * Muestra el carrito de compras.
     */
    @GetMapping("/cart")
    public String mostrarCarrito() {
        return "cliente/cart";
    }

    /**
     * Muestra la página de checkout.
     */
    @GetMapping("/checkout")
    public String mostrarCheckout() {
        return "cliente/checkout";
    }
}


