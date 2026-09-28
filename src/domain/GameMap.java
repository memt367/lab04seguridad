package domain;

public class GameMap {

    public static final int TILE_SIZE = 32;
    public static final int WIDTH = 15;
    public static final int HEIGHT = 15;

    private int[][] grid;
    private int[][] fruitMap;

    public GameMap() {
        grid = new int[WIDTH][HEIGHT];
        fruitMap = new int[WIDTH][HEIGHT];

        generarMapa();
        generarFrutas();
    }

    private void generarMapa() {

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                grid[x][y] = 0;
            }
        }

        
        for (int x = 0; x < WIDTH; x++) {
            grid[x][0] = 1;
            grid[x][HEIGHT - 1] = 1;
        }
        for (int y = 0; y < HEIGHT; y++) {
            grid[0][y] = 1;
            grid[WIDTH - 1][y] = 1;
        }


        grid[7][7] = 1;
        grid[7][8] = 1;
        grid[8][7] = 1;
        grid[8][8] = 1;



        int leftColX = 4;
        int rightColX = 11;


        for (int y = 3; y <= 10; y++) {
            grid[leftColX][y] = 1;
            grid[rightColX][y] = 1;
        }


        grid[leftColX][11] = 1;
        grid[leftColX + 1][11] = 1;
        grid[rightColX - 1][11] = 1;
        grid[rightColX][11] = 1;


        for (int x = leftColX + 1; x <= rightColX - 1; x++) {
            grid[x][3] = 1;
        }
    }
    private void generarFrutas() {


        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                fruitMap[x][y] = 0;
            }
        }


        int[][] frutas = {


                {5,5,2}, {6,5,1}, {7,5,1}, {8,5,1}, {9,5,2},
                {4,6,2}, {5,6,1}, {6,6,2},          {8,6,1}, {9,6,2},
                {5,7,2},                            {9,7,2},
                {6,8,1},          {8,8,2}, {9,8,1},


                {1,1,1}, {1,2,1}, {1,3,1}, {1,4,1}, {1,5,1},
                {1,6,1}, {1,7,1}, {1,8,1}, {1,9,1}, {1,10,1},
                {1,11,1}, {1,12,1}, {1,13,1},


                {2,1,2}, {3,1,2}, {4,1,2}, {5,1,2}, {6,1,2}, {7,1,2},
                {8,1,2}, {9,1,2}, {10,1,2}, {11,1,2}, {12,1,2}, {13,1,2},


                {13,2,1}, {13,3,1}, {13,4,1}, {13,5,1}, {13,6,1},
                {13,7,1}, {13,8,1}, {13,9,1}, {13,10,1}, {13,11,1},
                {13,12,1}, {13,13,1},


                {2,13,2}, {3,13,2}, {4,13,2}, {5,13,2}, {6,13,2},
                {7,13,2}, {8,13,2}, {9,13,2}, {10,13,2}, {11,13,2}, {12,13,2}
        };

        for (int[] f : frutas) {
            fruitMap[f[0]][f[1]] = f[2];
        }
    }


    public int getTile(int x, int y) {
        return grid[x][y];
    }

    public int getFruit(int x, int y) {
        return fruitMap[x][y];
    }

    public void setTile(int x, int y, int value) {
        grid[x][y] = value;
    }
    public void setFruit(int x, int y, int value) {
        fruitMap[x][y] = value;
    }
    public void clearFruits() {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                fruitMap[x][y] = 0;
            }
        }
    }
}
