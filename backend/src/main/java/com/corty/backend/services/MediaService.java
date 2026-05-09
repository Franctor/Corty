package com.corty.backend.services;

import com.corty.backend.exception.EmptyFileException;
import com.corty.backend.exception.FileStorageException;
import com.corty.backend.exception.FileTooLargeException;
import com.corty.backend.exception.UnsupportedFileTypeException;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class MediaService {

    private static final List<String> ALLOWED_IMAGE_TYPES = List.of("image/jpeg", "image/png", "image/webp", "image/svg+xml");
    private static final long MAX_SIZE_BYTES = 10 * 1024 * 1024;

    // Elementos SVG que pueden ejecutar código
    private static final Set<String> DANGEROUS_ELEMENTS = Set.of(
            "script", "foreignObject", "use", "animate", "set",
            "animateTransform", "animateMotion", "animateColor"
    );

    // Atributos que pueden ejecutar JS (on*)
    private static final String EVENT_ATTR_PREFIX = "on";

    // Atributos que pueden contener URLs con javascript:
    private static final Set<String> URL_ATTRS = Set.of("href", "xlink:href", "src", "action", "data");

    @Value("${corty.media.upload-dir}")
    private String uploadDir;

    public String uploadFile(MultipartFile file, String folder) {
        return uploadFile(file, folder, null);
    }

    public String uploadFile(MultipartFile file, String folder, Long entityId) {
        validateFile(file);

        String extension  = extensionFromContentType(resolveContentType(file));
        String filename   = (entityId != null ? entityId.toString() : UUID.randomUUID().toString()) + "." + extension;
        Path   folderPath = Paths.get(uploadDir, folder);
        Path   destination = folderPath.resolve(filename);

        try {
            Files.createDirectories(folderPath);
            if (entityId != null) deletePreviousFiles(folderPath, entityId.toString());

            if ("svg".equals(extension)) {
                saveSanitizedSvg(file, destination);
            } else {
                Thumbnails.of(file.getInputStream())
                        .size(1024, 1024)
                        .keepAspectRatio(true)
                        .outputFormat(extension.equals("jpg") ? "jpeg" : extension)
                        .outputQuality(0.80)
                        .toFile(destination.toFile());
            }
        } catch (IOException e) {
            throw new FileStorageException("Error al procesar la imagen");
        }

        return "/" + folder + "/" + filename;
    }

    private void saveSanitizedSvg(MultipartFile file, Path destination) throws IOException {
        try {
            var factory = DocumentBuilderFactory.newInstance();
            // Deshabilitar DTD y entidades externas (XXE)
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setExpandEntityReferences(false);

            Document doc = factory.newDocumentBuilder().parse(file.getInputStream());
            sanitizeSvgNode(doc.getDocumentElement());

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(new DOMSource(doc), new StreamResult(destination.toFile()));

        } catch (ParserConfigurationException | SAXException | TransformerException e) {
            throw new FileStorageException("El archivo SVG no es válido o contiene contenido no permitido");
        }
    }

    private void sanitizeSvgNode(Node node) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            String tag = node.getLocalName() != null ? node.getLocalName().toLowerCase() : node.getNodeName().toLowerCase();

            if (DANGEROUS_ELEMENTS.contains(tag)) {
                node.getParentNode().removeChild(node);
                return;
            }

            NamedNodeMap attrs = node.getAttributes();
            List<Attr> toRemove = new java.util.ArrayList<>();
            for (int i = 0; i < attrs.getLength(); i++) {
                Attr attr = (Attr) attrs.item(i);
                String name  = attr.getName().toLowerCase();
                String value = attr.getValue().toLowerCase().replaceAll("\\s", "");

                if (name.startsWith(EVENT_ATTR_PREFIX) || URL_ATTRS.contains(name) && value.startsWith("javascript:")) {
                    toRemove.add(attr);
                }
            }
            toRemove.forEach(a -> attrs.removeNamedItem(a.getName()));
        }

        NodeList children = node.getChildNodes();
        // Iterar en reversa porque podemos eliminar nodos durante el recorrido
        for (int i = children.getLength() - 1; i >= 0; i--) {
            sanitizeSvgNode(children.item(i));
        }
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
        if (!ALLOWED_IMAGE_TYPES.contains(resolveContentType(file))) {
            throw new UnsupportedFileTypeException("Formato no permitido. Usa JPG, PNG, WEBP o SVG");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new FileTooLargeException("La imagen no puede superar los 5MB");
        }
    }

    private String resolveContentType(MultipartFile file) {
        String ct = file.getContentType();
        if (ct != null && !ct.equals("application/octet-stream") && ALLOWED_IMAGE_TYPES.contains(ct)) {
            return ct;
        }
        String name = file.getOriginalFilename();
        if (name != null) {
            String lower = name.toLowerCase();
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
            if (lower.endsWith(".png"))  return "image/png";
            if (lower.endsWith(".webp")) return "image/webp";
            if (lower.endsWith(".svg"))  return "image/svg+xml";
        }
        return ct != null ? ct : "";
    }

    private String extensionFromContentType(String contentType) {
        return switch (contentType) {
            case "image/jpeg"    -> "jpg";
            case "image/png"     -> "png";
            case "image/webp"    -> "webp";
            case "image/svg+xml" -> "svg";
            default              -> "jpg";
        };
    }
}
