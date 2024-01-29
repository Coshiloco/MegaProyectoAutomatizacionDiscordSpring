package com.example.demo;


import discord4j.core.object.entity.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CommandService {

    public Mono<Void> processCommand(Message message) {
        // Aquí se procesaría el mensaje y se ejecutarían los comandos correspondientes.
        String content = message.getContent();
        // Ejemplo simple para procesar el comando !ping
        if (content.startsWith("!ping")) {
            return message.getChannel()
                    .flatMap(channel -> channel.createMessage("Pong!"))
                    .then();
        }
        return Mono.empty();
    }
}