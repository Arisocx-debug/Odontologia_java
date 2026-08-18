package com.wilsonmontenegro.odontologia.controller;

import com.wilsonmontenegro.odontologia.dto.response.DashboardResponse;
import com.wilsonmontenegro.odontologia.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Panel de estadisticas. Equivalente a DashboardController.php.
 */
@Controller
@RequiredArgsConstructor
public class DashboardWebController {

    private final DashboardService dashboardService;

    @GetMapping("/admin/dashboard")
    public String index(@RequestParam(required = false) Integer anio,
                         @RequestParam(required = false) Integer mes,
                         Model model) {
        DashboardResponse stats = dashboardService.obtenerEstadisticas(anio, mes);
        model.addAttribute("stats", stats);
        return "dashboard/index";
    }
}
