package com.example.demo;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.rest.util.Permission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Component
public class RoleCommand implements Command {

    private final GatewayDiscordClient client;

    @Autowired
    public RoleCommand(GatewayDiscordClient client) {
        this.client = client;
    }

    @Override
    public String getName() {
        return "role";
    }

    @Override
    public Mono<Void> execute(Message message) {
        // Comprobamos si el mensaje se envió en un servidor
        return Mono.justOrEmpty(message.getGuildId())
                .flatMap(guildId -> message.getGuild().flatMap(guild -> {
                    List<String> args = Arrays.asList(message.getContent().split(" "));
                    if (args.size() < 3) {
                        // Si no hay suficientes argumentos, enviamos un mensaje de error
                        return message.getChannel().flatMap(channel -> channel.createMessage("Usage: !role @user rolename"));
                    }
                    // Extraemos el nombre del rol y el ID del usuario
                    String roleName = args.get(2);
                    String userId = args.get(1).replaceAll("[^0-9]", "");
                    // Buscamos el rol por nombre
                    return guild.getRoles()
                            .filter(role -> role.getName().equalsIgnoreCase(roleName))
                            .next()
                            .flatMap(role -> {
                                // Buscamos el miembro por ID
                                return guild.getMemberById(Snowflake.of(userId))
                                        .flatMap(member -> {
                                            // Comprobamos los permisos del miembro que invoca el comando
                                            return member.getBasePermissions()
                                                    .flatMap(permissions -> {
                                                        if (permissions.contains(Permission.MANAGE_ROLES)) {
                                                            // Si tiene permisos, asignamos el rol
                                                            return member.addRole(role.getId())
                                                                    .then(message.getChannel().flatMap(channel -> channel.createMessage("Role " + roleName + " added to <@" + userId + ">")));
                                                        } else {
                                                            // Si no, enviamos un mensaje de error
                                                            return message.getChannel().flatMap(channel -> channel.createMessage("You do not have permissions to manage roles."));
                                                        }
                                                    });
                                        });
                            })
                            // Si algo falla en la cadena, enviamos el mensaje de error
                            .onErrorResume(e -> message.getChannel().flatMap(channel -> channel.createMessage("Error: " + e.getMessage())));
                }))
                // Convertimos el resultado a Mono<Void> para cumplir con la firma del método
                .then();
    }
}
