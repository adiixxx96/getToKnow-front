package com.adape.gtk.front.configuration;


import java.util.ArrayList;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

@Configuration
public class MessageConfig implements WebMvcConfigurer{

	@Bean
	public LocaleResolver localeResolver() {
	    SessionLocaleResolver slr = new SessionLocaleResolver();
	    slr.setDefaultLocale(new Locale("es"));
	    return slr;
	}

	@Bean
	public LocaleChangeInterceptor localeChangeInterceptor() {
	    LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
	    lci.setIgnoreInvalidLocale(true);
	    lci.setParamName("lang");
	    return lci;
	}
	
	@Bean
	public MessageSource messageSource() {
	    ReloadableResourceBundleMessageSource messageSource = 
	                                               new ReloadableResourceBundleMessageSource();
	    ArrayList<String> filenames = new ArrayList<String>();
 		try {
 			
 			ClassLoader cl = this.getClass().getClassLoader();
 			ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
 			Resource[] resources = resolver.getResources("classpath*:/messages/*.properties") ;
 			for (Resource resource: resources){
 				if(!resource.getFilename().toString().contains("_") && resource.getFilename().toString().endsWith(".properties")) {
 					filenames.add("classpath:messages/" + resource.getFilename().toString().replace(".properties", ""));
 				}
 			}
 			
		} catch (Exception e) {e.printStackTrace();}
	    messageSource.setBasenames(filenames.toArray(new String[filenames.size()]));
	    messageSource.setDefaultEncoding("UTF-8");

	    return messageSource;
	}
	
}
