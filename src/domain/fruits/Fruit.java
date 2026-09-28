package domain.fruits;

import java.awt.*;

public abstract class Fruit {


    protected int x, y;


    protected Image sprite;


    protected boolean collected = false;

    public static final int SIZE = 32;


    public Fruit(int tileX, int tileY) {
        this.x = tileX * SIZE;
        this.y = tileY * SIZE;
    }


    public void draw(Graphics g) {
        if (!collected) {
            g.drawImage(sprite, x, y, SIZE, SIZE, null);
        }
    }


    public boolean checkCollision(int px, int py) {
        if (collected) return false;

        Rectangle rPlayer = new Rectangle(px, py, SIZE, SIZE);
        Rectangle rFruit = new Rectangle(x, y, SIZE, SIZE);

        if (rPlayer.intersects(rFruit)) {
            collected = true;
            return true;
        }
        return false;
    }

    public boolean isCollected() {
        return collected;
    }

    public void setX(int pixelX) {
        this.x = pixelX;
    }

    public void setY(int pixelY) {
        this.y = pixelY;
    }


    public int getX() {
        return x / SIZE;
    }

    public int getY() {
        return y / SIZE;
    }


    public int getPixelX() {
        return x;
    }

    public int getPixelY() {
        return y;
    }

    public abstract int getPoints();
}
