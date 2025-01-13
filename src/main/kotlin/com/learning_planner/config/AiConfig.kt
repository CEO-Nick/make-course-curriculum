package com.learning_planner.config

import com.learning_planner.config.LearningPrompts.Companion.systemPrompt
import org.springframework.ai.chat.client.ChatClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AiConfig {

    @Bean
    fun chatClient(builder: ChatClient.Builder) : ChatClient {
        return builder.defaultSystem(systemPrompt).build()
    }
}