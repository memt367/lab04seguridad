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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import javax.swing.*;

public class Level1Panel extends JPanel implements KeyListener {

    private GameMap mapa;
    private IceCream helado;
    private Image iceBlock;
    private Image wallBlock;
    private ArrayList<Fruit> frutas;
    private ArrayList<Enemigos> enemigos;
    private ArrayList<Obstaculos> obstaculos;

    private javax.swing.Timer enemyTimer;
    private javax.swing.Timer gameTimer;


    private int lastDx = 0;
    private int lastDy = -1;

    private int score = 0;
    private int bananaCount = 0;
    private int grapeCount = 0;
    private int points = 0;

    private String saborInicial;
    private IceCreamGUI parent;
    private int timeRemaining = 120;


    public Level1Panel(IceCreamGUI parent, String sabor) {

        this.parent = parent;
        saborInicial = sabor;

        frutas = new ArrayList<>();
        enemigos = new ArrayList<>();
        obstaculos = new ArrayList<>();

        this.mapa = new GameMap();
        this.iceBlock = new ImageIcon("src/imagenes/ice_block.png").getImage();
        this.wallBlock = new ImageIcon("src/imagenes/muro.png").getImage();

        this.helado = new IceCream(sabor, mapa);

        mapa.clearFruits();
        placeRandomFruits();
        generarObstaculosAleatorios();


        Random rnd = new Random();
        int t1x = (1 + rnd.nextInt(GameMap.WIDTH - 2)) * GameMap.TILE_SIZE;
        int t1y = (1 + rnd.nextInt(GameMap.HEIGHT - 2)) * GameMap.TILE_SIZE;
        enemigos.add(new Troll(t1x, t1y, mapa));

        int t2x, t2y;
        do {
            t2x = (1 + rnd.nextInt(GameMap.WIDTH - 2)) * GameMap.TILE_SIZE;
            t2y = (1 + rnd.nextInt(GameMap.HEIGHT - 2)) * GameMap.TILE_SIZE;
        } while (Math.abs(t2x - t1x) < 100 && Math.abs(t2y - t1y) < 100);

        enemigos.add(new Troll(t2x, t2y, mapa));

        setFocusable(true);
        addKeyListener(this);


        enemyTimer = new javax.swing.Timer(400, e -> {
            for (Enemigos ene : enemigos) {
                ene.move(helado.getX(), helado.getY());

                if (ene.touches(helado.getX(), helado.getY())) {
                    stopTimers();
                    JOptionPane.showMessageDialog(this, "Has sido atrapado...", "Derrota", JOptionPane.ERROR_MESSAGE);
                    parent.showPanel(new ChooseIceCreamPanel(parent));
                    return;
                }
            }
            
            // Actualizar obstáculos (para reencender fogatas)
            for (Obstaculos obs : obstaculos) {
                obs.update();
            }
            
            repaint();
        });
        enemyTimer.start();


        gameTimer = new javax.swing.Timer(1000, e -> {
            timeRemaining--;
            if (timeRemaining <= 0) {
                stopTimers();
                JOptionPane.showMessageDialog(this, "¡Tiempo agotado!", "Tiempo", JOptionPane.ERROR_MESSAGE);
                parent.showPanel(new ChooseIceCreamPanel(parent));
            }
            repaint();
        });
        gameTimer.start();
    }

    private void stopTimers() {
        if (enemyTimer != null) enemyTimer.stop();
        if (gameTimer != null) gameTimer.stop();
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
        int x = 480, y = 50, w = 280, h = 200;

        g.setColor(new Color(50, 50, 70, 200));
        g.fillRoundRect(x, y, w, h, 15, 15);
        g.setColor(Color.WHITE);
        g.drawRoundRect(x, y, w, h, 15, 15);

        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.setColor(new Color(255, 215, 0));
        g.drawString("SCORE", x + 95, y + 35);

        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.setColor(Color.WHITE);
        g.drawString("Plátanos:", x + 20, y + 80);
        g.setColor(new Color(255, 220, 100));
        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.drawString(bananaCount + " / 8", x + 140, y + 82);

        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.setColor(Color.WHITE);
        g.drawString("Uvas:", x + 20, y + 130);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.setColor(new Color(180, 120, 200));
        g.drawString(grapeCount + " / 8", x + 140, y + 132);

        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.setColor(Color.LIGHT_GRAY);
        g.drawString("Total: " + score + " / 16", x + 95, y + 175);

        drawTimerPanel(g);
    }

    private void drawTimerPanel(Graphics g) {
        int x = 480, y = 270, w = 280, h = 100;
        g.setColor(new Color(50, 50, 70, 200));
        g.fillRoundRect(x, y, w, h, 15, 15);
        g.setColor(Color.WHITE);
        g.drawRoundRect(x, y, w, h, 15, 15);

        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.setColor(new Color(255, 215, 0));
        g.drawString("TIEMPO", x + 95, y + 30);

        int min = timeRemaining / 60;
        int sec = timeRemaining % 60;
        String time = String.format("%02d:%02d", min, sec);

        g.setFont(new Font("Arial", Font.BOLD, 36));

        if (timeRemaining > 60) g.setColor(Color.GREEN);
        else if (timeRemaining > 30) g.setColor(Color.YELLOW);
        else g.setColor(Color.RED);

        g.drawString(time, x + 75, y + 75);
    }
    private void drawPointsPanel(Graphics g) {
        int panelX = 480, panelY = 380;
        int w = 280, h = 110;
        g.setColor(new Color(50, 50, 70, 200));
        g.fillRoundRect(panelX, panelY, w, h, 15, 15);
        g.setColor(Color.WHITE);
        g.drawRoundRect(panelX, panelY, w, h, 15, 15);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.setColor(new Color(255, 215, 0));
        g.drawString("PUNTOS", panelX + 90, panelY + 32);
        int maxPoints = 8 * 50 + 8 * 100; // 8 uvas + 8 plátanos
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.setColor(new Color(120, 255, 120));
        g.drawString(points + " / " + maxPoints, panelX + 70, panelY + 78);
    }

