package com.example.demo;


import discord4j.core.object.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
public class CommandHandler {
    private final Map<String, Command> commands = new HashMap<>();

    @Autowired
    private PingCommand pingCommand; // Asume que PingCommand está anotado con @Component

    @Autowired
    private RoleCommand roleCommand;

    @Autowired
    private UserInfoCommand userInfoCommand;

    @Autowired
    private MusicCommand musicCommand;


    @Autowired
    private TalkWithIA talkWithIA;



    @PostConstruct
    public void init() {
        registerCommand(pingCommand);
        registerCommand(roleCommand);
        registerCommand(userInfoCommand);
        registerCommand(musicCommand);
        registerCommand(talkWithIA);
    }

    public void registerCommand(Command command) {
        commands.put(command.getName().toLowerCase(), command);
    }

    public Mono<Void> handle(String commandName, Message message) {
        return Mono.justOrEmpty(commands.get(commandName.toLowerCase()))
                .flatMap(command -> command.execute(message));
    }
}