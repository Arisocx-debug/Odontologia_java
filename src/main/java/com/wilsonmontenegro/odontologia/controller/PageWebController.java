package com.wilsonmontenegro.odontologia.controller;

import com.wilsonmontenegro.odontologia.service.ServicioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Paginas publicas del sitio. Equivalente a PageController.php
 * (inicio, mision, vision, objetivos, servicios).
 */
@Controller
@RequiredArgsConstructor
public class PageWebController {

    private final ServicioService servicioService;

    @GetMapping("/")
    public String inicio() {
        return "pages/inicio";
    }

    @GetMapping("/mision")
    public String mision() {
        return "pages/mision";
    }

    @GetMapping("/vision")
    public String vision() {
        return "pages/vision";
    }

    @GetMapping("/objetivos")
    public String objetivos() {
        return "pages/objetivos";
    }

    @GetMapping("/servicios-publicos")
    public String servicios(Model model) {
        model.addAttribute("servicios", servicioService.listarTodos());
        return "pages/servicios";
    }

    @GetMapping("/error/403")
    public String accesoDenegado() {
        return "error/403";
    }
}
