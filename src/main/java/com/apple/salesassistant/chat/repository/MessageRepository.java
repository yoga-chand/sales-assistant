package com.apple.salesassistant.chat.repository;

import com.apple.salesassistant.chat.entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<MessageEntity, UUID> {


}
