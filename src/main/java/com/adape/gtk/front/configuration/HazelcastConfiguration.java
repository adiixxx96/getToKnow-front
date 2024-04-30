//package com.adape.gtk.front.configuration;
//
//import javax.cache.Cache;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import com.hazelcast.config.CacheSimpleConfig;
//import com.hazelcast.config.Config;
//import com.hazelcast.core.Hazelcast;
//import com.hazelcast.core.HazelcastInstance;
//import com.hazelcast.core.ICacheManager;
//
//import io.github.bucket4j.grid.GridBucketState;
//
//@Configuration
//public class HazelcastConfiguration {
//
//	private final Logger log = LoggerFactory.getLogger(HazelcastConfiguration.class);
//	
//	@Value("#{environment['kubernetesDiscovery']}")
//	public Boolean kubeDiscovery;
//	
//	@Value("#{environment['namespace']}")
//	public String namespace;
//	
//	@Value("#{environment['serviceName']}")
//	public String serviceName;
//
//	@Bean
//    Cache<String, GridBucketState> cache() {
//        Config config = new Config();
//	    config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(true);
//		config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
//
//        if (kubeDiscovery != null && kubeDiscovery) {
//        	log.info("Kubernetes discovery configuration " + namespace + " - " + serviceName);	
//			config.getNetworkConfig().getJoin().getKubernetesConfig().setEnabled(true)
//					.setProperty("namespace", namespace)
//					.setProperty("service-name", serviceName);
//        }
//        CacheSimpleConfig cacheConfig = new CacheSimpleConfig();
//        cacheConfig.setName("buckets");
//        config.addCacheConfig(cacheConfig);
//
//        HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config);
//        ICacheManager cacheManager = hazelcastInstance.getCacheManager();
//        Cache<String, GridBucketState> cache = cacheManager.getCache("buckets");
//        return cache;
//    }
//	
//}
