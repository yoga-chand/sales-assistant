package com.apple.salesassistant.chat.api;


import com.apple.salesassistant.auth.api.filter.JwtAuthFilter;
import com.apple.salesassistant.chat.service.ConversationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for ConversationController:
 *  - GET /v1/conversations/{conversationId}
 *  - GET /v1/conversations/{conversationId}/messages
 *
 * Requires Spring Security on the test classpath.
 * If you have a custom JwtAuthFilter bean, consider @MockBean-ing it to avoid real auth.
 */
@WebMvcTest(controllers = ConversationController.class)
class ConversationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @MockBean
    private ConversationService conversationService;


    @Test
    void getConversationById_asGuest_forbidden401() throws Exception {
        UUID cid = UUID.randomUUID();
        // service won't be called if access denied, but safe to keep a default
        when(conversationService.getConversationById(any(UUID.class)))
                .thenReturn(Map.of("conversationId", cid.toString()));

        mockMvc.perform(get("/v1/conversations/{conversationId}", cid)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listMessages_unauthenticated_unauthorized401() throws Exception {
        UUID cid = UUID.randomUUID();

        mockMvc.perform(get("/v1/conversations/{conversationId}/messages", cid)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
