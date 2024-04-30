package com.adape.gtk.front.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.adape.gtk.core.client.beans.Response;
import com.adape.gtk.core.client.beans.ResponseMessage;
import com.adape.gtk.core.client.beans.UserDTO;
import com.adape.gtk.core.client.service.UserIntService;
import com.adape.gtk.front.utils.Utils;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping({ "/login" })
public class LoginController {

	Logger log = LoggerFactory.getLogger(LoginController.class);
	
	@Autowired private UserIntService userclient;
	
	@RequestMapping(value="/")
    public String loginPage() {
	
        return "pages/login";
    }
	
	@PostMapping("/save")
	public String loginUser(@ModelAttribute UserDTO user, RedirectAttributes attr, HttpSession session) {
		
		String email = user.getEmail();
		String password = user.getPassword();
		
		//Check if email or password inputs are empty
		if (email.trim() == "" || password.trim() == "") {
			log.error("Input email or password are empty");
			attr.addFlashAttribute("error", Utils.message("form.input.empty"));
			return "redirect:/login/";
		}
		
		UserDTO userLogged = null;
		
		//Check if user exists with that email and password
		ResponseMessage resp = userclient.login(email, password);
		if (resp.isOK()) {
			Response<UserDTO> userResp = (Response<UserDTO>) resp.getMessage();
			userLogged = userResp.getResults().get(0);		
			session.setAttribute("user", userLogged);
			log.info("User successfully created");
			return "redirect:/";
						
		} else {
			log.error("User does not exist in database");
			attr.addFlashAttribute("error", Utils.message("user.login.error"));
			return "redirect:/login/";
		}
	}

}