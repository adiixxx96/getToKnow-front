package com.adape.gtk.front.controller;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.adape.gtk.core.client.beans.CategoryDTO;
import com.adape.gtk.core.client.beans.CategoryIconMapper;
import com.adape.gtk.core.client.beans.CommentDTO;
import com.adape.gtk.core.client.beans.EventDTO;
import com.adape.gtk.core.client.beans.Filter;
import com.adape.gtk.core.client.beans.Filter.FilterBuilder;
import com.adape.gtk.core.client.beans.FilterElements;
import com.adape.gtk.core.client.beans.FilterElements.FilterType;
import com.adape.gtk.core.client.beans.FilterElements.OperatorType;
import com.adape.gtk.core.client.beans.GroupFilter;
import com.adape.gtk.core.client.beans.GroupFilter.GroupFilterBuilder;
import com.adape.gtk.core.client.beans.GroupFilter.Operator;
import com.adape.gtk.core.client.beans.Page;
import com.adape.gtk.core.client.beans.ProvinceEnum;
import com.adape.gtk.core.client.beans.Response;
import com.adape.gtk.core.client.beans.ResponseMessage;
import com.adape.gtk.core.client.beans.Sorting;
import com.adape.gtk.core.client.beans.UserDTO;
import com.adape.gtk.core.client.beans.Sorting.Order;
import com.adape.gtk.core.client.beans.TagByEventDTO;
import com.adape.gtk.core.client.beans.TagDTO;
import com.adape.gtk.core.client.beans.UserByEventDTO;
import com.adape.gtk.core.client.service.CategoryIntService;
import com.adape.gtk.core.client.service.CommentIntService;
import com.adape.gtk.core.client.service.EventIntService;
import com.adape.gtk.core.client.service.TagIntService;
import com.adape.gtk.core.client.service.UserIntService;
import com.adape.gtk.front.beans.CommentData;
import com.adape.gtk.front.beans.EventData;
import com.adape.gtk.front.beans.EventFilters;
import com.adape.gtk.front.utils.Utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping({ "/user" })
public class UserController {

	Logger log = LoggerFactory.getLogger(UserController.class);

	@Autowired
	private UserIntService userclient;
	@Autowired
	private EventIntService eventclient;


	@SuppressWarnings("unchecked")
	@GetMapping("/profile/")
	public String profile(Model model, HttpSession session, HttpServletRequest request) {
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
			log.error("User not logged, forbidden access to profile");
			return "redirect:/custom-error";
		}
		
		model.addAttribute("isLogged", isLogged);
		model.addAttribute("isAdmin", isAdmin);
		model.addAttribute("user", user);

		// Get user events
		FilterBuilder filter = Filter.builder();
		filter.page(Page.builder().pageNo(0).pageSize(Integer.MAX_VALUE).build());
		filter.sorting(List.of(Sorting.builder().field("id").order(Order.DESC).build()));
		filter.showParameters(List.of("users.user", "category"));
		filter.groupFilter(GroupFilter.builder().operator(GroupFilter.Operator.AND)
				.filterElements(Arrays.asList(FilterElements.builder().key("users.user.id").value(user.getId())
						.type(FilterElements.FilterType.INTEGER).operator(FilterElements.OperatorType.EQUALS).build()))
				.build());
		ResponseMessage response = eventclient.get(filter.build(), 0);
		
		List<EventDTO> events = new ArrayList<>();
		if (response.isOK()) {
			Response<EventDTO> res = (Response<EventDTO>) response.getMessage();
			events = res.getResults();		
		}
		
		if (!events.isEmpty()) {
			List<EventDTO> eventsOwner = new ArrayList<>();
			List<EventDTO> eventsParticipant = new ArrayList<>();
			
			for (EventDTO event : events) {
				for (UserByEventDTO u : event.getUsers()) {
					if (u.getUser().getId().equals(user.getId())) {
						if (u.getOwner()) {
							eventsOwner.add(event);
						} else {
							eventsParticipant.add(event);
						}
					}
				}
			}			
			model.addAttribute("eventsOwner", eventsOwner);
			model.addAttribute("eventsParticipant", eventsParticipant);
		}

		return "pages/user/userProfile";
	}

}