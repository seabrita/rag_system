package com.hseabra.demo_rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;

import java.util.ArrayList;
import java.util.List;

public class CustomizedTokenTextSplitter extends TokenTextSplitter {

    private final int chunkOverlap;

    public CustomizedTokenTextSplitter(int chunkSize, int chunkOverlap) {
        super(chunkSize, 350, 5, 10000, true);
        this.chunkOverlap = chunkOverlap;
    }

    @Override
    public List<Document> split(Document document) {
        // Get the base chunks from the parent implementation
        List<Document> baseChunks = super.split(document);

        // If we don't have overlap or only one chunk, return as is
        if (chunkOverlap <= 0 || baseChunks.size() <= 1) {
            return baseChunks;
        }

        List<Document> chunksWithOverlap = new ArrayList<>();

        for (int i = 0; i < baseChunks.size(); i++) {
            Document currentChunk = baseChunks.get(i);
            String currentContent = currentChunk.getText();

            // For chunks after the first one, prepend overlap from previous chunk
            if (i > 0) {
                Document previousChunk = baseChunks.get(i - 1);
                String previousContent = previousChunk.getText();

                // Get the last 'chunkOverlap' characters from the previous chunk
                String overlapText = getOverlapText(previousContent, chunkOverlap);
                currentContent = overlapText + currentContent;
            }

            // Create a new document with the overlapped content, preserving metadata
            Document newDoc = new Document(currentContent, currentChunk.getMetadata());
            chunksWithOverlap.add(newDoc);
        }

        return chunksWithOverlap;
    }

    /**
     * Extracts the last portion of text for overlap, trying to break at word boundaries
     */
    private String getOverlapText(String text, int maxOverlapChars) {
        if (text.length() <= maxOverlapChars) {
            return text;
        }

        // Try to find a good breaking point (space or newline) near the overlap size
        int startPos = text.length() - maxOverlapChars;
        int spacePos = text.indexOf(' ', startPos);
        int newlinePos = text.indexOf('\n', startPos);

        // Use the first boundary we find, or just use the character count
        int breakPoint = startPos;
        if (spacePos != -1 && spacePos < text.length() - 1) {
            breakPoint = spacePos + 1; // Include the space
        } else if (newlinePos != -1 && newlinePos < text.length() - 1) {
            breakPoint = newlinePos + 1; // Include the newline
        }

        return text.substring(breakPoint);
    }
}