    private void pintarMapa(Graphics g) {
        for (int x = 0; x < GameMap.WIDTH; x++) {
            for (int y = 0; y < GameMap.HEIGHT; y++) {
                int t = mapa.getTile(x, y);

                if (t == 1) {
                    Image img = (x == 0 || y == 0 || x == GameMap.WIDTH - 1 || y == GameMap.HEIGHT - 1)
                            ? wallBlock : iceBlock;

                    g.drawImage(img, x * GameMap.TILE_SIZE, y * GameMap.TILE_SIZE,
                            GameMap.TILE_SIZE, GameMap.TILE_SIZE, null);
                } else {
                    g.setColor(new Color(240, 250, 255));
                    g.fillRect(x * GameMap.TILE_SIZE, y * GameMap.TILE_SIZE, GameMap.TILE_SIZE, GameMap.TILE_SIZE);
                }
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {

        int code = e.getKeyCode();
        int dx = 0, dy = 0;

        if (code == KeyEvent.VK_UP || code == KeyEvent.VK_W) dy = -1;
        if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S) dy = 1;
        if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) dx = -1;
        if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) dx = 1;


        if (dx != 0 || dy != 0) {
            lastDx = dx;
            lastDy = dy;
            
            // Verificar si hay obstáculos bloqueantes en la posición destino
            int tileX = helado.getX() / GameMap.TILE_SIZE;
            int tileY = helado.getY() / GameMap.TILE_SIZE;
            int nxTile = tileX + dx;
            int nyTile = tileY + dy;
            
            boolean bloqueado = false;
            for (Obstaculos obs : obstaculos) {
                if (obs.isBlocking() && obs.getTileX() == nxTile && obs.getTileY() == nyTile) {
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
                        stopTimers();
                        JOptionPane.showMessageDialog(this, "¡Te has quemado con una fogata!", "Derrota", JOptionPane.ERROR_MESSAGE);
                        parent.showPanel(new ChooseIceCreamPanel(parent));
                        return;
                    }
                }
            }
        }


        if (code == KeyEvent.VK_SPACE) {
            placeIceInLastDirection();
        }

        if (code == KeyEvent.VK_Q) {
            breakIceInLastDirection();
        }


        for (Enemigos ene : enemigos) {
            if (ene.touches(helado.getX(), helado.getY())) {
                stopTimers();
                JOptionPane.showMessageDialog(this, "Has sido atrapado...", "Derrota", JOptionPane.ERROR_MESSAGE);
                parent.showPanel(new ChooseIceCreamPanel(parent));
                return;
            }
        }

        frutas.removeIf(f -> {
            if (f.checkCollision(helado.getX(), helado.getY())) {
                mapa.setFruit(f.getX(), f.getY(), 0);
                score++;

                if (f instanceof Banana) {
                    bananaCount++;
                    points += f.getPoints();
                } else if (f instanceof Grape) {
                    grapeCount++;
                    points += f.getPoints();
                }

                if (score == 16) {
                    stopTimers();
                    mostrarDialogoNivelCompletado();
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
            if (nx <= 0 || ny <= 0 || nx >= GameMap.WIDTH - 1 || ny >= GameMap.HEIGHT - 1) break;
            if (mapa.getTile(nx, ny) == 1) break;

            mapa.setTile(nx, ny, 1);
            
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
            
            // Apagar fogatas que queden bajo el hielo (solo si no se derritió)
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
            if (nx <= 0 || ny <= 0 || nx >= GameMap.WIDTH - 1 || ny >= GameMap.HEIGHT - 1) break;

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


    private void mostrarDialogoNivelCompletado() {
        Object[] opciones = {"Reiniciar Nivel 1", "Continuar a Nivel 2", "Terminar"};

        int opcion = JOptionPane.showOptionDialog(
                this,
                "¡Felicidades! Has completado el Nivel 1",
                "Nivel 1",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                opciones,
                opciones[1]
        );

        if (opcion == 0) parent.showPanel(new Level1Panel(parent, saborInicial));
        else if (opcion == 1) parent.showPanel(new Level2Panel(parent, saborInicial));
        else parent.showPanel(new ChooseIceCreamPanel(parent));
    }


    private void placeRandomFruits() {
        Random r = new Random();
        HashSet<String> used = new HashSet<>();

        int bananas = 0, grapes = 0;

        while (bananas < 8) {
            int tx = 1 + r.nextInt(GameMap.WIDTH - 2);
            int ty = 1 + r.nextInt(GameMap.HEIGHT - 2);

            if (mapa.getTile(tx, ty) == 1) continue;
            if (used.contains(tx + ":" + ty)) continue;

            used.add(tx + ":" + ty);
            frutas.add(new Banana(tx, ty));
            mapa.setFruit(tx, ty, 1);
            bananas++;
        }

        while (grapes < 8) {
            int tx = 1 + r.nextInt(GameMap.WIDTH - 2);
            int ty = 1 + r.nextInt(GameMap.HEIGHT - 2);

            if (mapa.getTile(tx, ty) == 1) continue;
            if (used.contains(tx + ":" + ty)) continue;

            used.add(tx + ":" + ty);
            frutas.add(new Grape(tx, ty));
            mapa.setFruit(tx, ty, 2);
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
