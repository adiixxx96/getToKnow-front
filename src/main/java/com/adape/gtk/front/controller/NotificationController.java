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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.adape.gtk.core.client.beans.BlockByUserDTO;
import com.adape.gtk.core.client.beans.CategoryDTO;
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
@RequestMapping({ "/notification" })
public class NotificationController {

	Logger log = LoggerFactory.getLogger(NotificationController.class);

	@Autowired 
	private NotificationIntService notificationclient;

	
	@SuppressWarnings("unchecked")
	@PostMapping("/markAsRead/{id}")
	public ResponseEntity<?> markAsRead(@PathVariable("id") Integer notificationId, RedirectAttributes attr, HttpSession session) {

		UserDTO user = (UserDTO) session.getAttribute("user");
		if (user == null) {
			log.error("User has no permission to read notifications");
			return ResponseEntity.internalServerError().body("User has no permission read notifications");
		}
		
		NotificationDTO notification = null;
		ResponseMessage response = notificationclient.get(notificationId, user.getId());
		if (response.isOK()) {
			notification = (NotificationDTO) response.getMessage();
		}
		
		notification.setRead(true);
		
		ResponseMessage resp = notificationclient.edit(notification, user.getId());
		if (!resp.isOK()) {
			attr.addFlashAttribute("error", Utils.message("user.delete.error", resp.getMessage().toString()));
		} else {
			attr.addFlashAttribute("success", Utils.message("user.delete.ok"));
		}

		return ResponseEntity.ok("Notification marked as read");
	}

}