package com.apple.salesassistant.configuration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LlmConfig {
  String activeProvider;
  String openaiModel;
  String ollamaModel;
  String effectiveModel() {
    return switch (activeProvider) {
      case "openai" -> openaiModel;
      case "ollama" -> ollamaModel;
      default -> throw new IllegalArgumentException();
    };
  }
}

public class LlmConfigTest {
  @Test
  void picksOpenAiModelWhenActive() {
    var c = new LlmConfig();
    c.activeProvider = "openai";
    c.openaiModel = "gpt-4o-mini";
    c.ollamaModel = "llama3.2:latest";
    assertThat(c.effectiveModel()).isEqualTo("gpt-4o-mini");
  }
  @Test
  void picksOllamaModelWhenActive() {
    var c = new LlmConfig();
    c.activeProvider = "ollama";
    c.openaiModel = "gpt-4o-mini";
    c.ollamaModel = "llama3.2:latest";
    assertThat(c.effectiveModel()).isEqualTo("llama3.2:latest");
  }
}
