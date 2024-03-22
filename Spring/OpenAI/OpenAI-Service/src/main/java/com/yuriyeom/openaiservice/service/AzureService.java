package com.yuriyeom.openaiservice.service;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.*;
import com.azure.core.credential.AzureKeyCredential;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AzureService {

    private final OpenAIAsyncClient client;

    String azureOpenaiKey = "{azure-open-ai-key}";
    String endpoint = "{azure-open-ai-endpoint}";
    String deploymentOrModelId = "{azure-open-ai-deployment-model-id}";

    public AzureService(){
        this.client = new OpenAIClientBuilder()
                .endpoint("")
                .credential(new AzureKeyCredential(""))
                .buildAsyncClient();
    }

    public List<ChatRequestMessage> makeChatMessage(){
        List<ChatRequestMessage> chatMessages = new ArrayList<>();
        chatMessages.add(new ChatRequestSystemMessage("You are a helpful assistant. You will talk like a pirate."));
        chatMessages.add(new ChatRequestUserMessage("Can you help me?"));
        chatMessages.add(new ChatRequestAssistantMessage("Of course, me hearty! What can I do for ye?"));
        chatMessages.add(new ChatRequestUserMessage("What's the best way to train a parrot?"));

        return chatMessages;
    }

    public void getChat(){

        List<ChatRequestMessage> messages = makeChatMessage();

        client.getChatCompletions(deploymentOrModelId, new ChatCompletionsOptions(messages)).subscribe(
                chatCompletions -> {
                    System.out.printf("Model ID=%s is created at %s.%n", chatCompletions.getId(), chatCompletions.getCreatedAt());
                    for(ChatChoice choice : chatCompletions.getChoices()){
                        ChatResponseMessage message = choice.getMessage();
                        System.out.printf("Index : %d, Chat Role : %s.%n", choice.getIndex(), message.getRole());
                        System.out.println("Message : ");
                        System.out.println(message.getContent());
                    }
                },
                error -> System.err.println("Threr was an error getting chat completions." + error),
                () -> System.out.println("Completed called getChatCompletions.")
        );

    }
}
