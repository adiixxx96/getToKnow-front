package com.adape.gtk.front.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.everis.bcs.core.client.beans.EmailDTO;
import com.everis.bcs.core.client.beans.Filter;
import com.everis.bcs.core.client.beans.FilterElements;
import com.everis.bcs.core.client.beans.GroupFilter;
import com.everis.bcs.core.client.beans.Page;
import com.everis.bcs.core.client.beans.PersonDTO;
import com.everis.bcs.core.client.beans.Response;
import com.everis.bcs.core.client.beans.ResponseMessage;
import com.everis.bcs.core.client.beans.Sorting;
import com.everis.bcs.core.client.beans.Sorting.Order;
import com.everis.bcs.core.client.service.EmailIntService;
import com.everis.bcs.core.client.service.PersonIntService;
import com.everis.bcs.core.utils.Constants;
import com.everis.bcs.core.utils.Utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FrontInterceptor implements HandlerInterceptor {

	private static Logger log = LoggerFactory.getLogger(FrontInterceptor.class);
	
	@Value("#{environment.webserviceFrontHost}")
	private String frontHost;
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
	}
	
}
