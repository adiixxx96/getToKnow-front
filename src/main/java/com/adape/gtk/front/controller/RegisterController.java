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
@RequestMapping({ "/register" })
public class RegisterController {

	Logger log = LoggerFactory.getLogger(RegisterController.class);
	
	@RequestMapping(value="/")
    public String registerPage() {
	
        return "pages/register";
    }

}