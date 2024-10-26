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
import com.adape.gtk.front.beans.CommentData;
import com.adape.gtk.front.beans.EventData;
import com.adape.gtk.front.beans.EventFilters;
import com.adape.gtk.front.utils.Utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping({ "/event" })
public class EventController {

	Logger log = LoggerFactory.getLogger(EventController.class);

	@Autowired
	private CategoryIntService categoryclient;
	@Autowired
	private TagIntService tagclient;
	@Autowired
	private EventIntService eventclient;
	@Autowired
	private CommentIntService commentclient;

	@RequestMapping(value = "/list", method = { RequestMethod.GET, RequestMethod.POST })
	public String eventController(@RequestParam(value = "category", required = false) Integer categoryId, Model model,
			HttpSession session, HttpServletRequest request) {

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
		model.addAttribute("initialLoad", true);

		// Get provinces to list in filter
		model.addAttribute("provinces", ProvinceEnum.values());

		// Get active categories to list in filter
		Filter filterCats = Filter.builder().groupFilter(GroupFilter.builder().operator(GroupFilter.Operator.AND)
				.filterElements(Arrays.asList(FilterElements.builder().key("active").value(true)
						.type(FilterElements.FilterType.BOOLEAN).operator(FilterElements.OperatorType.EQUALS).build()))
				.build()).showParameters(List.of("")).page(Page.builder().pageNo(0).pageSize(Integer.MAX_VALUE).build())
				.sorting(List.of(Sorting.builder().field("id").order(Order.ASC).build())).build();

		ResponseMessage rCat = categoryclient.get(filterCats, 0);
		if (rCat.isOK()) {
			Response<CategoryDTO> respCat = (Response<CategoryDTO>) rCat.getMessage();
			model.addAttribute("categories", respCat.getResults());
		}

		// Get active tags to list in filter
		Filter filterTags = Filter.builder().groupFilter(GroupFilter.builder().operator(GroupFilter.Operator.AND)
				.filterElements(Arrays.asList(FilterElements.builder().key("active").value(true)
						.type(FilterElements.FilterType.BOOLEAN).operator(FilterElements.OperatorType.EQUALS).build()))
				.build()).showParameters(List.of("")).page(Page.builder().pageNo(0).pageSize(Integer.MAX_VALUE).build())
				.sorting(List.of(Sorting.builder().field("tag").order(Order.ASC).build())).build();

		ResponseMessage rTag = tagclient.get(filterTags, 0);
		if (rTag.isOK()) {
			Response<TagDTO> respTag = (Response<TagDTO>) rTag.getMessage();
			model.addAttribute("tags", respTag.getResults());
		}

		FilterBuilder filter = Filter.builder();

		Page page = new Page(0, 9);

		page.setPageNo(page.getPageNo() / page.getPageSize());
		filter.page(page);

		Sorting sort = new Sorting("id", Sorting.Order.DESC);
		filter.sorting(List.of(sort));

		GroupFilterBuilder groupFilterMain = GroupFilter.builder();
		groupFilterMain.operator(Operator.AND);

		GroupFilterBuilder groupFilter = GroupFilter.builder();
		groupFilter.operator(Operator.AND);

		List<FilterElements> listElement = new ArrayList<FilterElements>();

		// If param category, get events by category
		if (categoryId != null) {
			listElement.add(FilterElements.builder().key("category.id").value(categoryId).operator(OperatorType.EQUALS)
					.type(FilterType.INTEGER).build());

			model.addAttribute("selectedCategoryId", categoryId);
		}

		// Get all future events
		Date currentDate = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		String currentDateString = dateFormat.format(currentDate);

		listElement.add(FilterElements.builder().key("eventDate").value(currentDateString)
				.operator(OperatorType.GREATER_THAN_EQUALS).type(FilterType.DATE_STRING).build());

		groupFilter.filterElements(listElement);

		List<GroupFilter> groups = new ArrayList<GroupFilter>();
		groups.add(groupFilter.build());

		groupFilterMain.groupFilter(groups);
		filter.groupFilter(groupFilterMain.build());

		filter.showParameters(List.of("users.user"));

		log.info(groupFilterMain.toString());

		List<EventDTO> eventsFiltered = new ArrayList<EventDTO>();

		long totalPages = 1;

		ResponseMessage response = eventclient.get(filter.build(), 0);
		if (response.isOK()) {
			Response<EventDTO> respEvent = (Response<EventDTO>) response.getMessage();
			eventsFiltered.addAll(respEvent.getResults());
			long totalResults = respEvent.getSize();
			int pageSize = 9;
			totalPages = (long) Math.ceil((double) totalResults / pageSize);
			model.addAttribute("totalPages", totalPages);
		} else {
			log.info("No events meet the filter criteria");
		}

		model.addAttribute("currentPage", 1);
		model.addAttribute("events", eventsFiltered);

		// Set participants number
		for (EventDTO event : eventsFiltered) {
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

		return "/pages/event/eventList";
	}

	@RequestMapping(value = "/search", method = { RequestMethod.GET, RequestMethod.POST })
	public String eventSearch(@RequestBody(required = false) EventFilters params, Model model, HttpSession session,
			HttpServletRequest request) {

		String requestURI = request.getRequestURI();
		model.addAttribute("requestURI", requestURI);

		boolean isLogged = false;

		UserDTO user = (UserDTO) session.getAttribute("user");
		if (user != null) {
			isLogged = true;
		}
		model.addAttribute("isLogged", isLogged);

		model.addAttribute("initialLoad", false);

		FilterBuilder filter = Filter.builder();
		int pageNo = params.getPage() - 1;
		if (pageNo == -1) pageNo = 0;
		Page page = new Page(pageNo, 9);
		filter.page(page);
		Sorting sort = new Sorting("id", Sorting.Order.DESC);
		filter.sorting(List.of(sort));
		List<FilterElements> listElement = new ArrayList<>();

		model.addAttribute("currentPage", params.getPage());

		// *****************Inicio lógica filtros*****************

		String body = params.getBody().trim();
		int provinceId = params.getProvince();
		String city = params.getCity().trim();
		String eventDateFrom = params.getEventDateFrom();
		String eventDateTo = params.getEventDateTo();
		int participantsFrom = params.getParticipantsFrom();
		int participantsTo = params.getParticipantsTo();
		BigDecimal priceFrom = params.getPriceFrom();
		BigDecimal priceTo = params.getPriceTo();
		List<Integer> categories = params.getCategories();
		List<Integer> tags = params.getTags();

		// Body
		if (!body.isEmpty()) {

			// Si va entrecomillado lo trataremos como búsqueda literal, sino como palabras
			// sueltas
			boolean isQuoted = body.startsWith("\"") && body.endsWith("\"");
			List<String> words = new ArrayList<>();
			if (isQuoted) {
				words.add(body.substring(1, body.length() - 1).trim());
			} else {
				words = Arrays.asList(body.split("\\s+"));
			}

			// Search for events with the all the words in title or description
			List<Integer> eventList = getEventIdsByBody(words);

			listElement.add(FilterElements.builder().key("id").value(eventList).operator(OperatorType.IN)
					.type(FilterType.INTEGER_LIST).build());
		}

		// Province
		if (provinceId != 0) {
			listElement.add(FilterElements.builder().key("province").value(provinceId).operator(OperatorType.EQUALS)
					.type(FilterType.INTEGER).build());
		}

		// City
		if (!city.isEmpty()) {
			listElement.add(FilterElements.builder().key("city").value(city).operator(OperatorType.CONTAINS)
					.type(FilterType.STRING).build());
		}

		// Event Date
		if (!eventDateFrom.isEmpty() || !eventDateTo.isEmpty()) {

			if (!eventDateFrom.isEmpty()) {
				listElement.add(FilterElements.builder().key("eventDate").value(eventDateFrom)
						.operator(OperatorType.GREATER_THAN_EQUALS).type(FilterType.DATE_STRING).build());

			}
			if (!eventDateTo.isEmpty()) {
				listElement.add(FilterElements.builder().key("eventDate").value(eventDateTo)
						.operator(OperatorType.LESS_THAN_EQUALS).type(FilterType.DATE_STRING).build());
			}

		} else {
			Date currentDate = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
			String currentDateString = dateFormat.format(currentDate);

			listElement.add(FilterElements.builder().key("eventDate").value(currentDateString)
					.operator(OperatorType.GREATER_THAN_EQUALS).type(FilterType.DATE_STRING).build());
		}

		// Categories
		if (!categories.isEmpty()) {

			listElement.add(FilterElements.builder().key("category.id").value(categories).operator(OperatorType.IN)
					.type(FilterType.INTEGER_LIST).build());
		}

		// Tags
		if (!tags.isEmpty()) {

			listElement.add(FilterElements.builder().key("tags.tag.id").value(tags).operator(OperatorType.IN)
					.type(FilterType.INTEGER_LIST).build());
		}

		// Price
		if (priceFrom.compareTo(BigDecimal.ZERO) != 0 || priceTo.compareTo(new BigDecimal("500")) != 0) {
			if (priceFrom.compareTo(BigDecimal.ZERO) != 0) {
				listElement.add(FilterElements.builder().key("price").value(priceFrom.doubleValue())
						.operator(OperatorType.GREATER_THAN_EQUALS).type(FilterType.DOUBLE).build());
			}
			if (priceTo.compareTo(new BigDecimal("500")) != 0) {
				listElement.add(FilterElements.builder().key("price").value(priceTo.doubleValue())
						.operator(OperatorType.LESS_THAN_EQUALS).type(FilterType.DOUBLE).build());
			}
		}

		// Participants
		if (participantsFrom != 0 || participantsTo != 100) {

			List<Integer> eventList = getEventIdsByParticipantsNumber(participantsFrom, participantsTo);

			listElement.add(FilterElements.builder().key("id").value(eventList).operator(OperatorType.IN)
					.type(FilterType.INTEGER_LIST).build());
		}

		// *****************Fin lógica filtros*****************

		if (!listElement.isEmpty()) {
			GroupFilterBuilder mainGroupFilter = GroupFilter.builder().operator(Operator.AND);
			mainGroupFilter.filterElements(listElement);
			filter.groupFilter(mainGroupFilter.build());
		}

		filter.showParameters(List.of("users.user"));
		log.info(filter.toString());

		List<EventDTO> eventsFiltered = new ArrayList<EventDTO>();
		long totalPages = 1;

		ResponseMessage response = eventclient.get(filter.build(), 0);
		if (response.isOK()) {
			Response<EventDTO> respEvent = (Response<EventDTO>) response.getMessage();
			eventsFiltered.addAll(respEvent.getResults());
			long totalResults = respEvent.getSize();
			int pageSize = 9;
			totalPages = (long) Math.ceil((double) totalResults / pageSize);
			model.addAttribute("totalPages", totalPages);
		} else {
			log.info("No events meet the filter criteria");
		}

		model.addAttribute("events", eventsFiltered);

		// Set participants number
		for (EventDTO event : eventsFiltered) {
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

		return "fragments/eventListCards :: eventListCards";
	}

	@RequestMapping(value = "/create")
	public String createEventPage(Model model, HttpSession session, HttpServletRequest request) {

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
			// User not logued -> no permission to create event
			return "redirect:/custom-error";
		}

		model.addAttribute("isLogged", isLogged);
		model.addAttribute("isAdmin", isAdmin);

		// Get provinces
		model.addAttribute("provinces", ProvinceEnum.values());

		// Get active categories
		Filter filterCats = Filter.builder().groupFilter(GroupFilter.builder().operator(GroupFilter.Operator.AND)
				.filterElements(Arrays.asList(FilterElements.builder().key("active").value(true)
						.type(FilterElements.FilterType.BOOLEAN).operator(FilterElements.OperatorType.EQUALS).build()))
				.build()).showParameters(List.of("")).page(Page.builder().pageNo(0).pageSize(Integer.MAX_VALUE).build())
				.sorting(List.of(Sorting.builder().field("id").order(Order.ASC).build())).build();

		ResponseMessage rCat = categoryclient.get(filterCats, user.getId());
		if (rCat.isOK()) {
			Response<CategoryDTO> respCat = (Response<CategoryDTO>) rCat.getMessage();
			model.addAttribute("categories", respCat.getResults());
		}

		// Get icons to map with categories
		model.addAttribute("iconMapper", CategoryIconMapper.class);

		// Get active tags
		Filter filterTags = Filter.builder().groupFilter(GroupFilter.builder().operator(GroupFilter.Operator.AND)
				.filterElements(Arrays.asList(FilterElements.builder().key("active").value(true)
						.type(FilterElements.FilterType.BOOLEAN).operator(FilterElements.OperatorType.EQUALS).build()))
				.build()).showParameters(List.of("")).page(Page.builder().pageNo(0).pageSize(Integer.MAX_VALUE).build())
				.sorting(List.of(Sorting.builder().field("id").order(Order.ASC).build())).build();

		ResponseMessage rTag = tagclient.get(filterTags, user.getId());
		if (rTag.isOK()) {
			Response<TagDTO> respTag = (Response<TagDTO>) rTag.getMessage();
			model.addAttribute("tags", respTag.getResults());
		}

		return "pages/event/eventCreate";
	}

	@PostMapping("/publish")
	public String save(@ModelAttribute EventData eventData, RedirectAttributes attr, HttpSession session) {

		UserDTO user = (UserDTO) session.getAttribute("user");
		if (user == null) {
			// User not logued -> no permission to create event
			return "redirect:/custom-error";
		}

		EventDTO event = parseEventDataToEvent(eventData, user);
		
		int eventId = 0;
		//Create or edit event
		if (event.getId() != null) {
			event.setUsers(null);
			ResponseMessage resp = eventclient.edit(event, user.getId());
			
			if (!resp.isOK()) {
				log.error("Error editing event");
				attr.addFlashAttribute("error", Utils.message("event.edit.error", resp.getMessage().toString()));
				return "redirect:/event/list/";
			} else {
				log.info("Event successfully edited");
				attr.addFlashAttribute("success", Utils.message("event.edit.ok"));
				eventId = event.getId();
			}
			
		} else {
			ResponseMessage resp = eventclient.create(event, user.getId());
			
			if (!resp.isOK()) {
				log.error("Error creating event");
				attr.addFlashAttribute("error", Utils.message("event.create.error", resp.getMessage().toString()));
				return "redirect:/event/list/";
			} else {
				log.info("Event successfully created");
				attr.addFlashAttribute("success", Utils.message("event.create.ok"));
				EventDTO eventCreated = (EventDTO) resp.getMessage();
				eventId = eventCreated.getId();
			}
		}

		return "redirect:/event/detail/" + eventId;
	}
	
	@PostMapping("/saveComment")
	public String saveComment(@ModelAttribute CommentData commentData, RedirectAttributes attr, HttpSession session) {

		UserDTO user = (UserDTO) session.getAttribute("user");
		if (user == null) {
			// User not logued -> no permission to publish comment
			return "redirect:/event/detail/" + commentData.getEventId();
		}

		CommentDTO comment = parseCommentDataToComment(commentData, user);

		ResponseMessage resp = commentclient.create(comment, user.getId());

		if (!resp.isOK()) {
			log.error("Error creating comment");
			attr.addFlashAttribute("error", Utils.message("comment.create.error", resp.getMessage().toString()));
			return "redirect:/event/detail/" + commentData.getEventId();
		} else {
			log.info("Comment successfully created");
			attr.addFlashAttribute("success", Utils.message("comment.create.ok"));
		}

		return "redirect:/event/detail/" + commentData.getEventId();
	}

	@SuppressWarnings("unchecked")
	@GetMapping("/detail/{id}")
	public String detail(@PathVariable("id") Integer id, Model model, HttpSession session, HttpServletRequest request) {
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
		model.addAttribute("user", user);

		// Get event with relations
		FilterBuilder filter = Filter.builder();
		filter.page(Page.builder().pageNo(0).pageSize(Integer.MAX_VALUE).build());
		filter.sorting(List.of(Sorting.builder().field("id").order(Order.DESC).build()));
		filter.showParameters(List.of("users.user", "tags.tag", "category", "comments.parent", "comments.user"));
		filter.groupFilter(GroupFilter.builder().operator(GroupFilter.Operator.AND)
				.filterElements(Arrays.asList(FilterElements.builder().key("id").value(id)
						.type(FilterElements.FilterType.INTEGER).operator(FilterElements.OperatorType.EQUALS).build()))
				.build());
		ResponseMessage response = eventclient.get(filter.build(), 0);
		EventDTO event = null;
		if (response.isOK()) {
			Response<EventDTO> res = (Response<EventDTO>) response.getMessage();
			event = res.getResults().get(0);
		}
		model.addAttribute("event", event);

		// Obtain province name
		String province = "";
		if (event.getProvince() != null) {
			ProvinceEnum provinceEnum = ProvinceEnum.setProvince(event.getProvince());
			province = provinceEnum.getName();
		}
		model.addAttribute("province", province);

		// Check if logged user is participant or owner of project
		boolean joinedUser = false;

		List<UserByEventDTO> usersList = new ArrayList<>();
		if (event.getUsers() != null) {
			usersList = event.getUsers();
		}

		if (isLogged) {
			for (UserByEventDTO userByEvent : usersList) {
				if (userByEvent.getUser().getId().equals(user.getId()) && userByEvent.getDeregistrationDate() == null) {
					joinedUser = true;
					break;
				}
			}
		}

		// Save owner and participants and set age to use in participants list
		UserDTO eventOwner = null;
		List<UserDTO> eventParticipants = new ArrayList<>();
		usersList.sort(Comparator.comparing(userByEvent -> userByEvent.getRegistrationDate()));
		for (UserByEventDTO userByEvent : usersList) {
			int age = Utils.calculateAge(userByEvent.getUser().getBirthDate());
			userByEvent.getUser().setAge(age);
			if (userByEvent.getOwner()) {
				eventOwner = userByEvent.getUser();
			} else if (userByEvent.getDeregistrationDate() == null) {
				eventParticipants.add(userByEvent.getUser());
			}
		}

		// Comments and replies
		List<CommentDTO> comments = new ArrayList<>();
		if (event.getComments() != null && !event.getComments().isEmpty()) {
			comments = event.getComments();
		}

		List<CommentDTO> parentComments = new ArrayList<>();
		Map<Integer, List<CommentDTO>> repliesMap = new HashMap<>();

		for (CommentDTO comment : comments) {
			if (comment.getParent() == null) {
				parentComments.add(comment);
			} else {
				repliesMap.computeIfAbsent(comment.getParent().getId(), k -> new ArrayList<>()).add(comment);
			}
		}
		parentComments.sort((a, b) -> b.getCreationDate().compareTo(a.getCreationDate()));
		for (List<CommentDTO> replies : repliesMap.values()) {
			replies.sort((a, b) -> a.getCreationDate().compareTo(b.getCreationDate()));
		}

		model.addAttribute("joinedUser", joinedUser);
		model.addAttribute("eventOwner", eventOwner);
		model.addAttribute("eventParticipants", eventParticipants);
		model.addAttribute("parentComments", parentComments);
		model.addAttribute("repliesMap", repliesMap);

		return "pages/event/eventDetail";
	}

	@SuppressWarnings("unchecked")
	@PostMapping("/join/{id}")
	public ResponseEntity<?> joinEvent(@PathVariable("id") Integer eventId, Model model, HttpSession session) {

		UserDTO user = (UserDTO) session.getAttribute("user");
		if (user == null) {
			// User not logued -> no permission to join event
			return ResponseEntity.internalServerError().body("User has no permission to join events");
		}
		try {
			// Obtain event with users parameters to add the new one
			FilterBuilder filter = Filter.builder();
			filter.page(Page.builder().pageNo(0).pageSize(Integer.MAX_VALUE).build());
			filter.sorting(List.of(Sorting.builder().field("id").order(Order.DESC).build()));
			filter.showParameters(List.of("users.user"));
			filter.groupFilter(
					GroupFilter.builder().operator(GroupFilter.Operator.AND)
							.filterElements(Arrays.asList(FilterElements.builder().key("id").value(eventId)
									.type(FilterElements.FilterType.INTEGER)
									.operator(FilterElements.OperatorType.EQUALS).build()))
							.build());
			ResponseMessage response = eventclient.get(filter.build(), user.getId());
			EventDTO event = null;
			if (response.isOK()) {
				Response<EventDTO> res = (Response<EventDTO>) response.getMessage();
				event = res.getResults().get(0);
			}
			if (event != null) {

				List<UserByEventDTO> usersList = new ArrayList<UserByEventDTO>();
				if (event.getUsers() != null)
					usersList = event.getUsers();

				// Check if user is already in the event
				Optional<UserByEventDTO> userFound = usersList.stream()
						.filter(u -> u.getUser().getId().equals(user.getId())).findFirst();

				if (userFound.isPresent()) {
					UserByEventDTO userByEventDTO = userFound.get();
					// Check if is duplicated or if was deregistered
					if (userByEventDTO.getDeregistrationDate() == null) {
						log.error("User duplicated in the event");
						return ResponseEntity.internalServerError().body("User duplicated in the event");
					} else {
						usersList.remove(userByEventDTO);
						log.info("User removed from the event list as it was previously deregistered");
					}
				}

				// Add new user and edit event
				usersList.add(UserByEventDTO.builder().user(UserDTO.builder().id(user.getId()).build())
						.event(EventDTO.builder().id(eventId).build()).owner(false).participant(true)
						.registrationDate(new Timestamp(System.currentTimeMillis())).build());

				event.setUsers(usersList);
				eventclient.edit(event, user.getId());

			} else {
				log.error("Error joining event");
				return ResponseEntity.internalServerError().body("Error joining event");
			}

		} catch (Exception e) {
			log.error("Error joining event");
			return ResponseEntity.internalServerError().body("Error joining event");
		}

		return ResponseEntity.ok("Join event ok");

	}

	@SuppressWarnings("unchecked")
	@PostMapping("/disjoin/{id}")
	public ResponseEntity<?> disjoinEvent(@PathVariable("id") Integer eventId, Model model, HttpSession session) {

		UserDTO user = (UserDTO) session.getAttribute("user");
		if (user == null) {
			// User not logued -> no permission to disjoin event
			return ResponseEntity.internalServerError().body("User has no permission to disjoin events");
		}
		try {
			// Obtain event with users parameters to remove the user
			FilterBuilder filter = Filter.builder();
			filter.page(Page.builder().pageNo(0).pageSize(Integer.MAX_VALUE).build());
			filter.sorting(List.of(Sorting.builder().field("id").order(Order.DESC).build()));
			filter.showParameters(List.of("users.user"));
			filter.groupFilter(
					GroupFilter.builder().operator(GroupFilter.Operator.AND)
							.filterElements(Arrays.asList(FilterElements.builder().key("id").value(eventId)
									.type(FilterElements.FilterType.INTEGER)
									.operator(FilterElements.OperatorType.EQUALS).build()))
							.build());
			ResponseMessage response = eventclient.get(filter.build(), user.getId());
			EventDTO event = null;
			if (response.isOK()) {
				Response<EventDTO> res = (Response<EventDTO>) response.getMessage();
				event = res.getResults().get(0);
			}
			if (event != null) {

				List<UserByEventDTO> usersList = new ArrayList<UserByEventDTO>();
				if (event.getUsers() != null)
					usersList = event.getUsers();

				// Remove the user who is disjoining
				Timestamp registrationDate = new Timestamp(System.currentTimeMillis());
				for (int i = 0; i < usersList.size(); i++) {
					UserByEventDTO userByEvent = usersList.get(i);
					if (userByEvent.getUser().getId().equals(user.getId())) {
						registrationDate = userByEvent.getRegistrationDate();
						usersList.remove(i);
						break;
					}
				}

				// Add the user who disjoined updating deregistration
				usersList.add(UserByEventDTO.builder().user(UserDTO.builder().id(user.getId()).build())
						.event(EventDTO.builder().id(eventId).build()).owner(false).participant(true)
						.registrationDate(registrationDate)
						.deregistrationDate(new Timestamp(System.currentTimeMillis())).deregistrationVoluntary(true)
						.build());

				event.setUsers(usersList);
				eventclient.edit(event, user.getId());

			} else {
				log.error("Error disjoining event");
				return ResponseEntity.internalServerError().body("Error disjoining event");
			}

		} catch (Exception e) {
			log.error("Error disjoining event");
			return ResponseEntity.internalServerError().body("Error disjoining event");
		}

		return ResponseEntity.ok("Disjoin event ok");

	}
	
	@RequestMapping(value = "/edit/{id}")
	public String editEventPage(@PathVariable("id") Integer id, Model model, HttpSession session, HttpServletRequest request) {

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
			// User not logued -> no permission to edit event
			return "redirect:/custom-error";
		}

		model.addAttribute("isLogged", isLogged);
		model.addAttribute("isAdmin", isAdmin);
		
		// Get event to edit
		FilterBuilder filter = Filter.builder();
		filter.page(Page.builder().pageNo(0).pageSize(Integer.MAX_VALUE).build());
		filter.sorting(List.of(Sorting.builder().field("id").order(Order.DESC).build()));
		filter.showParameters(List.of("users.user", "tags.tag", "category"));
		filter.groupFilter(GroupFilter.builder().operator(GroupFilter.Operator.AND)
				.filterElements(Arrays.asList(FilterElements.builder().key("id").value(id)
						.type(FilterElements.FilterType.INTEGER).operator(FilterElements.OperatorType.EQUALS).build()))
				.build());
		ResponseMessage response = eventclient.get(filter.build(), 0);
		EventDTO event = null;
		if (response.isOK()) {
			Response<EventDTO> res = (Response<EventDTO>) response.getMessage();
			event = res.getResults().get(0);
		}
		
		//Check if user is owner and allow to edit
		int ownerId = 0;
		for (UserByEventDTO userByEvent : event.getUsers()) {
			if (userByEvent.getOwner()) {
				ownerId = userByEvent.getUser().getId();
			}
		}	
		if (user.getId() != ownerId) {
			// User is not the owner of event -> no permission to edit event
			return "redirect:/custom-error";
		}
		
		model.addAttribute("event", event);

		// Get provinces
		model.addAttribute("provinces", ProvinceEnum.values());

		// Get active categories
		Filter filterCats = Filter.builder().groupFilter(GroupFilter.builder().operator(GroupFilter.Operator.AND)
				.filterElements(Arrays.asList(FilterElements.builder().key("active").value(true)
						.type(FilterElements.FilterType.BOOLEAN).operator(FilterElements.OperatorType.EQUALS).build()))
				.build()).showParameters(List.of("")).page(Page.builder().pageNo(0).pageSize(Integer.MAX_VALUE).build())
				.sorting(List.of(Sorting.builder().field("id").order(Order.ASC).build())).build();

		ResponseMessage rCat = categoryclient.get(filterCats, user.getId());
		if (rCat.isOK()) {
			Response<CategoryDTO> respCat = (Response<CategoryDTO>) rCat.getMessage();
			model.addAttribute("categories", respCat.getResults());
		}

		// Get icons to map with categories
		model.addAttribute("iconMapper", CategoryIconMapper.class);

		// Get active tags
		Filter filterTags = Filter.builder().groupFilter(GroupFilter.builder().operator(GroupFilter.Operator.AND)
				.filterElements(Arrays.asList(FilterElements.builder().key("active").value(true)
						.type(FilterElements.FilterType.BOOLEAN).operator(FilterElements.OperatorType.EQUALS).build()))
				.build()).showParameters(List.of("")).page(Page.builder().pageNo(0).pageSize(Integer.MAX_VALUE).build())
				.sorting(List.of(Sorting.builder().field("id").order(Order.ASC).build())).build();

		ResponseMessage rTag = tagclient.get(filterTags, user.getId());
		if (rTag.isOK()) {
			Response<TagDTO> respTag = (Response<TagDTO>) rTag.getMessage();
			model.addAttribute("tags", respTag.getResults());
		}
		
		//Get already selected tags for event
		List<Integer> selectedTagIds = new ArrayList<>();
		if (event.getTags() != null && !event.getTags().isEmpty()) {
			for (TagByEventDTO tbe : event.getTags()) {
				selectedTagIds.add(tbe.getTag().getId());
			}
		}
		String selectedTagIdsString = selectedTagIds.stream()
			    .map(String::valueOf)
			    .collect(Collectors.joining(","));

		model.addAttribute("selectedTagIds", selectedTagIds);
		model.addAttribute("selectedTagIdsString", selectedTagIdsString);
		
		return "pages/event/eventCreate";
	}

