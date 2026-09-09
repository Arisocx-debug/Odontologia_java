package com.wilsonmontenegro.odontologia.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.wilsonmontenegro.odontologia.dto.response.DashboardResponse;
import com.wilsonmontenegro.odontologia.service.DashboardService;

import lombok.RequiredArgsConstructor;

/**
 * Panel de estadisticas.
 * Equivalente a DashboardController.php.
 */
@Controller
@RequiredArgsConstructor
public class DashboardWebController {

    private final DashboardService dashboardService;

    @GetMapping({"/admin/dashboard", "/empleado/dashboard"})
    public String index(@RequestParam(required = false) Integer anio,
                        @RequestParam(required = false) Integer mes,
                        Model model) {

        // Si no se especifica un año, se utiliza 2026
        if (anio == null) {
            anio = 2026;
        }

        // Validar que el año no sea anterior a 2026
        if (anio < 2026) {

            model.addAttribute("error",
                    "El año no puede ser anterior a 2026.");

            // Mostrar nuevamente el dashboard con el año permitido
            DashboardResponse stats =
                    dashboardService.obtenerEstadisticas(2026, mes);

            model.addAttribute("stats", stats);

            return "dashboard/index";
        }

        // Año válido
        DashboardResponse stats =
                dashboardService.obtenerEstadisticas(anio, mes);

        model.addAttribute("stats", stats);

        return "dashboard/index";
    }
}