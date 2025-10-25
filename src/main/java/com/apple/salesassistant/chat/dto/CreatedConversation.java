package com.apple.salesassistant.chat.dto;

import java.time.Instant;
import java.util.Map;

public record CreatedConversation(
        String id, String title, String userId, Instant createdAt,
        Instant updatedAt, Map<String,Object> metadata
    ) {}