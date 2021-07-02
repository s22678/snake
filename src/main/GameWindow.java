package main;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;

public class GameWindow extends Application {

    public static final int SEGMENT_SIZE = 20;
    public static final int WIDTH = 45 * SEGMENT_SIZE;
    public static final int HEIGHT = 45 * SEGMENT_SIZE;

    public static GameState state = GameState.STOPPED;
    public static Direction dir = Direction.RIGHT;

    public static boolean moved = false;
    public static int score;

    Stage stage;
    GameLoop gameLoop;
    Scene game;
    Scene menu;
    Scene tutorialScene;

    Label scoreLabel;

    FileChooser fileChooser = new FileChooser();

    Button newGameButton;
    Button loadGameButton;
    Button saveGameButton;
    Button quitButton;

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        stage.setTitle("Snake!");
        stage.setResizable(false);

        //Create tutorial
        tutorialScene = getTutorialScene();

        // create food and snake
        Group snakeBody = new Group();
        Rectangle food = new Rectangle(SEGMENT_SIZE, SEGMENT_SIZE);
        food.setFill(Color.BLUE);
        food.setTranslateX(GameLoop.setRandomTranslate(WIDTH - SEGMENT_SIZE));
        food.setTranslateY(GameLoop.setRandomTranslate(HEIGHT - SEGMENT_SIZE));
        Pane rootGame = new Pane();
        game = new Scene(rootGame, WIDTH, HEIGHT, Color.rgb(160, 217, 63));
        rootGame.getChildren().addAll(snakeBody, food);

        // create menu
        newGameButton = new Button("new game");
        loadGameButton = new Button("load game");
        saveGameButton = new Button("save game");
        quitButton = new Button("quit game");
        scoreLabel = new Label("score");
        scoreLabel.setFont(new Font("Arial", 24));
        VBox rootMenu = new VBox(30);
        rootMenu.setAlignment(Pos.CENTER);
        rootMenu.getChildren().addAll(newGameButton, loadGameButton, saveGameButton, scoreLabel, quitButton);
        menu = new Scene(rootMenu, WIDTH, HEIGHT, Color.LIGHTBLUE);

        gameLoop = new GameLoop(snakeBody, food);
        if (state == GameState.RUNNING) {
            gameLoop.start();
        }
        stage.setScene(menu);
        stage.show();

