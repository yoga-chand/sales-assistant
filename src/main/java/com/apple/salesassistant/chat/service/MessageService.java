package com.apple.salesassistant.chat.service;

import com.apple.salesassistant.chat.dto.ConversationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class MessageService {

    private final ChatService chatService;

    public MessageService(ChatService chatService) {
        this.chatService = chatService;
    }

    public record AppendResult(String assistantText, Instant assistantCreatedAt) {}

    public ConversationResult replyToMessage(String guestMessage) {
        long start = System.currentTimeMillis();
        Map<String, Object> llmResponse = chatService.answer(guestMessage);
        long latency = System.currentTimeMillis() - start;

        UUID requestId = UUID.randomUUID();
        log.info("[replyToMessage] Completed requestId={} latency={}ms", requestId, latency);

        return new ConversationResult(
                requestId.toString(),
                Objects.toString(llmResponse.get("answer"), ""),
                requestId.toString(),
                latency,
                "ROLE_GUEST",
                Instant.now()
        );

    }
}

