package com.yuriyeom.openaiservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuriyeom.openaiservice.domain.ChatCompletionRequest;
import com.yuriyeom.openaiservice.domain.ChatCompletionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class OpenAIAPIService {

    private final WebClient webClient;
    @Value("${openai.model}")
    private String model;

    public OpenAIAPIService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper,
                            @Value("${openai.key}") String OPENAIAPIKEY) {
        this.webClient = webClientBuilder
                .baseUrl("https://api.openai.com/v1/chat/completions")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + OPENAIAPIKEY)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public String getOpenAIResponse(String prompt){
        ChatCompletionRequest chatCompletionRequest
                = new ChatCompletionRequest(model, prompt);

        ChatCompletionResponse response = webClient.post()
                .uri("")
                .bodyValue(chatCompletionRequest)
                .retrieve()
                .bodyToMono(ChatCompletionResponse.class)
                .block();

        return response.getChoices().get(0).getMessage().getContent();
    }
}
