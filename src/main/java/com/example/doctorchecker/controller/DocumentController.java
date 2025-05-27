package com.example.doctorchecker.controller;

import com.example.doctorchecker.dto.DocumentDTO;
import com.example.doctorchecker.service.DocumentService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService documentService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    // === Страница со списком документов ===
    @GetMapping
    public String listDocuments(Model model) {
        List<DocumentDTO> documents = documentService.getAllDocuments();
        model.addAttribute("documents", documents);
        return "document-list"; // шаблон: src/main/resources/templates/document-list.html
    }

    // === Загрузка документа ===
    @PostMapping("/upload")
    public String uploadDocument(@RequestParam("file") MultipartFile file, Model model) throws IOException {
        if (file.isEmpty()) {
            model.addAttribute("error", "Файл не выбран");
            return "redirect:/documents";
        }

        DocumentDTO uploaded = documentService.uploadDocument(file);
        model.addAttribute("uploaded", uploaded);
        return "redirect:/documents";
    }

    // === Скачивание оригинального файла ===
    @GetMapping("/download/{filename:.+}")
    public void downloadDocument(@PathVariable String filename, HttpServletResponse response) throws IOException {
        File file = documentService.getDocumentFile(filename);

        if (!file.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Файл не найден");
            return;
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                response.getOutputStream().write(buffer, 0, bytesRead);
            }
            response.getOutputStream().flush();
        }
    }

    // === Применить стиль ГОСТ и скачать ===
    @GetMapping("/apply-style/download/{id}")
    public ResponseEntity<ByteArrayResource> downloadStyledDocument(@PathVariable Long id) {
        try {
            byte[] styledDoc = documentService.applyStyleChangesAndGetBytes(id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename("document_gost.docx").build());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new ByteArrayResource(styledDoc));

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}