	private EventDTO parseEventDataToEvent(EventData eventData, UserDTO user) {

		EventDTO event = new EventDTO();

		//Id if edit
		if (eventData.getEventId() != null) {
			event.setId(eventData.getEventId());
		}	
		
		// Title
		event.setTitle(eventData.getTitle());

		// Description
		event.setDescription(eventData.getDescription());

		// Event date
		String eventDateString = eventData.getEventDate();
		Timestamp eventDate = null;

		Pattern pattern = Pattern.compile("(\\d{2}/\\d{2}/\\d{4})\\s+(\\d{2}:\\d{2})");
		Matcher matcher = pattern.matcher(eventDateString);

		if (matcher.find()) {
			String datePart = matcher.group(1);
			String timePart = matcher.group(2);

			SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

			try {
				String dateTime = datePart + " " + timePart;
				Date date = dateFormat.parse(dateTime);
				eventDate = new Timestamp(date.getTime());

			} catch (ParseException e) {
				e.printStackTrace();
			}
		} else {
			log.error("Formato de fecha y hora no válido.");
		}

		event.setEventDate(eventDate);

		// Creation date
		Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
		event.setCreationDate(currentTimestamp);

		// Max number of participants
		event.setMaxParticipants(eventData.getMaxParticipants());

		// Price
		event.setPrice(eventData.getPrice());

		// Address
		event.setAddress(eventData.getAddress());

		// City
		event.setCity(eventData.getCity());

		// Province
		event.setProvince(eventData.getProvince());

		// Category
		event.setCategory(CategoryDTO.builder().id(eventData.getCategory()).build());

		// Tags
		List<TagByEventDTO> tags = new ArrayList<>();
		for (Integer tagId : eventData.getTags()) {
			TagDTO tag = TagDTO.builder().id(tagId).build();
			TagByEventDTO tbe = new TagByEventDTO();
			tbe.setTag(tag);
			tags.add(tbe);
		}

		event.setTags(tags);

		// Owner
		List<UserByEventDTO> users = new ArrayList<>();
		UserByEventDTO ube = UserByEventDTO.builder().user(user).owner(true).participant(false)
				.registrationDate(currentTimestamp).build();
		users.add(ube);

		event.setUsers(users);

		// Image
		if (eventData.getImagePath() != null) {
			event.setImage(eventData.getImagePath());

		} else {
			// No image upload, assign image by default
			CategoryDTO eventCategory = null;
			ResponseMessage response = categoryclient.get(event.getCategory().getId(), user.getId());
			if (response.isOK()) {
				eventCategory = (CategoryDTO) response.getMessage();
			}
			String categoryName = eventCategory.getCategory();
			String image = "images/" + categoryName + ".jpg";
			event.setImage(image);
		}

		return event;
	}
	
