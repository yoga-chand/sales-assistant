package com.apple.salesassistant.chat.repository;

import com.apple.salesassistant.chat.entity.ConversationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<ConversationEntity, UUID> {
  List<ConversationEntity> findByUserIdOrderByUpdatedAtDesc(String userId);
}
