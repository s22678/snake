package main;

import javafx.animation.AnimationTimer;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.Rectangle;

class GameLoop extends AnimationTimer {
    Rectangle food;
    private ObservableList<Node> snake;
    Group snakeBody;
    Rectangle head;

    long lastTick = 0;
    double speed = 10;
    Node tail;

    public GameLoop(Group snakeBody, Rectangle food) {
        this.snakeBody = snakeBody;
        this.food = food;
        snake = snakeBody.getChildren();
        startNewGame();
    }

    @Override
    public void handle(long l) {
        if(l - lastTick > 1000000000L / speed) {
            lastTick = l;
            if(GameWindow.state == GameState.STOPPED) return;

            boolean toRemove = snake.size() > 1;

            try {
                tail = toRemove ? snake.remove(snake.size()-1) : snake.get(0);
            } catch(IndexOutOfBoundsException e) {
                System.out.println("INDEX OUT OF BOUNDS!");
                food.fireEvent(new CustomEvent(CustomEvent.STOP));
            }

            double tailX = tail.getTranslateX();
            double tailY = tail.getTranslateY();

            switch (GameWindow.dir) {
                case UP:
                    tail.setTranslateX(snake.get(0).getTranslateX());
                    tail.setTranslateY(snake.get(0).getTranslateY() - GameWindow.SEGMENT_SIZE);
                    break;
                case DOWN:
                    tail.setTranslateX(snake.get(0).getTranslateX());
                    tail.setTranslateY(snake.get(0).getTranslateY() + GameWindow.SEGMENT_SIZE);
                    break;
                case LEFT:
                    tail.setTranslateX(snake.get(0).getTranslateX() - GameWindow.SEGMENT_SIZE);
                    tail.setTranslateY(snake.get(0).getTranslateY());
                    break;
                case RIGHT:
                    tail.setTranslateX(snake.get(0).getTranslateX() + GameWindow.SEGMENT_SIZE);
                    tail.setTranslateY(snake.get(0).getTranslateY());
                    break;
            }

            GameWindow.moved = true;

            if(toRemove) snake.add(0, tail);

            // collision detection with snake's body
            for(Node node : snake) {
                if(node != tail && tail.getTranslateX() == node.getTranslateX() && tail.getTranslateY() == node.getTranslateY()) {
                    stopGame();
                    break;
                }
            }

            // collision detection with the wall
            if(tail.getTranslateX() < 0 || tail.getTranslateX() >= GameWindow.WIDTH || tail.getTranslateY() < 0 || tail.getTranslateY() >= GameWindow.WIDTH) {
                stopGame();
            }

            // when food is consumed by the snake, change its position
            if(tail.getTranslateX() == food.getTranslateX() && tail.getTranslateY() == food.getTranslateY()) {

                // check if new food position is already occupied
                boolean isSpaceTaken = true;

                while(isSpaceTaken) {
                    for (Node node : snake) {
                        int newFoodX = setRandomTranslate(GameWindow.WIDTH);
                        int newFoodY = setRandomTranslate(GameWindow.HEIGHT);
                        if (node.getTranslateX() != newFoodX && node.getTranslateY() != newFoodY) {
                            food.setTranslateX(newFoodX);
                            food.setTranslateY(newFoodY);
                            isSpaceTaken = false;
                            break;
                        }
                    }
                }

                Rectangle rect = new Rectangle(GameWindow.SEGMENT_SIZE, GameWindow.SEGMENT_SIZE);
                rect.setTranslateX(tailX);
                rect.setTranslateY(tailY);

                snake.add(rect);
                speed += 0.5;
                GameWindow.score++;
            }
        }
    }

    public ObservableList<Node> getSnake() {
        return snake;
    }

    public Rectangle getFood() {
        return food;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public void stopGame() {
        try {
            snake.get(0).fireEvent(new CustomEvent(CustomEvent.STOP));
        } catch (IndexOutOfBoundsException e) {
            System.out.println("that's ok");
        }
        snake.clear();
    }

    public void startNewGame() {
        head = new Rectangle(GameWindow.SEGMENT_SIZE, GameWindow.SEGMENT_SIZE);
        snake.add(head);
        GameWindow.dir = Direction.RIGHT;
        speed = 7;
    }

    public void startFromSave(String snakeSegments, String foodPos, String speed) {

        // clear the snake if it exists
        if(snake.size() != 0) {
            snake.clear();
        }
        // create a snake from coordinates from save file
        String[] snakeCoords = snakeSegments.split("\s|,\s");
        for(int i = 0; i < snakeCoords.length; i++) {
            if(i%2 == 0) {
                Rectangle segment = new Rectangle(GameWindow.SEGMENT_SIZE, GameWindow.SEGMENT_SIZE);
                segment.setTranslateX(Double.parseDouble(snakeCoords[i]));
                segment.setTranslateY(Double.parseDouble(snakeCoords[i+1]));
                snake.add(segment);
            }
        }

        // set food position
        String[] foodCoord = foodPos.split(" ");
        food.setTranslateX(Double.parseDouble(foodCoord[0]));
        food.setTranslateY(Double.parseDouble(foodCoord[1]));

        // set spped
        setSpeed(Double.parseDouble(speed));
    }

    public static int setRandomTranslate(int value) {
        return ((int)(Math.random() * (value - 80)) / GameWindow.SEGMENT_SIZE * GameWindow.SEGMENT_SIZE);
    }
}
