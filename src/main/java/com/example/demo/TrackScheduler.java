package com.example.demo;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler implements AudioLoadResultHandler {
    private static final Logger log = LoggerFactory.getLogger(TrackScheduler.class);
    private final AudioPlayer player;
    private final BlockingQueue<AudioTrack> queue = new LinkedBlockingQueue<>();

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
    }

    public void queue(AudioTrack track) {
        if (!player.startTrack(track, false)) {
            queue.offer(track);
        }
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        log.info("Track {} is now playing.", track.getInfo().title);
        queue(track);
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        log.info("Playlist {} is now playing.", playlist.getName());
        playlist.getTracks().forEach(this::queue);
    }

    @Override
    public void noMatches() {
        log.warn("There were no matches for the requested track.");
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        log.error("Loading of the track failed with message: {}", exception.getMessage());
    }
}
