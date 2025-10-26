package com.apple.salesassistant.chat.api;

import com.apple.salesassistant.chat.dto.ConversationResult;
import com.apple.salesassistant.chat.service.ConversationService;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1")
@Validated
public class ConversationController {

    @Autowired
    private ConversationService conversationService;

    public record CreateConversationRequest(@Size(max = 120) String title) {}

    @PostMapping("/conversations")
    @PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
    public ResponseEntity<ConversationResult> createConversation(@RequestBody(required = false) CreateConversationRequest req) {
        ConversationResult conversation = conversationService.createConversation(req.title());
        return ResponseEntity.ok(conversation);

    }

    public record AddMessageRequest(@Size(max = 120) String message) {}
    @PostMapping("/conversations/{conversationId}/messages")
    @PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
    public ResponseEntity<ConversationResult> addMessageToConversation(
            @PathVariable UUID conversationId,
            @RequestBody AddMessageRequest req) {
        ConversationResult conversationResult = conversationService.addMessageToConversation(conversationId, req.message());
        return ResponseEntity.ok(conversationResult);
    }

    @GetMapping("/conversations/{conversationId}")
    @PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
    public ResponseEntity<Map<String, Object>> listConversations(@PathVariable UUID conversationId) {
        Map<String, Object> conversations = conversationService.getConversationById(conversationId);
        return ResponseEntity.ok(conversations);
    }

    @GetMapping("/conversations/{conversationId}/messages")
    @PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
    public ResponseEntity<Map<String, Object>> listAllMessagesByConversationId(@PathVariable UUID conversationId) {
        Map<String, Object> conversations = conversationService.getAllMessagesByConversationId(conversationId);
        return ResponseEntity.ok(conversations);
    }

}

