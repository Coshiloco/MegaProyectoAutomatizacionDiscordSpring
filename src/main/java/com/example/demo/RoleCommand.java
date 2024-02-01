package com.example.demo;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
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
        return Mono.justOrEmpty(message.getGuildId())
                .flatMap(guildId -> message.getGuild().flatMap(guild -> {
                    List<String> args = Arrays.asList(message.getContent().split("\\s+"));
                    if (args.size() < 3) {
                        return message.getChannel().flatMap(channel -> channel.createMessage("Usage: !role @user rolename")).then();
                    }
                    String roleName = args.get(2);
                    String userId = args.get(1).replaceAll("[^0-9]", "");

                    return assignRoleToUser(guild, roleName, userId)
                            .then(createOrGetChannel(guild, "asignacion-roles"))
                            .flatMap(channel -> channel.createMessage("Role " + roleName + " added to <@" + userId + ">"));
                }))
                .then();
    }

    private Mono<Void> assignRoleToUser(Guild guild, String roleName, String userId) {
        return guild.getRoles()
                .filter(role -> role.getName().equalsIgnoreCase(roleName))
                .next()
                .flatMap(role -> guild.getMemberById(Snowflake.of(userId))
                        .flatMap(member -> member.getBasePermissions()
                                .flatMap(permissions -> {
                                    if (permissions.contains(Permission.MANAGE_ROLES)) {
                                        return member.addRole(role.getId());
                                    } else {
                                        return Mono.error(new IllegalArgumentException("You do not have permissions to manage roles."));
                                    }
                                })))
                .then(); // Aseguramos que se devuelva Mono<Void>
    }

    private Mono<TextChannel> createOrGetChannel(Guild guild, String channelName) {
        return guild.getChannels()
                .ofType(TextChannel.class)
                .filter(channel -> channel.getName().equalsIgnoreCase(channelName))
                .next()
                .switchIfEmpty(guild.createTextChannel(spec -> spec.setName(channelName)));
    }
}
