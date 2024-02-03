package com.example.demo;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.context.ConfigurableApplicationContext;

public class MusicPlayerGUI extends Application {

    private TrackScheduler scheduler;
    private MusicCommand musicCommand; // Agrega esta referencia para poder llamar a los métodos del comando de música

    @Override
    public void start(Stage primaryStage) {
        ConfigurableApplicationContext context = DiscordAutomatizacionServerApplication.getContext();
        this.scheduler = context.getBean(TrackScheduler.class);
        this.musicCommand = context.getBean(MusicCommand.class); // Asegúrate de obtener la instancia de MusicCommand desde el contexto de Spring

        VBox root = new VBox();
        TextField trackUrlField = new TextField(); // Un campo de texto para ingresar la URL de la pista
        trackUrlField.setPromptText("Enter track URL here");
        Button playButton = new Button("Play");
        Button pauseButton = new Button("Pause");
        Button skipButton = new Button("Skip");
        Button stopButton = new Button("Stop");

        playButton.setOnAction(event -> {
            String trackUrl = trackUrlField.getText(); // Obtén la URL de la pista del campo de texto
            if (!trackUrl.isEmpty()) {
                musicCommand.playTrackFromURL(trackUrl); // Implementa este método en MusicCommand
            }
        });
        pauseButton.setOnAction(event -> scheduler.pause());
        skipButton.setOnAction(event -> scheduler.skip());
        stopButton.setOnAction(event -> scheduler.stop());

        root.getChildren().addAll(trackUrlField, playButton, pauseButton, skipButton, stopButton);
        Scene scene = new Scene(root, 400, 200);
        primaryStage.setTitle("Discord Music Bot Control");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        DiscordAutomatizacionServerApplication.getContext().close();
    }
}
