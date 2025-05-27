package com.example.doctorchecker.service;

import com.example.doctorchecker.dto.DocumentDTO;
import com.example.doctorchecker.model.Document;
import com.example.doctorchecker.repository.DocumentRepository;
import com.example.doctorchecker.util.StyleApplier;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final DocumentRepository documentRepository;

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public List<DocumentDTO> getAllDocuments() {
        return documentRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public DocumentDTO uploadDocument(MultipartFile file) throws IOException {
        if (!file.isEmpty()) {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalName = file.getOriginalFilename();
            String storedFileName = System.currentTimeMillis() + "-" + originalName;

            // Сохраняем оригинальный файл
            try (FileOutputStream fos = new FileOutputStream(uploadDir + File.separator + storedFileName);
                 OPCPackage opcPackage = OPCPackage.open(file.getInputStream())) {
                XWPFDocument doc = new XWPFDocument(opcPackage);
                doc.write(fos);
            } catch (InvalidFormatException e) {
                throw new RuntimeException(e);
            }

            Document document = new Document();
            document.setOriginalName(originalName);
            document.setStoredFileName(storedFileName);
            document.setUploadDate(LocalDateTime.now());

            document = documentRepository.save(document);

            return convertToDTO(document);
        }
        return null;
    }

    public File getDocumentFile(String storedFileName) {
        return new File(uploadDir + File.separator + storedFileName);
    }

    public byte[] applyStyleChangesAndGetBytes(Long documentId) throws IOException {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Документ не найден"));

        File file = getDocumentFile(document.getStoredFileName());

        try (OPCPackage opcPackage = OPCPackage.open(file)) {
            XWPFDocument doc = new XWPFDocument(opcPackage);

            // Применяем стили
            StyleApplier.applyStyles(doc);
            StyleApplier.setMargins(doc);

            // Записываем в ByteArrayOutputStream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            doc.write(outputStream);
            return outputStream.toByteArray();
        } catch (InvalidFormatException e) {
            throw new RuntimeException(e);
        }
    }

    private DocumentDTO convertToDTO(Document document) {
        return new DocumentDTO(
                document.getId(),
                document.getOriginalName(),
                document.getStoredFileName(),
                document.getUploadDate().toString()
        );
    }
}