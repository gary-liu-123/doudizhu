package com.doudizhu.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis配置类
 */
@Configuration
public class RedisConfig {
    
    @Value("${spring.redis.password}")
    private String password;
    
    @Value("${spring.redis.cluster.nodes}")
    private String[] clusterNodes;
    
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useClusterServers()
                .addNodeAddress(formatNodes(clusterNodes))
                .setPassword(password)
                .setConnectTimeout(3000)
                .setTimeout(3000)
                .setMasterConnectionPoolSize(64)
                .setSlaveConnectionPoolSize(64);
        
        return Redisson.create(config);
    }
    
    private String[] formatNodes(String[] nodes) {
        String[] formattedNodes = new String[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            formattedNodes[i] = "redis://" + nodes[i];
        }
        return formattedNodes;
    }
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // 设置key序列化方式
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // 设置value序列化方式
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        
        template.afterPropertiesSet();
        return template;
    }
}