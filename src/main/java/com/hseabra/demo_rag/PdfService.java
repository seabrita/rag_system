package com.hseabra.demo_rag;

import lombok.AllArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class PdfService {
    private final ChunkingService chunkingService;

    public List<Document> loadPdfAll(String path) throws IOException {
        List<Document> pages = new ArrayList<>();

        String fullContent = "";
        try (PDDocument pdf = Loader.loadPDF(new File(path))) {
            PDFTextStripper stripper = new PDFTextStripper();

            int count = pdf.getNumberOfPages();
            for (int i = 1; i <= count; i++) {
                stripper.setStartPage(i);
                stripper.setEndPage(i);

                fullContent = fullContent.concat(clean(stripper.getText(pdf)) + " ");


            }
        }
        Document page = new Document(fullContent);
        return chunkingService.createChunks(page);
    }

    public List<Document> loadPdf(String path) throws IOException {
        List<Document> pages = new ArrayList<>();

        try (PDDocument pdf = Loader.loadPDF(new File(path))) {
            PDFTextStripper stripper = new PDFTextStripper();

            int count = pdf.getNumberOfPages();
            for (int i = 1; i <= count; i++) {
                stripper.setStartPage(i);
                stripper.setEndPage(i);

                String text = clean(stripper.getText(pdf));

                Document page = new Document(text, Map.of("page", String.valueOf(i)));

                pages.add(page);
            }
        }
        return pages;
    }

    private String clean(String text) {
        if (text == null) return "";
        return text
                .replaceAll("\\s+", " ")
                // Remove sequences of 3 or more dots (like ".....")
                .replaceAll("\\.{3,}", "")
                // Remove trailing numbers after a dot (page numbers)
                .replaceAll("(?<=\\.)\\s*\\d+\\s*$", "")
                .trim();
    }

}
