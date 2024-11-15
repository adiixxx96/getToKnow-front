package com.adape.gtk.front.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.adape.gtk.core.client.beans.BlockByUserDTO;
import com.adape.gtk.core.client.beans.ChatDTO;
import com.adape.gtk.core.client.beans.Filter;
import com.adape.gtk.core.client.beans.FilterElements;
import com.adape.gtk.core.client.beans.GroupFilter;
import com.adape.gtk.core.client.beans.LiteralDTO;
import com.adape.gtk.core.client.beans.LiteralTypeEnum;
import com.adape.gtk.core.client.beans.MessageDTO;
import com.adape.gtk.core.client.beans.Page;
import com.adape.gtk.core.client.beans.Response;
import com.adape.gtk.core.client.beans.ResponseMessage;
import com.adape.gtk.core.client.beans.Sorting;
import com.adape.gtk.core.client.beans.UserDTO;
import com.adape.gtk.core.client.beans.Filter.FilterBuilder;
import com.adape.gtk.core.client.beans.Sorting.Order;
import com.adape.gtk.core.client.service.BlockByUserIntService;
import com.adape.gtk.core.client.service.ChatIntService;
import com.adape.gtk.core.client.service.LiteralIntService;
import com.adape.gtk.core.client.service.MessageIntService;
import com.adape.gtk.front.utils.Utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping({ "/chat" })
public class ChatController {

	Logger log = LoggerFactory.getLogger(ChatController.class);
	
	@Autowired
    private SimpMessagingTemplate messagingTemplate;
	
	@Autowired
	private ChatIntService chatclient;
	@Autowired
	private LiteralIntService literalclient;
	@Autowired
	private BlockByUserIntService blockclient;
	@Autowired
	private MessageIntService messageclient;

	@RequestMapping(value = "/list", method = { RequestMethod.GET, RequestMethod.POST })
	public String chatListController(Model model, HttpSession session, HttpServletRequest request) {

		String requestURI = request.getRequestURI();
		model.addAttribute("requestURI", requestURI);

		boolean isLogged = false;
		boolean isAdmin = false;

		UserDTO user = (UserDTO) session.getAttribute("user");
		if (user != null) {
			isLogged = true;
			if (user.getRole() != null && user.getRole()) {
				isAdmin = true;
			}	
		} else {
			log.error("User not logged, forbidden access to chats");
			return "redirect:/custom-error";
		}

		model.addAttribute("isLogged", isLogged);
		model.addAttribute("isAdmin", isAdmin);
		model.addAttribute("user", user);

		Filter filter = Filter.builder()
    			.groupFilter(GroupFilter.builder()
    					.operator(GroupFilter.Operator.AND)
    					.groupFilter(Arrays.asList(GroupFilter.builder()
    								.operator(GroupFilter.Operator.OR)
    								.filterElements(Arrays.asList(
    										FilterElements.builder()
			    							.key("user1.id")
			    							.value(user.getId())
			    							.type(FilterElements.FilterType.INTEGER)
			    							.operator(FilterElements.OperatorType.EQUALS).build(),
		    								FilterElements.builder()
			    							.key("user2.id")
			    							.value(user.getId())
			    							.type(FilterElements.FilterType.INTEGER)
			    							.operator(FilterElements.OperatorType.EQUALS).build()))
		    						.build()))
    					.build())
    			.showParameters(List.of("user1", "user2", "messages"))
    			.page(Page.builder().pageNo(0).pageSize(Integer.MAX_VALUE).build())
    			.sorting(List.of(Sorting.builder().field("id").order(Order.DESC).build()))
    			.build();
		
		ResponseMessage response = chatclient.get(filter, user.getId());
		
		List<ChatDTO> chats = new ArrayList<>();
		if (response.isOK()) {
			Response<ChatDTO> resp = (Response<ChatDTO>) response.getMessage();
			chats = resp.getResults();
		} else {
			log.error("Error getting chats");
			return "/pages/chat/chatList";
		}
		
		//Set the user that is not you to UserTo
		for (ChatDTO chat : chats) {
			UserDTO userTo = null;
			if (!chat.getUser1().getId().equals(user.getId())) {
				userTo = chat.getUser1();
			} else {
				userTo = chat.getUser2();
			}
			
			userTo.setAge(Utils.calculateAge(userTo.getBirthDate()));
			chat.setUserTo(userTo);
		}
		
		//Set last message date and sort chats by recent date
		for (ChatDTO chat : chats) {
			if (chat.getMessages() != null && !chat.getMessages().isEmpty()) {
				chat.getMessages().sort((m1, m2) -> m2.getCreationDate().compareTo(m1.getCreationDate()));
		        chat.setRecentDate(chat.getMessages().get(0).getCreationDate());
				
			} else {
				chat.setRecentDate(chat.getCreationDate());
			}
		}
		chats.sort((c1, c2) -> c2.getRecentDate().compareTo(c1.getRecentDate()));
			
		//Divide chats in different list for html page
		List<ChatDTO> chatRequests = new ArrayList<>();
		List<ChatDTO> activeChats = new ArrayList<>();
		List<ChatDTO> inactiveChats = new ArrayList<>();
		
		for (ChatDTO chat : chats) {
			//Attribute status - null:request, true: accepted, false:declined
			boolean hasInactiveUsers = !chat.getUser1().getActive() || !chat.getUser2().getActive();
			
			if (hasInactiveUsers || (chat.getStatus() != null && !chat.getStatus())) {
				inactiveChats.add(chat);
			
			} else if (chat.getStatus() == null) {
				chatRequests.add(chat);
			
			} else {
				activeChats.add(chat);
			}
		}
		
		if (!chatRequests.isEmpty()) {
			model.addAttribute("chatRequests", chatRequests);
		}
		
		if (!activeChats.isEmpty()) {
			model.addAttribute("activeChats", activeChats);
		}
		
		if (!inactiveChats.isEmpty()) {
			model.addAttribute("inactiveChats", inactiveChats);
		}
		
		//Get literals for blocks to users
		Filter filterLiterals = Filter.builder()
    			.groupFilter(GroupFilter.builder()
    					.operator(GroupFilter.Operator.AND)
    					.groupFilter(Arrays.asList(GroupFilter.builder()
    								.operator(GroupFilter.Operator.AND)
    								.filterElements(Arrays.asList(
    										FilterElements.builder()
			    							.key("active")
			    							.value(true)
			    							.type(FilterElements.FilterType.BOOLEAN)
			    							.operator(FilterElements.OperatorType.EQUALS).build(),
		    								FilterElements.builder()
			    							.key("type")
			    							.value(LiteralTypeEnum.BLOCK.getType())
			    							.type(FilterElements.FilterType.INTEGER)
			    							.operator(FilterElements.OperatorType.EQUALS).build()))
		    						.build()))
    					.build())
    			.showParameters(List.of(""))
    			.page(Page.builder().pageNo(0).pageSize(Integer.MAX_VALUE).build())
    			.sorting(List.of(Sorting.builder().field("id").order(Order.ASC).build()))
    			.build();
		
		ResponseMessage responseLiteral = literalclient.get(filterLiterals, user.getId());
		
		List<LiteralDTO> literals = new ArrayList<>();
		if (responseLiteral.isOK()) {
			Response<LiteralDTO> resp = (Response<LiteralDTO>) responseLiteral.getMessage();
			literals = resp.getResults();
		} else {
			log.error("Error getting literals");
			return "/pages/chat/chatList";
		}
		
		model.addAttribute("literals", literals);
	
		return "/pages/chat/chatList";
	}
	
