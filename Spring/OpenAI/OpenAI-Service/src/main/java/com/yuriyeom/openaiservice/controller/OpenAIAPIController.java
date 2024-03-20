package com.yuriyeom.openaiservice.controller;

import com.yuriyeom.openaiservice.domain.ChatCompletionRequest;
import com.yuriyeom.openaiservice.domain.ChatCompletionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class OpenAIAPIController {

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping("/hitOpenAIAPI")
    public String getOpenAIResponse(@RequestBody String prompt){
        ChatCompletionRequest chatCompletionRequest
                = new ChatCompletionRequest("gpt-3.5-turbo", prompt);

        ChatCompletionResponse response =
                restTemplate.postForObject("https://api.openai.com/v1/chat/completions",
                        chatCompletionRequest, ChatCompletionResponse.class );

        return response.getChoices().get(0).getMessage().getContent();
    }
}
