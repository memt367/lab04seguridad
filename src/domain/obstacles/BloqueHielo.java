package domain.obstacles;

import domain.GameMap;
import domain.Obstaculos;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;

/**
 * Bloque de hielo sólido que bloquea el paso de jugadores, enemigos y frutas.
 * Puede ser parte del escenario o creado por el jugador.
 */
public class BloqueHielo extends Obstaculos {

    private Image sprite;

    public BloqueHielo(int tileX, int tileY, GameMap mapa) {
        super(tileX, tileY, mapa);
        this.sprite = new ImageIcon("src/imagenes/HIELO.png").getImage();
        this.destructible = true; // puede ser destruido
    }

    @Override
    public void draw(Graphics g) {
        g.drawImage(sprite, x, y, SIZE, SIZE, null);
    }

    @Override
    public boolean isBlocking() {
        return true; // bloquea el paso de todos
    }
}