	@SuppressWarnings("unchecked")
	@GetMapping("/detail/{id}")
	public String chatDetail(@PathVariable("id") Integer id, Model model, HttpSession session, HttpServletRequest request) {
		String requestURI = request.getRequestURI();
		model.addAttribute("requestURI", requestURI);

		boolean isLogged = false;
		boolean isAdmin = false;

		UserDTO user = (UserDTO) session.getAttribute("user");
		if (user != null) {
			isLogged = true;
			if (user.getRole() != null && user.getRole()) {
				isAdmin = true;
			}
		} else {
			log.error("User has no permission to access chat detail");
			return "redirect:/custom-error";
		}
		
		model.addAttribute("isLogged", isLogged);
		model.addAttribute("isAdmin", isAdmin);
		model.addAttribute("user", user);

		// Get chat with relations
		FilterBuilder filter = Filter.builder();
		filter.page(Page.builder().pageNo(0).pageSize(Integer.MAX_VALUE).build());
		filter.sorting(List.of(Sorting.builder().field("id").order(Order.DESC).build()));
		filter.showParameters(List.of("user1", "user2", "messages.user"));
		filter.groupFilter(GroupFilter.builder().operator(GroupFilter.Operator.AND)
				.filterElements(Arrays.asList(FilterElements.builder().key("id").value(id)
						.type(FilterElements.FilterType.INTEGER).operator(FilterElements.OperatorType.EQUALS).build()))
				.build());
		
		ResponseMessage response = chatclient.get(filter.build(), 0);
		ChatDTO chat = null;
		if (response.isOK()) {
			Response<ChatDTO> res = (Response<ChatDTO>) response.getMessage();
			chat = res.getResults().get(0);
		}
		
		if (chat == null) {
			log.error("The chatt does not exist");
			return "redirect:/custom-error";
		}
		
		if (user.getId().equals(chat.getUser1().getId())) {
			chat.setUserTo(chat.getUser2());
		} else {
			chat.setUserTo(chat.getUser1());
		}
		
		List<MessageDTO> updatedMessages = new ArrayList<>();
		if (chat.getMessages() != null) {
			Collections.sort(chat.getMessages(), (m1, m2) -> m1.getCreationDate().compareTo(m2.getCreationDate()));
			for (MessageDTO message : chat.getMessages()) {
				if (!message.getIsRead() && !message.getUser().getId().equals(user.getId())) {
					message.setIsRead(true);
					messageclient.edit(message, user.getId());
					updatedMessages.add(message);
				}			
			}
		}
		
		// Update messages through websocket so update read live
		if (!updatedMessages.isEmpty()) {
			 Map<String, String> updateMessage = new HashMap<>();
			 updateMessage.put("type", "updateReadStatus");
			 messagingTemplate.convertAndSend("/topic/chat/" + chat.getId(), updateMessage);
	    }
		
		model.addAttribute("chat", chat);

		return "pages/chat/chatDetail";
	}
	
