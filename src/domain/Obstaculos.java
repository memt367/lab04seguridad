package domain;

import java.awt.Graphics;

/**
 * Clase abstracta para los obstáculos que aparecen en el mapa.
 */
public abstract class Obstaculos {

    protected int x; 
    protected int y; 
    protected GameMap mapa;
    protected boolean destructible; 
    
    public static final int SIZE = GameMap.TILE_SIZE; 

    public Obstaculos(int tileX, int tileY, GameMap mapa) {
        this.x = tileX * SIZE;
        this.y = tileY * SIZE;
        this.mapa = mapa;
        this.destructible = false; 
    }

    public abstract void draw(Graphics g);

    public void update() {
        
    }

    public abstract boolean isBlocking();

    public boolean isDestructible() {
        return destructible;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getTileX() {
        return x / SIZE;
    }

    public int getTileY() {
        return y / SIZE;
    }

    public boolean touches(int px, int py) {
        return Math.abs(px - x) < SIZE && Math.abs(py - y) < SIZE;
    }
}
