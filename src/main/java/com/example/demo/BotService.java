package com.example.demo;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class BotService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BotService.class);
    private final GatewayDiscordClient client;
    private final CommandService commandService;

    @Autowired
    public BotService(GatewayDiscordClient client, CommandService commandService) {
        this.client = client;
        this.commandService = commandService;
        registerEventHandlers();
    }

    private void registerEventHandlers() {
        client.on(ReadyEvent.class, event -> {
            LOGGER.info("Bot is ready!");
            return Mono.empty();
        }).subscribe();

        // Procesar cada mensaje que recibe el bot
        client.on(MessageCreateEvent.class, this::handleMessageCreateEvent).subscribe();
    }

    private Mono<Void> handleMessageCreateEvent(MessageCreateEvent event) {
        return Mono.just(event)
                .filter(e -> e.getMessage().getAuthor().map(user -> !user.isBot()).orElse(false))
                .flatMap(e -> commandService.processCommand(e.getMessage()))
                .then();
    }

    @EventListener(ContextClosedEvent.class)
    public void stopBot() {
        if (client != null) {
            client.logout().block();
            LOGGER.info("Bot stopped successfully.");
        }
    }
}
