package com.adape.gtk.front;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.FileTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

@SpringBootApplication
@ComponentScan({
	"com.adape.gtk.front",
	"com.adape.gtk.core.client"
	})
public class GtkFrontApplication {
	
	public static final String EMAIL_TEMPLATE_ENCODING = "UTF-8";

	public static void main(String[] args) {
		SpringApplication.run(GtkFrontApplication.class, args);
	}
	
	@Bean(name="frontRestTemplate")
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		RestTemplate restTemplate = builder.build();

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);

		restTemplate.setRequestFactory(factory);

		return restTemplate;
	}
	
	@Bean
    public TemplateEngine gtkTemplateEngine() {
        final SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        
        templateEngine.addTemplateResolver(gtkTemplateResolver());
        
        return templateEngine;
    }

	private ITemplateResolver gtkTemplateResolver() {
	    
        final FileTemplateResolver templateResolver = new FileTemplateResolver();
        templateResolver.setOrder(Integer.valueOf(1));
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding(EMAIL_TEMPLATE_ENCODING);
        templateResolver.setCacheable(false);
        
        return templateResolver;
    }
}
