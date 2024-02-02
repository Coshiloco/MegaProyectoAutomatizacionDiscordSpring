package com.example.demo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.context.ConfigurableApplicationContext;

public class MusicPlayerGUI extends Application {

    private ConfigurableApplicationContext context;
    private TrackScheduler scheduler;

    @Override
    public void init() throws Exception {
        this.context = DiscordAutomatizacionServerApplication.runSpringApplication();
        this.context.getAutowireCapableBeanFactory().autowireBean(this);
        this.scheduler = context.getBean(TrackScheduler.class);
    }

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox();
        Button playButton = new Button("Play");
        Button pauseButton = new Button("Pause");
        Button skipButton = new Button("Skip");
        Button stopButton = new Button("Stop");

        pauseButton.setOnAction(event -> scheduler.pause());
        skipButton.setOnAction(event -> scheduler.nextTrack());
        stopButton.setOnAction(event -> scheduler.stop());

        // Tu lógica de manejo del botón Play

        root.getChildren().addAll(playButton, pauseButton, skipButton, stopButton);
        Scene scene = new Scene(root, 400, 200);
        primaryStage.setTitle("Discord Music Bot Control");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        context.close();
        Platform.exit();
    }
}
