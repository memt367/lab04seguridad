package domain.obstacles;

import domain.GameMap;
import domain.Obstaculos;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;

/**
 * Baldosa caliente que derrite bloques de hielo inmediatamente.
 * Los bloques de hielo creados sobre ella se derriten al instante.
 * No afecta a jugadores, enemigos ni frutas directamente.
 */
public class BaldosaCaliente extends Obstaculos {

    private Image sprite;

    public BaldosaCaliente(int tileX, int tileY, GameMap mapa) {
        super(tileX, tileY, mapa);
        this.sprite = new ImageIcon("src/imagenes/baldosa_caliente.png").getImage();
        this.destructible = false; // no se puede destruir
    }

    @Override
    public void draw(Graphics g) {
        g.drawImage(sprite, x, y, SIZE, SIZE, null);
    }

    @Override
    public boolean isBlocking() {
        return false; // no bloquea el paso
    }

    /**
     * Verifica si derrite bloques de hielo colocados sobre ella
     */
    public boolean derriteHielo() {
        return true;
    }
}
