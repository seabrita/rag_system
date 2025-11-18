# RAG System Demo

A Retrieval-Augmented Generation (RAG) system built with Spring Boot, Spring AI, and Elasticsearch for document ingestion, chunking, and intelligent question-answering.

## Overview

This project demonstrates a complete RAG pipeline that:
- Ingests PDF documents using Apache Tika
- Splits documents into chunks with customizable tokenization
- Implements parent-child document retrieval strategy
- Stores embeddings in Elasticsearch vector store
- Uses OpenAI embeddings and chat models for semantic search and responses

## Tech Stack

- **Java 21**
- **Spring Boot 3.5.7**
- **Spring AI 1.1.0**
- **Elasticsearch** (vector store)
- **OpenAI API** (embeddings & chat)
- **Apache Tika** (document parsing)
- **Gradle** (build tool)

## Prerequisites

- Java 21 or higher
- Elasticsearch (see setup instructions below)
- OpenAI API key

## Setup Instructions

### 1. Start Local Elasticsearch

Run the following command to start a local Elasticsearch instance:

```bash
curl -fsSL https://elastic.co/start-local | sh
```

This will:
- Download and start Elasticsearch in a Docker container
- Set up the default credentials (elastic/changeme or custom)
- Expose Elasticsearch on `http://localhost:9200`

**Important**: After starting Elasticsearch, note the username and password provided in the output.

### 2. Configure Application

Set up your environment variables. Create a `.env` file or export the following variables:

```bash
export OPENAI_API_KEY=your-openai-api-key-here
export ELASTICSEARCH_PASSWORD=your-elasticsearch-password
```

Alternatively, you can copy `.env.example` to `.env` and update the values:

```bash
cp .env .env
# Edit .env with your actual credentials
```

**Security Note**: The application uses environment variables to avoid hardcoding credentials. Never commit `.env` files or sensitive credentials to version control.

### 3. Build the Project

```bash
./gradlew build
```

### 4. Run the Application

```bash
./gradlew bootRun
```

Or run the compiled JAR:

```bash
java -jar build/libs/demo-rag-0.0.1-SNAPSHOT.jar
```

The application will start on the default port (typically 8080).

## Features

### Document Ingestion
- PDF document parsing with Apache Tika
- Automatic text extraction and preprocessing

### Chunking Service
- Customizable token-based text splitting
- Configurable chunk size and overlap
- Parent-child document relationship management

### Vector Storage
- Elasticsearch vector store integration
- Configurable embedding dimensions (1536 for text-embedding-3-small)
- Cosine similarity search

### RAG Inference
- Context-aware question answering
- OpenAI GPT-4o-mini integration
- Temperature-controlled response generation

### Parent-Child Retrieval
- Advanced retrieval strategy for better context
- Parent document storage for full context preservation

## API Endpoints

(Document your API endpoints here based on your controllers)

## Project Structure

```
src/
├── main/
│   ├── java/com/hseabra/demo_rag/
│   │   ├── AiConfig.java                    # Spring AI configuration
│   │   ├── ChunkingService.java             # Document chunking logic
│   │   ├── CustomizedTokenTextSplitter.java # Custom text splitting
│   │   ├── DemoRagApplication.java          # Main application class
│   │   ├── IngestionService.java            # Document ingestion
│   │   ├── ParentChildRetriever.java        # Retrieval strategy
│   │   ├── ParentDocumentStore.java         # Parent doc storage
│   │   ├── PdfService.java                  # PDF processing
│   │   ├── RagInferenceService.java         # Question answering
│   │   ├── TopicClassifier.java             # Document classification
│   │   └── Test.java                        # Test utilities
│   └── resources/
│       ├── application.yaml                  # Configuration
│       └── *.pdf                             # Sample PDF documents
└── test/
    └── java/com/hseabra/demo_rag/
        └── DemoRagApplicationTests.java      # Test cases
```

## Configuration Options

Key configuration properties in `application.yaml`:

| Property | Description | Default |
|----------|-------------|---------|
| `spring.ai.vectorstore.elasticsearch.index-name` | Elasticsearch index name | `custom-index` |
| `spring.ai.vectorstore.elasticsearch.dimensions` | Embedding dimensions | `1536` |
| `spring.ai.vectorstore.elasticsearch.similarity` | Similarity metric | `cosine` |
| `spring.ai.openai.embedding.options.model` | OpenAI embedding model | `text-embedding-3-small` |
| `spring.ai.openai.chat.options.model` | OpenAI chat model | `gpt-4o-mini` |
| `spring.ai.openai.chat.options.temperature` | Response creativity | `0.7` |

## Development

### Running Tests

```bash
./gradlew test
```

### Building for Production

```bash
./gradlew clean build -x test
```

## Troubleshooting

### Elasticsearch Connection Issues

1. Verify Elasticsearch is running:
   ```bash
   curl -u elastic:your-password http://localhost:9200
   ```

2. Check Docker containers:
   ```bash
   docker ps
   ```

3. View Elasticsearch logs:
   ```bash
   docker logs <container-id>
   ```

### OpenAI API Issues

- Verify your API key is valid and has sufficient credits
- Check rate limits if experiencing throttling
- Ensure the specified models are available in your OpenAI account

## License

(Add your license information here)

## Contributors

(Add contributor information here)

## Acknowledgments

- Spring AI team for the excellent AI integration framework
- Elasticsearch for vector storage capabilities
- OpenAI for embedding and chat models

