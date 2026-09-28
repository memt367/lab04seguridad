package domain.enemies;

import domain.Enemigos;
import domain.GameMap;
import java.awt.Graphics;
import javax.swing.ImageIcon;


public class Maceta extends Enemigos {

    private java.awt.Image sprite;

    public Maceta(int x, int y, GameMap mapa) {
        super(x, y, mapa);
        this.speed = 3;
        this.sprite = new ImageIcon("src/imagenes/Maceta.png").getImage();
    }

    @Override
    public boolean canBreakBlocks() {
        return false;
    }

    @Override
    public void move(int playerX, int playerY) {

        int myTileX = x / GameMap.TILE_SIZE;
        int myTileY = y / GameMap.TILE_SIZE;

        int pTileX = playerX / GameMap.TILE_SIZE;
        int pTileY = playerY / GameMap.TILE_SIZE;


        int distX = Math.abs(pTileX - myTileX);
        int distY = Math.abs(pTileY - myTileY);

        int dx = Integer.compare(pTileX, myTileX);
        int dy = Integer.compare(pTileY, myTileY);


        int[][] dirs;

        if (distX > distY) {
            dirs = new int[][] {
                    {dx, 0},    
                    {0, dy},     
                    {dx, dy},    
                    {-dx, dy},   
                    {dx, -dy}
            };
        } else {
            dirs = new int[][] {
                    {0, dy},
                    {dx, 0},
                    {dx, dy},
                    {-dx, dy},
                    {dx, -dy}
            };
        }


        for (int[] d : dirs) {
            int nx = myTileX + d[0];
            int ny = myTileY + d[1];

            if (canMove(nx, ny)) {
                x = nx * GameMap.TILE_SIZE;
                y = ny * GameMap.TILE_SIZE;
                return;
            }
        }

        int[][] fallbackDirs = {{1,0},{-1,0},{0,1},{0,-1}};
        for (int[] d : fallbackDirs) {
            int nx = myTileX + d[0];
            int ny = myTileY + d[1];
            if (canMove(nx, ny)) {
                x = nx * GameMap.TILE_SIZE;
                y = ny * GameMap.TILE_SIZE;
                return;
            }
        }
    }

    
    private boolean canMove(int tx, int ty) {
        if (tx < 0 || ty < 0 || tx >= GameMap.WIDTH || ty >= GameMap.HEIGHT) return false;
        if (mapa.getTile(tx, ty) == 1) return false; 
        return true;
    }

    @Override
    public void draw(Graphics g) {
        g.drawImage(sprite, x, y, domain.IceCream.SIZE, domain.IceCream.SIZE, null);
    }
}
