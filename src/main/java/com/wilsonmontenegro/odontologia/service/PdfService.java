package com.wilsonmontenegro.odontologia.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.wilsonmontenegro.odontologia.model.Cita;
import com.wilsonmontenegro.odontologia.model.Venta;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Generacion de reportes PDF. Equivalente al uso de Barryvdh\DomPDF\Facade\Pdf en Laravel
 * (metodos generarPdf() de AdminCitaController, EmpleadoCitaController, ClienteCitaController y VentaController).
 */
@Service
@RequiredArgsConstructor
public class PdfService {

    private static final DeviceRgb AZUL_MARCA = new DeviceRgb(13, 110, 253);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final NumberFormat FORMATO_MONEDA = NumberFormat.getNumberInstance(new Locale("es", "CO"));

    public byte[] generarPdfCita(Cita cita) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfDocument pdfDoc = new PdfDocument(new PdfWriter(baos));
            Document document = new Document(pdfDoc);

            document.add(tituloPrincipal("ODONTOLOGIA DR. WILSON MONTENEGRO"));
            document.add(subtitulo("Factura de Atencion Odontologica"));

            Table tabla = new Table(UnitValue.createPercentArray(new float[]{1, 2})).useAllAvailableWidth();
            String nombrePaciente = cita.getCliente() != null && cita.getCliente().getUsuario() != null
                    ? cita.getCliente().getUsuario().getName() : "-";
            String email = cita.getCliente() != null && cita.getCliente().getUsuario() != null
                    ? cita.getCliente().getUsuario().getEmail() : "-";
            String servicioNombre = cita.getServicio() != null ? cita.getServicio().getNombre() : "-";
            String precio = cita.getServicio() != null
                    ? "$ " + FORMATO_MONEDA.format(cita.getServicio().getCosto()) : "-";

            agregarFila(tabla, "Factura N.", "FAC-" + cita.getIdCita());
            agregarFila(tabla, "Paciente", nombrePaciente);
            agregarFila(tabla, "Correo", email);
            agregarFila(tabla, "Servicio", servicioNombre);
            agregarFila(tabla, "Precio", precio);
            agregarFila(tabla, "Fecha Entrada", cita.getFechaEntrada() != null ? cita.getFechaEntrada().format(FORMATO_FECHA) : "-");
            agregarFila(tabla, "Estado", cita.getEstado().name());

            document.add(tabla);
            document.add(new Paragraph(
                    "Observacion: Este documento certifica la programacion y/o atencion de la cita "
                            + "odontologica registrada en el sistema.")
                    .setMarginTop(15).setBold());

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando el PDF de la cita", e);
        }
    }

    public byte[] generarPdfVenta(Venta venta) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfDocument pdfDoc = new PdfDocument(new PdfWriter(baos));
            Document document = new Document(pdfDoc);

            document.add(tituloPrincipal("FACTURA DE VENTA"));

            Table tabla = new Table(UnitValue.createPercentArray(new float[]{1, 2})).useAllAvailableWidth();
            String nombreProducto = venta.getProducto() != null ? venta.getProducto().getNombre() : "Producto eliminado";

            agregarFila(tabla, "Factura N.", String.valueOf(venta.getIdVenta()));
            agregarFila(tabla, "Producto", nombreProducto);
            agregarFila(tabla, "Cantidad", String.valueOf(venta.getCantidad()));
            agregarFila(tabla, "Subtotal", "$ " + FORMATO_MONEDA.format(venta.getSubtotal()));
            agregarFila(tabla, "Descuento", "$ " + FORMATO_MONEDA.format(venta.getDescuento()));
            agregarFila(tabla, "Total", "$ " + FORMATO_MONEDA.format(venta.getTotal()));
            agregarFila(tabla, "Fecha de compra",
                    venta.getCreatedAt() != null ? venta.getCreatedAt().format(FORMATO_FECHA) : "-");

            document.add(tabla);
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando el PDF de la venta", e);
        }
    }

    private Paragraph tituloPrincipal(String texto) {
        return new Paragraph(texto)
                .setBold()
                .setFontSize(18)
                .setFontColor(ColorConstants.WHITE)
                .setBackgroundColor(AZUL_MARCA)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(8);
    }

    private Paragraph subtitulo(String texto) {
        return new Paragraph(texto)
                .setBold()
                .setFontSize(13)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(15);
    }

    private void agregarFila(Table tabla, String etiqueta, String valor) {
        tabla.addCell(new Cell().add(new Paragraph(etiqueta).setBold())
                .setBackgroundColor(new DeviceRgb(217, 234, 247)));
        tabla.addCell(new Cell().add(new Paragraph(valor != null ? valor : "-")));
    }
}
