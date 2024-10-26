package com.adape.gtk.front.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorController {

	Logger log = LoggerFactory.getLogger(ErrorController.class);
	
	@RequestMapping(value="/custom-error")
    public String errorPage() {
		
		log.error("Route does not exist or user has no permission");
	
        return "pages/error";
    }

}