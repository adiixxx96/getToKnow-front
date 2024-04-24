//package com.adape.gtk.front.configuration;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.util.AntPathMatcher;
//import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//import com.adape.gtk.front.configuration.LoggerInterceptor;
//
//
//@Configuration
//public class AppConfig implements WebMvcConfigurer{
//	
//	@Autowired FrontInterceptor frontInterceptor;
//	
//	@Override
//	public void addInterceptors(InterceptorRegistry registry) {
//	    registry.addInterceptor(new LoggerInterceptor())
//	            .excludePathPatterns(
//	                    "/error",
//	                    "/logout",
//	                    "/images/**",
//	                    "/css/**",
//	                    "/login",
//	                    "/login/callback",
//	                    "/webjars/**",
//	                    "/js/**",
//	                    "/favicon.ico"
//	            ).pathMatcher(new AntPathMatcher());
//	    // Para comprobar si el usuario está logueado en cada página
//	    registry.addInterceptor(new LoginInterceptor())
//	    .addPathPatterns(
//	    				"/",
//		                "/aboutUs",
//		                "/contactUs"    
//		        )
//        .excludePathPatterns(
//                "/error",
//                "/logout",
//                "/images/**",
//                "/css/**",
//                "/login",
//                "/login/callback",
//                "/webjars/**",
//                "/js/**",
//                "/favicon.ico"
//        ).pathMatcher(new AntPathMatcher());
//	    registry.addInterceptor(frontInterceptor)
//        .excludePathPatterns(
//                "/error",
//                "/logout",
//                "/images/**",
//                "/css/**",
//                "/login",
//                "/login/callback",
//                "/webjars/**",
//                "/js/**",
//                "/favicon.ico"
//        ).pathMatcher(new AntPathMatcher());
//	}
//	
//}