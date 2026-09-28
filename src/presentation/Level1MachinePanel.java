package presentation;

import domain.Enemigos;
import domain.GameMap;
import domain.IceCream;
import domain.Obstaculos;
import domain.enemies.Troll;
import domain.fruits.Banana;
import domain.fruits.Fruit;
import domain.fruits.Grape;
import domain.obstacles.BaldosaCaliente;
import domain.obstacles.BloqueHielo;
import domain.obstacles.Fogata;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import javax.swing.*;

public class Level1MachinePanel extends JPanel {

    private GameMap mapa;
    private IceCream aiHelado;
    private Image iceBlock;
    private Image wallBlock;

    private ArrayList<Fruit> frutas;
    private ArrayList<Enemigos> enemigos;
    private ArrayList<Obstaculos> obstaculos;

    private Timer aiTimer;
    private Timer enemyTimer;
    private Timer gameTimer;

    private int score = 0;
    private int bananaCount = 0;
    private int grapeCount = 0;
    private int points = 0;

    private String saborInicial;
    private IceCreamGUI parent;

    private int timeRemaining = 120;

    public Level1MachinePanel(IceCreamGUI parent, String sabor) {
        this.parent = parent;
        this.saborInicial = sabor;

        mapa = new GameMap();
        mapa.clearFruits();
        frutas = new ArrayList<>();
        enemigos = new ArrayList<>();
        obstaculos = new ArrayList<>();

        iceBlock = new ImageIcon("src/imagenes/ice_block.png").getImage();
        wallBlock = new ImageIcon("src/imagenes/muro.png").getImage();

        aiHelado = new IceCream(sabor, mapa);

        placeRandomFruits();
        generarObstaculosAleatorios();

        Random rnd = new Random();
        enemigos.add(new Troll(
                (1 + rnd.nextInt(GameMap.WIDTH - 2)) * GameMap.TILE_SIZE,
                (1 + rnd.nextInt(GameMap.HEIGHT - 2)) * GameMap.TILE_SIZE,
                mapa
        ));
        enemigos.add(new Troll(
                (1 + rnd.nextInt(GameMap.WIDTH - 2)) * GameMap.TILE_SIZE,
                (1 + rnd.nextInt(GameMap.HEIGHT - 2)) * GameMap.TILE_SIZE,
                mapa
        ));

        aiTimer = new Timer(250, e -> moveAI());
        aiTimer.start();

        enemyTimer = new Timer(400, e -> updateEnemies());
        enemyTimer.start();

        gameTimer = new Timer(1000, e -> tickTime());
        gameTimer.start();

        setFocusable(true);
    }

    private void moveAI() {
        if (frutas.isEmpty()) return;

        Fruit target = getClosestFruit();

        int aiTileX = aiHelado.getX() / GameMap.TILE_SIZE;
        int aiTileY = aiHelado.getY() / GameMap.TILE_SIZE;

        int targetX = target.getX();
        int targetY = target.getY();

        int dx = Integer.compare(targetX, aiTileX);
        int dy = Integer.compare(targetY, aiTileY);

        int distX = Math.abs(targetX - aiTileX);
        int distY = Math.abs(targetY - aiTileY);

        boolean moved;

        if (distX > distY) {
            moved = tryMoveAI(dx, 0);
            if (!moved) tryMoveAI(0, dy);
        } else {
            moved = tryMoveAI(0, dy);
            if (!moved) tryMoveAI(dx, 0);
        }

        for (Enemigos ene : enemigos) {
            if (rectCollision(ene.getX(), ene.getY(), aiHelado.getX(), aiHelado.getY())) {
                enemyCaught();
                return;
            }
        }
        
        // Verificar colisión con fogatas
        for (Obstaculos obs : obstaculos) {
            if (obs instanceof Fogata) {
                Fogata fogata = (Fogata) obs;
                if (fogata.touches(aiHelado.getX(), aiHelado.getY()) && fogata.puedeEliminarHelado()) {
                    lose("¡El bot se ha quemado con una fogata!");
                    return;
                }
            }
        }

        repaint();
    }

