package presentation;

import domain.Enemigos;
import domain.GameMap;
import domain.IceCream;
import domain.Obstaculos;
import domain.enemies.Maceta;
import domain.fruits.Banana;
import domain.fruits.Cactus;
import domain.fruits.Fruit;
import domain.fruits.Pineapple;
import domain.obstacles.BaldosaCaliente;
import domain.obstacles.BloqueHielo;
import domain.obstacles.Fogata;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import javax.swing.*;

public class Level2Panel extends JPanel implements KeyListener {

    private GameMap mapa;
    private IceCream helado;
    private Image iceBlock;
    private Image wallBlock;
    private ArrayList<Fruit> frutas;
    private ArrayList<Enemigos> enemigos;
    private ArrayList<Obstaculos> obstaculos;
    private javax.swing.Timer enemyTimer;
    private javax.swing.Timer fruitTimer;
    private int score = 0;
    private int bananaCount = 0;
    private int pineappleCount = 0;
    private int cactusCount = 0;
    private int points = 0;
    private String saborInicial;
    private IceCreamGUI parent;
    private javax.swing.Timer gameTimer;
    private javax.swing.Timer cactusTimer;
    private int timeRemaining = 120;

    private int lastDx = 0;
    private int lastDy = -1;

    public Level2Panel(IceCreamGUI parent, String sabor) {
        this.parent = parent;
        this.saborInicial = sabor;
        frutas = new ArrayList<>();
        enemigos = new ArrayList<>();
        obstaculos = new ArrayList<>();
        this.mapa = new GameMap();
        generarMapaLevel2();

        this.iceBlock = new ImageIcon("src/imagenes/ice_block.png").getImage();
        this.wallBlock = new ImageIcon("src/imagenes/muro.png").getImage();
        this.helado = new IceCream(sabor, mapa);
        mapa.clearFruits();
        placeRandomFruits();
        generarObstaculosAleatorios();

        Random rnd = new Random();

        int heladoTileX = helado.getX() / GameMap.TILE_SIZE;
        int heladoTileY = helado.getY() / GameMap.TILE_SIZE;

        int maceta1X = 0, maceta1Y = 0;
        boolean found1 = false;
        for (int attempts = 0; attempts < 200 && !found1; attempts++) {
            int tileX = 2 + rnd.nextInt(GameMap.WIDTH - 4);
            int tileY = 2 + rnd.nextInt(GameMap.HEIGHT - 4);

            if (mapa.getTile(tileX, tileY) == 0 &&
                !(tileX >= 7 && tileX <= 8 && tileY >= 7 && tileY <= 8) &&
                (Math.abs(tileX - heladoTileX) > 5 || Math.abs(tileY - heladoTileY) > 5)) {
                maceta1X = tileX * GameMap.TILE_SIZE;
                maceta1Y = tileY * GameMap.TILE_SIZE;
                found1 = true;
            }
        }

        if (!found1) {
            maceta1X = 2 * GameMap.TILE_SIZE;
            maceta1Y = 2 * GameMap.TILE_SIZE;
        }
        enemigos.add(new Maceta(maceta1X, maceta1Y, mapa));

        int maceta2X = 0, maceta2Y = 0;
        boolean found2 = false;
        for (int attempts = 0; attempts < 200 && !found2; attempts++) {
            int tileX = 2 + rnd.nextInt(GameMap.WIDTH - 4);
            int tileY = 2 + rnd.nextInt(GameMap.HEIGHT - 4);

            if (mapa.getTile(tileX, tileY) == 0 &&
                !(tileX >= 7 && tileX <= 8 && tileY >= 7 && tileY <= 8) &&
                (Math.abs(tileX - heladoTileX) > 5 || Math.abs(tileY - heladoTileY) > 5) &&
                (Math.abs(tileX * GameMap.TILE_SIZE - maceta1X) > 128 ||
                 Math.abs(tileY * GameMap.TILE_SIZE - maceta1Y) > 128)) {
                maceta2X = tileX * GameMap.TILE_SIZE;
                maceta2Y = tileY * GameMap.TILE_SIZE;
                found2 = true;
            }
        }

        if (!found2) {
            maceta2X = (GameMap.WIDTH - 3) * GameMap.TILE_SIZE;
            maceta2Y = (GameMap.HEIGHT - 3) * GameMap.TILE_SIZE;
        }
        enemigos.add(new Maceta(maceta2X, maceta2Y, mapa));

        setFocusable(true);
        addKeyListener(this);

        enemyTimer = new javax.swing.Timer(1300, e -> {
            for (Enemigos ene : enemigos) {
                ene.move(helado.getX(), helado.getY());
                if (ene.touches(helado.getX(), helado.getY())) {
                    if (enemyTimer != null) enemyTimer.stop();
                    if (gameTimer != null) gameTimer.stop();
                    if (fruitTimer != null) fruitTimer.stop();
                    JOptionPane.showMessageDialog(this, "Has sido atrapado por la maceta", "Derrota", JOptionPane.ERROR_MESSAGE);
                    parent.showPanel(new Level2Panel(parent, saborInicial));
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

        fruitTimer = new javax.swing.Timer(500, e -> {
            movePineapples();
            repaint();
        });
        fruitTimer.start();

        gameTimer = new javax.swing.Timer(1000, e -> {
            timeRemaining--;
            if (timeRemaining <= 0) {
                gameTimer.stop();
                if (enemyTimer != null) enemyTimer.stop();
                if (fruitTimer != null) fruitTimer.stop();
                JOptionPane.showMessageDialog(this, "¡Tiempo agotado! Has perdido.", "Tiempo Agotado", JOptionPane.ERROR_MESSAGE);
                parent.showPanel(new ChooseIceCreamPanel(parent));
            }
            repaint();
        });
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
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        pintarMapa(g);

        for (Obstaculos obs : obstaculos) obs.draw(g);
        for (Fruit f : frutas) {
            f.draw(g);
        }

        for (Enemigos ene : enemigos) {
            ene.draw(g);
        }

        helado.dibujar(g);

        drawScorePanel(g);
        drawTimerPanel(g);
        drawPointsPanel(g);
    }

    private void drawScorePanel(Graphics g) {
        int panelX = 480;
        int panelY = 50;
        int panelWidth = 280;
        int panelHeight = 240;

        g.setColor(new Color(50, 50, 70, 200));
        g.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 15, 15);
        g.setColor(Color.WHITE);
        g.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 15, 15);

        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.setColor(new Color(255, 215, 0));
        g.drawString("SCORE L2", panelX + 70, panelY + 35);

        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.setColor(Color.WHITE);
        g.drawString("Plátanos:", panelX + 20, panelY + 70);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.setColor(new Color(255, 220, 100));
        g.drawString(bananaCount + " / 8", panelX + 160, panelY + 72);

        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.setColor(Color.WHITE);
        g.drawString("Piñas:", panelX + 20, panelY + 115);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.setColor(new Color(255, 255, 150));
        g.drawString(pineappleCount + " / 8", panelX + 160, panelY + 117);

        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.setColor(Color.WHITE);
        g.drawString("Cactus:", panelX + 20, panelY + 160);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.setColor(new Color(150, 255, 150));
        g.drawString(cactusCount + " / 8", panelX + 160, panelY + 162);

        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.setColor(new Color(200, 200, 200));
        g.drawString("Total: " + score + " / 24", panelX + 95, panelY + 205);

        drawTimerPanel(g);
    }

    private void drawTimerPanel(Graphics g) {
        int panelX = 480;
        int panelY = 270;
        int panelWidth = 280;
        int panelHeight = 100;

        g.setColor(new Color(50, 50, 70, 200));
        g.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 15, 15);
        g.setColor(Color.WHITE);
        g.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 15, 15);

        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.setColor(new Color(255, 215, 0));
        g.drawString("TIEMPO", panelX + 95, panelY + 30);

        int minutes = timeRemaining / 60;
        int seconds = timeRemaining % 60;
        String timeStr = String.format("%02d:%02d", minutes, seconds);

        g.setFont(new Font("Arial", Font.BOLD, 36));
        if (timeRemaining > 60) {
            g.setColor(new Color(100, 255, 100));
        } else if (timeRemaining > 30) {
            g.setColor(new Color(255, 255, 100));
        } else {
            g.setColor(new Color(255, 100, 100));
        }
        g.drawString(timeStr, panelX + 75, panelY + 72);
    }

