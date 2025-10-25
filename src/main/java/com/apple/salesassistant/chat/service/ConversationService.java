package com.apple.salesassistant.chat.service;

import com.apple.salesassistant.auth.api.filter.JwtAuthFilter;
import com.apple.salesassistant.chat.dto.CreatedConversation;
import com.apple.salesassistant.chat.entity.ConversationEntity;
import com.apple.salesassistant.chat.entity.MessageEntity;
import com.apple.salesassistant.chat.repository.ConversationRepository;
import com.apple.salesassistant.chat.repository.MessageRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;

    private final MessageRepository messageRepository;

    private final ChatService chatService;

    public ConversationService(ConversationRepository conversationRepository, MessageRepository messageRepository, ChatService chatService) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.chatService = chatService;
    }



    @Transactional
    public CreatedConversation createConversation(String title) {
        String userId = null;
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof JwtAuthFilter.AuthUser u) {
            userId = u.id();
        }

        var newConversation = createConversationEntity(title);

        // 2) Persist user message (optionally with idempotency)
        insertUserMessage(newConversation.getId(), "");

        // 3) Invoke ABAC + LLM (ChatService already builds prompt & retrieves KB)
        long t0 = System.currentTimeMillis();
        Map<String,Object> llmResponse = chatService.answer(title); // returns {answer, role, citations[]}
        long latency = System.currentTimeMillis() - t0;

        String answer = Objects.toString(llmResponse.get("answer"), "");

        @SuppressWarnings("unchecked")
        List<Map<String,Object>> citesMap = (List<Map<String,Object>>) llmResponse.getOrDefault("citations", List.of());
        Map<String, Object> citation = new HashMap<>();
        citation.put("citations", citesMap);
        insertAssistantMessage(newConversation.getId(), null, answer);
        return new CreatedConversation(newConversation.getId().toString(), newConversation.getTitle(), newConversation.getUserId(), newConversation.getCreatedAt(), newConversation.getUpdatedAt(), llmResponse);

    }


    private  String currentUserIdOrNull() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof JwtAuthFilter.AuthUser u) {
            return u.id();
        }
        return null; // guest
    }

    private UUID insertUserMessage(UUID conversationId, String content) {
        var conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        MessageEntity message = new MessageEntity();
        message.setId(UUID.randomUUID());
        message.setConversation(conversation);
        message.setRole(MessageEntity.Role.user);
        message.setContent(content);
        return messageRepository.save(message).getId();
    }

    private MessageEntity insertAssistantMessage(UUID conversationId, UUID replyToMessageId, String content) {
        var conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException(    "Conversation not found"));
        MessageEntity m = new MessageEntity();
        m.setId(UUID.randomUUID());
        m.setConversation(conversation);
        m.setRole(MessageEntity.Role.assistant);
        m.setContent(content);
        //m.setCitations(citations);
        return messageRepository.save(m);
    }

    private ConversationEntity createConversationEntity(String title) {
        String ttl = (title != null && !title.isBlank()) ? title : "New Conversation";
        ConversationEntity conv = new ConversationEntity();
        conv.setTitle(ttl);
        conv.setUserId(currentUserIdOrNull());

        return conversationRepository.save(conv);
    }

}


