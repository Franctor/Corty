package com.corty.backend.services;

import com.corty.backend.exception.EmptyFileException;
import com.corty.backend.exception.FileStorageException;
import com.corty.backend.exception.FileTooLargeException;
import com.corty.backend.exception.UnsupportedFileTypeException;
import org.springframework.beans.factory.annotation.Value;
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

    public String uploadFile(MultipartFile file, String folder) {
        return uploadFile(file, folder, null);
    }

    public String uploadFile(MultipartFile file, String folder, Long entityId) {
        validateFile(file);

        String extension  = getExtension(file.getOriginalFilename());
        String filename   = (entityId != null ? entityId.toString() : UUID.randomUUID().toString()) + "." + extension;
        Path   folderPath = Paths.get(uploadDir, folder);
        Path   destination = folderPath.resolve(filename);

        try {
            Files.createDirectories(folderPath);
            if (entityId != null) deletePreviousFiles(folderPath, entityId.toString());
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new FileStorageException("Error al guardar el archivo");
        }

        return "/" + folder + "/" + filename;
    }

    private void deletePreviousFiles(Path folderPath, String baseName) throws IOException {
        if (!Files.exists(folderPath)) return;
        try (var stream = Files.list(folderPath)) {
            stream.filter(p -> {
                String name = p.getFileName().toString();
                int dot = name.lastIndexOf('.');
                return dot > 0 && name.substring(0, dot).equals(baseName);
            }).forEach(p -> {
                try { Files.deleteIfExists(p); } catch (IOException ignored) {}
            });
        }
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
        final String extension;
        if (filename == null || !filename.contains(".")) {
            extension = "bin";
        } else {
            extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        }
        return extension;
    }
}