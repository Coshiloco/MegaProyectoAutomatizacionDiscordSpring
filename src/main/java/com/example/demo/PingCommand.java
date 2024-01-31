package com.example.demo;

import discord4j.core.object.entity.Message;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import com.example.demo.Command;

@Component
public class PingCommand implements Command {
    @Override
    public String getName() {
        return "ping"; // Elimina el '!' para que coincida con el manejador
    }

    @Override
    public Mono<Void> execute(Message message) {
        return message.getChannel()
                .flatMap(channel -> channel.createMessage("Pong!"))
                .then();
    }
}