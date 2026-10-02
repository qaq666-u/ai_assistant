package com.liys.ai_assistant.service;

import com.liys.ai_assistant.common.AppProperties;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/** 单机内存版会话记忆，按会话隔离并保留最近若干轮消息。 */
@Component
public class ConversationStore {

    private final int maxMessages;
    private final ConcurrentHashMap<String, Deque<Message>> history = new ConcurrentHashMap<>();

    public ConversationStore(AppProperties properties) {
        this.maxMessages = Math.max(1, properties.getChat().getMaxHistoryRounds()) * 2;
    }

    public List<Message> get(String conversationId) {
        Deque<Message> messages = history.get(conversationId);
        if (messages == null) return List.of();
        synchronized (messages) {
            return new ArrayList<>(messages);
        }
    }

    public void addUser(String conversationId, String content) {
        Deque<Message> messages = history.computeIfAbsent(conversationId, ignored -> new ArrayDeque<>());
        synchronized (messages) {
            // Reserve room for the matching assistant message and evict complete old turns.
            while (messages.size() + 2 > maxMessages) removeOldestTurn(messages);
            messages.addLast(new UserMessage(content));
        }
    }

    public void addAssistant(String conversationId, String content) {
        Deque<Message> messages = history.computeIfAbsent(conversationId, ignored -> new ArrayDeque<>());
        synchronized (messages) {
            messages.addLast(new AssistantMessage(content));
            while (messages.size() > maxMessages) removeOldestTurn(messages);
        }
    }

    public void clear(String conversationId) {
        history.remove(conversationId);
    }

    private void removeOldestTurn(Deque<Message> messages) {
        if (!messages.isEmpty()) messages.removeFirst();
        if (!messages.isEmpty()) messages.removeFirst();
    }
}
