//package com.apple.salesassistant.service;
//
//package com.apple.app.conversation;
//
//import com.apple.domain.conversation.Conversation;
//import com.apple.domain.conversation.ConversationRepository;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.Instant;
//import java.util.UUID;
//
//@Service
//public class ConversationService {
//
//    private final ConversationRepository conversations;
//
//    public ConversationService(ConversationRepository conversations) {
//        this.conversations = conversations;
//    }
//
//    @Transactional
//    public Conversation create(String title) {
//        String ttl = (title != null && !title.isBlank()) ? title : "New Conversation";
//        var conv = new Conversation(UUID.randomUUID(), ttl, Instant.now(), Instant.now());
//        return conversations.save(conv);
//    }
//
//    public Conversation require(UUID conversationId) {
//        return conversations.findById(conversationId)
//                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
//    }
//}
//
