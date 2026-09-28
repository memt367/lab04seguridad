package domain.fruits;

import javax.swing.ImageIcon;

public class Cherry extends Fruit {

    public Cherry(int tileX, int tileY) {
        super(tileX, tileY);
        this.sprite = new ImageIcon("src/imagenes/cereza.png").getImage();
    }

    @Override
    public int getPoints() {
        return 150;
    }
}