	private CommentDTO parseCommentDataToComment(CommentData commentData, UserDTO user) {

		CommentDTO comment = new CommentDTO();

		// Comment
		if (commentData.getComment() != null) {
			comment.setComment(commentData.getComment());
		} else {
			comment.setComment(commentData.getReply());
		}			
		//Event
		comment.setEvent(EventDTO.builder().id(commentData.getEventId()).build());
		//User
		comment.setUser(user);
		//Parent comment
		if (commentData.getParentId() != null) {
			comment.setParent(CommentDTO.builder().id(commentData.getParentId()).build());
		}
		//Creation date
		Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
		comment.setCreationDate(currentTimestamp);

		return comment;
	}

	private EventDTO getEventRecentlyCreated(int userId) {

		EventDTO event = null;

		Filter filter = Filter.builder().groupFilter(GroupFilter.builder().operator(GroupFilter.Operator.AND)
				.filterElements(Arrays.asList(FilterElements.builder().key("users.user.id").value(userId)
						.type(FilterElements.FilterType.INTEGER).operator(FilterElements.OperatorType.EQUALS).build(),
						FilterElements.builder().key("users.owner").value(true).type(FilterElements.FilterType.BOOLEAN)
								.operator(FilterElements.OperatorType.EQUALS).build()))
				.build()).showParameters(List.of("")).page(Page.builder().pageNo(0).pageSize(Integer.MAX_VALUE).build())
				.sorting(List.of(Sorting.builder().field("id").order(Order.DESC).build())).build();

		ResponseMessage response = eventclient.get(filter, userId);
		if (response.isOK()) {
			Response<EventDTO> resp = (Response<EventDTO>) response.getMessage();
			event = resp.getResults().get(0);
		}

		return event;
	}

	@SuppressWarnings("unchecked")
	private List<Integer> getEventIdsByBody(List<String> words) {
		ResponseMessage response = eventclient.getEventIdsByBody(words);
		List<Integer> eventList = new ArrayList<>();
		if (response.isOK()) {
			eventList = (List<Integer>) response.getMessage();
		}
		return eventList;
	}

	@SuppressWarnings("unchecked")
	private List<Integer> getEventIdsByParticipantsNumber(int min, int max) {
		ResponseMessage response = eventclient.getEventIdsByParticipantsNumber(min, max);
		List<Integer> eventList = new ArrayList<>();
		if (response.isOK()) {
			eventList = (List<Integer>) response.getMessage();
		}
		return eventList;
	}
	
	

}