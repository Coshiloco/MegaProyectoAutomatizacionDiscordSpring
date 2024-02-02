package com.example.demo;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TrackScheduler implements AudioLoadResultHandler {

    private static final Logger log = LoggerFactory.getLogger(TrackScheduler.class);
    private final AudioPlayer player;

    public TrackScheduler(final AudioPlayer player) {
        this.player = player;
    }

    @Override
    public void trackLoaded(final AudioTrack track) {
        // LavaPlayer found an audio source for us to play
        if (!player.startTrack(track, false)) {
            log.info("Track {} is now playing.", track.getInfo().title);
        } else {
            log.warn("Could not start track, maybe a track is already playing.");
        }
    }

    @Override
    public void playlistLoaded(final AudioPlaylist playlist) {
        // LavaPlayer found multiple AudioTracks from some playlist
        for (AudioTrack track : playlist.getTracks()) {
            if (player.startTrack(track, false)) {
                log.info("Playlist {} is now playing.", playlist.getName());
                return;
            }
        }
        log.warn("Could not start any track from the playlist {}", playlist.getName());
    }

    @Override
    public void noMatches() {
        // LavaPlayer did not find any audio to extract
        log.warn("There were no matches for the requested track.");
    }

    @Override
    public void loadFailed(final FriendlyException exception) {
        // LavaPlayer could not parse an audio source for some reason
        log.error("Loading of the track failed with message: {}", exception.getMessage());
    }
}
