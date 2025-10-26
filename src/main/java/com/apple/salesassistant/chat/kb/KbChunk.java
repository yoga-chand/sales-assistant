package com.apple.salesassistant.chat.kb;

import java.util.Set;
import java.util.UUID;

public record KbChunk(
    UUID chunkId,
    String docId,
    String title,
    String text,
    String tenantId,                  // nullable
    AccessScope accessScope,          // AGGREGATE | DETAIL | CONFIDENTIAL
    MinRole minRole,                  // GUEST | ANALYST | ADMIN
    Set<String> tags
) {
  public enum AccessScope { AGGREGATE, DETAIL, CONFIDENTIAL }
  public enum MinRole { GUEST, ANALYST, ADMIN }
}