	@SuppressWarnings("unchecked")
	@GetMapping("/accept/{id}")
	public String acceptChat(@PathVariable("id") Integer chatId, RedirectAttributes attr, 
			HttpServletRequest request, HttpSession session) {

		UserDTO user = (UserDTO) session.getAttribute("user");
		if (user == null) {
			log.error("The user has no permission to edit chat");
			return "redirect:/custom-error";
		}

		// Get chat
		FilterBuilder filter = Filter.builder();
		filter.page(Page.builder().pageNo(0).pageSize(Integer.MAX_VALUE).build());
		filter.sorting(List.of(Sorting.builder().field("id").order(Order.DESC).build()));
		filter.showParameters(List.of(""));
		filter.groupFilter(GroupFilter.builder().operator(GroupFilter.Operator.AND)
				.filterElements(Arrays.asList(FilterElements.builder().key("id").value(chatId)
						.type(FilterElements.FilterType.INTEGER).operator(FilterElements.OperatorType.EQUALS).build()))
				.build());
		
		ResponseMessage response = chatclient.get(filter.build(), 0);
		ChatDTO chat = null;
		if (response.isOK()) {
			Response<ChatDTO> res = (Response<ChatDTO>) response.getMessage();
			chat = res.getResults().get(0);
		}
		
		if (chat == null) {
			log.error("The chat does not exist");
			return "redirect:/custom-error";
		}
		
		//Change chat status to accepted and edit chat entity
		chat.setStatus(true);
		
		chatclient.edit(chat, user.getId());
		
		return "redirect:/chat/detail/" + chatId;
		
	}
	
	@SuppressWarnings("unchecked")
	@GetMapping("/open/{id}")
	public String openChat(@PathVariable("id") Integer userToId, RedirectAttributes attr, HttpSession session) {

		UserDTO user = (UserDTO) session.getAttribute("user");
		if (user == null) {
			log.error("The user has no permission to open chat");
			return "redirect:/custom-error";
		}

		// Try to find existing chat
		List<Integer> userIds = new ArrayList<>();
		userIds.add(userToId);
		userIds.add(user.getId());
		
				Filter filter = Filter.builder()
		    			.groupFilter(GroupFilter.builder()
		    					.operator(GroupFilter.Operator.AND)
		    					.groupFilter(Arrays.asList(GroupFilter.builder()
		    								.operator(GroupFilter.Operator.AND)
		    								.filterElements(Arrays.asList(
		    										FilterElements.builder()
					    							.key("user1.id")
					    							.value(userIds)
					    							.type(FilterElements.FilterType.INTEGER_LIST)
					    							.operator(FilterElements.OperatorType.IN).build(),
				    								FilterElements.builder()
					    							.key("user2.id")
					    							.value(userIds)
					    							.type(FilterElements.FilterType.INTEGER_LIST)
					    							.operator(FilterElements.OperatorType.IN).build()))
				    						.build()))
		    					.build())
		    			.showParameters(List.of("user1", "user2", "messages.user"))
		    			.page(Page.builder().pageNo(0).pageSize(Integer.MAX_VALUE).build())
		    			.sorting(List.of(Sorting.builder().field("id").order(Order.ASC).build()))
		    			.build();
		
		ResponseMessage response = chatclient.get(filter, 0);
		
		ChatDTO chat = null;
		if (response.isOK()) {
			Response<ChatDTO> res = (Response<ChatDTO>) response.getMessage();
			chat = res.getResults().get(0);
		}
		
		if (chat == null) {
			//Chat does not exist, we must create a new one as chat request
			ChatDTO newChat = ChatDTO.builder().user1(UserDTO.builder().id(user.getId()).build())
					.user2(UserDTO.builder().id(userToId).build())
					.creationDate(new Timestamp(System.currentTimeMillis()))
					.status(null)
					.build();
			
			ResponseMessage resp = chatclient.create(newChat, user.getId());
			if (!resp.isOK()) {
				log.error("Error creating new chat");
				attr.addFlashAttribute("error", Utils.message("chat.create.error", resp.getMessage().toString()));
				return "redirect:/chat/list/";
			} else {
				log.info("Chat successfully created");
				chat = (ChatDTO) resp.getMessage();
			}
		
		} else {
			//If chat status if false, we must change it first into chat request
			if (chat.getStatus()!= null && !chat.getStatus()) {
				chat.setStatus(null);
				chat.setUser1(user);
				chat.setUser2(UserDTO.builder().id(userToId).build());
				chatclient.edit(chat, user.getId());			
			}		
		}
			
		return "redirect:/chat/detail/" + chat.getId();
		
	}
	
