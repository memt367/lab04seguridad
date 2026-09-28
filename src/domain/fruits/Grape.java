package domain.fruits;

import javax.swing.*;

public class Grape extends Fruit {

    public Grape(int x, int y) {
        super(x, y);
        sprite = new ImageIcon("src/imagenes/grape.png").getImage();
    }

    @Override
    public int getPoints() {
        return 50;
    }
}
