package com.adape.gtk.front.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;


public class LoggerInterceptor implements HandlerInterceptor {
	private static Logger log = LoggerFactory.getLogger(LoggerInterceptor.class);
	
	private static final long MAX_INACTIVE_SESSION_TIME = 6000 * 1000;
	
	@Override
	public boolean preHandle(
	  HttpServletRequest request,
	  HttpServletResponse response, 
	  Object handler) throws Exception {
	    log.info("Time since last request in this session: {} ms",
	      System.currentTimeMillis() - request.getSession().getLastAccessedTime());
	    
		HttpSession session = request.getSession();
		long lastTimeAccessed = 0;
		try {
			lastTimeAccessed = (long) session.getAttribute("lastTimeAccessed");
		}catch(Exception e) {
		}
		
		if(session.getAttribute("redirect") != null) {
			if(request.getRequestURI().equals(session.getAttribute("redirect"))) {
				session.removeAttribute("redirect");
			}
		}
		if(isUserLogged(session)) {
			log.info("isUserLoged: true");
		    if (System.currentTimeMillis() - lastTimeAccessed
		  	      > MAX_INACTIVE_SESSION_TIME) {
		  	        log.warn("Logging out, due to inactive session");
		  	        response.sendRedirect("/logout");
		  	        session.setAttribute("lastTimeAccessed", System.currentTimeMillis());
				    return false;
		  	}
		    
		} else {
			log.info("isUserLoged: false");
	        if (request.getHeader("x-requested-with") == null || !request.getHeader("x-requested-with").equalsIgnoreCase("XMLHttpRequest")) {        	
	        	//redirect if no ajax request
	        	session.setAttribute("lastTimeAccessed", System.currentTimeMillis());
	        	session.setAttribute("redirect", request.getRequestURI());
	        	response.sendRedirect("/login");
	        }
	        
			return false;
		}

        session.setAttribute("lastTimeAccessed", System.currentTimeMillis());
	  	
	    return true;
	}
	
	@Override
	public void postHandle(
	  HttpServletRequest request, 
	  HttpServletResponse response,
	  Object handler, 
	  ModelAndView modelAndView) throws Exception {
	}
	
	@Override
	public void afterCompletion(
	  HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) 
	  throws Exception {
	    if (ex != null){
	        ex.printStackTrace();
	    }
	}
	
	private boolean isUserLogged(HttpSession session) {
		return session.getAttribute("user") != null;
	}
	
}