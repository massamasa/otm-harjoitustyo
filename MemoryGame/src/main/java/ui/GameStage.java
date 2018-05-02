package ui;

import domain.CountryGameBoard;
import domain.GameBoard;
import domain.DataLogic;
import domain.Score;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class GameStage {

    private Button firstMemoryButton;
    private int timer;
    private int penalty;
    private boolean spaceDown;
    private int dimension;
    private GameBoard gameBoard;
    private Label foundNumbers;
    private GridPane playableGp;
    private GridPane blankGp;
    private Label penaltyLabel;
    private Stage primaryStage;
    private Button returnToMenuButton;
    private int gameType;

    /**
     * Default GameStage object with plain Integers
     *
     * @param primaryStage
     */
    public GameStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.gameType = 0;
    }

    /**
     * GameStage object with an option to change game types
     *
     * @param primaryStage
     * @param gameType 0 for plain integers, 1 for country codes
     */
    public GameStage(Stage primaryStage, int gameType) {
        this.primaryStage = primaryStage;
        this.gameType = gameType;
    }

    /**
     * Scene providing the graphical user interface for playing the game.
     *
     * @param dimension rectangular dimensions of the game board's card table
     * @param nickname player's nickname
     * @return Scene object
     */
    public Scene gameScene(int dimension, String nickname) {
        this.dimension = dimension;
        this.timer = 0;
        this.firstMemoryButton = newBlankCardButton();
        if (gameType == 1) {
            this.gameBoard = new CountryGameBoard(dimension);
        } else {
            this.gameBoard = new GameBoard(dimension);
        }
        this.playableGp = playableGp();
        this.blankGp = nakedGp();
        BorderPane bp = new BorderPane(playableGp);
        Label timerLabel = newLabelRightSize("0");
        this.penaltyLabel = newLabelRightSize("Recheck penalty: ");
        this.foundNumbers = newLabelRightSize("Found: ");
        bp.setBottom(this.foundNumbers);

        BorderPane timerBp = new BorderPane();
        timerBp.setRight(timerLabel);
        timerBp.setLeft(penaltyLabel);
        bp.setTop(timerBp);
        returnToMenuButton = new Button("RETURN TO MENU");
        returnToMenuButton.setOnMouseClicked((event) -> {
            try {
                primaryStage.setScene(new StartMenu(primaryStage).startingScene());
            } catch (Exception ex) {

            }
        });
        Scene gameScene = new Scene(bp);

        gameScene.setOnKeyPressed((event) -> {
            if (event.getCode().equals(KeyCode.S)) {
                this.spaceDown = true;
                bp.setCenter(this.blankGp);
            }
        });
        gameScene.setOnKeyReleased((event) -> {
            if (event.getCode().equals(KeyCode.S)) {
                this.spaceDown = false;
                bp.setCenter(this.playableGp);
            }
        });

        new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 100000000) {
                    if (gameBoard.foundAllPairs()) {
                        double finalTime = (timer / 10.0);
                        double score = finalTime + gameBoard.getCardCheckedPenalty();
                        VBox scoreVbox = new VBox();
                        Label scoreLabel = newLabelRightSize("Score: " + score);
                        scoreVbox.getChildren().add(scoreLabel);
                        scoreVbox.getChildren().add(returnToMenuButton);
                        bp.setCenter(scoreVbox);
                        try {
                            new DataLogic().addScore(dimension, new Score(nickname, score));
                        } catch (Exception ex) {

                        }

                        stop();
                    }
                    timerLabel.setText("Base: " + (timer / 10.0));
                    if (spaceDown) {
                        timer += 3;
                    } else {
                        timer += 1;
                    }
                    lastUpdate = now;
                }
            }
        }.start();
        return gameScene;
    }

    private GridPane nakedGp() {
        GridPane gp2 = new GridPane();
        for (int y = 0; y < this.dimension; y++) {
            for (int x = 0; x < this.dimension; x++) {
                Button button = newBlankCardButton();
                button.setText(this.gameBoard.getCardNameFromCard2DArray(x, y));
                gp2.add(button, y, x);
            }
        }
        return gp2;
    }

    private GridPane playableGp() {
        GridPane gp = new GridPane();
        for (int y = 0; y < this.dimension; y++) {
            for (int x = 0; x < this.dimension; x++) {
                Button cardButton = newBlankCardButton();
                cardButton.setText("U");
                Integer xx = x;
                Integer yy = y;
                cardButton.setOnMouseClicked((event) -> {
                    if (this.gameBoard.matchingCardInDifferentCoordinate(xx, yy)) {
                        this.firstMemoryButton.setText("F"); //Found
                        cardButton.setText("F");
                        this.foundNumbers.setText(gameBoard.foundPairsString());
                        this.firstMemoryButton.setOnMouseClicked(null);
                        cardButton.setOnMouseClicked(null);

                    } else {

                        penaltyLabel.setText("Recheck penalty: " + gameBoard.getCardCheckedPenalty());

                        if (!this.firstMemoryButton.getText().equals("F")) {
                            this.firstMemoryButton.setText("T");
                        }

                        cardButton.setText(gameBoard.getCardNameFromCard2DArray(xx, yy));
                        this.firstMemoryButton = cardButton;
                    }
                });
                gp.add(cardButton, y, x);

            }
        }
        return gp;
    }

    private Label newLabelRightSize(String text) {
        Label rightSizeLabel = new Label(text);
        rightSizeLabel.setFont(Font.font(20));
        return rightSizeLabel;
    }

    private Button newBlankCardButton() {
        Button cardButton = new Button();
        cardButton.setFont(Font.font(40));
        cardButton.setMinSize(160, 90);
        cardButton.setMaxSize(160, 90);
        return cardButton;
    }

}
