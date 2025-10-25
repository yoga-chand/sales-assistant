//package com.apple.salesassistant.service;
//
//package com.apple.app.conversation;
//
//import com.apple.app.llm.ChatService;
//import com.apple.domain.conversation.Conversation;
//import com.apple.domain.message.Message;
//import com.apple.domain.message.MessageRepository;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.Instant;
//import java.util.UUID;
//
//@Service
//public class MessageService {
//
//    private final ConversationService conversationService;
//    private final MessageRepository messages;
//    private final ChatService chatService;
//
//    public MessageService(ConversationService conversationService,
//                          MessageRepository messages,
//                          ChatService chatService) {
//        this.conversationService = conversationService;
//        this.messages = messages;
//        this.chatService = chatService;
//    }
//
//    public record AppendResult(String assistantText, Instant assistantCreatedAt) {}
//
//    @Transactional
//    public AppendResult appendUserAndReply(UUID conversationId, String userText) {
//
//        Conversation conv = conversationService.require(conversationId);
//
//        UUID userMsgId = messages.insertUserMessage(conv.id(), userText);
//
//        String assistant = chatService.chatWithDefaults(userText);
//
//        Message assistantMsg = messages.insertAssistantMessage(conv.id(), userMsgId, assistant);
//
//        return new AppendResult(assistantMsg.content(), assistantMsg.createdAt());
//    }
//}
//
