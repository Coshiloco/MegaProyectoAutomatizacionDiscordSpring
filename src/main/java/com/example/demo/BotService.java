package com.example.demo;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class BotService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BotService.class);
    private GatewayDiscordClient client;

    // Inyectamos el GatewayDiscordClient a través del constructor
    public BotService(GatewayDiscordClient client) {
        this.client = client;
        registerEventHandlers();
    }

    private void registerEventHandlers() {
        client.on(ReadyEvent.class, event -> {
            LOGGER.info("Bot is ready!");
            return Mono.empty();
        }).subscribe();

        client.on(MessageCreateEvent.class, event -> {
            // Lógica para manejar la creación de mensajes
            return Mono.empty();
        }).subscribe();
    }

    @EventListener(ContextClosedEvent.class)
    public void stopBot() {
        if (client != null) {
            client.logout().block();
            LOGGER.info("Bot stopped successfully.");
        }
    }
}