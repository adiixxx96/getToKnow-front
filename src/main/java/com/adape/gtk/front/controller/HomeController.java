package com.adape.gtk.front.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.adape.gtk.core.client.beans.CategoryDTO;
import com.adape.gtk.core.client.beans.CategoryIconMapper;
import com.adape.gtk.core.client.beans.EventDTO;
import com.adape.gtk.core.client.beans.Filter;
import com.adape.gtk.core.client.beans.FilterElements;
import com.adape.gtk.core.client.beans.GroupFilter;
import com.adape.gtk.core.client.beans.NotificationDTO;
import com.adape.gtk.core.client.beans.Page;
import com.adape.gtk.core.client.beans.Response;
import com.adape.gtk.core.client.beans.ResponseMessage;
import com.adape.gtk.core.client.beans.Sorting;
import com.adape.gtk.core.client.beans.TagDTO;
import com.adape.gtk.core.client.beans.UserByEventDTO;
import com.adape.gtk.core.client.beans.UserDTO;
import com.adape.gtk.core.client.beans.Sorting.Order;
import com.adape.gtk.core.client.service.CategoryIntService;
import com.adape.gtk.core.client.service.EventIntService;
import com.adape.gtk.core.client.service.NotificationIntService;
import com.adape.gtk.front.utils.Utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

	Logger log = LoggerFactory.getLogger(HomeController.class);
	
	@Autowired private EventIntService eventclient;
	@Autowired private CategoryIntService categoryclient;
	@Autowired private NotificationIntService notificationclient;
	
	@RequestMapping(value="/")
    public String homePage(Model model, HttpSession session, HttpServletRequest request) {
		
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
			model.addAttribute("user", user);
		}
		
		model.addAttribute("isLogged", isLogged);
        model.addAttribute("isAdmin", isAdmin);
        
        //If logged, get notifications
        if (isLogged) {
        	List<NotificationDTO> notifications = new ArrayList<>();
        	notifications = getNotificationsByUser(user.getId());       	
        	model.addAttribute("notifications", notifications);
        }
        
        //Get 6 newest events for future
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String currentDateString = dateFormat.format(currentDate);
        
        List<EventDTO> events = new ArrayList<>();
        
        Filter filterEvents = Filter.builder()
    			.groupFilter(GroupFilter.builder()
    					.operator(GroupFilter.Operator.AND)
    					.filterElements(Arrays.asList(FilterElements.builder()
    							.key("eventDate")
    							.value(currentDateString)
    							.type(FilterElements.FilterType.DATE_STRING)
    							.operator(FilterElements.OperatorType.GREATER_THAN_EQUALS).build()
    							)).build())
    			.showParameters(List.of("users.user"))
    			.page(Page.builder().pageNo(0).pageSize(6).build())
    			.sorting(List.of(Sorting.builder().field("eventDate").order(Order.ASC).build()))
    			.build();
        
        ResponseMessage rEvent = eventclient.get(filterEvents, 0);
		if (rEvent.isOK()) {
			Response<EventDTO> respEvent = (Response<EventDTO>) rEvent.getMessage();
			events = respEvent.getResults();
			model.addAttribute("events", events);
		}
		
		//Set participants number
		for (EventDTO event : events) {
			List<UserByEventDTO> usersList = new ArrayList<>();
			if (event.getUsers() != null) {
				usersList = event.getUsers();
			}
			List<UserDTO> eventParticipants = new ArrayList<>();
			for (UserByEventDTO userByEvent : usersList) {
				if (userByEvent.getParticipant() && userByEvent.getDeregistrationDate() == null) {
					eventParticipants.add(userByEvent.getUser());
				}
			}
			event.setParticipantsNumber(eventParticipants.size());
		}
		
		//Get active categories
        Filter filterCats = Filter.builder()
    			.groupFilter(GroupFilter.builder()
    					.operator(GroupFilter.Operator.AND)
    					.filterElements(Arrays.asList(FilterElements.builder()
    							.key("active")
    							.value(true)
    							.type(FilterElements.FilterType.BOOLEAN)
    							.operator(FilterElements.OperatorType.EQUALS).build()
    							)).build())
    			.showParameters(List.of(""))
    			.page(Page.builder().pageNo(0).pageSize(Integer.MAX_VALUE).build())
    			.sorting(List.of(Sorting.builder().field("id").order(Order.ASC).build()))
    			.build();
        
        ResponseMessage rCat = categoryclient.get(filterCats, 0);
		if (rCat.isOK()) {
			Response<CategoryDTO> respCat = (Response<CategoryDTO>) rCat.getMessage();
			model.addAttribute("categories", respCat.getResults());
		}
		
		//Get icons to map with categories
		 model.addAttribute("iconMapper", CategoryIconMapper.class);
	
        return "pages/home";
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