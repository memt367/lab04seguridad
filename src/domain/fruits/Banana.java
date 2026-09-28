package domain.fruits;

import javax.swing.*;

public class Banana extends Fruit {

    public Banana(int x, int y) {
        super(x, y);
        sprite = new ImageIcon("src/imagenes/banana.png").getImage();
    }

    @Override
    public int getPoints() {
        return 100;
    }
}
