package com.safely.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    // 이 설정을 사용하면 application.yml파일의 redis password가 무시됨.
    // 그래서 주석처리 하고 아래의 코드를 추가함.
//    @Bean
//    public RedisConnectionFactory redisConnectionFactory() {
//        // application.yml에 host, port 설정되어 있으면
//        // Spring Boot가 자동으로 주입해줌
//        return new LettuceConnectionFactory();
//    }

    // @Value값과 public RedisConnectionFactory redisConnectionFactory() 코드들은 없어도 잘 동작함.
    // new LettuceConnectionFactory()가 application.yaml의 설정을 무시하기 때문에 직접 값을 넣어준 코드임.
    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.password}")
    private String password;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(host);
        config.setPort(port);
        config.setPassword(password);

        return new LettuceConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(
            RedisConnectionFactory connectionFactory
    ) {
        RedisTemplate<String, String> template =
                new RedisTemplate<>();

        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());

        return template;
    }
}