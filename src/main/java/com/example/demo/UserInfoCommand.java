package com.example.demo;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.presence.Presence;
import discord4j.core.object.entity.Role;
import discord4j.rest.util.Color;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.Duration;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class UserInfoCommand implements Command {

    private final GatewayDiscordClient client;

    @Autowired
    public UserInfoCommand(GatewayDiscordClient client) {
        this.client = client;
    }

    @Override
    public String getName() {
        return "userinfo";
    }

    @Override
    public Mono<Void> execute(Message message) {
        String[] content = message.getContent().split("\\s+");
        if (content.length < 2) {
            return message.getChannel()
                    .flatMap(channel -> channel.createMessage("Usage: !userinfo @username"))
                    .then();
        }

        String userIdString = content[1].replaceAll("<@!?(\\d+)>", "$1");
        if (userIdString.isEmpty()) {
            return message.getChannel()
                    .flatMap(channel -> channel.createMessage("Error: Invalid user ID."))
                    .then();
        }

        Snowflake userId = Snowflake.of(userIdString);

        return message.getGuild()
                .flatMap(guild -> guild.getMemberById(userId)
                        .flatMap(member -> client.getUserById(userId)
                                .flatMap(user -> createOrGetChannel(guild, "informacion-usuarios")
                                        .flatMap(channel -> {
                                            String info = formatUserInfo(guild, user, member);
                                            return channel.createMessage(info);
                                        }))))
                .then();
    }

    private String formatUserInfo(Guild guild, User user, Member member) {
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL)
                .withLocale(Locale.ENGLISH);

        // User's creation date
        ZonedDateTime creationDate = Instant.ofEpochMilli(user.getId().getTimestamp().toEpochMilli())
                .atZone(ZoneId.systemDefault());
        String formattedCreationDate = creationDate.format(formatter);

        // Member's join date
        String formattedJoinDate = member.getJoinTime()
                .map(time -> ZonedDateTime.ofInstant(time, ZoneId.systemDefault()).format(formatter))
                .orElse("Unknown");

        // Member's roles, resolving the roles into a string needs to be done in a reactive chain
        Mono<String> rolesMono = member.getRoles()
                .map(Role::getName)
                .collect(Collectors.joining(", "))
                .defaultIfEmpty("No roles");

        // User's status, this needs to be done reactively
        Mono<String> statusMono = member.getPresence()
                .map(Presence::getStatus)
                .map(Object::toString)
                .defaultIfEmpty("Unknown");

        // Since discriminatorValue is not deprecated, you can use it directly
        String discriminator = user.getDiscriminator(); // Even though it's deprecated, if no alternative exists yet, this is what you use.

        // Time in Server
        Duration timeInServer = Duration.between(member.getJoinTime().orElse(Instant.now()), Instant.now());
        long days = timeInServer.toDays();
        long hours = timeInServer.toHours() % 24;
        long minutes = timeInServer.toMinutes() % 60;
        long seconds = timeInServer.getSeconds() % 60;
        String formattedTimeInServer = String.format("%d días %02d horas %02d minutos %02d segundos", days, hours, minutes, seconds);

        // Combine everything into a reactive chain
        return Mono.zip(Mono.just(formattedCreationDate), Mono.just(formattedJoinDate), rolesMono, statusMono)
                .map(tuple -> {
                    String roles = tuple.getT3();
                    String status = tuple.getT4();
                    return "User Information:\n" +
                            "ID: " + user.getId().asString() + "\n" +
                            "Username: " + user.getUsername() + "#" + discriminator + "\n" +
                            "Avatar URL: " + user.getAvatarUrl() + "\n" +
                            "Is Bot: " + user.isBot() + "\n" +
                            "Status: " + status + "\n" +
                            "Roles: " + roles + "\n" +
                            "Account Creation Date: " + tuple.getT1() + "\n" +
                            "Server Join Date: " + tuple.getT2() + "\n"
                            + "Time in Server: " + formattedTimeInServer + "\n";
                })
                .block(); // Finally block to resolve the Mono to a String
    }



    private Mono<TextChannel> createOrGetChannel(Guild guild, String channelName) {
        return guild.getChannels()
                .ofType(TextChannel.class)
                .filter(channel -> channel.getName().equalsIgnoreCase(channelName))
                .next()
                .switchIfEmpty(guild.createTextChannel(channelName)
                        .doOnRequest(ignored -> {/* Aquí puedes realizar acciones adicionales si es necesario */})
                        .cast(TextChannel.class));
    }



}
