package com.example.demo;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class TrackScheduler implements AudioLoadResultHandler {
    private static final Logger log = LoggerFactory.getLogger(TrackScheduler.class);
    private final AudioPlayer player;
    private final BlockingQueue<AudioTrack> queue = new LinkedBlockingQueue<>();

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
    }

    public void queue(AudioTrack track) {
        if (!player.startTrack(track, true)) {
            queue.offer(track);
        }
    }

    public void nextTrack() {
        player.startTrack(queue.poll(), false);
    }

    // Métodos expuestos para la GUI
    public void pause() {
        System.out.println("Pause button clicked");
        player.setPaused(true);
    }

    public void resume() {
        System.out.println("Resume button clicked");
        player.setPaused(false);
    }

    public void stop() {
        System.out.println("Stop button clicked");
        queue.clear();
        player.stopTrack();
        // Asumiendo que queremos también resetear el estado de pausa
        player.setPaused(false);
    }


    public void skip() {
        System.out.println("Skip button clicked");
        nextTrack();
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        log.info("Cargando pista: {}", track.getInfo().title);
        queue(track);
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        log.info("Cargando playlist: {}", playlist.getName());
        playlist.getTracks().forEach(this::queue);
    }

    @Override
    public void noMatches() {
        log.warn("No se encontraron coincidencias.");
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        log.error("La carga de la pista falló: {}", exception.getMessage());
    }

}
