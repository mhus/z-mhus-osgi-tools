package de.mhus.osgi.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.Builder;
import org.ehcache.config.ResourcePools;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.core.statistics.DefaultStatisticsService;
import org.junit.jupiter.api.Test;

public class CacheTest {

//    @Test
    public void testTTL() throws InterruptedException {
        
        DefaultStatisticsService statisticsService = new DefaultStatisticsService();
        CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .using(statisticsService)
                .build(false);
        cacheManager.init();
        
        String name = "testTTL";
        
        Builder<? extends ResourcePools> resourcePoolsBuilder = ResourcePoolsBuilder.heap(10);

        CacheConfigurationBuilder<String, String> ccb =
                CacheConfigurationBuilder.newCacheConfigurationBuilder(
                        String.class, String.class, resourcePoolsBuilder);
        
        
        ccb.withExpiry(
                ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds( 1 )));

        Cache<String, String> cache = cacheManager.createCache(name, ccb.build());

        cache.put("test", "aloa");
        {
            String val = cache.get("test");
            assertEquals("aloa", val);
        }
        
        Thread.sleep(4000);
        
        {
            for (int i = 0; i < 20; i++)
            cache.put("a" + i, "b" + i);
            String val = cache.get("test");
            assertEquals("aloa", val);
        }
        
    }
    
}
