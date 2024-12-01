package com.adape.gtk.front.controller;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.adape.gtk.core.client.beans.BlockByUserDTO;
import com.adape.gtk.core.client.beans.ChatDTO;
import com.adape.gtk.core.client.beans.DeregistrationByUserDTO;
import com.adape.gtk.core.client.beans.EventDTO;
import com.adape.gtk.core.client.beans.Filter;
import com.adape.gtk.core.client.beans.Filter.FilterBuilder;
import com.adape.gtk.core.client.beans.FilterElements;
import com.adape.gtk.core.client.beans.GroupFilter;
import com.adape.gtk.core.client.beans.LiteralDTO;
import com.adape.gtk.core.client.beans.NotificationDTO;
import com.adape.gtk.core.client.beans.Page;
import com.adape.gtk.core.client.beans.ReportByEventDTO;
import com.adape.gtk.core.client.beans.Response;
import com.adape.gtk.core.client.beans.ResponseMessage;
import com.adape.gtk.core.client.beans.Sorting;
import com.adape.gtk.core.client.beans.UserDTO;
import com.adape.gtk.core.client.beans.Sorting.Order;
import com.adape.gtk.core.client.beans.UserByEventDTO;
import com.adape.gtk.core.client.service.BlockByUserIntService;
import com.adape.gtk.core.client.service.DeregistrationByUserIntService;
import com.adape.gtk.core.client.service.EventIntService;
import com.adape.gtk.core.client.service.NotificationIntService;
import com.adape.gtk.core.client.service.ReportByEventIntService;
import com.adape.gtk.core.client.service.UserIntService;
import com.adape.gtk.front.beans.UserData;
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
	@Autowired
	private BlockByUserIntService blockclient;
	@Autowired
	private ReportByEventIntService reportclient;
	@Autowired
	private DeregistrationByUserIntService deregistrationclient;
	@Autowired 
	private NotificationIntService notificationclient;

	@SuppressWarnings("unchecked")
	@GetMapping("/profile/")
	public String profile(Model model, HttpSession session, HttpServletRequest request) {
		String requestURI = request.getRequestURI();
		model.addAttribute("requestURI", requestURI);

		boolean isLogged = false;
		boolean isAdmin = false;

		UserDTO userSession = (UserDTO) session.getAttribute("user");
		if (userSession != null) {
			isLogged = true;
			if (userSession.getRole() != null && userSession.getRole()) {
				isAdmin = true;
			}
		} else {
			log.error("User not logged, forbidden access to profile");
			return "redirect:/custom-error";
		}

		UserDTO user = getUserById(userSession.getId());

		model.addAttribute("isLogged", isLogged);
		model.addAttribute("isAdmin", isAdmin);
		model.addAttribute("user", user);
		
		//If logged, get notifications
        if (isLogged) {
        	List<NotificationDTO> notifications = new ArrayList<>();
        	notifications = getNotificationsByUser(user.getId());       	
        	model.addAttribute("notifications", notifications);
        }

		// Format birthDate
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		String formattedBirthDate = dateFormat.format(user.getBirthDate());
		model.addAttribute("formattedBirthDate", formattedBirthDate);

		// Decode password
		String decodedPassword = decodePassword(user.getPassword());
		model.addAttribute("decodedPassword", decodedPassword);

		// Get user events
		FilterBuilder filter = Filter.builder();
		filter.page(Page.builder().pageNo(0).pageSize(Integer.MAX_VALUE).build());
		filter.sorting(List.of(Sorting.builder().field("eventDate").order(Order.ASC).build()));
		filter.showParameters(List.of("users.user"));
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
					if (u.getDeregistrationDate() == null && u.getUser().getId().equals(user.getId())) {
						if (u.getOwner()) {
							eventsOwner.add(event);
						} else {
							eventsParticipant.add(event);
						}
					}
				}
			}
			if (!eventsOwner.isEmpty()) {
				List<EventDTO> eventsOwnerFuture = new ArrayList<>();
				List<EventDTO> eventsOwnerPast = new ArrayList<>();
				Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());

				for (EventDTO event : eventsOwner) {
					if (currentTimestamp.before(event.getEventDate())) {
						eventsOwnerFuture.add(event);
					} else {
						eventsOwnerPast.add(event);
					}
				}
				if (!eventsOwnerFuture.isEmpty()) {
					model.addAttribute("eventsOwnerFuture", eventsOwnerFuture);
				}
				if (!eventsOwnerPast.isEmpty()) {
					model.addAttribute("eventsOwnerPast", eventsOwnerPast);
				}
			}
			if (!eventsParticipant.isEmpty()) {
				List<EventDTO> eventsParticipantFuture = new ArrayList<>();
				List<EventDTO> eventsParticipantPast = new ArrayList<>();
				Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());

				for (EventDTO event : eventsParticipant) {
					if (currentTimestamp.before(event.getEventDate())) {
						eventsParticipantFuture.add(event);
					} else {
						eventsParticipantPast.add(event);
					}
				}
				if (!eventsParticipantFuture.isEmpty()) {
					model.addAttribute("eventsParticipantFuture", eventsParticipantFuture);
				}
				if (!eventsParticipantPast.isEmpty()) {
					model.addAttribute("eventsParticipantPast", eventsParticipantPast);
				}
			}
		}

		return "pages/user/userProfile";
	}

	@PostMapping("/editProfile")
	public String editProfile(@ModelAttribute UserData userData, RedirectAttributes attr, HttpSession session) {

		UserDTO user = (UserDTO) session.getAttribute("user");
		if (user == null || !user.getId().equals(userData.getUserId())) {
			// User not logued or different user
			return "redirect:/custom-error";
		}

		UserDTO userEdit = parseUserDataToUser(userData);
		
		//Convert profile image to base64 if uploaded
	    if (userData.getProfileImage() != null && !userData.getProfileImage().isEmpty()) {
	        try {
	            byte[] imageBytes = userData.getProfileImage().getBytes();
	            String base64Image = "data:image/" + getFileExtension(userData.getProfileImage().getOriginalFilename()) 
	            + ";base64," + Base64.getEncoder().encodeToString(imageBytes);
	            userEdit.setProfileImage(base64Image);
	        } catch (IOException e) {
	            log.error("Error converting image to base64", e);
	            attr.addFlashAttribute("error", "Error al procesar la imagen.");
	            return "redirect:/user/profile/";
	        }
	    }

		ResponseMessage resp = userclient.edit(userEdit, user.getId());

		if (!resp.isOK()) {
			log.error("Error editing user");
			attr.addFlashAttribute("error", Utils.message("user.edit.error", resp.getMessage().toString()));
			return "redirect:/user/profile/";
		} else {
			log.info("User successfully edited");
			attr.addFlashAttribute("success", Utils.message("user.edit.ok"));

			// Encoding password
			UserDTO u = (UserDTO) resp.getMessage();
			userclient.updatePasswordById(u.getPassword(), u.getId());

			return "redirect:/user/profile/";
		}

	}

	@SuppressWarnings("unchecked")
	@GetMapping("/admin/")
	public String adminPage(Model model, HttpSession session, HttpServletRequest request) {
		String requestURI = request.getRequestURI();
		model.addAttribute("requestURI", requestURI);

		boolean isLogged = false;
		boolean isAdmin = false;

		UserDTO user = (UserDTO) session.getAttribute("user");
		if (user != null) {
			isLogged = true;
			if (user.getRole() != null && user.getRole()) {
				isAdmin = true;
			} else {
				log.error("User is not admin, forbidden access to admin page");
				return "redirect:/custom-error";
			}
		} else {
			log.error("User not logged, forbidden access to admin page");
			return "redirect:/custom-error";
		}

		model.addAttribute("isLogged", isLogged);
		model.addAttribute("isAdmin", isAdmin);
		model.addAttribute("user", user);
		
		//If logged, get notifications
        if (isLogged) {
        	List<NotificationDTO> notifications = new ArrayList<>();
        	notifications = getNotificationsByUser(user.getId());       	
        	model.addAttribute("notifications", notifications);
        }


		// Get users lists - active and inactive
		List<UserDTO> usersList = new ArrayList<>();

		FilterBuilder filterUsers = Filter.builder();
		filterUsers.page(Page.builder().pageNo(0).pageSize(Integer.MAX_VALUE).build());
		filterUsers.sorting(List.of(Sorting.builder().field("creationDate").order(Order.DESC).build()));
		filterUsers.showParameters(List.of(""));
		filterUsers.groupFilter(GroupFilter.builder().operator(GroupFilter.Operator.AND)
				.filterElements(Arrays.asList(FilterElements.builder().key("id").type(FilterElements.FilterType.SPECIAL)
						.operator(FilterElements.OperatorType.IS_NOT_NULL).build()))
				.build());
		ResponseMessage responseUser = userclient.get(filterUsers.build(), 0);

		if (responseUser.isOK()) {
			Response<UserDTO> res = (Response<UserDTO>) responseUser.getMessage();
			usersList = res.getResults();
		}

		List<UserDTO> activeUsers = new ArrayList<>();
		List<UserDTO> inactiveUsers = new ArrayList<>();
		for (UserDTO u : usersList) {
			u.setAge(Utils.calculateAge(u.getBirthDate()));
			if (u.getActive()) {
				activeUsers.add(u);
			} else {
				inactiveUsers.add(u);
			}
		}

		// Get events lists - future and past
		List<EventDTO> eventsList = new ArrayList<>();

		FilterBuilder filterEvents = Filter.builder();
		filterEvents.page(Page.builder().pageNo(0).pageSize(Integer.MAX_VALUE).build());
		filterEvents.sorting(List.of(Sorting.builder().field("eventDate").order(Order.ASC).build()));
		filterEvents.showParameters(List.of("users.user", "category"));
		filterEvents
				.groupFilter(
						GroupFilter.builder().operator(GroupFilter.Operator.AND)
								.filterElements(Arrays.asList(FilterElements.builder().key("id").value(0)
										.type(FilterElements.FilterType.INTEGER)
										.operator(FilterElements.OperatorType.NOT_EQUALS).build()))
								.build());
		ResponseMessage responseEvent = eventclient.get(filterEvents.build(), 0);

		if (responseEvent.isOK()) {
			Response<EventDTO> res = (Response<EventDTO>) responseEvent.getMessage();
			eventsList = res.getResults();
		}

		List<EventDTO> futureEvents = new ArrayList<>();
		List<EventDTO> pastEvents = new ArrayList<>();
		Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());

		for (EventDTO e : eventsList) {
			for (UserByEventDTO ube : e.getUsers()) {
				if (ube.getOwner()) {
					e.setEventOwner(ube.getUser());
					break;
				}
			}
			if (currentTimestamp.before(e.getEventDate())) {
				futureEvents.add(e);
			} else {
				pastEvents.add(e);
			}
		}

		// Get users blocked
		List<BlockByUserDTO> blocks = new ArrayList<>();

		FilterBuilder filterBlocks = Filter.builder();
		filterBlocks.page(Page.builder().pageNo(0).pageSize(Integer.MAX_VALUE).build());
		filterBlocks.sorting(List.of(Sorting.builder().field("id").order(Order.DESC).build()));
		filterBlocks.showParameters(List.of("blocked", "reporter", "literal"));
		filterBlocks
				.groupFilter(
						GroupFilter.builder().operator(GroupFilter.Operator.AND)
								.filterElements(Arrays.asList(FilterElements.builder().key("id").value(0)
										.type(FilterElements.FilterType.INTEGER)
										.operator(FilterElements.OperatorType.NOT_EQUALS).build()))
								.build());
		ResponseMessage responseBlock = blockclient.get(filterBlocks.build(), 0);

		if (responseBlock.isOK()) {
			Response<BlockByUserDTO> res = (Response<BlockByUserDTO>) responseBlock.getMessage();
			blocks = res.getResults();
		}

		// Get events reported
		List<ReportByEventDTO> reports = new ArrayList<>();

		FilterBuilder filterReports = Filter.builder();
		filterReports.page(Page.builder().pageNo(0).pageSize(Integer.MAX_VALUE).build());
		filterReports.sorting(List.of(Sorting.builder().field("id").order(Order.DESC).build()));
		filterReports.showParameters(List.of("reporter", "event", "literal"));
		filterReports
				.groupFilter(
						GroupFilter.builder().operator(GroupFilter.Operator.AND)
								.filterElements(Arrays.asList(FilterElements.builder().key("id").value(0)
										.type(FilterElements.FilterType.INTEGER)
										.operator(FilterElements.OperatorType.NOT_EQUALS).build()))
								.build());
		ResponseMessage responseReport = reportclient.get(filterReports.build(), 0);

		if (responseReport.isOK()) {
			Response<ReportByEventDTO> res = (Response<ReportByEventDTO>) responseReport.getMessage();
			reports = res.getResults();
		}

		// Get users deregistered
		List<DeregistrationByUserDTO> deregistrations = new ArrayList<>();

		FilterBuilder filterDeregistrations = Filter.builder();
		filterDeregistrations.page(Page.builder().pageNo(0).pageSize(Integer.MAX_VALUE).build());
		filterDeregistrations.sorting(List.of(Sorting.builder().field("id").order(Order.DESC).build()));
		filterDeregistrations.showParameters(List.of("user", "event", "literal"));
		filterDeregistrations
				.groupFilter(
						GroupFilter.builder().operator(GroupFilter.Operator.AND)
								.filterElements(Arrays.asList(FilterElements.builder().key("id").value(0)
										.type(FilterElements.FilterType.INTEGER)
										.operator(FilterElements.OperatorType.NOT_EQUALS).build()))
								.build());
		ResponseMessage responseDeregistration = deregistrationclient.get(filterDeregistrations.build(), 0);

		if (responseDeregistration.isOK()) {
			Response<DeregistrationByUserDTO> res = (Response<DeregistrationByUserDTO>) responseDeregistration
					.getMessage();
			deregistrations = res.getResults();
		}

		model.addAttribute("activeUsers", activeUsers);
		model.addAttribute("inactiveUsers", inactiveUsers);
		model.addAttribute("futureEvents", futureEvents);
		model.addAttribute("pastEvents", pastEvents);
		model.addAttribute("blocks", blocks);
		model.addAttribute("reports", reports);
		model.addAttribute("deregistrations", deregistrations);

		return "pages/user/adminManagement";
	}

	@SuppressWarnings("unchecked")
	@GetMapping("/delete/{id}")
	public String deleteUser(@PathVariable("id") Integer id, RedirectAttributes attr, HttpSession session) {

		UserDTO user = (UserDTO) session.getAttribute("user");
		if (user == null || !user.getRole()) {
			log.error("User has no permission to delete users");
			return "redirect:/custom-error";
		}

		ResponseMessage resp = userclient.delete(List.of(id), user.getId());
		if (!resp.isOK()) {
			attr.addFlashAttribute("error", Utils.message("user.delete.error", resp.getMessage().toString()));
		} else {
			attr.addFlashAttribute("success", Utils.message("user.delete.ok"));
		}

		return "redirect:/user/admin/";
	}

	@SuppressWarnings("unchecked")
	@GetMapping("/inactive/{id}")
	public String inactiveUser(@PathVariable("id") Integer id, RedirectAttributes attr, HttpSession session) {

		UserDTO currentUser = (UserDTO) session.getAttribute("user");
		if (currentUser == null || !currentUser.getRole()) {
			log.error("User has no permission to inactive users");
			return "redirect:/custom-error";
		}

		// Get user to edit
		FilterBuilder filter = Filter.builder();
		filter.page(Page.builder().pageNo(0).pageSize(Integer.MAX_VALUE).build());
		filter.sorting(List.of(Sorting.builder().field("id").order(Order.DESC).build()));
		filter.showParameters(List.of("blocks", "events.event", "chats"));
		filter.groupFilter(GroupFilter.builder().operator(GroupFilter.Operator.AND)
				.filterElements(Arrays.asList(FilterElements.builder().key("id").value(id)
						.type(FilterElements.FilterType.INTEGER).operator(FilterElements.OperatorType.EQUALS).build()))
				.build());
		ResponseMessage response = userclient.get(filter.build(), 0);
		UserDTO user = null;
		if (response.isOK()) {
			Response<UserDTO> res = (Response<UserDTO>) response.getMessage();
			user = res.getResults().get(0);
		}

		// Set inactive
		user.setActive(false);

		// Remove blocks
		if (user.getBlocks() != null && !user.getBlocks().isEmpty()) {
			List<Integer> blockIds = new ArrayList<>();
			for (BlockByUserDTO block : user.getBlocks()) {
				blockIds.add(block.getId());
			}

			blockclient.delete(blockIds, currentUser.getId());

			List<BlockByUserDTO> blocksEmpty = new ArrayList<>();
			user.setBlocks(blocksEmpty);
		}

		// Deregister from all future events and create deregistration
		if (user.getEvents() != null && !user.getEvents().isEmpty()) {
			List<UserByEventDTO> eventsList = new ArrayList<>(user.getEvents());
			Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());

			for (UserByEventDTO ube : user.getEvents()) {
				if (ube.getEvent().getEventDate().after(currentTimestamp) && ube.getDeregistrationDate() == null) {

					eventsList.removeIf(event -> event.getEvent().getId().equals(ube.getEvent().getId()));

					eventsList.add(UserByEventDTO.builder().user(UserDTO.builder().id(user.getId()).build())
							.event(EventDTO.builder().id(ube.getEvent().getId()).build()).owner(ube.getOwner())
							.participant(ube.getParticipant()).registrationDate(ube.getRegistrationDate())
							.deregistrationDate(currentTimestamp).deregistrationVoluntary(false).build());

					// Create new deregistration
					DeregistrationByUserDTO deregistration = DeregistrationByUserDTO.builder()
							.user(UserDTO.builder().id(user.getId()).build())
							.event(EventDTO.builder().id(ube.getEvent().getId()).build())
							.literal(LiteralDTO.builder().id(13).build()).build();

					deregistrationclient.create(deregistration, currentUser.getId());
				}
			}

			user.setEvents(eventsList);
		}

		// Inactive chats
		if (user.getChats() != null && !user.getChats().isEmpty()) {
			for (ChatDTO chat : user.getChats()) {
				if (chat.getStatus() == null || chat.getStatus()) {
					chat.setStatus(false);
				}
			}
		}

		// Edit user
		ResponseMessage resp = userclient.edit(user, currentUser.getId());
		if (!resp.isOK()) {
			attr.addFlashAttribute("error", Utils.message("user.edit.error", resp.getMessage().toString()));
		} else {
			attr.addFlashAttribute("success", Utils.message("user.edit.ok"));
		}

		return "redirect:/user/admin/";
	}

	@SuppressWarnings("unchecked")
	@GetMapping("/active/{id}")
	public String activeUser(@PathVariable("id") Integer id, RedirectAttributes attr, HttpSession session) {

		UserDTO currentUser = (UserDTO) session.getAttribute("user");
		if (currentUser == null || !currentUser.getRole()) {
			log.error("User has no permission to inactive users");
			return "redirect:/custom-error";
		}

		// Get user to edit
		FilterBuilder filter = Filter.builder();
		filter.page(Page.builder().pageNo(0).pageSize(Integer.MAX_VALUE).build());
		filter.sorting(List.of(Sorting.builder().field("id").order(Order.DESC).build()));
		filter.showParameters(List.of("deregistrations", "events.event", "chats"));
		filter.groupFilter(GroupFilter.builder().operator(GroupFilter.Operator.AND)
				.filterElements(Arrays.asList(FilterElements.builder().key("id").value(id)
						.type(FilterElements.FilterType.INTEGER).operator(FilterElements.OperatorType.EQUALS).build()))
				.build());
		ResponseMessage response = userclient.get(filter.build(), 0);
		UserDTO user = null;
		if (response.isOK()) {
			Response<UserDTO> res = (Response<UserDTO>) response.getMessage();
			user = res.getResults().get(0);
		}

		// Set active
		user.setActive(true);

		// Remove deregistrations
		if (user.getDeregistrations() != null && !user.getDeregistrations().isEmpty()) {
			List<Integer> deregistrationIds = new ArrayList<>();
			for (DeregistrationByUserDTO deregistration : user.getDeregistrations()) {
				deregistrationIds.add(deregistration.getId());
			}

			deregistrationclient.delete(deregistrationIds, currentUser.getId());

			List<DeregistrationByUserDTO> deregistrationsEmpty = new ArrayList<>();
			user.setDeregistrations(deregistrationsEmpty);
		}

		// Register again to all future events
		if (user.getEvents() != null && !user.getEvents().isEmpty()) {
			List<UserByEventDTO> eventsList = new ArrayList<>(user.getEvents());
			Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());

			for (UserByEventDTO ube : user.getEvents()) {
				if (ube.getEvent().getEventDate().after(currentTimestamp) && ube.getDeregistrationDate() != null
						&& ube.getDeregistrationVoluntary() != null && !ube.getDeregistrationVoluntary()) {

					eventsList.removeIf(event -> event.getEvent().getId().equals(ube.getEvent().getId()));

					eventsList.add(UserByEventDTO.builder().user(UserDTO.builder().id(user.getId()).build())
							.event(EventDTO.builder().id(ube.getEvent().getId()).build()).owner(ube.getOwner())
							.participant(ube.getParticipant()).registrationDate(ube.getRegistrationDate())
							.deregistrationDate(null).deregistrationVoluntary(null).build());

				}
			}

			user.setEvents(eventsList);
		}

		// Active chats
		if (user.getChats() != null && !user.getChats().isEmpty()) {
			for (ChatDTO chat : user.getChats()) {
				if (chat.getStatus() != null && !chat.getStatus()) {
					chat.setStatus(true);
				}
			}
		}

		// Edit user
		ResponseMessage resp = userclient.edit(user, currentUser.getId());
		if (!resp.isOK()) {
			attr.addFlashAttribute("error", Utils.message("user.edit.error", resp.getMessage().toString()));
		} else {
			attr.addFlashAttribute("success", Utils.message("user.edit.ok"));
		}

		return "redirect:/user/admin/";
	}
	
	@SuppressWarnings("unchecked")
	@GetMapping("/removeBlock/{id}")
	public String removeBlock(@PathVariable("id") Integer id, RedirectAttributes attr, HttpSession session) {

		UserDTO currentUser = (UserDTO) session.getAttribute("user");
		if (currentUser == null || !currentUser.getRole()) {
			log.error("User has no permission to remove blocks");
			return "redirect:/custom-error";
		}

		// Get user to edit chats
		FilterBuilder filterBlock = Filter.builder();
		filterBlock.page(Page.builder().pageNo(0).pageSize(Integer.MAX_VALUE).build());
		filterBlock.sorting(List.of(Sorting.builder().field("id").order(Order.DESC).build()));
		filterBlock.showParameters(List.of("blocked"));
		filterBlock.groupFilter(GroupFilter.builder().operator(GroupFilter.Operator.AND)
				.filterElements(Arrays.asList(FilterElements.builder().key("id").value(id)
						.type(FilterElements.FilterType.INTEGER).operator(FilterElements.OperatorType.EQUALS).build()))
				.build());
		ResponseMessage r = blockclient.get(filterBlock.build(), 0);
		BlockByUserDTO block = null;
		if (r.isOK()) {
			Response<BlockByUserDTO> res = (Response<BlockByUserDTO>) r.getMessage();
			block = res.getResults().get(0);
		}
		
		Integer userId = block.getBlocked().getId();
		
		FilterBuilder filter = Filter.builder();
		filter.page(Page.builder().pageNo(0).pageSize(Integer.MAX_VALUE).build());
		filter.sorting(List.of(Sorting.builder().field("id").order(Order.DESC).build()));
		filter.showParameters(List.of("chats"));
		filter.groupFilter(GroupFilter.builder().operator(GroupFilter.Operator.AND)
				.filterElements(Arrays.asList(FilterElements.builder().key("id").value(userId)
						.type(FilterElements.FilterType.INTEGER).operator(FilterElements.OperatorType.EQUALS).build()))
				.build());
		ResponseMessage response = userclient.get(filter.build(), 0);
		UserDTO user = null;
		if (response.isOK()) {
			Response<UserDTO> res = (Response<UserDTO>) response.getMessage();
			user = res.getResults().get(0);
		}
		
		// Active chats
		if (user.getChats() != null && !user.getChats().isEmpty()) {
			for (ChatDTO chat : user.getChats()) {
				if (chat.getStatus() != null && !chat.getStatus()) {
					chat.setStatus(true);
				}
			}
			userclient.edit(user, currentUser.getId());
		}
		
		// Remove blocks
		List<Integer> blockIds = new ArrayList<>();
		blockIds.add(id);

		ResponseMessage resp = blockclient.delete(blockIds, currentUser.getId());

		if (!resp.isOK()) {
			attr.addFlashAttribute("error", Utils.message("block.delete.error", resp.getMessage().toString()));
		} else {
			attr.addFlashAttribute("success", Utils.message("block.delete.ok"));
		}

		return "redirect:/user/admin/";
	}

	private UserDTO parseUserDataToUser(UserData userData) {

		UserDTO user = new UserDTO();
		UserDTO oldUser = null;

		// Id
		if (userData.getUserId() != null) {
			user.setId(userData.getUserId());

			// Get old user finding by id
			oldUser = getUserById(user.getId());
		}

		// Fullname
		if (userData.getFullname() != null) {
			user.setFullname(userData.getFullname());
		} else {
			user.setFullname(oldUser.getFullname());
		}

		// Password
		if (userData.getPassword() != null) {
			user.setPassword(userData.getPassword());
		} else {
			user.setPassword(oldUser.getPassword());
		}

		// Email
		if (oldUser != null) {
			user.setEmail(oldUser.getEmail());
		} else {
			user.setEmail(null);
		}

		// Birthdate
		if (oldUser != null) {
			user.setBirthDate(oldUser.getBirthDate());
		} else {
			user.setBirthDate(null);
		}

		// CreationDate
		Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
		if (oldUser != null) {
			user.setCreationDate(oldUser.getCreationDate());
		} else {
			user.setCreationDate(currentTimestamp);
		}

		// Active
		if (oldUser != null) {
			user.setActive(oldUser.getActive());
		} else {
			user.setActive(true);
		}

		// Role
		if (oldUser != null) {
			user.setRole(oldUser.getRole());
		} else {
			user.setRole(true);
		}
		
		// Profile image
		if (userData.getProfileImage() == null || userData.getProfileImage().isEmpty()) {
			if (oldUser.getProfileImage() != null && !oldUser.getProfileImage().isEmpty()) {
				user.setProfileImage(oldUser.getProfileImage());
			}
		}

		return user;
	}

	@SuppressWarnings("unchecked")
	private UserDTO getUserById(Integer id) {
		FilterBuilder filter = Filter.builder();
		filter.page(Page.builder().pageNo(0).pageSize(Integer.MAX_VALUE).build());
		filter.sorting(List.of(Sorting.builder().field("id").order(Order.DESC).build()));
		filter.showParameters(List.of(""));
		filter.groupFilter(GroupFilter.builder().operator(GroupFilter.Operator.AND)
				.filterElements(Arrays.asList(FilterElements.builder().key("id").value(id)
						.type(FilterElements.FilterType.INTEGER).operator(FilterElements.OperatorType.EQUALS).build()))
				.build());

		ResponseMessage response = userclient.get(filter.build(), 0);
		UserDTO user = null;
		if (response.isOK()) {
			Response<UserDTO> res = (Response<UserDTO>) response.getMessage();
			user = res.getResults().get(0);
		}

		return user;
	}

	private String decodePassword(String encodedPassword) {
		byte[] decodedBytes = Base64.getDecoder().decode(encodedPassword);
		return new String(decodedBytes);
	}
	
	private String getFileExtension(String filename) {
	    String extension = filename.substring(filename.lastIndexOf(".") + 1);
	    return extension.toLowerCase();
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