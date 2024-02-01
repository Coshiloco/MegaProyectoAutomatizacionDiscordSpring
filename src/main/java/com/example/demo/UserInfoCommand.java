package com.example.demo;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.ZoneId;
import java.util.Locale;

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
                .flatMap(guild -> client.getUserById(userId)
                        .flatMap(user -> createOrGetChannel(guild, "informacion-usuarios")
                                .flatMap(channel -> {
                                    String info = formatUserInfo(user);
                                    return channel.createMessage(info);
                                })))
                .then();
    }

    private String formatUserInfo(User user) {
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL).withLocale(Locale.ENGLISH);
        ZonedDateTime creationDate = user.getId().getTimestamp().atZone(ZoneId.systemDefault());
        String formattedDate = creationDate.format(formatter);

        return "User Information:\n" +
                "ID: " + user.getId().asString() + "\n" +
                "Username: " + user.getUsername() + "\n" +
                "Avatar URL: " + user.getAvatarUrl() + "\n" +
                "Account Creation Date: " + formattedDate + "\n";
    }

    private Mono<TextChannel> createOrGetChannel(Guild guild, String channelName) {
        return guild.getChannels()
                .ofType(TextChannel.class)
                .filter(channel -> channel.getName().equalsIgnoreCase(channelName))
                .next()
                .switchIfEmpty(guild.createTextChannel(spec -> spec.setName(channelName)));
    }
}
