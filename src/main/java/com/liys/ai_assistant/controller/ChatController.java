package com.liys.ai_assistant.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liys.ai_assistant.common.ApiResponse;
import com.liys.ai_assistant.common.Citation;
import com.liys.ai_assistant.common.ChatRequest;
import com.liys.ai_assistant.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    private final ChatService chatService;
    private final ObjectMapper objectMapper;

    public ChatController(ChatService chatService, ObjectMapper objectMapper) {
        this.chatService = chatService;
        this.objectMapper = objectMapper;
    }

    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@Valid @RequestBody ChatRequest request) {
        SseEmitter emitter = new SseEmitter(0L);
        AtomicReference<Disposable> subscription = new AtomicReference<>();
        emitter.onCompletion(() -> dispose(subscription));
        emitter.onTimeout(() -> dispose(subscription));
        emitter.onError(error -> dispose(subscription));

        List<org.springframework.ai.document.Document> documents =
                chatService.retrieve(request.question(), request.fileName());
        List<Citation> citations = chatService.toCitations(documents);
        String citationsJson = toJson(citations);

        if (documents.isEmpty()) {
            chatService.rememberQuestion(request.conversationId(), request.question());
            String noResults = "知识库中没有检索到相关内容。请先上传文档，或换个问法再试。";
            send(emitter, "meta", citationsJson);
            send(emitter, "message", noResults);
            chatService.rememberAnswer(request.conversationId(), noResults);
            send(emitter, "done", "");
            emitter.complete();
            return emitter;
        }

        Flux<String> answer = chatService.streamAnswer(
                request.conversationId(), request.question(), documents);
        // streamAnswer captures the old history synchronously; save this question for the next turn.
        chatService.rememberQuestion(request.conversationId(), request.question());
        send(emitter, "meta", citationsJson);

        Disposable disposable = answer.subscribe(
                token -> send(emitter, "message", token),
                error -> {
                    logger.error("Streaming response failed", error);
                    send(emitter, "error", "回答生成失败，请稍后重试。");
                    send(emitter, "done", "");
                    emitter.complete();
                },
                () -> {
                    send(emitter, "done", "");
                    emitter.complete();
                });
        subscription.set(disposable);
        if (disposable.isDisposed()) dispose(subscription);
        return emitter;
    }

    @DeleteMapping("/{conversationId}")
    public ApiResponse<Void> clearConversation(@PathVariable String conversationId) {
        chatService.clearConversation(conversationId);
        return ApiResponse.ok(null);
    }

    private String toJson(List<Citation> citations) {
        try {
            return objectMapper.writeValueAsString(citations);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("无法生成引用信息", e);
        }
    }

    private void send(SseEmitter emitter, String name, String data) {
        try {
            emitter.send(SseEmitter.event().name(name).data(data == null ? "" : data));
        } catch (IOException error) {
            logger.debug("SSE client disconnected", error);
            emitter.completeWithError(error);
        }
    }

    private void dispose(AtomicReference<Disposable> subscription) {
        Disposable disposable = subscription.get();
        if (disposable != null && !disposable.isDisposed()) disposable.dispose();
    }

}
