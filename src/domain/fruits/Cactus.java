package domain.fruits;

import java.awt.*;
import javax.swing.ImageIcon;

public class Cactus extends Fruit {

    private boolean spiky = false;
    private Image spikySprite;

    public Cactus(int tileX, int tileY) {
        super(tileX, tileY);
        this.sprite = new ImageIcon("src/imagenes/cactus.png").getImage();
        this.spikySprite = this.sprite; 
    }

    public void setSpiky(boolean spiky) {
        this.spiky = spiky;
    }

    public boolean isSpiky() {
        return spiky;
    }

    @Override
    public void draw(Graphics g) {
        if (!collected) {
            Image img = spiky ? spikySprite : sprite;
            g.drawImage(img, x, y, SIZE, SIZE, null);
            if (spiky) {
                g.setColor(new Color(0, 200, 0, 120));
                g.drawOval(x + 4, y + 4, SIZE - 8, SIZE - 8);
            }
        }
    }

    @Override
    public int getPoints() {
        return 250;
    }
}
