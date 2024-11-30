package com.adape.gtk.front.controller;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.adape.gtk.core.client.beans.Filter;
import com.adape.gtk.core.client.beans.FilterElements;
import com.adape.gtk.core.client.beans.GroupFilter;
import com.adape.gtk.core.client.beans.Page;
import com.adape.gtk.core.client.beans.ResponseMessage;
import com.adape.gtk.core.client.beans.Sorting;
import com.adape.gtk.core.client.beans.Sorting.Order;
import com.adape.gtk.core.client.beans.UserDTO;
import com.adape.gtk.core.client.service.UserIntService;
import com.adape.gtk.front.utils.Utils;


@Controller
@RequestMapping({ "/register" })
public class RegisterController {

	Logger log = LoggerFactory.getLogger(RegisterController.class);
	
	@Autowired private UserIntService userclient;
	
	@RequestMapping(value="/")
    public String registerPage() {
	
        return "pages/register";
    }
	
	@PostMapping("/save")
	public String saveUser(@RequestParam("fullname") String fullname,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("birthDate") String birthDateString, RedirectAttributes attr) {
		
		//Convert birthDate in Timestamp	
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Timestamp birthDate = null;
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sdf.parse(birthDateString));
            birthDate = new Timestamp(calendar.getTimeInMillis());
     
        } catch (ParseException e) {
            e.printStackTrace();
        }
		
		UserDTO user = UserDTO.builder()
				.fullname(fullname)
				.email(email)
				.password(password)
				.birthDate(birthDate)
				.build();
		
		//Check if email already exists in database	
		Filter filter = Filter.builder()
    			.groupFilter(GroupFilter.builder()
    					.operator(GroupFilter.Operator.AND)
    					.filterElements(Arrays.asList(FilterElements.builder()
    							.key("email")
    							.value(user.getEmail())
    							.type(FilterElements.FilterType.STRING)
    							.operator(FilterElements.OperatorType.EQUALS).build()
    							)).build())
    			.showParameters(List.of(""))
    			.page(Page.builder().pageNo(0).pageSize(Integer.MAX_VALUE).build())
    			.sorting(List.of(Sorting.builder().field("id").order(Order.DESC).build()))
    			.build();
		
		ResponseMessage response = userclient.get(filter, 0);
		if (response.isOK()) {
			//User with that email already exists
			log.error("User with that email already exists");
			attr.addFlashAttribute("error", Utils.message("user.email.exists"));
			return "redirect:/register/";
		}
		
		//Set current creation date to user
		Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        user.setCreationDate(currentTimestamp);
        
        ResponseMessage resp = userclient.create(user, 0);
        
        if(!resp.isOK()) {
        	log.error("Error creating user");
			attr.addFlashAttribute("error", Utils.message("user.create.error", resp.getMessage().toString()));
			return "redirect:/register/";
		} else {
			log.info("User successfully created");
			attr.addFlashAttribute("success", Utils.message("user.create.ok"));
			
			//Encoding password
			UserDTO u = (UserDTO) resp.getMessage();
			userclient.updatePasswordById(user.getPassword(), u.getId());
			
			return "redirect:/login/";
		}
	}

}