package com.adape.gtk.front.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.adape.gtk.core.client.beans.UserDTO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping({ "/event" })
public class EventController {

	Logger log = LoggerFactory.getLogger(EventController.class);
	
	@RequestMapping(value="/")
    public String eventController (Model model, HttpSession session, HttpServletRequest request) {
		
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
	
        return "pages/home";
    }
	
	@RequestMapping(value="/create")
    public String createEventPage (Model model, HttpSession session, HttpServletRequest request) {
		
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
	
        return "pages/home";
    }

}