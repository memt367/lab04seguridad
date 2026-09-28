package presentation;

import domain.Enemigos;
import domain.GameMap;
import domain.IceCream;
import domain.Obstaculos;
import domain.enemies.Maceta;
import domain.fruits.Banana;
import domain.fruits.Fruit;
import domain.fruits.Pineapple;
import domain.obstacles.BloqueHielo;
import domain.obstacles.Fogata;
import domain.obstacles.BaldosaCaliente;
import java.awt.*;
import java.util.*;
import javax.swing.*;

public class Level2MachinePanel extends JPanel {

    private GameMap mapa;
    private IceCream helado;
    private Image iceBlock;
    private Image wallBlock;

    private ArrayList<Fruit> frutas;
    private ArrayList<Enemigos> enemigos;
    private ArrayList<Obstaculos> obstaculos;

    private javax.swing.Timer aiTimer;
    private javax.swing.Timer enemyTimer;
    private javax.swing.Timer fruitTimer;
    private javax.swing.Timer gameTimer;
    private javax.swing.Timer cactusTimer;

    private String saborInicial;
    private IceCreamGUI parent;
    private int timeRemaining = 120;


    private Point lastTarget = null;


    private int score = 0;
    private int bananaCount = 0;
    private int pineappleCount = 0;
    private int cactusCount = 0;
    private int points = 0;

    public Level2MachinePanel(IceCreamGUI parent, String sabor) {
        this.parent = parent;
        this.saborInicial = sabor;

        mapa = new GameMap();
        frutas = new ArrayList<>();
        enemigos = new ArrayList<>();
        obstaculos = new ArrayList<>();

        helado = new IceCream(sabor, mapa);

        iceBlock = new ImageIcon("src/imagenes/ice_block.png").getImage();
        wallBlock = new ImageIcon("src/imagenes/muro.png").getImage();

        generarMapaLevel2();
        mapa.clearFruits();
        placeRandomFruits();
        generarObstaculosAleatorios();

        colocarEnemigos();


        aiTimer = new javax.swing.Timer(180, e -> runBotStep());
        enemyTimer = new javax.swing.Timer(600, e -> updateEnemies());
        fruitTimer = new javax.swing.Timer(500, e -> movePineapples());
        gameTimer = new javax.swing.Timer(1000, e -> tickTime());

        aiTimer.start();
        enemyTimer.start();
        fruitTimer.start();
        gameTimer.start();

        cactusTimer = new javax.swing.Timer(30000, e -> {
            for (Fruit f : frutas) {
                if (f instanceof Cactus) {
                    Cactus c = (Cactus) f;
                    c.setSpiky(!c.isSpiky());
                }
            }
            repaint();
        });
        cactusTimer.start();

        setFocusable(true);
    }


    private void runBotStep() {
        if (frutas.isEmpty()) return;

        int hx = helado.getX() / GameMap.TILE_SIZE;
        int hy = helado.getY() / GameMap.TILE_SIZE;

        Fruit target = getClosestFruitDynamic();

        if (target == null) {
            attemptUnblock(hx, hy);
            return;
        }

        java.util.List<Point> path = bfs(hx, hy, target);


        if (path == null || path.size() < 2) {
            attemptUnblock(hx, hy);
            return;
        }


        Point next = path.get(1);
        int dx = next.x - hx;
        int dy = next.y - hy;

        // Verificar obstáculos bloqueantes
        boolean bloqueado = false;
        for (Obstaculos obs : obstaculos) {
            if (obs.isBlocking() && obs.getTileX() == next.x && obs.getTileY() == next.y) {
                bloqueado = true;
                break;
            }
        }

        if (!bloqueado) {
            helado.moverTile(dx, dy);
        }
        
        // Verificar colisión con fogatas
        for (Obstaculos obs : obstaculos) {
            if (obs instanceof Fogata) {
                Fogata fogata = (Fogata) obs;
                if (fogata.touches(helado.getX(), helado.getY()) && fogata.puedeEliminarHelado()) {
                    lose("¡El bot se ha quemado con una fogata!");
                    return;
                }
            }
        }
        
        checkFruitCollision();
        repaint();
    }

    private void attemptUnblock(int hx, int hy) {
        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};

