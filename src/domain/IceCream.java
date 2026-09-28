package domain;

import java.awt.*;
import javax.swing.*;

public class   IceCream {

    private int x;
    private int y;
    private Image sprite;
    private String sabor;
    private GameMap mapa;

    public static final int SIZE = 32;

    public IceCream(String sabor, GameMap mapa) {
        this.mapa = mapa;
        this.sabor = sabor;
        cargarSprite(sabor);
        this.x = 32 * 7;
        this.y = 32 * 11;
    }


    private void cargarSprite(String sabor) {
        switch (sabor.toLowerCase()) {
            case "chocolate":
                sprite = new ImageIcon("src/imagenes/icecream_chocolate.png").getImage();
                break;
            case "fresa":
                sprite = new ImageIcon("src/imagenes/icecream_fresa.png").getImage();
                break;
            default:
                sprite = new ImageIcon("src/imagenes/icecream_vainilla.png").getImage();
                break;
        }
    }

    public void mover(int dx, int dy) {
        if (puedeMover(dx, dy)) {
            x += dx;
            y += dy;
        }
    }

    public void moverTile(int dxTile, int dyTile) {
        int tileX = x / GameMap.TILE_SIZE;
        int tileY = y / GameMap.TILE_SIZE;
        int nxTile = tileX + dxTile;
        int nyTile = tileY + dyTile;
        if (nxTile < 0 || nyTile < 0 || nxTile >= GameMap.WIDTH || nyTile >= GameMap.HEIGHT) return;
        if (mapa.getTile(nxTile, nyTile) == 1) return;
        x = nxTile * GameMap.TILE_SIZE;
        y = nyTile * GameMap.TILE_SIZE;
    }

    public void dibujar(Graphics g) {
        g.drawImage(sprite, x, y, SIZE, SIZE, null);
    }

    public int getX() { return x; }
    public int getY() { return y; }

    private boolean puedeMover(int dx, int dy) {

        int newX = x + dx;
        int newY = y + dy;

        int tileX = newX / GameMap.TILE_SIZE;
        int tileY = newY / GameMap.TILE_SIZE;


        if (tileX < 0 || tileY < 0 || tileX >= GameMap.WIDTH || tileY >= GameMap.HEIGHT) {
            return false;
        }

        if (mapa.getTile(tileX, tileY) == 1) {
            return false;
        }

        return true;
    }
}
