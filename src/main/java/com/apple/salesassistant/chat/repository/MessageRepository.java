package com.apple.salesassistant.chat.repository;

import com.apple.salesassistant.chat.entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<MessageEntity, UUID> {

    Optional<MessageEntity> findTopByConversationIdAndContentOrderByCreatedAtDesc(UUID conversationId, String content);

    Optional<MessageEntity> findTopByConversationIdAndRoleOrderByCreatedAtDesc(UUID conversationId, String role);

    List<MessageEntity> findAllByConversationIdOrderByCreatedAtAsc(UUID conversationId);
}
