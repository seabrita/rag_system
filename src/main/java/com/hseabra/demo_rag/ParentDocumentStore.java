package com.hseabra.demo_rag;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ParentDocumentStore {

    private final Map<String, Document> store = new ConcurrentHashMap<>();

    public void save(String parentId, Document doc) {
        store.put(parentId, doc);
    }

    public Document get(String parentId) {
        return store.get(parentId);
    }

    public Collection<Document> getAll() {
        return store.values();
    }

    public void delete(String parentId) {
        store.remove(parentId);
    }

    public int size() {
        return store.size();
    }
}