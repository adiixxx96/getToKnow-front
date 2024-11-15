package com.adape.gtk.front.controller;

import java.sql.Timestamp;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.adape.gtk.core.client.beans.ChatDTO;
import com.adape.gtk.core.client.beans.MessageDTO;
import com.adape.gtk.core.client.beans.UserDTO;
import com.adape.gtk.core.client.service.MessageIntService;
import com.adape.gtk.front.beans.ChatMessage;

@Controller
public class WebSocketChatController {

	Logger log = LoggerFactory.getLogger(WebSocketChatController.class);

	@Autowired
	private SimpMessagingTemplate messagingTemplate;

	@Autowired
	private MessageIntService messageclient;

	@MessageMapping("/chat.sendMessage")
	public void sendMessage(@Payload ChatMessage chatMessage) {

		// First send message through websocket
		if (chatMessage.getId() == null) {
            chatMessage.setId(UUID.randomUUID().toString());
        }
		String destination = "/topic/chat/" + chatMessage.getChatId();
		messagingTemplate.convertAndSend(destination, chatMessage);

		// Second save message in database
		MessageDTO message = new MessageDTO();
		message.setChat(ChatDTO.builder().id(chatMessage.getChatId()).build());
		message.setUser(UserDTO.builder().id(chatMessage.getUserId()).build());
		message.setMessage(chatMessage.getMessage());
		message.setIsRead(chatMessage.getRead());
		message.setCreationDate(new Timestamp(chatMessage.getCreationDate().getTime()));

		messageclient.create(message, chatMessage.getUserId());

	}
	
	@MessageMapping("/chat.messageRead")
	public void messageRead(@Payload ChatMessage chatMessage) {
	    String destination = "/topic/chat/" + chatMessage.getChatId();
	    messagingTemplate.convertAndSend(destination, chatMessage);
	}

}