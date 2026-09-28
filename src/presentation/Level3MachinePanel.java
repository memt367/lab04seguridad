package presentation;

import domain.Enemigos;
import domain.GameMap;
import domain.IceCream;
import domain.Obstaculos;
import domain.enemies.Narval;
import domain.fruits.Cherry;
import domain.fruits.Fruit;
import domain.fruits.Pineapple;
import domain.obstacles.BaldosaCaliente;
import domain.obstacles.BloqueHielo;
import domain.obstacles.Fogata;
import java.awt.*;
import java.util.*;
import javax.swing.*;

public class Level3MachinePanel extends JPanel {

    private GameMap mapa;
    private IceCream helado;
    private Image iceBlock;
    private Image wallBlock;
    private ArrayList<Fruit> frutas;
    private ArrayList<Enemigos> enemigos;
    private ArrayList<Obstaculos> obstaculos;

    private javax.swing.Timer enemyTimer;
    private javax.swing.Timer fruitTimer;
    private javax.swing.Timer gameTimer;
    private javax.swing.Timer botTimer;

    private int score = 0;
    private int pineappleCount = 0;
    private int cherryCount = 0;
    private int points = 0;
    private String saborInicial;
    private IceCreamGUI parent;
    private int timeRemaining = 120;

    private int lastDx = 0;
    private int lastDy = -1;

    public Level3MachinePanel(IceCreamGUI parent, String sabor) {
        this.parent = parent;
        this.saborInicial = sabor;

        frutas = new ArrayList<>();
        enemigos = new ArrayList<>();
        obstaculos = new ArrayList<>();
        this.mapa = new GameMap();
        generarMapaLevel3();

        this.iceBlock = new ImageIcon("src/imagenes/ice_block.png").getImage();
        this.wallBlock = new ImageIcon("src/imagenes/muro.png").getImage();
        this.helado = new IceCream(sabor, mapa);

        mapa.clearFruits();
        placeRandomFruits();
        generarObstaculosAleatorios();
        Random rnd = new Random();
        int eX = (1 + rnd.nextInt(GameMap.WIDTH - 2)) * GameMap.TILE_SIZE;
        int eY = (1 + rnd.nextInt(GameMap.HEIGHT - 2)) * GameMap.TILE_SIZE;
        enemigos.add(new Narval(eX, eY, mapa));

        setFocusable(true);

        enemyTimer = new javax.swing.Timer(700, e -> {
            for (Enemigos ene : enemigos) {
                ene.move(helado.getX(), helado.getY());
                if (ene.touches(helado.getX(), helado.getY())) {
                    stopAllTimers();
                    JOptionPane.showMessageDialog(this, "Has sido atrapado por el calamar", "Derrota", JOptionPane.ERROR_MESSAGE);
                    parent.showPanel(new Level3MachinePanel(parent, saborInicial));
                    return;
                }
            }
            
            // Actualizar obstáculos (reencender fogatas)
            for (Obstaculos obs : obstaculos) {
                obs.update();
            }
            
            repaint();
        });
        enemyTimer.start();

        fruitTimer = new javax.swing.Timer(600, e -> {
            movePineapples();
            teleportCherries();
            repaint();
        });
        fruitTimer.start();

        gameTimer = new javax.swing.Timer(1000, e -> {
            timeRemaining--;
            if (timeRemaining <= 0) {
                stopAllTimers();
                JOptionPane.showMessageDialog(this, "¡Tiempo agotado! Has perdido.", "Tiempo Agotado", JOptionPane.ERROR_MESSAGE);
                parent.showPanel(new Level3MachinePanel(parent, saborInicial));
            }
            repaint();
        });
        gameTimer.start();

        botTimer = new javax.swing.Timer(180, e -> runBotStep());
        // Progressive slowdown for the bot due to Narval: increase delay up to 600ms
        new javax.swing.Timer(15000, e -> {
            int newDelay = Math.min(botTimer.getDelay() + 60, 600);
            botTimer.setDelay(newDelay);
        }).start();
        botTimer.start();
    }

    private void stopAllTimers() {
        if (enemyTimer != null) enemyTimer.stop();
        if (fruitTimer != null) fruitTimer.stop();
        if (gameTimer != null) gameTimer.stop();
        if (botTimer != null) botTimer.stop();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        pintarMapa(g);
        for (Obstaculos obs : obstaculos) obs.draw(g);
        for (Fruit f : frutas) f.draw(g);
        for (Enemigos ene : enemigos) ene.draw(g);
        helado.dibujar(g);
        drawScorePanel(g);
        drawTimerPanel(g);
        drawPointsPanel(g);
    }

