package com.adape.gtk.front.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.adape.gtk.core.client.beans.UserDTO;
import com.adape.gtk.front.utils.Utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping({ "/contactUs" })
public class ContactUsController {

	Logger log = LoggerFactory.getLogger(ContactUsController.class);
	
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
		
		return "pages/contactUs";
    }

	@RequestMapping(value="/send")
	public String send(RedirectAttributes attr) {
		 
		log.info("Message sended");
		attr.addFlashAttribute("success", Utils.message("contact.send.ok"));
		
		return "redirect:/contactUs/";
    }
}
