package com.wilsonmontenegro.odontologia.service;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Genera reportes tabulares a partir de los resultados de los filtros. */
@Service
public class ReporteService {

    private static final DeviceRgb AZUL_MARCA = new DeviceRgb(13, 110, 253);
    private static final DeviceRgb AZUL_OSCURO = new DeviceRgb(8, 60, 140);
    private static final DeviceRgb AZUL_SUAVE = new DeviceRgb(232, 242, 254);
    private static final DeviceRgb GRIS_TEXTO = new DeviceRgb(73, 80, 87);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String CLINICA = "ODONTOLOGÍA DR. WILSON MONTENEGRO";

    public byte[] generarExcel(String titulo, String[] encabezados, List<String[]> filas) {
        return generarExcel(titulo, encabezados, filas, Map.of());
    }

    public byte[] generarExcel(String titulo, String[] encabezados, List<String[]> filas, Map<String, String> criterios) {
        try (XSSFWorkbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Sheet hoja = libro.createSheet("Reporte");
            int columnas = Math.max(encabezados.length, 2);
            EstilosExcel estilos = new EstilosExcel(libro);

            int fila = 0;
            fila = escribirBanner(hoja, fila, columnas, CLINICA, estilos.banner);
            fila = escribirBanner(hoja, fila, columnas, titulo, estilos.subtitulo);
            fila = escribirMeta(hoja, fila, columnas, "Fecha de generación",
                    LocalDateTime.now().format(FORMATO_FECHA_HORA), estilos);
            fila = escribirMeta(hoja, fila, columnas, "Registros encontrados",
                    String.valueOf(filas.size()), estilos);
            fila++;
            fila = escribirCriterios(hoja, fila, columnas, criterios, estilos);
            fila++;

            int filaEncabezado = fila;
            Row encabezadoFila = hoja.createRow(fila++);
            encabezadoFila.setHeightInPoints(22);
            for (int i = 0; i < encabezados.length; i++) {
                org.apache.poi.ss.usermodel.Cell celda = encabezadoFila.createCell(i);
                celda.setCellValue(encabezados[i]);
                celda.setCellStyle(estilos.encabezadoTabla);
            }

            for (int i = 0; i < filas.size(); i++) {
                Row filaDatos = hoja.createRow(fila++);
                filaDatos.setHeightInPoints(18);
                String[] valores = filas.get(i);
                XSSFCellStyle estiloFila = i % 2 == 0 ? estilos.celdaClara : estilos.celdaZebra;
                for (int j = 0; j < encabezados.length; j++) {
                    org.apache.poi.ss.usermodel.Cell celda = filaDatos.createCell(j);
                    celda.setCellValue(texto(j < valores.length ? valores[j] : null));
                    celda.setCellStyle(estiloFila);
                }
            }

            if (filas.isEmpty()) {
                Row vacia = hoja.createRow(fila++);
                org.apache.poi.ss.usermodel.Cell celda = vacia.createCell(0);
                celda.setCellValue("No hay registros para los filtros seleccionados.");
                celda.setCellStyle(estilos.nota);
                if (columnas > 1) {
                    hoja.addMergedRegion(new CellRangeAddress(vacia.getRowNum(), vacia.getRowNum(), 0, columnas - 1));
                }
            }

            fila++;
            Row pie = hoja.createRow(fila);
            org.apache.poi.ss.usermodel.Cell celdaPie = pie.createCell(0);
            celdaPie.setCellValue("Documento generado automáticamente por el sistema de gestión odontológica.");
            celdaPie.setCellStyle(estilos.pie);
            if (columnas > 1) {
                hoja.addMergedRegion(new CellRangeAddress(fila, fila, 0, columnas - 1));
            }

            hoja.setAutoFilter(new CellRangeAddress(filaEncabezado, Math.max(filaEncabezado, filaEncabezado + filas.size()), 0, encabezados.length - 1));
            hoja.createFreezePane(0, filaEncabezado + 1);
            ajustarAnchos(hoja, encabezados, filas);
            hoja.setDisplayGridlines(false);
            hoja.setPrintGridlines(false);
            hoja.setFitToPage(true);
            hoja.setHorizontallyCenter(true);
            PrintSetup impresion = hoja.getPrintSetup();
            impresion.setLandscape(true);
            impresion.setFitWidth((short) 1);
            impresion.setFitHeight((short) 0);
            hoja.setRepeatingRows(CellRangeAddress.valueOf("1:" + (filaEncabezado + 1)));
            hoja.getFooter().setLeft("&B" + CLINICA);
            hoja.getFooter().setRight("Página &P de &N");

            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("No fue posible generar el reporte Excel.", e);
        }
    }

