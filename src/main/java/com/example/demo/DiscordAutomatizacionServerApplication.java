package com.example.demo;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import javafx.application.Application;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DiscordAutomatizacionServerApplication {

	@Value("${bot.token}")
	private String token;

	private static String[] savedArgs;

	public static void main(String[] args) {
		savedArgs = args;
		Application.launch(MusicPlayerGUI.class, args);
	}

	@Bean
	public GatewayDiscordClient gatewayDiscordClient() {
		return DiscordClientBuilder.create(token)
				.build()
				.login()
				.block();
	}

	static ConfigurableApplicationContext runSpringApplication() {
		return new SpringApplicationBuilder(DiscordAutomatizacionServerApplication.class)
				.run(savedArgs);
	}

	@Bean
	public AudioPlayerManager audioPlayerManager() {
		return new DefaultAudioPlayerManager();
	}

	@Bean
	public AudioPlayer audioPlayer(AudioPlayerManager audioPlayerManager) {
		return audioPlayerManager.createPlayer();
	}
}
