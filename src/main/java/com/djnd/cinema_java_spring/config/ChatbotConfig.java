package com.djnd.cinema_java_spring.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ChatbotConfig {

    @Bean
    public RestTemplate chatbotRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2_000); // 2s để mở kết nối tới Groq
        factory.setReadTimeout(5_000); // 5s chờ Groq trả lời
        return new RestTemplate(factory);
    }

    @Bean(name = "chatbotExecutor")
    public Executor chatbotExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("chatbot-groq-");
        executor.initialize();
        return executor;
    }
}
