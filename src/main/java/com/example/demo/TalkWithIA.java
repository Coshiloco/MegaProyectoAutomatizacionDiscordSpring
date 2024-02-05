package com.example.demo;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.voice.VoiceConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class TalkWithIA implements Command {

    private final GatewayDiscordClient client;

    @Autowired
    public TalkWithIA(GatewayDiscordClient client) {
        this.client = client;
    }

    @Override
    public String getName() {
        return "ia";
    }

    @Override
    public Mono<Void> execute(Message message) {
        // Obtiene el estado de voz del miembro de forma reactiva
        return message.getAuthorAsMember()
                .flatMap(Member::getVoiceState) // Obtiene el VoiceState del Member
                .flatMap(VoiceState::getChannel) // Obtiene el canal de voz actual del VoiceState
                .flatMap(channel ->
                        // Primero verifica si ya existe una conexión de voz para este guild
                        client.getVoiceConnectionRegistry().getVoiceConnection(channel.getGuildId())
                                .flatMap(VoiceConnection::disconnect) // Si existe, desconéctala
                                .then(channel.join(spec -> {
                                })) // Luego, únete al nuevo canal de voz
                )
                .then(message.getChannel().flatMap(channel -> channel.createMessage("Unido al canal de voz."))) // Envía un mensaje de confirmación
                .then(); // Completa la operación
    }

}
