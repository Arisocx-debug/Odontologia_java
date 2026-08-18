package com.wilsonmontenegro.odontologia.service;

import com.wilsonmontenegro.odontologia.model.Cita;
import com.wilsonmontenegro.odontologia.model.Venta;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Generacion de reportes Excel. Equivalente a app/Exports/FacturaExport.php y VentaExport.php
 * (que usaban Maatwebsite\Excel + PhpSpreadsheet).
 */
@Service
public class ExcelService {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final NumberFormat FORMATO_MONEDA = NumberFormat.getNumberInstance(new Locale("es", "CO"));

    public byte[] generarExcelFactura(Cita cita) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Factura");

            CellStyle estiloTitulo = estiloTitulo(workbook);
            CellStyle estiloEtiqueta = estiloEtiqueta(workbook);

            int fila = 0;
            crearFilaTitulo(sheet, fila++, "ODONTOLOGIA DR. WILSON MONTENEGRO", estiloTitulo);
            crearFilaTitulo(sheet, fila++, "Factura de Atencion Odontologica", estiloTitulo);

            String nombrePaciente = cita.getCliente() != null && cita.getCliente().getUsuario() != null
                    ? cita.getCliente().getUsuario().getName() : "-";
            String email = cita.getCliente() != null && cita.getCliente().getUsuario() != null
                    ? cita.getCliente().getUsuario().getEmail() : "-";
            String servicioNombre = cita.getServicio() != null ? cita.getServicio().getNombre() : "-";
            String precio = cita.getServicio() != null ? "$ " + FORMATO_MONEDA.format(cita.getServicio().getCosto()) : "-";

            fila = crearFilaDato(sheet, fila, "Factura N.", "FAC-" + cita.getIdCita(), estiloEtiqueta);
            fila = crearFilaDato(sheet, fila, "Paciente", nombrePaciente, estiloEtiqueta);
            fila = crearFilaDato(sheet, fila, "Correo", email, estiloEtiqueta);
            fila = crearFilaDato(sheet, fila, "Servicio", servicioNombre, estiloEtiqueta);
            fila = crearFilaDato(sheet, fila, "Precio", precio, estiloEtiqueta);
            fila = crearFilaDato(sheet, fila, "Fecha Entrada",
                    cita.getFechaEntrada() != null ? cita.getFechaEntrada().format(FORMATO_FECHA) : "-", estiloEtiqueta);
            fila = crearFilaDato(sheet, fila, "Estado", cita.getEstado().name(), estiloEtiqueta);

            crearFilaTitulo(sheet, fila,
                    "Observacion: certifica la programacion/atencion de la cita odontologica registrada.",
                    estiloEtiqueta);

            for (int i = 0; i < 2; i++) sheet.autoSizeColumn(i);

            workbook.write(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error generando el Excel de la factura", e);
        }
    }

    public byte[] generarExcelVenta(Venta venta) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Venta");

            CellStyle estiloTitulo = estiloTitulo(workbook);
            CellStyle estiloEtiqueta = estiloEtiqueta(workbook);

            int fila = 0;
            crearFilaTitulo(sheet, fila++, "FACTURA DE VENTA", estiloTitulo);
            fila++;

            String nombreProducto = venta.getProducto() != null ? venta.getProducto().getNombre() : "Producto eliminado";

            fila = crearFilaDato(sheet, fila, "Factura N.", String.valueOf(venta.getIdVenta()), estiloEtiqueta);
            fila = crearFilaDato(sheet, fila, "Producto", nombreProducto, estiloEtiqueta);
            fila = crearFilaDato(sheet, fila, "Cantidad", String.valueOf(venta.getCantidad()), estiloEtiqueta);
            fila = crearFilaDato(sheet, fila, "Subtotal", "$ " + FORMATO_MONEDA.format(venta.getSubtotal()), estiloEtiqueta);
            fila = crearFilaDato(sheet, fila, "Descuento", "$ " + FORMATO_MONEDA.format(venta.getDescuento()), estiloEtiqueta);
            fila = crearFilaDato(sheet, fila, "Total", "$ " + FORMATO_MONEDA.format(venta.getTotal()), estiloEtiqueta);
            crearFilaDato(sheet, fila, "Fecha de compra",
                    venta.getCreatedAt() != null ? venta.getCreatedAt().format(FORMATO_FECHA) : "-", estiloEtiqueta);

            for (int i = 0; i < 2; i++) sheet.autoSizeColumn(i);

            workbook.write(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error generando el Excel de la venta", e);
        }
    }

    // ── Helpers de estilo ───────────────────────────────────────────────

    private CellStyle estiloTitulo(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle estiloEtiqueta(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private void crearFilaTitulo(Sheet sheet, int filaIndex, String texto, CellStyle style) {
        Row row = sheet.createRow(filaIndex);
        Cell cell = row.createCell(0);
        cell.setCellValue(texto);
        cell.setCellStyle(style);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(filaIndex, filaIndex, 0, 1));
    }

    private int crearFilaDato(Sheet sheet, int filaIndex, String etiqueta, String valor, CellStyle estiloEtiqueta) {
        Row row = sheet.createRow(filaIndex);
        Cell celdaEtiqueta = row.createCell(0);
        celdaEtiqueta.setCellValue(etiqueta);
        celdaEtiqueta.setCellStyle(estiloEtiqueta);
        row.createCell(1).setCellValue(valor);
        return filaIndex + 1;
    }
}
