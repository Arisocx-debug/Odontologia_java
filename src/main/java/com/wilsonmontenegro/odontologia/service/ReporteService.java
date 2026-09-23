package com.wilsonmontenegro.odontologia.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/** Genera reportes tabulares a partir de los resultados de los filtros. */
@Service
public class ReporteService {

    public byte[] generarExcel(String titulo, String[] encabezados, List<String[]> filas) {
        try (Workbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Sheet hoja = libro.createSheet("Reporte");
            Row tituloFila = hoja.createRow(0);
            tituloFila.createCell(0).setCellValue(titulo);
            Row encabezadoFila = hoja.createRow(2);
            for (int i = 0; i < encabezados.length; i++) encabezadoFila.createCell(i).setCellValue(encabezados[i]);
            for (int i = 0; i < filas.size(); i++) {
                Row fila = hoja.createRow(i + 3);
                String[] valores = filas.get(i);
                for (int j = 0; j < valores.length; j++) fila.createCell(j).setCellValue(valores[j]);
            }
            for (int i = 0; i < encabezados.length; i++) hoja.autoSizeColumn(i);
            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("No fue posible generar el reporte Excel.", e);
        }
    }

    public byte[] generarPdf(String titulo, String[] encabezados, List<String[]> filas) {
        try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            PdfDocument pdf = new PdfDocument(new PdfWriter(salida));
            Document documento = new Document(pdf);
            documento.add(new Paragraph(titulo).setBold().setFontSize(16));
            documento.add(new Paragraph("Registros encontrados: " + filas.size()));
            Table tabla = new Table(UnitValue.createPercentArray(encabezados.length)).useAllAvailableWidth();
            for (String encabezado : encabezados) tabla.addHeaderCell(new Cell().add(new Paragraph(encabezado).setBold()));
            for (String[] fila : filas) for (String valor : fila) tabla.addCell(new Cell().add(new Paragraph(valor == null ? "-" : valor)));
            documento.add(tabla);
            documento.close();
            return salida.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("No fue posible generar el reporte PDF.", e);
        }
    }
}
