package com.wilsonmontenegro.odontologia.controller;

import com.wilsonmontenegro.odontologia.model.enums.EstadoCita;
import com.wilsonmontenegro.odontologia.service.CitaService;
import com.wilsonmontenegro.odontologia.util.AuthUtil;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/cliente/historial-citas")
@RequiredArgsConstructor
public class ClienteHistorialCitaController {

    private final CitaService citaService;

    @GetMapping
    public String index(
            @RequestParam(required = false) String fechaDesde,
            @RequestParam(required = false) String fechaHasta,
            @RequestParam(required = false) EstadoCita estado,
            @RequestParam(required = false, defaultValue = "") String search,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (search != null && search.matches(".*\\d.*")) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Solo se puede buscar por servicio.");

            return "redirect:/cliente/historial-citas";
        }

        Long usuarioId = AuthUtil.idUsuarioActual();

        model.addAttribute(
                "citas",
                citaService.buscarHistorialPorUsuario(
                        usuarioId,
                        fechaDesde,
                        fechaHasta,
                        estado,
                        search));

        model.addAttribute("fechaDesde", fechaDesde);
        model.addAttribute("fechaHasta", fechaHasta);
        model.addAttribute("estado", estado);
        model.addAttribute("search", search);

        return "cliente/historial-citas";
    }
}