        // actions for nodes
        nodeActions(stage);
    }

    @Override
    public void stop() {
        gameLoop.stop();
    }

    // all node actions in one method
    private void nodeActions(Stage stage) {
        newGameButton.setOnAction(this::newGameAction);
        saveGameButton.setOnAction(this::saveGameAction);
        loadGameButton.setOnAction(this::loadGameAction);
        quitButton.setOnAction(this::quitGameAction);

        stage.addEventFilter(KeyEvent.KEY_PRESSED, this::keyControlledLoop);
        stage.addEventFilter(CustomEvent.START, this::startLoop);
        stage.addEventFilter(CustomEvent.STOP, this::stopLoop);

        game.addEventFilter(KeyEvent.KEY_PRESSED, this::keyControlledMovement);
    }

    // tutorial scene
    private Scene getTutorialScene() {
        Label tutorialLabel = new Label("Collect food to get points.\n\nPress 'w' or 'up' to go up\nPress 's' or 'down' to go down\nPress 'a' or 'left' to go left\nPress 'd' or 'right' to go right.\n\nSpeed increases with each food eaten!\nPress enter to continue");
        tutorialLabel.setFont(new Font("Arial", 20));
        VBox tutorialRoot = new VBox();
        tutorialRoot.setAlignment(Pos.CENTER);
        tutorialRoot.getChildren().addAll(tutorialLabel);
        Scene tutorialScene = new Scene(tutorialRoot, WIDTH, HEIGHT);
        return tutorialScene;
    }

    // Key Events
    public void keyControlledMovement(KeyEvent evt) {
        if(moved) {
            switch (evt.getCode()) {
                case UP:
                case W:
                    if(dir != Direction.DOWN) dir = Direction.UP; break;
                case DOWN:
                case S:
                    if(dir != Direction.UP) dir = Direction.DOWN; break;
                case LEFT:
                case A:
                    if(dir != Direction.RIGHT) dir = Direction.LEFT; break;
                case RIGHT:
                case D:
                    if(dir != Direction.LEFT) dir = Direction.RIGHT; break;
            }
        }
        moved = false;
    }

    public void keyControlledLoop(KeyEvent evt) {
        if (evt.getCode() == KeyCode.ESCAPE) {
            System.out.println("pressed escape");
            if (state == GameState.RUNNING) {
                pauseGame();
            } else if (state == GameState.STOPPED) {
                startNewGame();
            } else if (state == GameState.PAUSED) {
                resumeGame();
            } else {
                System.out.println("shouldn't happen");
            }
        }
        if (evt.getCode() == KeyCode.ENTER && state == GameState.TUTORIAL) {
            System.out.println(state.toString());
            startNewGame();
        }
    }

    // Action Events
    public void newGameAction(ActionEvent avt) {
        state = GameState.TUTORIAL;
        gameLoop.stopGame();
        stage.setScene(tutorialScene);
    }

    public void saveGameAction(ActionEvent avt) {
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                PrintWriter writer;
                writer = new PrintWriter(file);
                for(Node n : gameLoop.getSnake()) {
                    writer.print(n.getTranslateX() + " " + n.getTranslateY() + ", ");
                }
                writer.print(": " + gameLoop.getFood().getTranslateX() + " " + gameLoop.getFood().getTranslateY());
                writer.print(": " + gameLoop.getSpeed());
                writer.print(": " + score);
                writer.print(": " + dir.toString());
                writer.close();
            } catch (IOException ex) {
                System.out.println("ERROR SAVING THE FILE!");
            }
        }
    }

    public void loadGameAction(ActionEvent avt) {
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(stage);
        if(file != null) {
            try {
                BufferedReader input = new BufferedReader(new FileReader(file));
                String line = input.readLine();
                String[] data = line.split(": ");
                gameLoop.startFromSave(data[0], data[1], data[2]);
                score = Integer.parseInt(data[3]);
                dir.setDirection(data[4]);
                resumeGame();
            } catch (FileNotFoundException ex) {
                System.out.println("FILE NOT FOUND");
            } catch (IOException ex) {
                System.out.println("IO EXCEPTION");
            }
        }
    }

    public void quitGameAction(ActionEvent avt) {
        Stage stage = (Stage) quitButton.getScene().getWindow();
        stage.close();
    }

    // Custom Events
    public void stopLoop(CustomEvent evt) {
        if(state == GameState.RUNNING) {
            System.out.println("stopping");
            stopGame();
        }
    }

    public void startLoop(CustomEvent evt) {
        if(state == GameState.PAUSED) {
            System.out.println("starting");
            startNewGame();
        }
    }

    public void addSceneToStage(Scene scene) {
        stage.setScene(scene);
    }

    // Game state control
    public void pauseGame() {
        System.out.println("pausing game");
        state = GameState.PAUSED;
        scoreLabel.setText("score " + score);
        gameLoop.stop();
        addSceneToStage(menu);
        System.out.println("paused game");
    }

    public void resumeGame() {
        System.out.println("resuming game");
        state = GameState.RUNNING;
        saveGameButton.setDisable(false);
        gameLoop.start();
        addSceneToStage(game);
        System.out.println("resumed game");
    }

    public void stopGame() {
        System.out.println("stopping game");
        state = GameState.STOPPED;
        scoreLabel.setText("score " + score);
        gameLoop.stop();
        saveGameButton.setDisable(true);
        addSceneToStage(menu);
        System.out.println("stopped game");
    }

    public void startNewGame() {
        System.out.println("starting new game");
        state = GameState.RUNNING;
        score = 0;
        saveGameButton.setDisable(false);
        gameLoop.startNewGame();
        addSceneToStage(game);
        gameLoop.start();
        System.out.println("started new game");
    }

    // main method
    public static void main(String[] args) {
        launch(args);
    }
}