    public byte[] generarPdf(String titulo, String[] encabezados, List<String[]> filas) {
        return generarPdf(titulo, encabezados, filas, Map.of());
    }

    public byte[] generarPdf(String titulo, String[] encabezados, List<String[]> filas, Map<String, String> criterios) {
        try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            PdfDocument pdf = new PdfDocument(new PdfWriter(salida));
            Document documento = new Document(pdf);
            agregarCabeceraInstitucional(documento, titulo);
            documento.add(new Paragraph("Registros encontrados: " + filas.size())
                    .setFontSize(10).setFontColor(GRIS_TEXTO).setMarginBottom(6));
            agregarCriteriosPdf(documento, criterios);
            Table tabla = new Table(UnitValue.createPercentArray(encabezados.length)).useAllAvailableWidth();
            for (String encabezado : encabezados) {
                tabla.addHeaderCell(new Cell().add(new Paragraph(encabezado).setBold().setFontColor(ColorConstants.WHITE).setFontSize(9))
                        .setBackgroundColor(AZUL_MARCA).setPadding(5));
            }
            int indice = 0;
            for (String[] fila : filas) {
                for (int i = 0; i < encabezados.length; i++) {
                    String valor = i < fila.length ? texto(fila[i]) : "—";
                    tabla.addCell(new Cell().add(new Paragraph(valor).setFontSize(8))
                            .setBackgroundColor(indice % 2 == 0 ? ColorConstants.WHITE : AZUL_SUAVE)
                            .setPadding(4));
                }
                indice++;
            }
            if (filas.isEmpty()) {
                Cell vacia = new Cell(1, encabezados.length)
                        .add(new Paragraph("No hay registros para los filtros seleccionados.").setFontSize(9))
                        .setTextAlignment(TextAlignment.CENTER)
                        .setPadding(8);
                tabla.addCell(vacia);
            }
            documento.add(tabla);
            documento.close();
            return salida.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("No fue posible generar el reporte PDF.", e);
        }
    }

    /** Construye el mapa de filtros omitiendo valores vacíos. */
    public static Map<String, String> filtros(Object... pares) {
        Map<String, String> mapa = new LinkedHashMap<>();
        for (int i = 0; i + 1 < pares.length; i += 2) {
            String clave = String.valueOf(pares[i]);
            Object valor = pares[i + 1];
            if (valor == null) continue;
            String texto = String.valueOf(valor).trim();
            if (texto.isEmpty() || "null".equalsIgnoreCase(texto)) continue;
            mapa.put(clave, texto);
        }
        return mapa;
    }

    private int escribirBanner(Sheet hoja, int fila, int columnas, String texto, XSSFCellStyle estilo) {
        Row row = hoja.createRow(fila);
        row.setHeightInPoints(28);
        org.apache.poi.ss.usermodel.Cell celda = row.createCell(0);
        celda.setCellValue(texto);
        celda.setCellStyle(estilo);
        if (columnas > 1) {
            CellRangeAddress region = new CellRangeAddress(fila, fila, 0, columnas - 1);
            hoja.addMergedRegion(region);
            aplicarBorde(hoja, region);
        }
        for (int i = 1; i < columnas; i++) {
            org.apache.poi.ss.usermodel.Cell extra = row.createCell(i);
            extra.setCellStyle(estilo);
        }
        return fila + 1;
    }

    private int escribirMeta(Sheet hoja, int fila, int columnas, String etiqueta, String valor, EstilosExcel estilos) {
        Row row = hoja.createRow(fila);
        row.setHeightInPoints(18);
        org.apache.poi.ss.usermodel.Cell celdaEtiqueta = row.createCell(0);
        celdaEtiqueta.setCellValue(etiqueta);
        celdaEtiqueta.setCellStyle(estilos.etiqueta);
        org.apache.poi.ss.usermodel.Cell celdaValor = row.createCell(1);
        celdaValor.setCellValue(valor);
        celdaValor.setCellStyle(estilos.valor);
        if (columnas > 2) {
            hoja.addMergedRegion(new CellRangeAddress(fila, fila, 1, columnas - 1));
            for (int i = 2; i < columnas; i++) row.createCell(i).setCellStyle(estilos.valor);
        }
        return fila + 1;
    }

    private int escribirCriterios(Sheet hoja, int fila, int columnas, Map<String, String> criterios, EstilosExcel estilos) {
        Row titulo = hoja.createRow(fila);
        titulo.setHeightInPoints(20);
        org.apache.poi.ss.usermodel.Cell celdaTitulo = titulo.createCell(0);
        celdaTitulo.setCellValue("Filtros aplicados");
        celdaTitulo.setCellStyle(estilos.seccion);
        if (columnas > 1) {
            hoja.addMergedRegion(new CellRangeAddress(fila, fila, 0, columnas - 1));
            for (int i = 1; i < columnas; i++) titulo.createCell(i).setCellStyle(estilos.seccion);
        }
        fila++;

        Map<String, String> visibles = criterios == null || criterios.isEmpty()
                ? Map.of("Criterios", "Sin filtros (todos los registros)")
                : criterios;
        for (Map.Entry<String, String> criterio : visibles.entrySet()) {
            Row row = hoja.createRow(fila);
            row.setHeightInPoints(18);
            org.apache.poi.ss.usermodel.Cell etiqueta = row.createCell(0);
            etiqueta.setCellValue(criterio.getKey());
            etiqueta.setCellStyle(estilos.etiqueta);
            org.apache.poi.ss.usermodel.Cell valor = row.createCell(1);
            valor.setCellValue(criterio.getValue());
            valor.setCellStyle(estilos.valor);
            if (columnas > 2) {
                hoja.addMergedRegion(new CellRangeAddress(fila, fila, 1, columnas - 1));
                for (int i = 2; i < columnas; i++) row.createCell(i).setCellStyle(estilos.valor);
            }
            fila++;
        }
        return fila;
    }

    private void ajustarAnchos(Sheet hoja, String[] encabezados, List<String[]> filas) {
        for (int i = 0; i < encabezados.length; i++) {
            int max = encabezados[i] == null ? 12 : encabezados[i].length();
            for (String[] fila : filas) {
                if (i < fila.length && fila[i] != null) {
                    max = Math.max(max, fila[i].length());
                }
            }
            int ancho = Math.min(48, Math.max(14, max + 4)) * 256;
            hoja.setColumnWidth(i, ancho);
        }
    }

    private void aplicarBorde(Sheet hoja, CellRangeAddress region) {
        RegionUtil.setBorderBottom(BorderStyle.THIN, region, hoja);
        RegionUtil.setBorderTop(BorderStyle.THIN, region, hoja);
        RegionUtil.setBorderLeft(BorderStyle.THIN, region, hoja);
        RegionUtil.setBorderRight(BorderStyle.THIN, region, hoja);
    }

    private void agregarCriteriosPdf(Document documento, Map<String, String> criterios) {
        Map<String, String> visibles = criterios == null || criterios.isEmpty()
                ? Map.of("Criterios", "Sin filtros (todos los registros)")
                : criterios;
        documento.add(new Paragraph("Filtros aplicados").setBold().setFontSize(10).setFontColor(AZUL_OSCURO).setMarginBottom(4));
        for (Map.Entry<String, String> criterio : visibles.entrySet()) {
            documento.add(new Paragraph(criterio.getKey() + ": " + criterio.getValue())
                    .setFontSize(9).setFontColor(GRIS_TEXTO).setMarginBottom(1));
        }
        documento.add(new Paragraph(" ").setMarginBottom(8));
    }

    private void agregarCabeceraInstitucional(Document documento, String titulo) throws IOException {
        Table cabecera = new Table(UnitValue.createPercentArray(new float[]{1, 4})).useAllAvailableWidth();
        Cell celdaLogo = new Cell().setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
        try (var flujoLogo = new ClassPathResource("static/img/WILSON.png").getInputStream()) {
            Image imagenLogo = new Image(ImageDataFactory.create(flujoLogo.readAllBytes())).scaleToFit(58, 58);
            celdaLogo.add(imagenLogo);
        } catch (Exception ignored) {
            celdaLogo.add(new Paragraph("WM").setBold().setFontColor(AZUL_MARCA).setFontSize(18));
        }
        cabecera.addCell(celdaLogo);
        cabecera.addCell(new Cell()
                .add(new Paragraph(CLINICA).setBold().setFontSize(16).setFontColor(AZUL_MARCA))
                .add(new Paragraph(titulo).setBold().setFontSize(12))
                .add(new Paragraph("Fecha de generación: " + java.time.LocalDate.now().format(FORMATO_FECHA))
                        .setFontSize(9).setMarginTop(3).setFontColor(GRIS_TEXTO))
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
        documento.add(cabecera);
        documento.add(new Paragraph("").setBorderBottom(new com.itextpdf.layout.borders.SolidBorder(AZUL_MARCA, 1))
                .setMarginTop(4).setMarginBottom(10));
    }

    private static String texto(String valor) {
        return valor == null || valor.isBlank() ? "—" : valor;
    }

    private static final class EstilosExcel {
        private final XSSFCellStyle banner;
        private final XSSFCellStyle subtitulo;
        private final XSSFCellStyle seccion;
        private final XSSFCellStyle etiqueta;
        private final XSSFCellStyle valor;
        private final XSSFCellStyle encabezadoTabla;
        private final XSSFCellStyle celdaClara;
        private final XSSFCellStyle celdaZebra;
        private final XSSFCellStyle nota;
        private final XSSFCellStyle pie;

        private EstilosExcel(XSSFWorkbook libro) {
            XSSFColor azul = color(13, 110, 253);
            XSSFColor azulOscuro = color(8, 60, 140);
            XSSFColor azulSuave = color(232, 242, 254);
            XSSFColor blanco = color(255, 255, 255);
            XSSFColor grisFondo = color(248, 249, 250);
            XSSFColor grisTexto = color(73, 80, 87);

            banner = fondo(libro, azulOscuro, blanco, true, 16, HorizontalAlignment.CENTER);
            subtitulo = fondo(libro, azul, blanco, true, 13, HorizontalAlignment.CENTER);
            seccion = fondo(libro, azulSuave, azulOscuro, true, 11, HorizontalAlignment.LEFT);
            etiqueta = fondo(libro, grisFondo, grisTexto, true, 10, HorizontalAlignment.LEFT);
            valor = fondo(libro, blanco, grisTexto, false, 10, HorizontalAlignment.LEFT);
            encabezadoTabla = fondo(libro, azul, blanco, true, 10, HorizontalAlignment.CENTER);
            celdaClara = fondo(libro, blanco, grisTexto, false, 10, HorizontalAlignment.LEFT);
            celdaZebra = fondo(libro, azulSuave, grisTexto, false, 10, HorizontalAlignment.LEFT);
            nota = fondo(libro, grisFondo, grisTexto, false, 10, HorizontalAlignment.CENTER);
            pie = fondo(libro, blanco, grisTexto, false, 8, HorizontalAlignment.LEFT);
        }

        private static XSSFColor color(int r, int g, int b) {
            return new XSSFColor(new byte[]{(byte) r, (byte) g, (byte) b}, new DefaultIndexedColorMap());
        }

        private static XSSFCellStyle fondo(XSSFWorkbook libro, XSSFColor fondo, XSSFColor texto,
                boolean negrita, int tamano, HorizontalAlignment alineacion) {
            XSSFCellStyle estilo = libro.createCellStyle();
            XSSFFont fuente = libro.createFont();
            fuente.setFontName("Calibri");
            fuente.setBold(negrita);
            fuente.setFontHeightInPoints((short) tamano);
            fuente.setColor(texto);
            estilo.setFont(fuente);
            estilo.setFillForegroundColor(fondo);
            estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            estilo.setAlignment(alineacion);
            estilo.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
            estilo.setBorderBottom(BorderStyle.THIN);
            estilo.setBorderTop(BorderStyle.THIN);
            estilo.setBorderLeft(BorderStyle.THIN);
            estilo.setBorderRight(BorderStyle.THIN);
            estilo.setWrapText(true);
            return estilo;
        }
    }
}
