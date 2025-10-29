package com.apple.salesassistant.chat.service;

import com.apple.salesassistant.auth.api.filter.JwtAuthFilter;
import com.apple.salesassistant.chat.dto.ConversationResult;
import com.apple.salesassistant.chat.entity.ConversationEntity;
import com.apple.salesassistant.chat.entity.MessageEntity;
import com.apple.salesassistant.chat.repository.ConversationRepository;
import com.apple.salesassistant.chat.repository.MessageRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Slf4j
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
    public ConversationResult createConversation(String title) {

        var newConversation = createConversationEntity(title);
        log.info("Created new conversation with id {}", newConversation.getId());
        // 2) Persist user message (optionally with idempotency)
        UUID userMessageUuid = insertUserMessage(newConversation.getId(), "");
        log.info("Inserted user message with id {}");
        // 3) Invoke ABAC + LLM (ChatService already builds prompt & retrieves KB)
        long t0 = System.currentTimeMillis();
        Map<String,Object> llmResponse = chatService.answer(title); // returns {answer, role, citations[]}
        long latency = System.currentTimeMillis() - t0;

        String answer = Objects.toString(llmResponse.get("answer"), "");

        MessageEntity assistantMessageEntity = insertAssistantMessage(newConversation.getId(), userMessageUuid, latency, answer);
        log.info("Inserted assistant message with id {}");
        return new ConversationResult(
                newConversation.getId().toString(),
                answer,
                assistantMessageEntity.getIdempotencyKey().toString(),
                latency,
                Objects.toString(llmResponse.get("role"), ""),
                Instant.now()
        );

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

    private MessageEntity insertAssistantMessage(UUID conversationId, UUID replyToMessageId,
                                                 long latency, String content) {
        var conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException(    "Conversation not found"));
        MessageEntity message = new MessageEntity();
        message.setId(UUID.randomUUID());
        message.setConversation(conversation);
        message.setIdempotencyKey(UUID.randomUUID());
        message.setRole(MessageEntity.Role.assistant);
        message.setContent(content);
        message.setReplyToMessageId(replyToMessageId);
        message.setLatencyMs(latency);
        return messageRepository.save(message);
    }

    private ConversationEntity createConversationEntity(String title) {
        String ttl = (title != null && !title.isBlank()) ? title : "New Conversation";
        ConversationEntity conv = new ConversationEntity();
        conv.setTitle(ttl);
        conv.setUserId(currentAuthUser() != null ? currentAuthUser().id() : null);
        return conversationRepository.save(conv);
    }

    @Transactional
    public ConversationResult addMessageToConversation(UUID conversationId, String message) {

        Optional<ConversationResult> conversationResult =  checkForDuplicatedMessage(conversationId, message);
        if (conversationResult.isPresent()) {
            return conversationResult.get();
        }
        // 2) Persist user message (optionally with idempotency)
        UUID userMessageUuid = insertUserMessage(conversationId, message);
        // 3) Invoke ABAC + LLM (ChatService already builds prompt & retrieves KB)
        long t0 = System.currentTimeMillis();
        Map<String,Object> llmResponse = chatService.answer(message); // returns {answer, role, citations[]}
        long latency = System.currentTimeMillis() - t0;
        String answer = Objects.toString(llmResponse.get("answer"), "");
        MessageEntity assistantMessageEntity = insertAssistantMessage(conversationId, userMessageUuid, latency, answer);
        return new ConversationResult(
                conversationId.toString(),
                answer,
                assistantMessageEntity.getIdempotencyKey().toString(),
                latency,
                Objects.toString(llmResponse.get("role"), ""),
                Instant.now()
        );
    }



    private Optional<ConversationResult> checkForDuplicatedMessage(UUID conversationId, String message) {
        Optional<MessageEntity> existingUserMsg =
                messageRepository.findTopByConversationIdAndContentOrderByCreatedAtDesc(
                        conversationId, message);
        if (existingUserMsg.isPresent()) {
            log.info("Duplicate detected for conversation {} input='{}', returning previous answer",
                    conversationId, message);
            MessageEntity lastAssistant = messageRepository
                    .findTopByConversationIdAndRoleOrderByCreatedAtDesc(conversationId, MessageEntity.Role.assistant)
                    .orElseThrow(() -> new IllegalArgumentException("No assistant message found for conversation"));
            return Optional.of(toResult(lastAssistant));
        }
        return Optional.empty();
    }

    private JwtAuthFilter.AuthUser currentAuthUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof JwtAuthFilter.AuthUser u) {
            return u;
        }
        return null;
    }

    private ConversationResult toResult(MessageEntity msg) {
        return new ConversationResult(
                msg.getConversation().getId().toString(),
                msg.getContent(),
                msg.getIdempotencyKey().toString(),
                msg.getLatencyMs(),
                currentAuthUser().roles().getFirst(),
                Instant.now()
        );
    }

    public Map<String, Object> getConversationById(UUID conversationId) {
        var conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        Map<String, Object> result = new HashMap<>();
        result.put("id", conversation.getId().toString());
        result.put("title", conversation.getTitle());
        result.put("userId", conversation.getUserId());
        result.put("createdAt", conversation.getCreatedAt());
        return result;
    }

    public Map<String, Object> getAllMessagesByConversationId(UUID conversationId) {
        var conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        List<MessageEntity> messages = messageRepository.findAllByConversationIdOrderByCreatedAtAsc(conversationId);

        List<Map<String, Object>> messagesList = new ArrayList<>();
        for (MessageEntity msg : messages) {
            Map<String, Object> msgMap = new HashMap<>();
            msgMap.put("id", msg.getId().toString());
            msgMap.put("role", msg.getRole().toString());
            msgMap.put("content", msg.getContent());
            msgMap.put("createdAt", msg.getCreatedAt());
            messagesList.add(msgMap);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("conversationId", conversation.getId().toString());
        result.put("messages", messagesList);
        return result;
    }
}


