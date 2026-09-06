package com.wilsonmontenegro.odontologia.controller;

import com.wilsonmontenegro.odontologia.model.enums.EstadoCita;
import com.wilsonmontenegro.odontologia.service.CitaService;
import com.wilsonmontenegro.odontologia.service.ClienteConsultaService;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/empleado/historial-citas")
@RequiredArgsConstructor
public class EmpleadoHistorialCitaController {

        private final CitaService citaService;
        private final ClienteConsultaService clienteConsultaService;

        @GetMapping
        public String index(
                        @RequestParam(required = false) Long clienteId,
                        @RequestParam(required = false) String fechaDesde,
                        @RequestParam(required = false) String fechaHasta,
                        @RequestParam(required = false) EstadoCita estado,
                        @RequestParam(required = false, defaultValue = "") String search,
                        Model model,
                        RedirectAttributes redirectAttributes) {

                if (search != null && search.matches(".*\\d.*")) {
                        redirectAttributes.addFlashAttribute(
                                        "error",
                                        "Solo se puede buscar por nombre, estado o servicio.");

                        return "redirect:/empleado/historial-citas";
                }

                model.addAttribute(
                                "citas",
                                citaService.buscarHistorial(
                                                clienteId,
                                                fechaDesde,
                                                fechaHasta,
                                                estado,
                                                search));

                model.addAttribute(
                                "clientes",
                                clienteConsultaService.listarClientesConUsuario());

                model.addAttribute("clienteId", clienteId);
                model.addAttribute("fechaDesde", fechaDesde);
                model.addAttribute("fechaHasta", fechaHasta);
                model.addAttribute("estado", estado);
                model.addAttribute("search", search);

                return "empleado/historial-citas";
        }
}