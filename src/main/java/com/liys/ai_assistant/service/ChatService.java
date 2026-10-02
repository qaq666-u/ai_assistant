package com.liys.ai_assistant.service;

import com.liys.ai_assistant.common.AppProperties;
import com.liys.ai_assistant.common.Citation;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    private static final String SYSTEM_TEMPLATE = """
            你是智能文档知识库助手。请严格依据【参考资料】回答问题，并用 [编号] 标注引用。
            如果参考资料无法支持答案，请明确说明“根据已上传资料无法回答”，不要编造。
            请使用与用户问题一致的语言。

            【参考资料】
            %s
            """;

    private final ChatModel chatModel;
    private final VectorStore vectorStore;
    private final AppProperties properties;
    private final ConversationStore conversationStore;

    public ChatService(ChatModel chatModel, VectorStore vectorStore,
                       AppProperties properties, ConversationStore conversationStore) {
        this.chatModel = chatModel;
        this.vectorStore = vectorStore;
        this.properties = properties;
        this.conversationStore = conversationStore;
    }

    public List<Document> retrieve(String question, String fileName) {
        var rag = properties.getRag();
        SearchRequest.Builder request = SearchRequest.builder()
                .query(question)
                .topK(rag.getTopK())
                .similarityThreshold(rag.getSimilarityThreshold());
        if (fileName != null && !fileName.isBlank()) {
            request.filterExpression(new Filter.Expression(
                    Filter.ExpressionType.EQ,
                    new Filter.Key(DocumentService.METADATA_FILE_NAME),
                    new Filter.Value(fileName.trim())));
        }
        List<Document> results = vectorStore.similaritySearch(request.build());
        if (results == null) return List.of();
        for (Document document : results) {
            logger.info("RAG hit: file={}, page={}, score={}",
                    document.getMetadata().get(DocumentService.METADATA_FILE_NAME),
                    document.getMetadata().get(DocumentService.META_PAGE),
                    document.getScore());
        }
        return results;
    }

    public List<Citation> toCitations(List<Document> documents) {
        List<Citation> citations = new ArrayList<>(documents.size());
        for (Document document : documents) {
            Map<String, Object> metadata = document.getMetadata();
            String fileName = String.valueOf(metadata.getOrDefault(DocumentService.METADATA_FILE_NAME, "未知文件"));
            Integer page = toInteger(metadata.get(DocumentService.META_PAGE));
            citations.add(new Citation(fileName, page, snippet(document.getText()), document.getScore()));
        }
        return citations;
    }

    /** Prompt and history snapshot are constructed before the current question is saved. */
    public Flux<String> streamAnswer(String conversationId, String question, List<Document> documents) {
        Prompt prompt = buildPrompt(conversationId, question, documents);
        StringBuilder answer = new StringBuilder();
        return chatModel.stream(prompt)
                .map(ChatService::extractContent)
                .filter(text -> text != null && !text.isEmpty())
                .switchIfEmpty(Flux.just("模型未返回内容，请稍后重试。"))
                .doOnNext(answer::append)
                .doOnComplete(() -> {
                    if (answer.length() > 0) conversationStore.addAssistant(conversationId, answer.toString());
                })
                .doOnError(error -> {
                    logger.error("Chat stream failed for conversation {}", conversationId, error);
                    conversationStore.addAssistant(conversationId, answer.length() == 0
                            ? "回答生成失败，请稍后重试。" : answer.toString());
                });
    }

    public void rememberQuestion(String conversationId, String question) {
        conversationStore.addUser(conversationId, question);
    }

    public void rememberAnswer(String conversationId, String answer) {
        conversationStore.addAssistant(conversationId, answer);
    }

    public void clearConversation(String conversationId) {
        conversationStore.clear(conversationId);
    }

    private Prompt buildPrompt(String conversationId, String question, List<Document> documents) {
        StringBuilder context = new StringBuilder();
        for (int i = 0; i < documents.size(); i++) {
            Document document = documents.get(i);
            Map<String, Object> metadata = document.getMetadata();
            String fileName = String.valueOf(metadata.getOrDefault(DocumentService.METADATA_FILE_NAME, "未知文件"));
            Object page = metadata.getOrDefault(DocumentService.META_PAGE, "未知");
            context.append('[').append(i + 1).append("] (来源: ")
                    .append(fileName).append(", 第 ").append(page).append(" 页)\n")
                    .append(document.getText()).append("\n\n");
        }

        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(SYSTEM_TEMPLATE.formatted(context)));
        messages.addAll(conversationStore.get(conversationId));
        messages.add(new UserMessage(question));
        return new Prompt(messages);
    }

    private static String extractContent(ChatResponse response) {
        if (response == null || response.getResult() == null || response.getResult().getOutput() == null) {
            return null;
        }
        return response.getResult().getOutput().getText();
    }

    private static Integer toInteger(Object value) {
        if (value instanceof Number number) return number.intValue();
        if (value == null) return null;
        try { return Integer.valueOf(value.toString()); }
        catch (NumberFormatException ignored) { return null; }
    }

    private static String snippet(String text) {
        if (text == null) return "";
        int codePoints = text.codePointCount(0, text.length());
        if (codePoints <= 120) return text;
        return text.substring(0, text.offsetByCodePoints(0, 120)) + "…";
    }
}
