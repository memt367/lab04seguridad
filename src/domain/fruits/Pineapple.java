package domain.fruits;

import javax.swing.ImageIcon;

public class Pineapple extends Fruit {

    public Pineapple(int tileX, int tileY) {
        super(tileX, tileY);
        this.sprite = new ImageIcon("src/imagenes/piña.png").getImage();
    }

    @Override
    public int getPoints() {
        return 200;
    }
}
