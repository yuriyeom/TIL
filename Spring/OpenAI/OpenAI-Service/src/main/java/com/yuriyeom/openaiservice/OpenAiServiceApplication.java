package com.yuriyeom.openaiservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class OpenAiServiceApplication {

    @Value("${openai.key}")
    private String openaiApiKey;

    public static void main(String[] args) {
        SpringApplication.run(OpenAiServiceApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate(){
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(((request, body, execution) -> {
            request.getHeaders().add("Authorization",
                    "Bearer " + openaiApiKey);
            return execution.execute(request, body);
        }));
        return restTemplate;
    }
}
