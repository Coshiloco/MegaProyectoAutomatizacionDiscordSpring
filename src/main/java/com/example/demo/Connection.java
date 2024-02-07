package com.example.demo;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.voice.VoiceConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class Connection {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final GatewayDiscordClient client;

    @Autowired
    public Connection(GatewayDiscordClient client) {
        this.client = client;
    }

    public Mono<Void> connect(VoiceChannel channel) {
        // Realiza la conexión al canal de voz
        return this.client.getVoiceConnectionRegistry().getVoiceConnection(channel.getGuildId())
                .flatMap(VoiceConnection::disconnect)
                .then(channel.join(spec -> {
                }))
                .doOnSuccess(voiceConnection -> logger.info("Conectado al canal de voz: {}", channel.getName()))
                .then();
    }

    public Mono<Void> disconnect(VoiceChannel channel) {
        // Desconecta del canal de voz
        return this.client.getVoiceConnectionRegistry().getVoiceConnection(channel.getGuildId())
                .flatMap(VoiceConnection::disconnect)
                .doOnSuccess(unused -> logger.info("Desconectado del canal de voz: {}", channel.getName()));
    }
}
