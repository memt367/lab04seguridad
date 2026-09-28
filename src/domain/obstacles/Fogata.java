package domain.obstacles;

import domain.GameMap;
import domain.Obstaculos;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;

/**
 * Fogata que elimina a los helados al contacto.
 * Los enemigos no sufren daño.
 * Puede apagarse temporalmente con un bloque de hielo (10 segundos).
 */
public class Fogata extends Obstaculos {

    private Image sprite;
    private boolean encendida;
    private long tiempoApagado;
    private static final long TIEMPO_REENCENDIDO = 10000; // 10 segundos en milisegundos

    public Fogata(int tileX, int tileY, GameMap mapa) {
        super(tileX, tileY, mapa);
        this.sprite = new ImageIcon("src/imagenes/fogata.png").getImage();
        this.encendida = true;
        this.tiempoApagado = 0;
        this.destructible = false; // no se puede destruir permanentemente
    }

    @Override
    public void draw(Graphics g) {
        g.drawImage(sprite, x, y, SIZE, SIZE, null);
        // Si está apagada, dibujar un overlay oscuro
        if (!encendida) {
            g.setColor(new java.awt.Color(0, 0, 0, 100));
            g.fillRect(x, y, SIZE, SIZE);
        }
    }

    @Override
    public void update() {
        // Verificar si debe reencenderse
        if (!encendida && System.currentTimeMillis() - tiempoApagado >= TIEMPO_REENCENDIDO) {
            encendida = true;
        }
    }

    @Override
    public boolean isBlocking() {
        return false; // no bloquea el paso físicamente
    }

    /**
     * Verifica si la fogata está encendida y puede dañar al helado
     */
    public boolean isEncendida() {
        return encendida;
    }

    /**
     * Apaga la fogata temporalmente
     */
    public void apagar() {
        if (encendida) {
            encendida = false;
            tiempoApagado = System.currentTimeMillis();
        }
    }

    /**
     * Verifica si puede dañar a un helado (solo si está encendida)
     */
    public boolean puedeEliminarHelado() {
        return encendida;
    }
}
