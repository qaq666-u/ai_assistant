package com.liys.ai_assistant.common;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String vectorStore = "simple";
    private Rag rag = new Rag();
    private Chat chat = new Chat();

    public String getVectorStore() { return vectorStore; }
    public void setVectorStore(String vectorStore) { this.vectorStore = vectorStore; }
    public Rag getRag() { return rag; }
    public void setRag(Rag rag) { this.rag = rag; }
    public Chat getChat() { return chat; }
    public void setChat(Chat chat) { this.chat = chat; }

    public static class Rag {
        private int chunkSize = 500;
        private int chunkOverlap = 50;
        private int topK = 4;
        private double similarityThreshold = 0.0;

        public int getChunkSize() { return chunkSize; }
        public void setChunkSize(int chunkSize) { this.chunkSize = chunkSize; }
        public int getChunkOverlap() { return chunkOverlap; }
        public void setChunkOverlap(int chunkOverlap) { this.chunkOverlap = chunkOverlap; }
        public int getTopK() { return topK; }
        public void setTopK(int topK) { this.topK = topK; }
        public double getSimilarityThreshold() { return similarityThreshold; }
        public void setSimilarityThreshold(double similarityThreshold) { this.similarityThreshold = similarityThreshold; }
    }

    public static class Chat {
        private int maxHistoryRounds = 10;

        public int getMaxHistoryRounds() { return maxHistoryRounds; }
        public void setMaxHistoryRounds(int maxHistoryRounds) { this.maxHistoryRounds = maxHistoryRounds; }
    }
}
