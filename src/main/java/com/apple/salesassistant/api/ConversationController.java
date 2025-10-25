package com.apple.salesassistant.api;

import com.apple.salesassistant.service.ChatService;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/v1")
@Validated
public class ConversationController {

//    private final ConversationService conversationService;
//    private final MessageService messageService;

    @Autowired
    private ChatService chatService;

//    public ConversationController(ConversationService conversationService, MessageService messageService) {
//        this.conversationService = conversationService;
//        this.messageService = messageService;
//    }

    // === Create conversation ===
    public record CreateConversationRequest(@Size(max = 120) String title) {}
    public record CreateConversationResponse(UUID id, String title, Instant createdAt) {}

    @PostMapping("/conversations")
    public ResponseEntity<CreateConversationResponse> createConversation(@RequestBody(required = false) CreateConversationRequest req) {
//        var conv = conversationService.create(req == null ? null : req.title());
//        return new CreateConversationResponse(conv.id(), conv.title(), conv.createdAt());
       // CreateConversationResponse response = new CreateConversationResponse(UUID.randomUUID(), chatService.chatWithDefaults(req.title()), Instant.now());
        CreateConversationResponse response = new CreateConversationResponse(UUID.randomUUID(), chatService.chat(req.title()), Instant.now());
          return ResponseEntity.ok(response);
    }

//    // === Append message (sync LLM) ===
//    public record SendMessageRequest(@NotBlank @Size(max = 8000) String content) {}
//    public record MessageResponse(String role, String content, Instant createdAt) {}
//
//    @PostMapping("/conversations/{conversationId}/messages")
//    public Map<String, Object> sendMessage(@PathVariable UUID conversationId,
//                                           @RequestBody SendMessageRequest req) {
//        var result = messageService.appendUserAndReply(conversationId, req.content());
//        return Map.of(
//                "conversation_id", conversationId,
//                "message", new MessageResponse("assistant", result.assistantText(), result.assistantCreatedAt())
//        );
//    }
}

