//package com.adape.gtk.front.configuration;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.web.servlet.HandlerInterceptor;
//import org.springframework.web.servlet.ModelAndView;
//
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import jakarta.servlet.http.HttpSession;
//
//public class LoginInterceptor implements HandlerInterceptor {
//	private static Logger log = LoggerFactory.getLogger(LoginInterceptor.class);
//			
//	@Override
//	public boolean preHandle(
//	  HttpServletRequest request,
//	  HttpServletResponse response, 
//	  Object handler) throws Exception {
//	    log.info("Time since last request in this session: {} ms",
//	      System.currentTimeMillis() - request.getSession().getLastAccessedTime());
//	    
//		HttpSession session = request.getSession();
//		if(session.getAttribute("sessionType").equals("adfs")) {
//			 if(session.getAttribute("authenticated") == null
//		                || !Boolean.parseBoolean(session.getAttribute("authenticated").toString())) {
//	            session.setAttribute("redirect", request.getRequestURI());
//	            
//	            if(!response.isCommitted())
//	            response.sendRedirect("/login");
//	            if (request.getAttribute("org.springframework.web.servlet.DispatcherServlet.INPUT_FLASH_MAP") != null) {
//	            	HashMap flashMap =  new HashMap((Map) request.getAttribute("org.springframework.web.servlet.DispatcherServlet.INPUT_FLASH_MAP"));
//	            	session.setAttribute("flashAttribute", flashMap);
//	            }
//
//	            log.info("Not authenticated -> Redirect to /login");
//	            return false;
//	        } else {
//	            session.removeAttribute("authenticated");
//	            return true;
//	        }     
//		} else {
//			return isUserLogged(session);
//		}
//       		
//	}
//	
//	@Override
//	public void postHandle(
//	  HttpServletRequest request, 
//	  HttpServletResponse response,
//	  Object handler, 
//	  ModelAndView modelAndView) throws Exception {
//
//	}
//	
//	@Override
//	public void afterCompletion(
//	  HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) 
//	  throws Exception {
//	    if (ex != null){
//	        ex.printStackTrace();
//	    }
//	}
//	
//	private boolean isUserLogged(HttpSession session) {
//		return session.getAttribute("user") != null;
//	}
//}