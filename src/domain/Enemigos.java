package domain;

import java.awt.Graphics;


public abstract class Enemigos {

	protected int x;
	protected int y;
	protected int speed = 2;
	protected GameMap mapa;

	public Enemigos(int x, int y, GameMap mapa) {
		this.x = x;
		this.y = y;
		this.mapa = mapa;
	}

	public int getX() { return x; }
	public int getY() { return y; }

	public abstract void move(int playerX, int playerY);

	public abstract void draw(Graphics g);

	public boolean canBreakBlocks() { return false; }

	protected void tryMoveTo(int nx, int ny) {
		int size = IceCream.SIZE;

		int x1 = nx;
		int y1 = ny;
		int x2 = nx + size - 1;
		int y2 = ny;
		int x3 = nx;
		int y3 = ny + size - 1;
		int x4 = nx + size - 1;
		int y4 = ny + size - 1;

		int t1x = x1 / GameMap.TILE_SIZE, t1y = y1 / GameMap.TILE_SIZE;
		int t2x = x2 / GameMap.TILE_SIZE, t2y = y2 / GameMap.TILE_SIZE;
		int t3x = x3 / GameMap.TILE_SIZE, t3y = y3 / GameMap.TILE_SIZE;
		int t4x = x4 / GameMap.TILE_SIZE, t4y = y4 / GameMap.TILE_SIZE;

		if (t1x < 0 || t2x < 0 || t3x < 0 || t4x < 0) return;
		if (t1y < 0 || t2y < 0 || t3y < 0 || t4y < 0) return;
		if (t1x >= GameMap.WIDTH || t2x >= GameMap.WIDTH || t3x >= GameMap.WIDTH || t4x >= GameMap.WIDTH) return;
		if (t1y >= GameMap.HEIGHT || t2y >= GameMap.HEIGHT || t3y >= GameMap.HEIGHT || t4y >= GameMap.HEIGHT) return;

		boolean blocked =
			mapa.getTile(t1x, t1y) == 1 ||
			mapa.getTile(t2x, t2y) == 1 ||
			mapa.getTile(t3x, t3y) == 1 ||
			mapa.getTile(t4x, t4y) == 1;

		boolean fruitThere =
			mapa.getFruit(t1x, t1y) != 0 ||
			mapa.getFruit(t2x, t2y) != 0 ||
			mapa.getFruit(t3x, t3y) != 0 ||
			mapa.getFruit(t4x, t4y) != 0;
		if (fruitThere) {
			return;
		}

		if (blocked) {
			if (canBreakBlocks()) {
				if (mapa.getTile(t1x, t1y) == 1) mapa.setTile(t1x, t1y, 0);
				else if (mapa.getTile(t2x, t2y) == 1) mapa.setTile(t2x, t2y, 0);
				else if (mapa.getTile(t3x, t3y) == 1) mapa.setTile(t3x, t3y, 0);
				else if (mapa.getTile(t4x, t4y) == 1) mapa.setTile(t4x, t4y, 0);
			} else {
				return;
			}
		}

		this.x = nx;
		this.y = ny;
	}

	public boolean touches(int px, int py) {
		int size = IceCream.SIZE;
		return Math.abs(px - x) < size && Math.abs(py - y) < size;
	}
}