    private void drawPointsPanel(Graphics g) {
        int panelX = 480;
        int panelY = 380;
        int panelWidth = 280;
        int panelHeight = 110;

        g.setColor(new Color(50, 50, 70, 200));
        g.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 15, 15);
        g.setColor(Color.WHITE);
        g.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 15, 15);

        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.setColor(new Color(255, 215, 0));
        g.drawString("PUNTOS", panelX + 90, panelY + 30);

        int maxPoints = 8 * 100 + 8 * 200 + 8 * 250; // 8 plátanos + 8 piñas + 8 cactus
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.setColor(new Color(120, 255, 120));
        g.drawString(points + " / " + maxPoints, panelX + 70, panelY + 75);
    }

    private void pintarMapa(Graphics g) {
        for (int x = 0; x < GameMap.WIDTH; x++) {
            for (int y = 0; y < GameMap.HEIGHT; y++) {
                int tile = mapa.getTile(x, y);
                if (tile == 1) {
                    Image img = iceBlock;
                    if (x == 0 || y == 0 || x == GameMap.WIDTH - 1 || y == GameMap.HEIGHT - 1) {
                        img = wallBlock;
                    }
                    g.drawImage(img,
                            x * GameMap.TILE_SIZE,
                            y * GameMap.TILE_SIZE,
                            GameMap.TILE_SIZE,
                            GameMap.TILE_SIZE,
                            null);
                } else {
                    g.setColor(new Color(240, 250, 255));
                    g.fillRect(x * GameMap.TILE_SIZE,
                            y * GameMap.TILE_SIZE,
                            GameMap.TILE_SIZE,
                            GameMap.TILE_SIZE);
                }
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        int dxTile = 0;
        int dyTile = 0;

        if (code == KeyEvent.VK_UP || code == KeyEvent.VK_W) dyTile = -1;
        if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S) dyTile = 1;
        if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) dxTile = -1;
        if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) dxTile = 1;

        if (dxTile != 0 || dyTile != 0) {
            lastDx = dxTile;
            lastDy = dyTile;
            
            // Verificar si hay obstáculos bloqueantes
            int tileX = helado.getX() / GameMap.TILE_SIZE;
            int tileY = helado.getY() / GameMap.TILE_SIZE;
            int nxTile = tileX + dxTile;
            int nyTile = tileY + dyTile;
            
            boolean bloqueado = false;
            for (Obstaculos obs : obstaculos) {
                if (obs.isBlocking() && obs.getTileX() == nxTile && obs.getTileY() == nyTile) {
                    bloqueado = true;
                    break;
                }
            }
            
            if (!bloqueado) {
                helado.moverTile(dxTile, dyTile);
            }
            
            // Verificar colisión con fogatas
            for (Obstaculos obs : obstaculos) {
                if (obs instanceof Fogata) {
                    Fogata fogata = (Fogata) obs;
                    if (fogata.touches(helado.getX(), helado.getY()) && fogata.puedeEliminarHelado()) {
                        if (enemyTimer != null) enemyTimer.stop();
                        if (fruitTimer != null) fruitTimer.stop();
                        if (gameTimer != null) gameTimer.stop();
                        if (cactusTimer != null) cactusTimer.stop();
                        JOptionPane.showMessageDialog(this, "¡Te has quemado con una fogata!", "Derrota", JOptionPane.ERROR_MESSAGE);
                        parent.showPanel(new ChooseIceCreamPanel(parent));
                        return;
                    }
                }
            }
        }

        for (Enemigos ene : enemigos) {
            if (ene.touches(helado.getX(), helado.getY())) {
                if (enemyTimer != null) enemyTimer.stop();
                if (fruitTimer != null) fruitTimer.stop();
                if (gameTimer != null) gameTimer.stop();
                JOptionPane.showMessageDialog(this, "Has sido atrapado por la maceta", "Derrota", JOptionPane.ERROR_MESSAGE);
                parent.showPanel(new Level2Panel(parent, saborInicial));
                return;
            }
        }

        if (code == KeyEvent.VK_SPACE) {
            placeIceInLastDirection();
        }

        if (code == KeyEvent.VK_Q) {
            breakIceInLastDirection();
        }

        frutas.removeIf(f -> {
            if (f.checkCollision(helado.getX(), helado.getY())) {
                mapa.setFruit(f.getX(), f.getY(), 0);
                if (f instanceof Cactus) {
                    Cactus c = (Cactus) f;
                    if (c.isSpiky()) {
                        if (gameTimer != null) gameTimer.stop();
                        if (enemyTimer != null) enemyTimer.stop();
                        if (fruitTimer != null) fruitTimer.stop();
                        if (cactusTimer != null) cactusTimer.stop();
                        JOptionPane.showMessageDialog(this, "¡Te pinchó un cactus!", "Derrota", JOptionPane.ERROR_MESSAGE);
                        parent.showPanel(new Level2Panel(parent, saborInicial));
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

                if (score >= 24) {
                    if (gameTimer != null) gameTimer.stop();
                    if (enemyTimer != null) enemyTimer.stop();
                    if (fruitTimer != null) fruitTimer.stop();
                    Object[] opciones = {"Reiniciar Nivel 2", "Continuar a Nivel 3", "Terminar"};
                    int opcion = JOptionPane.showOptionDialog(
                        this,
                        "¡Felicidades! Has completado el Nivel 2\n¿Qué deseas hacer?",
                        "Nivel 2 Completado",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null,
                        opciones,
                        opciones[1]
                    );
                    if (opcion == 0) {
                        parent.showPanel(new Level2Panel(parent, saborInicial));
                    } else if (opcion == 1) {
                        parent.showPanel(new Level3Panel(parent, saborInicial));
                    } else {
                        parent.showPanel(new ChooseIceCreamPanel(parent));
                    }
                }
                return true;
            }
            return false;
        });

        repaint();
    }

    private void placeIceInLastDirection() {
        int tileX = helado.getX() / GameMap.TILE_SIZE;
        int tileY = helado.getY() / GameMap.TILE_SIZE;

        int nx = tileX + lastDx;
        int ny = tileY + lastDy;

        while (true) {
            if (nx <= 0 || ny <= 0 || nx >= GameMap.WIDTH - 1 || ny >= GameMap.HEIGHT - 1) {
                break;
            }

            for (Enemigos ene : enemigos) {
                if (ene.getX() / GameMap.TILE_SIZE == nx && ene.getY() / GameMap.TILE_SIZE == ny) {
                    return;
                }
            }

            if (mapa.getTile(nx, ny) == 1) {
                break;
            }

            mapa.setTile(nx, ny, 1);

            if (mapa.getFruit(nx, ny) != 0) {
                mapa.setFruit(nx, ny, 0);
                int finalNx = nx;
                int finalNy = ny;
                frutas.removeIf(f -> f.getX() == finalNx && f.getY() == finalNy);
            }
            
            // Verificar si hay baldosa caliente y derretir el hielo inmediatamente
            boolean derretido = false;
            for (Obstaculos obs : obstaculos) {
                if (obs instanceof BaldosaCaliente) {
                    BaldosaCaliente baldosa = (BaldosaCaliente) obs;
                    if (baldosa.getTileX() == nx && baldosa.getTileY() == ny) {
                        mapa.setTile(nx, ny, 0); // derretir el hielo
                        derretido = true;
                        break;
                    }
                }
            }
            
            // Apagar fogatas bajo el hielo (solo si no se derritió)
            if (!derretido) {
                for (Obstaculos obs : obstaculos) {
                    if (obs instanceof Fogata) {
                        Fogata fogata = (Fogata) obs;
                        if (fogata.getTileX() == nx && fogata.getTileY() == ny) {
                            fogata.apagar();
                        }
                    }
                }
            }

            nx += lastDx;
            ny += lastDy;
        }

        repaint();
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
        repaint();
    }

    private void movePineapples() {
        Random r = new Random();
        for (Fruit f : frutas) {
            if (!(f instanceof Pineapple)) continue;

            int tileX = f.getX();
            int tileY = f.getY();

            int[] dx = {1, -1, 0, 0};
            int[] dy = {0, 0, 1, -1};
            int dir = r.nextInt(4);
            int nx = tileX + dx[dir];
            int ny = tileY + dy[dir];

            if (nx <= 0 || ny <= 0 || nx >= GameMap.WIDTH - 1 || ny >= GameMap.HEIGHT - 1) continue;
            if (mapa.getTile(nx, ny) == 1) continue;

            mapa.setFruit(tileX, tileY, 0);
            mapa.setFruit(nx, ny, 2);

            int px = nx * Fruit.SIZE;
            int py = ny * Fruit.SIZE;
            try {
                java.lang.reflect.Field fx = Fruit.class.getDeclaredField("x");
                java.lang.reflect.Field fy = Fruit.class.getDeclaredField("y");
                fx.setAccessible(true);
                fy.setAccessible(true);
                fx.setInt(f, px);
                fy.setInt(f, py);
            } catch (Exception ignored) {}
        }
    }

    private void placeRandomFruits() {
        Random r = new Random();
        HashSet<String> used = new HashSet<>();
        int bananas = 0;
        int pineapples = 0;
        int cacti = 0;

        while (bananas < 8) {
            int tx = 1 + r.nextInt(GameMap.WIDTH - 2);
            int ty = 1 + r.nextInt(GameMap.HEIGHT - 2);
            if (mapa.getTile(tx, ty) == 1) continue;
            if (tx == helado.getX() / GameMap.TILE_SIZE && ty == helado.getY() / GameMap.TILE_SIZE) continue;
            String key = tx + ":" + ty;
            if (used.contains(key)) continue;
            used.add(key);
            mapa.setFruit(tx, ty, 1);
            frutas.add(new Banana(tx, ty));
            bananas++;
        }

        while (pineapples < 8) {
            int tx = 1 + r.nextInt(GameMap.WIDTH - 2);
            int ty = 1 + r.nextInt(GameMap.HEIGHT - 2);
            if (mapa.getTile(tx, ty) == 1) continue;
            if (tx == helado.getX() / GameMap.TILE_SIZE && ty == helado.getY() / GameMap.TILE_SIZE) continue;
            String key = tx + ":" + ty;
            if (used.contains(key)) continue;
            used.add(key);
            mapa.setFruit(tx, ty, 2);
            frutas.add(new Pineapple(tx, ty));
            pineapples++;
        }

        while (cacti < 8) {
            int tx = 1 + r.nextInt(GameMap.WIDTH - 2);
            int ty = 1 + r.nextInt(GameMap.HEIGHT - 2);
            if (mapa.getTile(tx, ty) == 1) continue;
            if (tx == helado.getX() / GameMap.TILE_SIZE && ty == helado.getY() / GameMap.TILE_SIZE) continue;
            String key = tx + ":" + ty;
            if (used.contains(key)) continue;
            used.add(key);
            mapa.setFruit(tx, ty, 3);
            frutas.add(new Cactus(tx, ty));
            cacti++;
        }
    }

    private void generarMapaLevel2() {
        for (int x = 0; x < GameMap.WIDTH; x++) {
            for (int y = 0; y < GameMap.HEIGHT; y++) {
                mapa.setTile(x, y, 0);
            }
        }

        for (int x = 0; x < GameMap.WIDTH; x++) {
            for (int y = 0; y < GameMap.HEIGHT; y++) {
                if (x == 0 || y == 0 || x == GameMap.WIDTH - 1 || y == GameMap.HEIGHT - 1) {
                    mapa.setTile(x, y, 1);
                } else {
                    mapa.setTile(x, y, 1);
                }
            }
        }

        mapa.setTile(7, 7, 1);
        mapa.setTile(7, 8, 1);
        mapa.setTile(8, 7, 1);
        mapa.setTile(8, 8, 1);

        Random rnd = new Random();
        HashSet<String> emptySpaces = new HashSet<>();

        mapa.setTile(7, 11, 0);
        emptySpaces.add("7:11");
        mapa.setTile(6, 11, 0);
        emptySpaces.add("6:11");
        mapa.setTile(8, 11, 0);
        emptySpaces.add("8:11");

        while (emptySpaces.size() < 33) {
            int rx = 2 + rnd.nextInt(GameMap.WIDTH - 4);
            int ry = 2 + rnd.nextInt(GameMap.HEIGHT - 4);
            String key = rx + ":" + ry;
            if ((rx == 7 || rx == 8) && (ry == 7 || ry == 8)) continue;
            if (!emptySpaces.contains(key)) {
                mapa.setTile(rx, ry, 0);
                emptySpaces.add(key);
            }
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

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}
}