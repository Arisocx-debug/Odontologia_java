package com.wilsonmontenegro.odontologia.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.wilsonmontenegro.odontologia.exception.BusinessException;
import com.wilsonmontenegro.odontologia.model.Venta;
import com.wilsonmontenegro.odontologia.model.enums.EstadoVenta;
import com.wilsonmontenegro.odontologia.service.ExcelService;
import com.wilsonmontenegro.odontologia.service.InventarioService;
import com.wilsonmontenegro.odontologia.service.PdfService;
import com.wilsonmontenegro.odontologia.service.ReporteService;
import com.wilsonmontenegro.odontologia.service.VentaService;
import com.wilsonmontenegro.odontologia.util.AuthUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * Registro y administración de ventas desde el panel
 * de Administrador y Empleado.
 *
 * También muestra las compras realizadas por los clientes,
 * ya que todas las compras quedan registradas como Venta.
 */
@Controller
@RequiredArgsConstructor
public class VentaWebController {

    private final VentaService ventaService;
    private final InventarioService inventarioService;
    private final PdfService pdfService;
    private final ExcelService excelService;
    private final ReporteService reporteService;


    // ============================================================
    // LISTAR VENTAS
    // ============================================================

    @GetMapping({"/admin/ventas", "/empleado/ventas"})
    public String index(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false) Long productoId,
            @RequestParam(required = false) EstadoVenta estado,
            @RequestParam(required = false) String fechaDesde,
            @RequestParam(required = false) String fechaHasta,
            Model model,
            HttpServletRequest request) {

        List<Venta> ventas = ventaService.buscarConFiltros(
                productoId, estado, fechaDesde, fechaHasta, search);

        model.addAttribute(
                "ventas",
                ventas
        );

        model.addAttribute(
                "productos",
                inventarioService.listarTodos()
        );
        model.addAttribute("search", search);
        model.addAttribute("productoId", productoId);
        model.addAttribute("estado", estado);
        model.addAttribute("fechaDesde", fechaDesde);
        model.addAttribute("fechaHasta", fechaHasta);

        String base =
                request.getRequestURI()
                        .startsWith("/admin")
                        ? "/admin/ventas"
                        : "/empleado/ventas";

        model.addAttribute(
                "base",
                base
        );

        return "ventas/index";
    }

    @GetMapping({"/admin/ventas/reporte/{formato}", "/empleado/ventas/reporte/{formato}"})
    public ResponseEntity<byte[]> reporte(@PathVariable String formato,
            @RequestParam(required = false, defaultValue = "") String search, @RequestParam(required = false) Long productoId,
            @RequestParam(required = false) com.wilsonmontenegro.odontologia.model.enums.EstadoVenta estado,
            @RequestParam(required = false) String fechaDesde, @RequestParam(required = false) String fechaHasta) {
        var ventas = ventaService.buscarConFiltros(productoId, estado, fechaDesde, fechaHasta, search);
        String[] encabezados = {"ID", "Producto", "Cantidad", "Total", "Estado", "Fecha"};
        var filas = ventas.stream().map(v -> new String[]{String.valueOf(v.getIdVenta()), v.getProducto().getNombre(), String.valueOf(v.getCantidad()), String.valueOf(v.getTotal()), String.valueOf(v.getEstado()), String.valueOf(v.getCreatedAt())}).toList();
        boolean pdf = "pdf".equalsIgnoreCase(formato);
        byte[] contenido = pdf ? reporteService.generarPdf("Reporte de ventas", encabezados, filas) : reporteService.generarExcel("Reporte de ventas", encabezados, filas);
        String extension = pdf ? "pdf" : "xlsx";
        MediaType tipo = pdf ? MediaType.APPLICATION_PDF : MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ventas." + extension).contentType(tipo).body(contenido);
    }


    // ============================================================
    // REGISTRAR VENTA INTERNA
    // ============================================================

    @PostMapping({"/admin/ventas", "/empleado/ventas"})
    public String store(
            @RequestParam Long idInventario,
            @RequestParam Integer cantidad,
            @RequestParam(
                    required = false,
                    defaultValue = "0"
            ) BigDecimal descuento,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {

        try {

            if (idInventario == null) {
                throw new BusinessException(
                        "Debes seleccionar un producto."
                );
            }

            if (cantidad == null || cantidad <= 0) {
                throw new BusinessException(
                        "La cantidad debe ser mayor que cero."
                );
            }

            String responsable =
                    AuthUtil.usuarioActual() != null
                            ? AuthUtil.usuarioActual().getName()
                            : "Sistema";

            ventaService.registrarVenta(
                    idInventario,
                    cantidad,
                    descuento,
                    responsable,
                    "VENTA_INTERNA",
                    null
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Venta registrada correctamente."
            );

        } catch (BusinessException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }


        String base =
                request.getRequestURI()
                        .startsWith("/admin")
                        ? "/admin/ventas"
                        : "/empleado/ventas";

        return "redirect:" + base;
    }


    // ============================================================
    // ELIMINAR / ANULAR VENTA
    // ============================================================

    @DeleteMapping({
            "/admin/ventas/{id}",
            "/empleado/ventas/{id}"
    })
    public String destroy(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {

        try {

            ventaService.eliminar(id);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Venta anulada y stock restaurado correctamente."
            );

        } catch (BusinessException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }


        String base =
                request.getRequestURI()
                        .startsWith("/admin")
                        ? "/admin/ventas"
                        : "/empleado/ventas";

        return "redirect:" + base;
    }


    // ============================================================
    // CAMBIAR ESTADO DE VENTA
    // ============================================================

    @PatchMapping({
            "/admin/ventas/{id}/estado",
            "/empleado/ventas/{id}/estado"
    })
    public String toggleEstado(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {

        try {

            ventaService.toggleEstado(id);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Estado de la venta actualizado correctamente."
            );

        } catch (BusinessException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }


        String base =
                request.getRequestURI()
                        .startsWith("/admin")
                        ? "/admin/ventas"
                        : "/empleado/ventas";

        return "redirect:" + base;
    }


    // ============================================================
    // GENERAR PDF
    // ============================================================

    @GetMapping({
            "/admin/ventas/{id}/pdf",
            "/empleado/ventas/{id}/pdf"
    })
    public ResponseEntity<byte[]> generarPdf(
            @PathVariable Long id) {

        Venta venta =
                ventaService.obtenerPorId(id);

        byte[] pdf =
                pdfService.generarPdfVenta(venta);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=venta_" + id + ".pdf"
                )
                .body(pdf);
    }


    // ============================================================
    // GENERAR EXCEL
    // ============================================================

    @GetMapping({
            "/admin/ventas/{id}/excel",
            "/empleado/ventas/{id}/excel"
    })
    public ResponseEntity<byte[]> generarExcel(
            @PathVariable Long id) {

        Venta venta =
                ventaService.obtenerPorId(id);

        byte[] excel =
                excelService.generarExcelVenta(venta);

        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        )
                )
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=Venta_" + id + ".xlsx"
                )
                .body(excel);
    }
}
