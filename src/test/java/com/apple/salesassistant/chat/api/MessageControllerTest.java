package com.apple.salesassistant.chat.api;

import com.apple.salesassistant.chat.dto.ConversationResult;
import com.apple.salesassistant.chat.service.MessageService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MessageControllerTest {

  @Test
  void completeGuestMessage_returnsAnswer() throws Exception {
    var mockSvc = Mockito.mock(MessageService.class);
    Mockito.when(mockSvc.replyToMessage(anyString()))
        .thenReturn(new ConversationResult("122","HELLO", "111", 100,"ROLE_GUEST", Instant.now()));

    MockMvc mvc = MockMvcBuilders
        .standaloneSetup(new MessageController(mockSvc))
        .build();

    mvc.perform(post("/v1/messages:complete")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                     {"message":"Hi"}
                     """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.answer").value("HELLO"));
  }
}