	@SuppressWarnings("unchecked")
	@GetMapping("/reject/{id}")
	public String rejectChat(@PathVariable("id") Integer chatId, RedirectAttributes attr, HttpSession session) {

		UserDTO user = (UserDTO) session.getAttribute("user");
		if (user == null) {
			log.error("The user has no permission to edit chat");
			return "redirect:/custom-error";
		}

		// Get chat
		FilterBuilder filter = Filter.builder();
		filter.page(Page.builder().pageNo(0).pageSize(Integer.MAX_VALUE).build());
		filter.sorting(List.of(Sorting.builder().field("id").order(Order.DESC).build()));
		filter.showParameters(List.of(""));
		filter.groupFilter(GroupFilter.builder().operator(GroupFilter.Operator.AND)
				.filterElements(Arrays.asList(FilterElements.builder().key("id").value(chatId)
						.type(FilterElements.FilterType.INTEGER).operator(FilterElements.OperatorType.EQUALS).build()))
				.build());
		ResponseMessage response = chatclient.get(filter.build(), 0);
		ChatDTO chat = null;
		if (response.isOK()) {
			Response<ChatDTO> res = (Response<ChatDTO>) response.getMessage();
			chat = res.getResults().get(0);
		}
		
		if (chat == null) {
			log.error("The chat does not exist");
			return "redirect:/custom-error";
		}
		
		//Change chat status to rejected and edit chat entity
		chat.setStatus(false);
		
		ResponseMessage resp = chatclient.edit(chat, user.getId());
		
		if (!resp.isOK()) {
			attr.addFlashAttribute("error", Utils.message("chat.edit.error", resp.getMessage().toString()));
		} else {
			attr.addFlashAttribute("success", Utils.message("chat.edit.ok"));
		}
		
		return "redirect:/chat/list/";
		
	}
	
	@SuppressWarnings("unchecked")
	@PostMapping("/blockUser")
	public ResponseEntity<?> blockUser(@RequestParam Integer chatId, @RequestParam Integer userId, 
			@RequestParam String reason, HttpSession session) {
	    
		UserDTO user = (UserDTO) session.getAttribute("user");
		if (user == null) {
			log.error("The user has no permission to edit chat");
			return ResponseEntity.internalServerError().body("User has no permission to block users");
		}
		
		//Get chat to set rejected
		FilterBuilder filter = Filter.builder();
		filter.page(Page.builder().pageNo(0).pageSize(Integer.MAX_VALUE).build());
		filter.sorting(List.of(Sorting.builder().field("id").order(Order.DESC).build()));
		filter.showParameters(List.of(""));
		filter.groupFilter(GroupFilter.builder().operator(GroupFilter.Operator.AND)
				.filterElements(Arrays.asList(FilterElements.builder().key("id").value(chatId)
						.type(FilterElements.FilterType.INTEGER).operator(FilterElements.OperatorType.EQUALS).build()))
				.build());
		ResponseMessage response = chatclient.get(filter.build(), 0);
		ChatDTO chat = null;
		if (response.isOK()) {
			Response<ChatDTO> res = (Response<ChatDTO>) response.getMessage();
			chat = res.getResults().get(0);
		}
		
		chat.setStatus(false);
		ResponseMessage respChat = chatclient.edit(chat, user.getId());
		
		if (!respChat.isOK()) {
			return ResponseEntity.internalServerError().body("Error when rejecting chat");
		}
		
		//Create block		
		BlockByUserDTO block = BlockByUserDTO.builder().blocked(UserDTO.builder().id(userId).build())
				.reporter(UserDTO.builder().id(user.getId()).build())
				.literal(LiteralDTO.builder().id(Integer.parseInt(reason)).build())
				.build();
		
		ResponseMessage respBlock = blockclient.create(block, user.getId());
		if (!respBlock.isOK()) {
			return ResponseEntity.internalServerError().body("Error when creating block");
		}
		
		return ResponseEntity.ok("Block user ok");
	    
	}
	
}