    private boolean tryMoveAI(int dx, int dy) {
        int nx = aiHelado.getX() / GameMap.TILE_SIZE + dx;
        int ny = aiHelado.getY() / GameMap.TILE_SIZE + dy;

        if (nx < 0 || ny < 0 || nx >= GameMap.WIDTH || ny >= GameMap.HEIGHT) return false;

        // Verificar obstáculos bloqueantes
        for (Obstaculos obs : obstaculos) {
            if (obs.isBlocking() && obs.getTileX() == nx && obs.getTileY() == ny) {
                return false; // no puede moverse, hay un obstáculo sólido
            }
        }

        int tile = mapa.getTile(nx, ny);

        if (tile == 1) {
            mapa.setTile(nx, ny, 0);
            return true;
        }

        aiHelado.moverTile(dx, dy);
        checkFruitCollision();
        return true;
    }

    private Fruit getClosestFruit() {
        Fruit best = null;
        double bestDist = Double.MAX_VALUE;

        int ax = aiHelado.getX() / GameMap.TILE_SIZE;
        int ay = aiHelado.getY() / GameMap.TILE_SIZE;

        for (Fruit f : frutas) {
            double d = Math.hypot(f.getX() - ax, f.getY() - ay);
            if (d < bestDist) {
                bestDist = d;
                best = f;
            }
        }
        return best;
    }

    private void updateEnemies() {
        for (Enemigos ene : enemigos) {
            ene.move(aiHelado.getX(), aiHelado.getY());
            if (rectCollision(ene.getX(), ene.getY(), aiHelado.getX(), aiHelado.getY())) {
                enemyCaught();
                return;
            }
        }
        
        // Actualizar obstáculos (reencender fogatas)
        for (Obstaculos obs : obstaculos) {
            obs.update();
        }
        
        repaint();
    }

    private boolean rectCollision(int ex, int ey, int px, int py) {
        Rectangle rEnemy = new Rectangle(ex, ey, GameMap.TILE_SIZE, GameMap.TILE_SIZE);
        Rectangle rPlayer = new Rectangle(px, py, GameMap.TILE_SIZE, GameMap.TILE_SIZE);
        return rEnemy.intersects(rPlayer);
    }

    private void tickTime() {
        timeRemaining--;
        if (timeRemaining <= 0) {
            lose("¡Tiempo agotado!");
        }
        repaint();
    }

    private void checkFruitCollision() {
        frutas.removeIf(f -> {
            if (f.checkCollision(aiHelado.getX(), aiHelado.getY())) {
                mapa.setFruit(f.getX(), f.getY(), 0);
                score++;

                if (f instanceof Banana) bananaCount++;
                if (f instanceof Grape) grapeCount++;

                if (score == 16) win();
                return true;
            }
            return false;
        });
    }

    private void enemyCaught() {
        stopAll();
        JOptionPane.showMessageDialog(this, "La máquina fue atrapada", "Derrota", JOptionPane.ERROR_MESSAGE);
        parent.showPanel(new Level1MachinePanel(parent, saborInicial));
    }

    private void win() {
        stopAll();
        JOptionPane.showMessageDialog(this, "¡La máquina ganó el Nivel 1!");
        parent.showPanel(new Level2MachinePanel(parent, saborInicial));
    }

    private void lose(String msg) {
        stopAll();
        JOptionPane.showMessageDialog(this, msg);
        parent.showPanel(new Level1MachinePanel(parent, saborInicial));
    }

    private void stopAll() {
        if (aiTimer != null) aiTimer.stop();
        if (enemyTimer != null) enemyTimer.stop();
        if (gameTimer != null) gameTimer.stop();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        paintMap(g);

        for (Obstaculos obs : obstaculos) obs.draw(g);
        for (Fruit f : frutas) f.draw(g);
        for (Enemigos e : enemigos) e.draw(g);
        aiHelado.dibujar(g);

        drawScorePanel(g);
        drawTimerPanel(g);
    }