    private void drawScorePanel(Graphics g) {
        int panelX = 480;
        int panelY = 50;
        int panelWidth = 280;
        int panelHeight = 200;

        g.setColor(new Color(50, 50, 70, 200));
        g.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 15, 15);
        g.setColor(Color.WHITE);
        g.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 15, 15);

        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.setColor(new Color(255, 215, 0));
        g.drawString("SCORE L3 (BOT)", panelX + 40, panelY + 35);

        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.setColor(Color.WHITE);
        g.drawString("Piñas:", panelX + 20, panelY + 80);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.setColor(new Color(255, 255, 150));
        g.drawString(pineappleCount + " / 8", panelX + 140, panelY + 82);

        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.setColor(Color.WHITE);
        g.drawString("Cerezas:", panelX + 20, panelY + 130);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.setColor(new Color(255, 120, 120));
        g.drawString(cherryCount + " / 8", panelX + 160, panelY + 132);

        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.setColor(new Color(200, 200, 200));
        g.drawString("Total: " + (pineappleCount + cherryCount) + " / 16", panelX + 85, panelY + 175);

        drawTimerPanel(g);
    }

    private void drawTimerPanel(Graphics g) {
        int panelX = 480;
        int panelY = 270;
        int panelWidth = 280;
        int panelHeight = 120;

        g.setColor(new Color(50, 50, 70, 200));
        g.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 15, 15);
        g.setColor(Color.WHITE);
        g.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 15, 15);

        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.setColor(new Color(255, 215, 0));
        g.drawString("TIEMPO", panelX + 90, panelY + 35);

        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.setColor(new Color(120, 255, 120));
        int min = timeRemaining / 60;
        int sec = timeRemaining % 60;
        String time = String.format("%02d:%02d", min, sec);
        g.drawString(time, panelX + 85, panelY + 90);
    }

    private void drawPointsPanel(Graphics g) {
        int panelX = 480, panelY = 400;
        int w = 280, h = 110;
        g.setColor(new Color(50, 50, 70, 200));
        g.fillRoundRect(panelX, panelY, w, h, 15, 15);
        g.setColor(Color.WHITE);
        g.drawRoundRect(panelX, panelY, w, h, 15, 15);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.setColor(new Color(255, 215, 0));
        g.drawString("PUNTOS (BOT)", panelX + 60, panelY + 32);
        int maxPoints = 8 * 200 + 8 * 150; // 8 piñas + 8 cerezas
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.setColor(new Color(120, 255, 120));
        g.drawString(points + " / " + maxPoints, panelX + 70, panelY + 78);
    }

    private void pintarMapa(Graphics g) {
        g.setColor(new Color(230, 245, 255));
        g.fillRect(0, 0, 480, 480);

        for (int y = 0; y < GameMap.HEIGHT; y++) {
            for (int x = 0; x < GameMap.WIDTH; x++) {
                if (mapa.getTile(x, y) == 1) {
                    g.drawImage(iceBlock, x * GameMap.TILE_SIZE, y * GameMap.TILE_SIZE, GameMap.TILE_SIZE, GameMap.TILE_SIZE, null);
                }
            }
        }

        for (int x = 0; x < GameMap.WIDTH; x++) {
            g.drawImage(wallBlock, x * GameMap.TILE_SIZE, 0, GameMap.TILE_SIZE, GameMap.TILE_SIZE, null);
            g.drawImage(wallBlock, x * GameMap.TILE_SIZE, (GameMap.HEIGHT - 1) * GameMap.TILE_SIZE, GameMap.TILE_SIZE, GameMap.TILE_SIZE, null);
        }
        for (int y = 0; y < GameMap.HEIGHT; y++) {
            g.drawImage(wallBlock, 0, y * GameMap.TILE_SIZE, GameMap.TILE_SIZE, GameMap.TILE_SIZE, null);
            g.drawImage(wallBlock, (GameMap.WIDTH - 1) * GameMap.TILE_SIZE, y * GameMap.TILE_SIZE, GameMap.TILE_SIZE, GameMap.TILE_SIZE, null);
        }
    }

    private void runBotStep() {
        if (frutas.isEmpty()) return;

        int hx = helado.getX() / GameMap.TILE_SIZE;
        int hy = helado.getY() / GameMap.TILE_SIZE;

        for (Enemigos ene : enemigos) {
            int ex = ene.getX() / GameMap.TILE_SIZE;
            int ey = ene.getY() / GameMap.TILE_SIZE;
            int dist = Math.abs(ex - hx) + Math.abs(ey - hy);
            if (dist <= 2) {
                int dx = 0, dy = 0;
                if (Math.abs(ex - hx) >= Math.abs(ey - hy)) {
                    dx = (ex > hx) ? 1 : -1;
                } else {
                    dy = (ey > hy) ? 1 : -1;
                }
                lastDx = dx;
                lastDy = dy;
                placeIceInLastDirection();
                repaint();
                return;
            }
        }

        java.util.List<Point> path = findPathToNearestFruit(hx, hy);
        if (path == null || path.size() < 2) {
            int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
            for (int[] d : dirs) {
                int nx = hx + d[0];
                int ny = hy + d[1];
                if (nx <= 0 || ny <= 0 || nx >= GameMap.WIDTH - 1 || ny >= GameMap.HEIGHT - 1) continue;
                if (mapa.getTile(nx, ny) == 1) {
                    lastDx = d[0];
                    lastDy = d[1];
                    breakIceInLastDirection();
                    repaint();
                    return;
                }
            }
            return;
        }

        Point next = path.get(1);
        int dxTile = next.x - hx;
        int dyTile = next.y - hy;
        lastDx = dxTile;
        lastDy = dyTile;

        // Verificar obstáculos bloqueantes
        boolean bloqueado = false;
        for (Obstaculos obs : obstaculos) {
            if (obs.isBlocking() && obs.getTileX() == next.x && obs.getTileY() == next.y) {
                bloqueado = true;
                break;
            }
        }

        if (!bloqueado) {
            helado.moverTile(dxTile, dyTile);
        }

        for (Enemigos ene : enemigos) {
            if (ene.touches(helado.getX(), helado.getY())) {
                stopAllTimers();
                    JOptionPane.showMessageDialog(this, "Has sido atrapado por el narval", "Derrota", JOptionPane.ERROR_MESSAGE);
                parent.showPanel(new Level3MachinePanel(parent, saborInicial));
                return;
            }
        }
        
        // Verificar colisión con fogatas
        for (Obstaculos obs : obstaculos) {
            if (obs instanceof Fogata) {
                Fogata fogata = (Fogata) obs;
                if (fogata.touches(helado.getX(), helado.getY()) && fogata.puedeEliminarHelado()) {
                    stopAllTimers();
                    JOptionPane.showMessageDialog(this, "¡El bot se ha quemado con una fogata!", "Derrota", JOptionPane.ERROR_MESSAGE);
                    parent.showPanel(new ChooseIceCreamPanel(parent));
                    return;
                }
            }
        }

        frutas.removeIf(f -> {
            if (f.checkCollision(helado.getX(), helado.getY())) {
                mapa.setFruit(f.getX(), f.getY(), 0);
                if (f instanceof Pineapple) {
                    pineappleCount++;
                    points += f.getPoints();
                } else if (f instanceof Cherry) {
                    cherryCount++;
                    points += f.getPoints();
                }
                score = pineappleCount + cherryCount;
                if (score >= 16) {
                    stopAllTimers();
                    Object[] opciones = {"Reiniciar Nivel 3 (BOT)", "Terminar"};
                    int opcion = JOptionPane.showOptionDialog(
                            this,
                            "¡Felicidades! El bot completó el Nivel 3\n¿Qué deseas hacer?",
                            "Nivel 3 Completado (Bot)",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.INFORMATION_MESSAGE,
                            null,
                            opciones,
                            opciones[1]
                    );
                    if (opcion == 0) parent.showPanel(new Level3MachinePanel(parent, saborInicial));
                    else parent.showPanel(new ChooseIceCreamPanel(parent));
                }
                return true;
            }
            return false;
        });

        repaint();
    }

    private java.util.List<Point> findPathToNearestFruit(int sx, int sy) {
        int w = GameMap.WIDTH;
        int h = GameMap.HEIGHT;

        boolean[][] visited = new boolean[w][h];
        Point[][] parent = new Point[w][h];
        ArrayDeque<Point> queue = new ArrayDeque<>();

        queue.add(new Point(sx, sy));
        visited[sx][sy] = true;

        while (!queue.isEmpty()) {
            Point p = queue.removeFirst();

            if (mapa.getFruit(p.x, p.y) != 0) {
                java.util.List<Point> path = new ArrayList<>();
                Point cur = p;
                while (cur != null) {
                    path.add(0, cur);
                    cur = parent[cur.x][cur.y];
                }
                return path;
            }

            int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
            for (int[] d : dirs) {
                int nx = p.x + d[0];
                int ny = p.y + d[1];
                if (nx < 0 || ny < 0 || nx >= w || ny >= h) continue;
                if (visited[nx][ny]) continue;
                if (mapa.getTile(nx, ny) == 1) continue;

                visited[nx][ny] = true;
                parent[nx][ny] = p;
                queue.addLast(new Point(nx, ny));
            }
        }
        return null;
    }

    private void placeIceInLastDirection() {
        int tileX = helado.getX() / GameMap.TILE_SIZE;
        int tileY = helado.getY() / GameMap.TILE_SIZE;

        int nx = tileX + lastDx;
        int ny = tileY + lastDy;
        Random rnd = new Random();

        while (true) {
            if (nx <= 0 || ny <= 0 || nx >= GameMap.WIDTH - 1 || ny >= GameMap.HEIGHT - 1) break;
            if (mapa.getTile(nx, ny) == 1) break;

            if (mapa.getFruit(nx, ny) != 0) {
                int fruitType = mapa.getFruit(nx, ny);
                mapa.setFruit(nx, ny, 0);
                int finalNx = nx, finalNy = ny;
                Fruit found = null;
                for (Fruit f : frutas) { if (f.getX() == finalNx && f.getY() == finalNy) { found = f; break; } }
                if (found != null) {
                    frutas.remove(found);
                    int newX, newY; int attempts = 0;
                    do {
                        newX = 1 + rnd.nextInt(GameMap.WIDTH - 2);
                        newY = 1 + rnd.nextInt(GameMap.HEIGHT - 2);
                        attempts++;
                    } while ((mapa.getTile(newX, newY) == 1 || mapa.getFruit(newX, newY) != 0 ||
                            (newX == helado.getX() / GameMap.TILE_SIZE && newY == helado.getY() / GameMap.TILE_SIZE)) && attempts < 100);
                    if (attempts < 100) {
                        mapa.setFruit(newX, newY, fruitType);
                        if (fruitType == 1) frutas.add(new Pineapple(newX, newY));
                        else frutas.add(new Cherry(newX, newY));
                    }
                }
            }

            mapa.setTile(nx, ny, 1);
            
            // Verificar si hay baldosa caliente y derretir el hielo inmediatamente
            for (Obstaculos obs : obstaculos) {
                if (obs instanceof BaldosaCaliente) {
                    BaldosaCaliente baldosa = (BaldosaCaliente) obs;
                    if (baldosa.getTileX() == nx && baldosa.getTileY() == ny) {
                        mapa.setTile(nx, ny, 0); // derretir el hielo
                        break;
                    }
                }
            }
            
            nx += lastDx; ny += lastDy;
        }
    }

    private void breakIceInLastDirection() {
        int tileX = helado.getX() / GameMap.TILE_SIZE;
        int tileY = helado.getY() / GameMap.TILE_SIZE;

        int nx = tileX + lastDx;
        int ny = tileY + lastDy;
        while (true) {
            if (nx <= 0 || ny <= 0 || nx >= GameMap.WIDTH - 1 || ny >= GameMap.HEIGHT - 1) {
                break;
            }
            if (mapa.getTile(nx, ny) == 1) {
                mapa.setTile(nx, ny, 0);
            } else {
                break;
            }
            nx += lastDx;
            ny += lastDy;
        }
    }

    private void movePineapples() {
        Random r = new Random();
        for (Fruit f : new ArrayList<>(frutas)) {
            if (!(f instanceof Pineapple)) continue;
            int tx = f.getX();
            int ty = f.getY();
            int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
            int[] d = dirs[r.nextInt(dirs.length)];
            int nx = tx + d[0], ny = ty + d[1];
            if (nx <= 0 || ny <= 0 || nx >= GameMap.WIDTH - 1 || ny >= GameMap.HEIGHT - 1) continue;
            if (mapa.getTile(nx, ny) == 1) continue;
            if (mapa.getFruit(nx, ny) != 0) continue;
            try {
                java.lang.reflect.Field fx = Fruit.class.getDeclaredField("x");
                java.lang.reflect.Field fy = Fruit.class.getDeclaredField("y");
                fx.setAccessible(true); fy.setAccessible(true);
                fx.setInt(f, nx * GameMap.TILE_SIZE);
                fy.setInt(f, ny * GameMap.TILE_SIZE);
                mapa.setFruit(tx, ty, 0);
                mapa.setFruit(nx, ny, 1);
            } catch (Exception ignored) {}
        }
    }

    private void teleportCherries() {
        Random r = new Random();
        for (Fruit f : new ArrayList<>(frutas)) {
            if (!(f instanceof Cherry)) continue;
            int newX, newY; int attempts = 0;
            do {
                newX = 1 + r.nextInt(GameMap.WIDTH - 2);
                newY = 1 + r.nextInt(GameMap.HEIGHT - 2);
                attempts++;
            } while ((mapa.getTile(newX, newY) == 1 || mapa.getFruit(newX, newY) != 0 ||
                    (newX == helado.getX() / GameMap.TILE_SIZE && newY == helado.getY() / GameMap.TILE_SIZE)) && attempts < 100);
            if (attempts < 100) {
                mapa.setFruit(f.getX(), f.getY(), 0);
                try {
                    java.lang.reflect.Field fx = Fruit.class.getDeclaredField("x");
                    java.lang.reflect.Field fy = Fruit.class.getDeclaredField("y");
                    fx.setAccessible(true); fy.setAccessible(true);
                    fx.setInt(f, newX * GameMap.TILE_SIZE);
                    fy.setInt(f, newY * GameMap.TILE_SIZE);
                } catch (Exception ignored) {}
                mapa.setFruit(newX, newY, 2);
            }
        }
    }

    private void placeRandomFruits() {
        Random r = new Random();
        HashSet<String> used = new HashSet<>();
        int pineapples = 0;
        int cherries = 0;

        while (pineapples < 8) {
            int tx = 1 + r.nextInt(GameMap.WIDTH - 2);
            int ty = 1 + r.nextInt(GameMap.HEIGHT - 2);
            if (mapa.getTile(tx, ty) == 1) continue;
            if (tx == helado.getX() / GameMap.TILE_SIZE && ty == helado.getY() / GameMap.TILE_SIZE) continue;
            String key = tx + ":" + ty;
            if (used.contains(key)) continue;
            used.add(key);
            mapa.setFruit(tx, ty, 1);
            frutas.add(new Pineapple(tx, ty));
            pineapples++;
        }

        while (cherries < 8) {
            int tx = 1 + r.nextInt(GameMap.WIDTH - 2);
            int ty = 1 + r.nextInt(GameMap.HEIGHT - 2);
            if (mapa.getTile(tx, ty) == 1) continue;
            if (tx == helado.getX() / GameMap.TILE_SIZE && ty == helado.getY() / GameMap.TILE_SIZE) continue;
            String key = tx + ":" + ty;
            if (used.contains(key)) continue;
            used.add(key);
            mapa.setFruit(tx, ty, 2);
            frutas.add(new Cherry(tx, ty));
            cherries++;
        }
    }

    private void generarMapaLevel3() {
        for (int x = 0; x < GameMap.WIDTH; x++) {
            for (int y = 0; y < GameMap.HEIGHT; y++) {
                if (x == 0 || y == 0 || x == GameMap.WIDTH - 1 || y == GameMap.HEIGHT - 1) mapa.setTile(x, y, 1);
                else mapa.setTile(x, y, 1);
            }
        }
        mapa.setTile(7, 7, 1); mapa.setTile(7, 8, 1); mapa.setTile(8, 7, 1); mapa.setTile(8, 8, 1);

        Random rnd = new Random();
        int[][] libres = {{7,11},{6,11},{8,11},{7,10},{6,10},{8,10}};
        for (int[] p : libres) mapa.setTile(p[0], p[1], 0);
        int created = 0;
        while (created < 25) {
            int rx = 2 + rnd.nextInt(GameMap.WIDTH - 4);
            int ry = 2 + rnd.nextInt(GameMap.HEIGHT - 4);
            if ((rx == 7 || rx == 8) && (ry == 7 || ry == 8)) continue;
            if (mapa.getTile(rx, ry) == 1) { mapa.setTile(rx, ry, 0); created++; }
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