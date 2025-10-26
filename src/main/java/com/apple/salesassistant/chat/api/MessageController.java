package com.apple.salesassistant.chat.api;

import com.apple.salesassistant.chat.dto.ConversationResult;
import com.apple.salesassistant.chat.service.MessageService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
@Validated
@AllArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/messages:complete")
    public ResponseEntity<ConversationResult> postMessage(@RequestBody(required = false) ConversationController.AddMessageRequest req) {
        ConversationResult conversation = messageService.replyToMessage(req.message());
        return ResponseEntity.ok(conversation);

    }
}
