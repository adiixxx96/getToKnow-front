//package com.adape.gtk.front.configuration;
//
//import java.io.IOException;
//import java.time.Duration;
//import java.util.AbstractMap;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.regex.Pattern;
//import java.util.stream.Collectors;
//
//import javax.cache.Cache;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import io.github.bucket4j.Bandwidth;
//import io.github.bucket4j.Bucket;
//import io.github.bucket4j.Bucket4j;
//import io.github.bucket4j.BucketConfiguration;
//import io.github.bucket4j.Refill;
//import io.github.bucket4j.grid.GridBucketState;
//import io.github.bucket4j.grid.ProxyManager;
//import io.github.bucket4j.grid.jcache.JCache;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.ServletRequest;
//import jakarta.servlet.ServletResponse;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//
//public class RateLimitIpThrottlingFilter implements jakarta.servlet.Filter {
//
//private final Logger log = LoggerFactory.getLogger(RateLimitIpThrottlingFilter.class);
//
//
//	private static Map<String, BucketConfiguration> URL_LIMITS_CONFIG;
//	static List<AbstractMap.SimpleEntry<String, BucketConfiguration>> urlConfigs = new ArrayList<AbstractMap.SimpleEntry<String, BucketConfiguration>>();
//	static  {
//		urlConfigs.add(
//				new AbstractMap.SimpleEntry<String, BucketConfiguration>(
//						"(/js|/css|/images|/webjars)/.*", 
//						Bucket4j
//							.configurationBuilder()
//							.addLimit(Bandwidth.simple(100, Duration.ofSeconds(1)))
//							.build()
//						)
//				);
//		urlConfigs.add(
//				new AbstractMap.SimpleEntry<String, BucketConfiguration>(
//						"(?!/js|/css|/images|/webjars)/.*", 
//						Bucket4j
//						.configurationBuilder()
//						.addLimit(Bandwidth.classic(20, Refill.intervally(10, Duration.ofSeconds(2))))
//						.build()
//						)
//				);
//		urlConfigs.add(
//				new AbstractMap.SimpleEntry<String, BucketConfiguration>(
//						".*(/list).*", 
//						Bucket4j
//						.configurationBuilder()
//						.addLimit(Bandwidth.classic(15, Refill.intervally(5, Duration.ofSeconds(2))))
//						.build()
//						)
//				);
//
//		URL_LIMITS_CONFIG = urlConfigs.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//	}
//
//	private static final String ERROR_MESSAGE = "{ \"status\": 429, \"error\": \"Too Many Requests\"}";
//	
//	private Cache<String, GridBucketState> cache;
//	private ProxyManager<String> buckets;
//
//	public RateLimitIpThrottlingFilter(Cache<String, GridBucketState> cache) {
//        this.cache = cache;
//        buckets = Bucket4j.extension(JCache.class).proxyManagerForCache(this.cache);
//    }
//
//	@Override
//	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
//			throws IOException, ServletException {
//		HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
//		Bucket bucket = getBucket(httpRequest);
//		
//		if (bucket.tryConsume(1)) {
//			filterChain.doFilter(servletRequest, servletResponse);
//		} else {
//			log.error("Not available tokens in bucket, response with error message");
//			HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
//			httpResponse.setContentType("application/json");
//			httpResponse.setStatus(429);
//			httpResponse.getWriter().append(ERROR_MESSAGE);
//		}
//		
//	}
//
//	private Bucket getBucket(HttpServletRequest httpRequest) {
//		BucketConfiguration config = getConfig(httpRequest);
//		String key = getProxyKey(httpRequest);
//		return buckets.getProxy(key, config);
//	}
//
//	private String getProxyKey(HttpServletRequest httpRequest) {
//		StringBuffer key = new StringBuffer();
//		key.append(getClientIpAddress(httpRequest));
//		String url = httpRequest.getRequestURI();
//		for (Map.Entry<String, BucketConfiguration> entry : URL_LIMITS_CONFIG.entrySet()) {
//			String pattern = entry.getKey();
//			Pattern patt = Pattern.compile(pattern);
//			if(patt.matcher(url).matches()) {
//				key.append("__");
//				key.append(pattern);
//				break;
//	        }
//		}
//		return key.toString();
//	}
//
//	private BucketConfiguration getConfig(HttpServletRequest request) {
//		String url = request.getRequestURI();
//		for (Map.Entry<String, BucketConfiguration> entry : URL_LIMITS_CONFIG.entrySet()) {
//			
//			String pattern = entry.getKey();
//			BucketConfiguration config = entry.getValue();
//			
//			Pattern patt = Pattern.compile(pattern);
//			if(patt.matcher(url).matches()) {
//
//				return config;	
//	        }
//		}
//		return Bucket4j.configurationBuilder().addLimit(Bandwidth.simple(25, Duration.ofSeconds(1))).build();
//		
//	}
//
//	private String getClientIpAddress(HttpServletRequest request) {
//		String[] HEADERS_TO_TRY = {
//	            "X-Forwarded-For",
//	            "Proxy-Client-IP",
//	            "WL-Proxy-Client-IP",
//	            "HTTP_X_FORWARDED_FOR",
//	            "HTTP_X_FORWARDED",
//	            "HTTP_X_CLUSTER_CLIENT_IP",
//	            "HTTP_CLIENT_IP",
//	            "HTTP_FORWARDED_FOR",
//	            "HTTP_FORWARDED",
//	            "HTTP_VIA",
//	            "REMOTE_ADDR" };
//	    for (String header : HEADERS_TO_TRY) {
//	        String ip = request.getHeader(header);
//	        if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
//	        	if(ip.contains(":")) {
//		        	String[] address = ip.split(":");
//		            return address[0];
//	        	} else {
//	        		return ip;
//	        	}
//	        }
//	    }
//	    
//	    if (request.getRemoteAddr().contains(":")) {
//	    	String[] address = request.getRemoteAddr().split(":");
//	    	return address[0];
//	    } else {
//	    	return request.getRemoteAddr();
//	    }
//	}
//}
