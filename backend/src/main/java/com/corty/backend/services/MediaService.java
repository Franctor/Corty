package com.corty.backend.services;

import com.corty.backend.exception.CortyException;
import com.corty.backend.exception.EmptyFileException;
import com.corty.backend.exception.FileTooLargeException;
import com.corty.backend.exception.UnsupportedFileTypeException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ContentTooLargeException;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MediaService {

    private static final List<String> ALLOWED_IMAGE_TYPES = List.of("image/jpeg", "image/png", "image/webp");
    private static final List<String> ALLOWED_DOC_TYPES   = List.of("application/pdf");
    private static final long MAX_SIZE_BYTES = 10 * 1024 * 1024; // 10MB // 5MB

    @Value("${corty.media.upload-dir}")
    private String uploadDir;

    // Uploads any allowed file to the given folder and returns its public URL
    public String uploadFile(MultipartFile file, String folder) {
        validateFile(file);

        String extension = getExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + "." + extension;
        Path destination  = Paths.get(uploadDir, folder).resolve(filename);

        try {
            Files.createDirectories(destination.getParent());
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new CortyException("Error al guardar el archivo", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return "/" + folder + "/" + filename;
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new EmptyFileException("El archivo está vacío");
        }
        List<String> allAllowed = new ArrayList<>(ALLOWED_IMAGE_TYPES);
        if (!allAllowed.contains(file.getContentType())) {
            throw new UnsupportedFileTypeException("Formato no permitido. Usa JPG, PNG o WEBP");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new FileTooLargeException("La imagen no puede superar los 5MB");
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "bin";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}