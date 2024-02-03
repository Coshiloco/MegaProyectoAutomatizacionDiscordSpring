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
        if (!player.isPaused()) {
            player.setPaused(true);
            if (player.isPaused()) {
                System.out.println("Player is now paused");
            } else {
                System.out.println("Failed to pause the player");
            }
        } else {
            System.out.println("Player was already paused");
        }
    }


    public void resume() {
        System.out.println("Resume button clicked");
        player.setPaused(false);
    }

    public void stop() {
        System.out.println("Stop button clicked");
        if (!queue.isEmpty() || player.getPlayingTrack() != null) {
            queue.clear();
            player.stopTrack();
            player.setPaused(false); // Esto asegura que el estado de pausa se resetea
            System.out.println("Playback has been stopped and the queue has been cleared.");
        } else {
            System.out.println("There was nothing to stop. The queue was empty and no track was playing.");
        }
    }



    public void skip() {
        System.out.println("Skip button clicked");
        if (!queue.isEmpty() || player.getPlayingTrack() != null) {
            AudioTrack playingTrack = player.getPlayingTrack();
            nextTrack();
            System.out.println("Skipped track: " + (playingTrack != null ? playingTrack.getInfo().title : "No track was playing."));
        } else {
            System.out.println("There was no track to skip.");
        }
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
