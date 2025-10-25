package com.apple.salesassistant.chat.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "messages")
public class MessageEntity {

  public enum Role { user, assistant }

  @Id
  @Column(columnDefinition = "uuid")
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "conversation_id", nullable = false)
  private ConversationEntity conversation;

  @Enumerated(EnumType.STRING)
  @Column(length = 16, nullable = false)
  private Role role;

  @Column(columnDefinition = "text", nullable = false)
  private String content;


//  @Column(columnDefinition = "jsonb")
//  @JdbcTypeCode(SqlTypes.JSON)
//  private Map<String, Object> citations;         // nullable; only on assistant messages

  @Column(name = "used_top_k")
  private Integer usedTopK;

  @Column(name = "latency_ms")
  private Long latencyMs;

  @Column(name = "idempotency_key", columnDefinition = "uuid")
  private UUID idempotencyKey;            // same key for user&assistant pair if provided

  @Column(name = "request_hash")
  private String requestHash;             // optional (e.g., SHA-256 of request body)

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  // getters & setters …
  public void setId(UUID id) { this.id = id; }
  public UUID getId() { return id; }
  public ConversationEntity getConversation() { return conversation; }
  public void setConversation(ConversationEntity conversation) { this.conversation = conversation; }
  public Role getRole() { return role; }
  public void setRole(Role role) { this.role = role; }
  public String getContent() { return content; }
  public void setContent(String content) { this.content = content; }
//  public Map<String, Object> getCitations() { return citations; }
//  public void setCitations(Map<String, Object> citations) { this.citations = citations; }
  public Integer getUsedTopK() { return usedTopK; }
  public void setUsedTopK(Integer usedTopK) { this.usedTopK = usedTopK; }
  public Long getLatencyMs() { return latencyMs; }
  public void setLatencyMs(Long latencyMs) { this.latencyMs = latencyMs; }
  public UUID getIdempotencyKey() { return idempotencyKey; }
  public void setIdempotencyKey(UUID idempotencyKey) { this.idempotencyKey = idempotencyKey; }
  public String getRequestHash() { return requestHash; }
  public void setRequestHash(String requestHash) { this.requestHash = requestHash; }
  public Instant getCreatedAt() { return createdAt; }
}
