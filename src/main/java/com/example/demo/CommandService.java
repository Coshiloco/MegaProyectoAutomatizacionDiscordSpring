package com.example.demo;


import com.example.demo.CommandHandler;
import discord4j.core.object.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CommandService {

    private final CommandHandler commandHandler;

    @Autowired
    public CommandService(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }


    public Mono<Void> processCommand(Message message) {
        String content = message.getContent();
        if (content.startsWith("!")) { // Asegurar que solo procesa mensajes que empiezan con '!'
            String[] splitMessage = content.split(" ", 2);
            String commandName = splitMessage[0].substring(1); // Elimina el '!' del inicio
            return commandHandler.handle(commandName, message);
        }
        return Mono.empty(); // Ignora mensajes que no son comandos
    }
}