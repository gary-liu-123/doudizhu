package com.doudizhu.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;

/**
 * Redis配置类
 */
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
public class RedisConfig {
    
    @Value("${spring.redis.password}")
    private String password;
    
    private Cluster cluster = new Cluster();
    
    public static class Cluster {
        private List<String> nodes;
        
        public List<String> getNodes() {
            return nodes;
        }
        
        public void setNodes(List<String> nodes) {
            this.nodes = nodes;
        }
    }
    
    public Cluster getCluster() {
        return cluster;
    }
    
    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }
    
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useClusterServers()
                .addNodeAddress(formatNodes(cluster.getNodes()))
                .setPassword(password)
                .setConnectTimeout(3000)
                .setTimeout(3000)
                .setMasterConnectionPoolSize(64)
                .setSlaveConnectionPoolSize(64);
        
        return Redisson.create(config);
    }
    
    private String[] formatNodes(List<String> nodes) {
        return nodes.stream()
                .map(node -> "redis://" + node)
                .toArray(String[]::new);
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