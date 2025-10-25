package com.apple.salesassistant.chat.api;

import com.apple.salesassistant.chat.dto.CreatedConversation;
import com.apple.salesassistant.chat.service.ChatService;
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
    public ResponseEntity<CreatedConversation> createConversation(@RequestBody(required = false) CreateConversationRequest req) {
        CreatedConversation conversation = conversationService.createConversation(req.title());
        return ResponseEntity.ok(conversation);

    }

//    @GetMapping("/conversations/{conversationId}")
//    @PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
//    public ResponseEntity<Map<String, Object>> listConversations(@PathVariable UUID conversationId) {
//        Map<String, Object> conversations = chatService.getConversationById(conversationId);
//        return ResponseEntity.ok(conversations);
//    }
//
//    @PostMapping("/conversations/{conversationId}/messages")
//    @PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
//    public ResponseEntity<Map<String, Object>> addMessageToConversation(
//            @PathVariable UUID conversationId,
//            @RequestBody AddMessageRequest req) {
//        Map<String, Object> response = chatService.addMessageToConversation(conversationId, req.message());
//        return ResponseEntity.ok(response);
//    }

}

