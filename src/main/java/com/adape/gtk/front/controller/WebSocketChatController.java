package com.adape.gtk.front.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.adape.gtk.core.client.beans.ChatDTO;
import com.adape.gtk.core.client.beans.Filter;
import com.adape.gtk.core.client.beans.FilterElements;
import com.adape.gtk.core.client.beans.GroupFilter;
import com.adape.gtk.core.client.beans.MessageDTO;
import com.adape.gtk.core.client.beans.NotificationDTO;
import com.adape.gtk.core.client.beans.Page;
import com.adape.gtk.core.client.beans.Response;
import com.adape.gtk.core.client.beans.ResponseMessage;
import com.adape.gtk.core.client.beans.Sorting;
import com.adape.gtk.core.client.beans.UserDTO;
import com.adape.gtk.core.client.beans.Sorting.Order;
import com.adape.gtk.core.client.service.ChatIntService;
import com.adape.gtk.core.client.service.MessageIntService;
import com.adape.gtk.core.client.service.NotificationIntService;
import com.adape.gtk.front.beans.ChatMessage;

@Controller
public class WebSocketChatController {

	Logger log = LoggerFactory.getLogger(WebSocketChatController.class);

	@Autowired
	private SimpMessagingTemplate messagingTemplate;

	@Autowired
	private MessageIntService messageclient;
	
	@Autowired 
	private NotificationIntService notificationclient;
	
	@Autowired 
	private ChatIntService chatclient;

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
		
		//Create notification of new messages
		
		//1. Get chat to obtain user to create notification
		ChatDTO chat = getChatById(chatMessage.getChatId());
		UserDTO userNotification = null;
		UserDTO userMessage = null;
		if (chat.getUser1().getId().equals(chatMessage.getUserId())) {
			userMessage = chat.getUser1();
			userNotification = chat.getUser2();
		} else {
			userMessage = chat.getUser2();
			userNotification = chat.getUser1();
		}
		
		//2. Check if user does not have already notification of new messages
		List<NotificationDTO> notifications = getNotificationsByUser(userNotification.getId());
		boolean hasAlreadyNotifications = false;
		if (!notifications.isEmpty()) {
			for (NotificationDTO n : notifications) {
				if (n.getNotification().contains("nuevos mensajes")) {
					hasAlreadyNotifications = true;
					break;
				}
			}
		}
		
		//2. Create notification
		if (!hasAlreadyNotifications) {
			String notificationMessage = "Tienes nuevos mensajes";
			NotificationDTO notification = NotificationDTO.builder().user(userNotification)
					.notification(notificationMessage)
					.isRead(false)
					.creationDate(new Timestamp(System.currentTimeMillis()))
					.build();		
			notificationclient.create(notification, userMessage.getId());
		}
		
	}
	
	@MessageMapping("/chat.messageRead")
	public void messageRead(@Payload ChatMessage chatMessage) {
	    String destination = "/topic/chat/" + chatMessage.getChatId();
	    messagingTemplate.convertAndSend(destination, chatMessage);
	}
	
	private ChatDTO getChatById(Integer chatId) {
		Filter filter = Filter.builder()
				.groupFilter(GroupFilter.builder()
						.operator(GroupFilter.Operator.AND)
						.filterElements(Arrays.asList(FilterElements.builder()
								.key("id")
								.value(chatId)
								.type(FilterElements.FilterType.INTEGER)
								.operator(FilterElements.OperatorType.EQUALS).build()))
						.build())
				.showParameters(List.of("user1","user2"))
				.page(Page.builder().pageNo(0).pageSize(Integer.MAX_VALUE).build())
				.sorting(List.of(Sorting.builder().field("id").order(Order.DESC).build()))
				.build();
	    
	    ResponseMessage response = chatclient.get(filter, 0);
	    ChatDTO chat = null;
		if (response.isOK()) {
			Response<ChatDTO> resp = (Response<ChatDTO>) response.getMessage();
			chat = resp.getResults().get(0);
		}
		
		return chat;
	}
	
private List<NotificationDTO> getNotificationsByUser(Integer userId) {
		
		Filter filter = Filter.builder()
    			.groupFilter(GroupFilter.builder()
    					.operator(GroupFilter.Operator.AND)
    					.filterElements(Arrays.asList(
    							FilterElements.builder()
    							.key("isRead")
    							.value(false)
    							.type(FilterElements.FilterType.BOOLEAN)
    							.operator(FilterElements.OperatorType.EQUALS).build(),
		    					FilterElements.builder()
								.key("user.id")
								.value(userId)
								.type(FilterElements.FilterType.INTEGER)
								.operator(FilterElements.OperatorType.EQUALS).build()))
    					.build())
    			.showParameters(List.of("user"))
    			.page(Page.builder().pageNo(0).pageSize(Integer.MAX_VALUE).build())
    			.sorting(List.of(Sorting.builder().field("creationDate").order(Order.DESC).build()))
    			.build();
        
        ResponseMessage response = notificationclient.get(filter, 0);
        List<NotificationDTO> notifications = new ArrayList<>();
		if (response.isOK()) {
			Response<NotificationDTO> resp = (Response<NotificationDTO>) response.getMessage();
			notifications = resp.getResults();
		}
		
		return notifications;
	}

}