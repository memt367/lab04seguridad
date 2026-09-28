package domain.enemies;

import domain.Enemigos;
import domain.GameMap;
import java.awt.Graphics;
import javax.swing.ImageIcon;


public class CalamarNaranja extends Enemigos {

    private java.awt.Image sprite;

    public CalamarNaranja(int x, int y, GameMap mapa) {
        super(x, y, mapa);
        this.speed = 2; 
        this.sprite = new ImageIcon("src/imagenes/calamar_naranja.PNG").getImage();
    }

    @Override
    public boolean canBreakBlocks() {
        return true;
    }

    @Override
    public void move(int playerX, int playerY) {

        int myTileX = x / GameMap.TILE_SIZE;
        int myTileY = y / GameMap.TILE_SIZE;

        int pTileX = playerX / GameMap.TILE_SIZE;
        int pTileY = playerY / GameMap.TILE_SIZE;

        int dx = Integer.compare(pTileX, myTileX);
        int dy = Integer.compare(pTileY, myTileY);

        int distX = Math.abs(pTileX - myTileX);
        int distY = Math.abs(pTileY - myTileY);
        int totalDist = distX + distY;


        int step = (totalDist >= 8) ? 2 : 1;

        int[][] directions = {
                {dx, dy},
                {dx, 0},
                {0, dy},
                {-dx, dy},
                {dx, -dy}
        };

        for (int[] d : directions) {

            int nx = myTileX + d[0] * step;
            int ny = myTileY + d[1] * step;

            if (canMoveOrBreak(nx, ny)) {
                x = nx * GameMap.TILE_SIZE;
                y = ny * GameMap.TILE_SIZE;
                return;
            }
        }

        int[][] fallback = {{1,0},{-1,0},{0,1},{0,-1}};
        for (int[] d : fallback) {
            int nx = myTileX + d[0];
            int ny = myTileY + d[1];
            if (canMoveOrBreak(nx, ny)) {
                x = nx * GameMap.TILE_SIZE;
                y = ny * GameMap.TILE_SIZE;
                return;
            }
        }
    }

    private boolean canMoveOrBreak(int tx, int ty) {
        if (tx < 0 || ty < 0 || tx >= GameMap.WIDTH || ty >= GameMap.HEIGHT) return false;

        if (mapa.getTile(tx, ty) == 1) {
            mapa.setTile(tx, ty, 0);  
            return true;
        }
        return true;
    }

    @Override
    public void draw(Graphics g) {
        g.drawImage(sprite, x, y, domain.IceCream.SIZE, domain.IceCream.SIZE, null);
    }
}
