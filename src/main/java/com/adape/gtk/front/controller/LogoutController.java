package com.adape.gtk.front.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping({ "/logout" })
public class LogoutController {

	Logger log = LoggerFactory.getLogger(LogoutController.class);
	
	@RequestMapping(value="/")
	public String logout(HttpServletRequest request) {
        // Set session to null
        HttpSession session = request.getSession(false);
        
        if (session != null) {
            session.invalidate();
        }
        
        return "redirect:/";
    }
}