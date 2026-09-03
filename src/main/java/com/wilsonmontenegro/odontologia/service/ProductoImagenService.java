package com.wilsonmontenegro.odontologia.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.wilsonmontenegro.odontologia.exception.BusinessException;

@Service
public class ProductoImagenService {

    private static final Set<String> EXTENSIONES_PERMITIDAS = Set.of(
            "jpg",
            "jpeg",
            "png",
            "webp"
    );

    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final Path directorioProductos;

    public ProductoImagenService(
            @Value("${app.uploads.productos-dir:uploads/productos}") String directorioProductos) {

        this.directorioProductos = Paths.get(directorioProductos)
                .toAbsolutePath()
                .normalize();
    }

    public String guardar(MultipartFile imagen) {

        if (imagen == null || imagen.isEmpty()) {
            return null;
        }

        String nombreOriginal = imagen.getOriginalFilename();
        String extension = obtenerExtension(nombreOriginal);
        String contentType = imagen.getContentType();

        if (!EXTENSIONES_PERMITIDAS.contains(extension)) {
            throw new BusinessException(
                    "La imagen debe ser JPG, JPEG, PNG o WEBP."
            );
        }

        if (contentType != null && !TIPOS_PERMITIDOS.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new BusinessException(
                    "El tipo de archivo de la imagen no es válido."
            );
        }

        try {
            Files.createDirectories(directorioProductos);

            String nombreArchivo = UUID.randomUUID() + "." + extension;
            Path destino = directorioProductos.resolve(nombreArchivo).normalize();

            if (!destino.startsWith(directorioProductos)) {
                throw new BusinessException("Nombre de imagen inválido.");
            }

            imagen.transferTo(destino);

            return "/uploads/productos/" + nombreArchivo;

        } catch (IOException e) {
            throw new BusinessException(
                    "No fue posible guardar la imagen del producto."
            );
        }
    }

    private String obtenerExtension(String nombreOriginal) {

        if (nombreOriginal == null || nombreOriginal.isBlank()) {
            throw new BusinessException("La imagen no tiene un nombre válido.");
        }

        String nombre = Paths.get(nombreOriginal)
                .getFileName()
                .toString();

        int punto = nombre.lastIndexOf('.');

        if (punto < 0 || punto == nombre.length() - 1) {
            throw new BusinessException(
                    "La imagen debe tener extensión JPG, JPEG, PNG o WEBP."
            );
        }

        return nombre.substring(punto + 1)
                .toLowerCase(Locale.ROOT);
    }
}