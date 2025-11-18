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

Create a `.env` file in the project root by copying the example `.env.example`:

Edit the `.env` file with your actual credentials:
```
# OpenAI Configuration
OPENAI_API_KEY=your-actual-openai-api-key

  # Elasticsearch Configuration
ELASTICSEARCH_PASSWORD=your-actual-elasticsearch-password
```

**Security Note**: Never commit sensitive credentials to version control. Consider using environment variables or a secure configuration management system for production deployments.

### 3. Build the Project

```bash
./gradlew build
```

### 4. Run the Application

```bash
./gradlew bootRun
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
