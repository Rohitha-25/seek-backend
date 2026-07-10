package com.seek.docQuery.service;

import com.seek.docQuery.entity.Document;
import com.seek.docQuery.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DocumentUploadService {
    private final DocumentRepository documentRepository;
    private final VectorStore vectorStore;
    private final Tika tika = new Tika();

    public Document uploadDocument(MultipartFile file) {
        try {
            String text = tika.parseToString(file.getInputStream());

            Document document = new Document();
            document.setFileName(file.getOriginalFilename());
            document.setUploadTime(LocalDateTime.now());
            document = documentRepository.save(document);

            org.springframework.ai.document.Document aiDocument = new org.springframework.ai.document.Document(
                    text, Map.of("documentId", document.getId().toString())
            );

            TokenTextSplitter tts = TokenTextSplitter.builder()
                    .withChunkSize(800)
                    .build();
            List<org.springframework.ai.document.Document> chunks = tts.split(List.of(aiDocument));
            vectorStore.add(chunks);

            return document;

        } catch (Exception e) {
            throw new RuntimeException("Failed to process file: " + e.getMessage(), e);
        }
    }
}
