## 📄 seek — backend

Spring Boot + Java backend for seek, a RAG (Retrieval-Augmented Generation) application that lets users upload documents and ask natural-language questions about their content.

### Workflow

Backend implements the RAG (Retrieval-Augmented Generation) pattern: retrieving the most relevant sections of an uploaded document, then handing that context to a generative AI model to produce a grounded answer.

#### Upload Flow

```
File uploaded via POST /api/documents/upload
      ↓
Apache Tika extracts raw text (PDF, DOCX, PPTX, TXT)
      ↓
Text is split into chunks (~800 tokens each)
      ↓
Each chunk is embedded (converted into a vector) via Gemini's embedding model
      ↓
Chunks + vectors are stored in an in-memory vector store
      ↓
File metadata (name, upload time) is saved to PostgreSQL
```

#### Query Flow

```
Question sent via POST /api/documents/{id}/query
      ↓
Question is embedded into a vector
      ↓
Similarity search finds the top 5 most relevant chunks (filtered to that document)
      ↓
Chunks are joined into a "context" block
      ↓
A prompt (instructions + context + question) is sent to Gemini's chat model
      ↓
Model generates a grounded answer, returned as JSON
```

### Tech Stack

<table>
  <tr>
    <th>Purpose</th>
    <th>Technology</th>
  </tr>
  <tr>
    <td>Language/Runtime</td>
    <td>Java 17</td>
  </tr>
  <tr>
    <td>Framework</td>
    <td>Spring Boot, Maven</td>
  </tr>
  <tr>
    <td>AI orchestration</td>
    <td>Spring AI</td>
  </tr>
  <tr>
    <td>Text extraction</td>
    <td>Apache Tika</td>
  </tr>
  <tr>
    <td>Embeddings + Chat Model</td>
    <td>Google Gemini (text-embedding-004, gemini-2.5-flash)</td>
  </tr>
  <tr>
    <td>Vector store</td>
    <td>Spring AI SimpleVectorStore (in-memory)</td>
  </tr>
  <tr>
    <td>Database</td>
    <td>PostgreSQL (document metadata only)</td>
  </tr>
</table>

### Project Structure

```
seek-backend
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── seek
│   │   │           └── docQuery
│   │   │               ├── config
│   │   │               │   └── VectorStoreConfig.java
│   │   │               ├── controller
│   │   │               │   └── DocumentController.java
│   │   │               ├── dto
│   │   │               │   ├── QueryRequest.java
│   │   │               │   └── QueryResponse.java
│   │   │               ├── entity
│   │   │               │   └── Document.java
│   │   │               ├── exception
│   │   │               │   └── GlobalExceptionHandler.java
│   │   │               ├── repository
│   │   │               │   └── DocumentRepository.java
│   │   │               ├── service
│   │   │               │   ├── DocumentQueryService.java
│   │   │               │   └── DocumentUploadService.java
│   │   │               └── SeekApplication.java
│   │   └── resources
│   │       └── application.properties
├── pom.xml
├── mvnw
└── mvnw.cmd
```

<table>
  <tr>
    <th>File</th>
    <th>Responsibility</th>
  </tr>
  <tr>
    <td>Document.java</td>
    <td>JPA entity mapping to the documents Postgres table (filename, upload time).</td>
  </tr>
  <tr>
    <td>DocumentRepository.java</td>
    <td>Spring Data JPA interface — provides CRUD operations with no hand-written SQL.</td>
  </tr>
  <tr>
    <td>DocumentUploadService.java</td>
    <td>Handles file upload: text extraction, chunking, embedding, storage.</td>
  </tr>
  <tr>
    <td>DocumentQueryService.java</td>
    <td>Handles query answering: embedding, similarity search, prompt construction, generation.</td>
  </tr>
  <tr>
    <td>QueryRequest.java</td>
    <td>Java record defining the shape of the incoming JSON body on query: { "query": "..." }.</td>
  </tr>
  <tr>
    <td>QueryResponse.java</td>
    <td>Java record defining the shape of the outgoing JSON response: { "answer": "..." }.</td>
  </tr>
  <tr>
    <td>DocumentController.java</td>
    <td>REST API layer exposing upload, list, query, and delete endpoints.</td>
  </tr>
  <tr>
    <td>VectorStoreConfig.java</td>
    <td>Manually configures the SimpleVectorStore bean.</td>
  </tr>
  <tr>
    <td>GlobalExceptionHandler.java</td>
    <td>Converts exceptions into clean JSON error responses.</td>
  </tr>
</table>

#### Why Spring AI?

Spring AI provides vendor-agnostic abstractions (EmbeddingModel, ChatModel, VectorStore) so the application logic doesn't depend on any single AI provider's SDK.

### API Endpoints

<table>
  <tr>
    <th>Method</th>
    <th>Endpoint</th>
    <th>Description</th>
  </tr>
  <tr>
    <td>POST</td>
    <td>/api/documents/upload</td>
    <td>Upload a file (multipart form-data, field name file).</td>
  </tr>
  <tr>
    <td>GET</td>
    <td>/api/documents</td>
    <td>List all uploaded documents.</td>
  </tr>
  <tr>
    <td>POST</td>
    <td>/api/documents/{id}/query</td>
    <td>Ask a question about a specific document — body: { "query": "..." }.</td>
  </tr>
  <tr>
    <td>DELETE</td>
    <td>/api/documents/{id}</td>
    <td>Delete a document's metadata record.</td>
  </tr>
</table>

### Local Setup

```
• Java 17
• Maven
• PostgreSQL running locally
• Google Gemini API key
```

#### Steps:

```
# create the database
psql -U postgres -c "CREATE DATABASE seek_db;"

# set your API key
export GEMINI_API_KEY=your-gemini-api-key

# run the app
./mvnw spring-boot:run
```

### Terminology

<table>
  <tr>
    <th>Term</th>
    <th>Meaning</th>
  </tr>
  <tr>
    <td>RAG</td>
    <td>Retrieval-Augmented Generation — retrieving relevant context, then generating an answer grounded in it.</td>
  </tr>
  <tr>
    <td>Embedding</td>
    <td>A numeric vector representation of text's meaning (handled entirely server-side).</td>
  </tr>
  <tr>
    <td>Chunk</td>
    <td>A smaller segment of a larger document, split for more precise retrieval.</td>
  </tr>
  <tr>
    <td>Vector Store</td>
    <td>Vector Store is optimized for saving and searching embeddings by similarity.</td>
  </tr>
  <tr>
    <td>Similarity Search</td>
    <td>Finding the stored vectors that are closest in meaning to a query vector.</td>
  </tr>
  <tr>
    <td>Prompt</td>
    <td>The instruction and context sent to a generative AI model.</td>
  </tr>
  <tr>
    <td>CORS</td>
    <td>Browser security policy that blocks unauthorized domains from accessing an API.</td>
  </tr>
</table>
