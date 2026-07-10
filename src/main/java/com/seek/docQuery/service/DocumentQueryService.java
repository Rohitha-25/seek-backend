package com.seek.docQuery.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentQueryService {
    private final VectorStore vectorStore;
    private final ChatClient.Builder chatClientBuilder;

    public String queryDocument(Long documentId, String query){
        FilterExpressionBuilder feb = new FilterExpressionBuilder();
        var filter = feb.eq("documentId", documentId.toString()).build();

        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(5)
                        .filterExpression(filter)
                        .build()
        );

        String context = results.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));

        String promptText = """
                Answer the question based only on the context provided.
                If the answer isn't in the context,
                reply:
                "Sorry, I couldn't find the answer."
                
                Context:
                %s
                
                Query:
                %s
                """
                .formatted(context, query);

        ChatClient chatClient = chatClientBuilder.build();

        return chatClient.prompt()
                .user(promptText)
                .call()
                .content();
    }
}
