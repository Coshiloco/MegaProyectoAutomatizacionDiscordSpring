package com.example.demo;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.voice.AudioProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


@Component
public class MusicCommand implements Command {

    private final GatewayDiscordClient client;
    private final AudioPlayerManager playerManager;
    private final AudioProvider provider;
    private final TrackScheduler scheduler;

    private final AudioPlayer player;

    @Autowired
    public MusicCommand(GatewayDiscordClient client) {
        this.client = client;
        this.playerManager = new DefaultAudioPlayerManager();
        playerManager.getConfiguration()
                .setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
        AudioSourceManagers.registerRemoteSources(playerManager);
        this.player = playerManager.createPlayer();
        this.provider = new LavaPlayerAudioProvider(player);
        this.scheduler = new TrackScheduler(player);
    }

    @Override
    public String getName() {
        return "music";
    }

    @Override
    public Mono<Void> execute(Message message) {
        String[] parts = message.getContent().split("\\s+", 3);
        if (parts.length < 2 || !"!music".equalsIgnoreCase(parts[0])) {
            return Mono.empty();
        }

        return switch (parts[1].toLowerCase()) {
            case "join" -> joinChannel(message);
            case "play" -> {
                if (parts.length < 3) {
                    yield Mono.empty();
                }
                yield playTrack(message, parts[2]); // URL is missing
            }
            default -> handleUnknownSubcommand(message);
        };
    }

    private Mono<Void> joinChannel(Message message) {
        return message.getAuthorAsMember()
                .flatMap(Member::getVoiceState)
                .flatMap(VoiceState::getChannel)
                .flatMap(channel -> channel.join(spec -> spec.setProvider(provider)))
                .then();
    }

    private Mono<Void> playTrack(Message message, String trackUrl) {
        return Mono.justOrEmpty(message.getGuildId())
                .flatMap(guildId -> {
                    return Mono.create(sink -> playerManager.loadItemOrdered(guildId, trackUrl, new AudioLoadResultHandler() {
                        @Override
                        public void trackLoaded(AudioTrack track) {
                            scheduler.queue(track);
                            sink.success();
                        }

                        @Override
                        public void playlistLoaded(AudioPlaylist playlist) {
                            var firstTrack = playlist.getSelectedTrack() != null ? playlist.getSelectedTrack() : playlist.getTracks().get(0);
                            scheduler.queue(firstTrack);
                            sink.success();
                        }

                        @Override
                        public void noMatches() {
                            sink.error(new IllegalArgumentException("No matches found for " + trackUrl));
                        }

                        @Override
                        public void loadFailed(FriendlyException exception) {
                            sink.error(exception);
                        }
                    }));
                })
                .then();
    }


    private Mono<Void> handleUnknownSubcommand(Message message) {
        // Implementation for handling unknown subcommands...
        return Mono.empty();
    }
}
