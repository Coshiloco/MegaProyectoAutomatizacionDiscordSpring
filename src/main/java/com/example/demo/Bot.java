package com.example.demo;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class Bot {

    private final GatewayDiscordClient gateway;
    private final CommandService commandService;

    @Autowired
    public Bot(GatewayDiscordClient gateway, CommandService commandService) {
        this.gateway = gateway;
        this.commandService = commandService;
        registerEventHandlers();
    }

    private void registerEventHandlers() {
        gateway.on(MessageCreateEvent.class, this::handleMessageCreateEvent).subscribe();
    }

    private Mono<Void> handleMessageCreateEvent(MessageCreateEvent event) {
        // La lógica actual parece correcta aquí
        return Mono.just(event)
                .filter(e -> e.getMessage().getAuthor().map(user -> !user.isBot()).orElse(false))
                .flatMap(e -> commandService.processCommand(e.getMessage()))
                .then();
    }
}