package com.apple.salesassistant.chat.dto;

import java.time.Instant;

public record ConversationResult(
        String conversationId, String answer, String idempotencyKey, long latency, String roleBanner, Instant createdAt

    ) {}