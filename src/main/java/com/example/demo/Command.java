package com.example.demo;

import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

public interface Command {
    String getName();
    Mono<Void> execute(Message message);
}
