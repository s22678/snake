package main;

public enum Direction {
    UP("UP"), DOWN("DOWN"), LEFT("LEFT"), RIGHT("RIGHT");

    private String direction;

    Direction(String direction) {
        this.direction = direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }
}