    private void paintMap(Graphics g) {
        for (int x = 0; x < GameMap.WIDTH; x++) {
            for (int y = 0; y < GameMap.HEIGHT; y++) {
                int tile = mapa.getTile(x, y);

                if (tile == 1) {
                    Image img = (x == 0 || y == 0 || x == GameMap.WIDTH - 1 || y == GameMap.HEIGHT - 1)
                            ? wallBlock : iceBlock;

                    g.drawImage(img,
                            x * GameMap.TILE_SIZE, y * GameMap.TILE_SIZE,
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
        int panelWidth = 280, panelHeight = 200;

        g.setColor(new Color(50, 50, 70, 200));
        g.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 15, 15);
        g.setColor(Color.WHITE);
        g.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 15, 15);

        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.setColor(new Color(255, 215, 0));
        g.drawString("SCORE L1 (BOT)", panelX + 55, panelY + 35);

        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.setColor(Color.WHITE);
        g.drawString("Plátanos:", panelX + 20, panelY + 80);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.setColor(new Color(255, 220, 100));
        g.drawString(bananaCount + " / 8", panelX + 160, panelY + 82);

        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.setColor(Color.WHITE);
        g.drawString("Uvas:", panelX + 20, panelY + 130);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.setColor(new Color(180, 120, 200));
        g.drawString(grapeCount + " / 8", panelX + 160, panelY + 132);

        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.setColor(new Color(200, 200, 200));
        g.drawString("Total: " + score + " / 16", panelX + 95, panelY + 175);
    }

    private void drawTimerPanel(Graphics g) {
        int panelX = 480, panelY = 270;
        int panelWidth = 280, panelHeight = 100;

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

        if (timeRemaining > 60) g.setColor(new Color(100, 255, 100));
        else if (timeRemaining > 30) g.setColor(new Color(255, 255, 100));
        else g.setColor(new Color(255, 100, 100));

        g.drawString(timeStr, panelX + 75, panelY + 72);
    }

    private void placeRandomFruits() {
        Random r = new Random();
        HashSet<String> used = new HashSet<>();
        int bananas = 0;
        int grapes = 0;

        while (bananas < 8) {
            int tx = 1 + r.nextInt(GameMap.WIDTH - 2);
            int ty = 1 + r.nextInt(GameMap.HEIGHT - 2);
            if (mapa.getTile(tx, ty) == 1) continue;

            String key = tx + ":" + ty;
            if (used.contains(key)) continue;

            used.add(key);
            mapa.setFruit(tx, ty, 1);
            frutas.add(new Banana(tx, ty));
            bananas++;
        }

        while (grapes < 8) {
            int tx = 1 + r.nextInt(GameMap.WIDTH - 2);
            int ty = 1 + r.nextInt(GameMap.HEIGHT - 2);
            if (mapa.getTile(tx, ty) == 1) continue;

            String key = tx + ":" + ty;
            if (used.contains(key)) continue;

            used.add(key);
            mapa.setFruit(tx, ty, 2);
            frutas.add(new Grape(tx, ty));
            grapes++;
        }
    }

    private void generarObstaculosAleatorios() {
        Random r = new Random();
        int numObstaculos = 5 + r.nextInt(3); // 5-7 obstáculos (mínimo 5)
        
        for (int i = 0; i < numObstaculos; i++) {
            int tx = 2 + r.nextInt(GameMap.WIDTH - 4);
            int ty = 2 + r.nextInt(GameMap.HEIGHT - 4);
            
            // No colocar en posición del helado ni donde hay frutas
            if (tx == aiHelado.getX() / GameMap.TILE_SIZE && ty == aiHelado.getY() / GameMap.TILE_SIZE) continue;
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
