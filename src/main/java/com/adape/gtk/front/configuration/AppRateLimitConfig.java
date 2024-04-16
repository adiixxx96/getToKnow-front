package com.adape.gtk.front.configuration;

import javax.cache.Cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.bucket4j.grid.GridBucketState;
import jakarta.servlet.Filter;



@Configuration
public class AppRateLimitConfig{
	
	@Autowired
	Cache<String, GridBucketState> cache;

    @Bean
    FilterRegistrationBean<Filter> filterRegistration() {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<Filter>();
        registration.setFilter(rateLimitIpthrottlingFilter());
        registration.addUrlPatterns("/*");
        return registration;
    }

    @Bean
    Filter rateLimitIpthrottlingFilter() {
        return new RateLimitIpThrottlingFilter(cache);
    }
	
}