        for (int[] d : dirs) {
            int nx = hx + d[0];
            int ny = hy + d[1];

            if (nx <= 0 || ny <= 0 || nx >= GameMap.WIDTH - 1 || ny >= GameMap.HEIGHT - 1)
                continue;

            if (mapa.getTile(nx, ny) == 1) {
                mapa.setTile(nx, ny, 0);
                repaint();
                return;
            }
        }
    }


    private java.util.List<Point> bfs(int sx, int sy, Fruit target) {

        int tx = target.getX();
        int ty = target.getY();

        boolean[][] visited = new boolean[GameMap.WIDTH][GameMap.HEIGHT];
        Point[][] parent = new Point[GameMap.WIDTH][GameMap.HEIGHT];

        ArrayDeque<Point> queue = new ArrayDeque<>();
        queue.add(new Point(sx, sy));
        visited[sx][sy] = true;

        while (!queue.isEmpty()) {
            Point p = queue.removeFirst();


            if (p.x == tx && p.y == ty)
                return reconstruct(parent, p);

            int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
            for (int[] d : dirs) {
                int nx = p.x + d[0];
                int ny = p.y + d[1];

                if (nx < 0 || ny < 0 || nx >= GameMap.WIDTH || ny >= GameMap.HEIGHT) continue;
                if (visited[nx][ny]) continue;
                if (mapa.getTile(nx, ny) == 1) continue;

                visited[nx][ny] = true;
                parent[nx][ny] = p;
                queue.add(new Point(nx, ny));
            }
        }
        return null;
    }

    private java.util.List<Point> reconstruct(Point[][] parent, Point end) {
        ArrayList<Point> path = new ArrayList<>();
        Point cur = end;
        while (cur != null) {
            path.add(0, cur);
            cur = parent[cur.x][cur.y];
        }
        return path;
    }


    private Fruit getClosestFruitDynamic() {
        Fruit best = null;
        int hx = helado.getX() / GameMap.TILE_SIZE;
        int hy = helado.getY() / GameMap.TILE_SIZE;

        double bestDist = 9999;

        for (Fruit f : frutas) {
            int fx = f.getX();
            int fy = f.getY();

            double d = Math.abs(fx - hx) + Math.abs(fy - hy);

            if (d < bestDist) {
                bestDist = d;
                best = f;
            }
        }
        return best;
    }


    private void movePineapples() {
        Random r = new Random();

        for (Fruit f : frutas) {
            if (!(f instanceof Pineapple)) continue;

            int fx = f.getX();
            int fy = f.getY();

            int[] dx = {1,-1,0,0};
            int[] dy = {0,0,1,-1};

            int dir = r.nextInt(4);

            int nx = fx + dx[dir];
            int ny = fy + dy[dir];

            if (nx <= 0 || ny <= 0 || nx >= GameMap.WIDTH - 1 || ny >= GameMap.HEIGHT - 1)
                continue;

            if (mapa.getTile(nx, ny) == 1)
                continue;

            mapa.setFruit(fx, fy, 0);
            mapa.setFruit(nx, ny, 2);

            f.setX(nx * Fruit.SIZE);
            f.setY(ny * Fruit.SIZE);
        }
    }

    private void updateEnemies() {
        for (Enemigos ene : enemigos) {
            ene.move(helado.getX(), helado.getY());
            if (ene.touches(helado.getX(), helado.getY())) {
                stopAllTimers();
                JOptionPane.showMessageDialog(this, "Has sido atrapado por la maceta", "Derrota", JOptionPane.ERROR_MESSAGE);
                parent.showPanel(new Level2MachinePanel(parent, saborInicial));
                return;
            }
        }
        
        // Actualizar obstáculos (reencender fogatas)
        for (Obstaculos obs : obstaculos) {
            obs.update();
        }
        
        repaint();
    }


    private void tickTime() {
        timeRemaining--;
        if (timeRemaining <= 0)
            lose("Tiempo agotado");

        repaint();
    }

    private void checkFruitCollision() {
        frutas.removeIf(f -> {
            if (f.checkCollision(helado.getX(), helado.getY())) {

                mapa.setFruit(f.getX(), f.getY(), 0);

                if (f instanceof Cactus) {
                    Cactus c = (Cactus) f;
                    if (c.isSpiky()) {
                        lose("Bot pinchado por cactus");
                        return true;
                    } else {
                        cactusCount++;
                        points += f.getPoints();
                    }
                } else if (f instanceof Banana) {
                    bananaCount++;
                    points += f.getPoints();
                } else if (f instanceof Pineapple) {
                    pineappleCount++;
                    points += f.getPoints();
                }

                score = bananaCount + pineappleCount + cactusCount;

                if (score >= 24) win();
                return true;
            }
            return false;
        });
    }

    private void win() {
        stopAll();
        JOptionPane.showMessageDialog(this, "¡Bot completó Nivel 2!");
        parent.showPanel(new Level3MachinePanel(parent, saborInicial));
    }

    private void lose(String msg) {
        stopAll();
        JOptionPane.showMessageDialog(this, msg);
        parent.showPanel(new ChooseIceCreamPanel(parent));
    }

    private void stopAll() {
        if (aiTimer != null) aiTimer.stop();
        if (enemyTimer != null) enemyTimer.stop();
        if (fruitTimer != null) fruitTimer.stop();
        if (gameTimer != null) gameTimer.stop();
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        pintarMapa(g);

        for (Obstaculos obs : obstaculos) obs.draw(g);
        for (Fruit f : frutas) f.draw(g);
        for (Enemigos e : enemigos) e.draw(g);

        helado.dibujar(g);
        drawScorePanel(g);
        drawTimerPanel(g);
        // points panel
        int panelX = 480, panelY = 380;
        int w = 280, h = 110;
        g.setColor(new Color(50, 50, 70, 200));
        g.fillRoundRect(panelX, panelY, w, h, 15, 15);
        g.setColor(Color.WHITE);
        g.drawRoundRect(panelX, panelY, w, h, 15, 15);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.setColor(new Color(255, 215, 0));
        g.drawString("PUNTOS (BOT)", panelX + 60, panelY + 32);
        int maxPoints = 8 * 100 + 8 * 200 + 8 * 250; // 8 plátanos + 8 piñas + 8 cactus
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.setColor(new Color(120, 255, 120));
        g.drawString(points + " / " + maxPoints, panelX + 70, panelY + 78);
    }

    private void pintarMapa(Graphics g) {
        for (int x = 0; x < GameMap.WIDTH; x++) {
            for (int y = 0; y < GameMap.HEIGHT; y++) {

                int tile = mapa.getTile(x, y);
                Image img = (tile == 1) ? iceBlock : null;

                if (x == 0 || y == 0 || x == GameMap.WIDTH - 1 || y == GameMap.HEIGHT - 1)
                    img = wallBlock;

                if (img != null) {
                    g.drawImage(img, x * GameMap.TILE_SIZE, y * GameMap.TILE_SIZE,
                            GameMap.TILE_SIZE, GameMap.TILE_SIZE, null);
                } else {
                    g.setColor(new Color(240, 250, 255));
                    g.fillRect(x * GameMap.TILE_SIZE, y * GameMap.TILE_SIZE,
                            GameMap.TILE_SIZE, GameMap.TILE_SIZE);
                }
            }
        }
    }

    private void drawScorePanel(Graphics g) {

        int panelX = 480, panelY = 50;
        int w = 280, h = 240;

        g.setColor(new Color(50, 50, 70, 200));
        g.fillRoundRect(panelX, panelY, w, h, 15, 15);
        g.setColor(Color.WHITE);
        g.drawRoundRect(panelX, panelY, w, h, 15, 15);

        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.setColor(new Color(255, 215, 0));
        g.drawString("SCORE L2 (BOT)", panelX + 40, panelY + 35);

        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.setColor(Color.WHITE);
        g.drawString("Plátanos:", panelX + 25, panelY + 90);
        g.drawString("Piñas:", panelX + 25, panelY + 140);
        g.drawString("Cactus:", panelX + 25, panelY + 190);

        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.setColor(new Color(255, 220, 100));
        g.drawString(bananaCount + " / 8", panelX + 160, panelY + 95);

        g.setColor(new Color(255, 255, 150));
        g.drawString(pineappleCount + " / 8", panelX + 160, panelY + 145);
        g.setColor(new Color(150, 255, 150));
        g.drawString(cactusCount + " / 8", panelX + 160, panelY + 195);

        drawTimerPanel(g);
    }

    private void drawTimerPanel(Graphics g) {
        int panelX = 480, panelY = 270;
        int w = 280, h = 100;

        g.setColor(new Color(50, 50, 70, 200));
        g.fillRoundRect(panelX, panelY, w, h, 15, 15);
        g.setColor(Color.WHITE);
        g.drawRoundRect(panelX, panelY, w, h, 15, 15);

        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.setColor(new Color(255, 215, 0));
        g.drawString("TIEMPO", panelX + 95, panelY + 32);

        int m = timeRemaining / 60;
        int s = timeRemaining % 60;
        String t = String.format("%02d:%02d", m, s);

        g.setFont(new Font("Arial", Font.BOLD, 36));

        if (timeRemaining > 60) g.setColor(Color.GREEN);
        else if (timeRemaining > 30) g.setColor(Color.YELLOW);
        else g.setColor(Color.RED);

        g.drawString(t, panelX + 78, panelY + 78);
    }

    private void generarMapaLevel2() {

        for (int x = 0; x < GameMap.WIDTH; x++) {
            for (int y = 0; y < GameMap.HEIGHT; y++) {


                if (x == 0 || y == 0 || x == GameMap.WIDTH - 1 || y == GameMap.HEIGHT - 1) {
                    mapa.setTile(x, y, 1);
                }
                else {

                    mapa.setTile(x, y, 1);
                }
            }
        }


        for (int x = 5; x <= 11; x++) {
            for (int y = 5; y <= 11; y++) {
                mapa.setTile(x, y, 0);
            }
        }


        for (int x = 2; x < GameMap.WIDTH - 2; x++) {
            mapa.setTile(x, 4, 0);
            mapa.setTile(x, 12, 0);
        }

        for (int y = 2; y < GameMap.HEIGHT - 2; y++) {
            mapa.setTile(4, y, 0);
            mapa.setTile(12, y, 0);
        }


        Random rnd = new Random();
        for (int i = 0; i < 40; i++) {
            int rx = 2 + rnd.nextInt(GameMap.WIDTH - 4);
            int ry = 2 + rnd.nextInt(GameMap.HEIGHT - 4);


            if (rx >= 5 && rx <= 11 && ry >= 5 && ry <= 11) continue;

            mapa.setTile(rx, ry, 0);
        }

    }
    private void enemyCaught() {
        stopAllTimers();

        int opcion = JOptionPane.showConfirmDialog(
                this,
                "¡El bot fue atrapado! ¿Quieres reiniciar el Nivel 2?",
                "Derrota",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE
        );

        if (opcion == JOptionPane.YES_OPTION) {
            parent.showPanel(new Level2MachinePanel(parent, saborInicial));
        } else {
            parent.showPanel(new ChooseIceCreamPanel(parent));
        }
    }



    private void stopAllTimers() {
        if (enemyTimer != null) enemyTimer.stop();
        if (fruitTimer != null)  fruitTimer.stop();
        if (gameTimer != null)   gameTimer.stop();

    }


    private void colocarEnemigos() {
        Random rnd = new Random();
        enemigos.add(new Maceta(2 * GameMap.TILE_SIZE, 2 * GameMap.TILE_SIZE, mapa));
        enemigos.add(new Maceta((GameMap.WIDTH-3)*GameMap.TILE_SIZE,
                (GameMap.HEIGHT-3)*GameMap.TILE_SIZE, mapa));
    }

    private void placeRandomFruits() {
        Random r = new Random();
        int bananas = 0;
        int pine = 0;
        int cacti = 0;

        while (bananas < 8) {
            int x = 1 + r.nextInt(GameMap.WIDTH - 2);
            int y = 1 + r.nextInt(GameMap.HEIGHT - 2);

            if (mapa.getTile(x,y)==1) continue;

            mapa.setFruit(x,y,1);
            frutas.add(new Banana(x,y));
            bananas++;
        }

        while (pine < 8) {
            int x = 1 + r.nextInt(GameMap.WIDTH - 2);
            int y = 1 + r.nextInt(GameMap.HEIGHT - 2);

            if (mapa.getTile(x,y)==1) continue;

            mapa.setFruit(x,y,2);
            frutas.add(new Pineapple(x,y));
            pine++;
        }

        while (cacti < 8) {
            int x = 1 + r.nextInt(GameMap.WIDTH - 2);
            int y = 1 + r.nextInt(GameMap.HEIGHT - 2);
            if (mapa.getTile(x,y)==1) continue;
            mapa.setFruit(x,y,3);
            frutas.add(new Cactus(x,y));
            cacti++;
        }
    }

    private void generarObstaculosAleatorios() {
        Random r = new Random();
        int numObstaculos = 5 + r.nextInt(3); // 5-7 obstáculos (mínimo 5)
        
        for (int i = 0; i < numObstaculos; i++) {
            int tx = 2 + r.nextInt(GameMap.WIDTH - 4);
            int ty = 2 + r.nextInt(GameMap.HEIGHT - 4);
            
            // No colocar en posición del helado ni donde hay frutas
            if (tx == helado.getX() / GameMap.TILE_SIZE && ty == helado.getY() / GameMap.TILE_SIZE) continue;
            if (mapa.getFruit(tx, ty) != 0) continue;
            
            // 50% probabilidad: reemplazar hielo, 50%: en espacio vacío
            if (r.nextBoolean() && mapa.getTile(tx, ty) == 1) {
                mapa.setTile(tx, ty, 0); // quitar hielo
            } else if (mapa.getTile(tx, ty) != 0) {
                continue; // si hay pared, saltar
            }
            
            // Generar tipo aleatorio de obstáculo (33% cada uno)
            int tipo = r.nextInt(3);
            Obstaculos obs = null;
            switch (tipo) {
                case 0:
                    obs = new BloqueHielo(tx, ty, mapa);
                    break;
                case 1:
                    obs = new Fogata(tx, ty, mapa);
                    break;
                case 2:
                    obs = new BaldosaCaliente(tx, ty, mapa);
                    break;
            }
            if (obs != null) {
                obstaculos.add(obs);
            }
        }
    }
}
