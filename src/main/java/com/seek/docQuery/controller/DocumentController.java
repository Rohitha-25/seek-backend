package com.seek.docQuery.controller;

import com.seek.docQuery.dto.QueryRequest;
import com.seek.docQuery.dto.QueryResponse;
import com.seek.docQuery.entity.Document;
import com.seek.docQuery.repository.DocumentRepository;
import com.seek.docQuery.service.DocumentQueryService;
import com.seek.docQuery.service.DocumentUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class DocumentController {
    private final DocumentUploadService documentUploadService;
    private final DocumentQueryService documentQueryService;
    private final DocumentRepository documentRepository;

    @PostMapping("/upload")
    public Document uploadDocument(@RequestParam("file") MultipartFile file) {
        return documentUploadService.uploadDocument(file);
    }

    @GetMapping
    public List<Document> getDocuments() {
        return documentRepository.findAll();
    }

    @PostMapping("/{id}/query")
    public QueryResponse queryDocument(@PathVariable Long id, @RequestBody QueryRequest request) {
        if (request.query() == null || request.query().isBlank()) {
            throw new IllegalArgumentException("query field is required");
        }
        String answer = documentQueryService.queryDocument(id, request.query());
        return new QueryResponse(answer);
    }

    @DeleteMapping("/{id}")
    public void deleteDocument(@PathVariable Long id) {
        documentRepository.deleteById(id);
    }
}
