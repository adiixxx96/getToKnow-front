package com.adape.gtk.front.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.adape.gtk.core.client.beans.Filter;
import com.adape.gtk.core.client.beans.FilterElements;
import com.adape.gtk.core.client.beans.GroupFilter;
import com.adape.gtk.core.client.beans.NotificationDTO;
import com.adape.gtk.core.client.beans.Page;
import com.adape.gtk.core.client.beans.Response;
import com.adape.gtk.core.client.beans.ResponseMessage;
import com.adape.gtk.core.client.beans.Sorting;
import com.adape.gtk.core.client.beans.UserDTO;
import com.adape.gtk.core.client.beans.Sorting.Order;
import com.adape.gtk.core.client.service.NotificationIntService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping({ "/privacyPolicy" })
public class PrivacyPolicyController {

	Logger log = LoggerFactory.getLogger(PrivacyPolicyController.class);
	
	@Autowired private NotificationIntService notificationclient;
	
	@RequestMapping(value="/")
	public String aboutUs(Model model, HttpSession session, HttpServletRequest request) {
		 
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
		}
		model.addAttribute("isLogged", isLogged);
        model.addAttribute("isAdmin", isAdmin);
        
      //If logged, get notifications
        if (isLogged) {
        	List<NotificationDTO> notifications = new ArrayList<>();
        	notifications = getNotificationsByUser(user.getId());       	
        	model.addAttribute("notifications", notifications);
        }
		
		return "pages/privacyPolicy";
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