package domain.enemies;

import domain.Enemigos;
import domain.GameMap;
import java.awt.Graphics;
import java.util.Random;
import javax.swing.ImageIcon;


public class Troll extends Enemigos {

    private int dirX = 1;
    private int dirY = 0;
    private final Random rnd = new Random();
    private java.awt.Image sprite;

    public Troll(int x, int y, GameMap mapa) {
        super(x, y, mapa);
        this.speed = 3;
        this.sprite = new ImageIcon("src/imagenes/troll.png").getImage();
    }

    @Override
    public void move(int playerX, int playerY) {

        int tileX = x / GameMap.TILE_SIZE;
        int tileY = y / GameMap.TILE_SIZE;

        int pX = playerX / GameMap.TILE_SIZE;
        int pY = playerY / GameMap.TILE_SIZE;

        int bestDX = Integer.compare(pX, tileX);
        int bestDY = Integer.compare(pY, tileY);

        int[][] dirs = {
                {bestDX, 0},
                {0, bestDY},
                {bestDX, bestDY},
                {1, 0}, {-1, 0}, {0, 1}, {0, -1}
        };


        for (int i = 0; i < dirs.length - 1; i++) {
            int j = rnd.nextInt(dirs.length);
            int[] t = dirs[i];
            dirs[i] = dirs[j];
            dirs[j] = t;
        }

        for (int[] d : dirs) {
            int dx = d[0];
            int dy = d[1];

            int nx = tileX + dx;
            int ny = tileY + dy;

            if (isFree(nx, ny)) {
                x = nx * GameMap.TILE_SIZE;
                y = ny * GameMap.TILE_SIZE;
                return;
            }
        }


        int[][] panicDirs = {{1,0},{-1,0},{0,1},{0,-1}};
        int[] pd = panicDirs[rnd.nextInt(4)];
        int nx = tileX + pd[0];
        int ny = tileY + pd[1];

        if (isFree(nx, ny)) {
            x = nx * GameMap.TILE_SIZE;
            y = ny * GameMap.TILE_SIZE;
        }
    }

    private boolean isFree(int tx, int ty) {
        if (tx < 0 || ty < 0 || tx >= GameMap.WIDTH || ty >= GameMap.HEIGHT) return false;
        if (mapa.getTile(tx, ty) == 1) return false;
        if (mapa.getFruit(tx, ty) != 0) return false;
        return true;
    }

    @Override
    public void draw(Graphics g) {
        g.drawImage(sprite, x, y, domain.IceCream.SIZE, domain.IceCream.SIZE, null);
    }
}
