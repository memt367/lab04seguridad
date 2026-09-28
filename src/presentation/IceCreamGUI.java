package presentation;

import domain.GameMap;
import domain.IceCream;
import domain.IceCreamExceptions;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import persistence.IceCreamPersistence;

public class IceCreamGUI extends JFrame {

    private Image background;
    private JButton playButton;
    private IceCream player;
    private JPanel mainMenuPanel;
    private String gameMode;

    public IceCreamGUI() {
        setTitle("BadDopoCream");
        setSize(800, 600);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        background = new ImageIcon("src/imagenes/menu.png").getImage();

        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(background, 0, 0, getWidth(), getHeight(), null);
            }
        };

        backgroundPanel.setLayout(null);
        mainMenuPanel = backgroundPanel;
        setContentPane(mainMenuPanel);

        prepareElementsMenu();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }
        });

        playButton = new JButton("PLAY");
        
        playButton.setBounds(220, 465, 350, 20);
        playButton.setFont(new Font("Arial", Font.BOLD, 18));
        playButton.setForeground(Color.BLACK);
        playButton.setBackground(new Color(245, 245, 220)); 
        playButton.setOpaque(true);
        playButton.setBorderPainted(false);
        playButton.setFocusPainted(false);
        backgroundPanel.add(playButton);

        playButton.addActionListener(e -> abrirVentanaDeJuego());
    }

    private void prepareElementsMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menuArchivo = new JMenu("Archivo");

        JMenuItem itemNuevo       = new JMenuItem("Nuevo");
        JMenuItem itemAbrir       = new JMenuItem("Abrir");
        JMenuItem itemGuardarComo = new JMenuItem("Guardar como");
        JMenuItem itemImportar    = new JMenuItem("Importar");
        JMenuItem itemExportarComo= new JMenuItem("Exportar como");
        JMenuItem itemSalir       = new JMenuItem("Salir");

        menuArchivo.add(itemNuevo);
        menuArchivo.add(itemAbrir);
        menuArchivo.add(itemGuardarComo);
        menuArchivo.addSeparator();
        menuArchivo.add(itemImportar);
        menuArchivo.add(itemExportarComo);
        menuArchivo.addSeparator();
        menuArchivo.add(itemSalir);

        menuBar.add(menuArchivo);
        setJMenuBar(menuBar);

        itemNuevo.addActionListener(e -> optionNuevo());
        itemAbrir.addActionListener(e -> optionAbrir());
        itemGuardarComo.addActionListener(e -> optionGuardarComo());
        itemImportar.addActionListener(e -> optionImportar());
        itemExportarComo.addActionListener(e -> optionExportarComo());
        itemSalir.addActionListener(e -> optionSalir());
    }
    public void setGameMode(String mode) {
        this.gameMode = mode;
    }

    public String getGameMode() {
        return gameMode;
    }

    private void optionNuevo() {
        try {
            String sabor = JOptionPane.showInputDialog(this, "Sabor (por ejemplo: vainilla, fresa, chocolate):", "vainilla");
            if (sabor == null) return;
            GameMap mapa = new GameMap();
            player = new IceCream(sabor, mapa);
            this.repaint();
            JOptionPane.showMessageDialog(this, "Nuevo jugador creado.", "Información", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "No se pudo crear nuevo IceCream:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void optionAbrir() {
        try {
            pauseActiveTimers();
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Texto (*.txt)", "txt"));
            int result = chooser.showOpenDialog(this);
            if (result != JFileChooser.APPROVE_OPTION) { resumeActiveTimers(); return; }

            File f = chooser.getSelectedFile();
            String mode = IceCreamPersistence.readMode(f);
            int level = IceCreamPersistence.readLevel(f);
            String flavor = IceCreamPersistence.readFlavor(f);
            int time = IceCreamPersistence.readTime(f);
            int score = IceCreamPersistence.readScore(f);
            setGameMode(mode);

            JPanel panel;
            boolean machine = mode != null && mode.equalsIgnoreCase("mvm");
            if (flavor == null || flavor.isEmpty()) flavor = "vainilla";
            switch (level) {
                case 1:
                    panel = machine ? new Level1MachinePanel(this, flavor) : new Level1Panel(this, flavor);
                    break;
                case 2:
                    panel = machine ? new Level2MachinePanel(this, flavor) : new Level2Panel(this, flavor);
                    break;
                case 3:
                default:
                    panel = machine ? new Level3MachinePanel(this, flavor) : new Level3Panel(this, flavor);
                    break;
            }
            showPanel(panel);
            applyLoadedStateFromFile(f, time, score);
            resumeActiveTimers();
            JOptionPane.showMessageDialog(this, "Partida cargada.", "Información", JOptionPane.INFORMATION_MESSAGE);
        } catch (IceCreamExceptions e) {
            resumeActiveTimers();
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void optionGuardarComo() {
        try {
            pauseActiveTimers();
            SnapshotParams p = buildSnapshotParams();
            if (p == null) {
                JOptionPane.showMessageDialog(this, "No hay un estado de juego activo para guardar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                resumeActiveTimers();
                return;
            }

            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Texto (*.txt)", "txt"));
            int result = chooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                if (!f.getName().toLowerCase().endsWith(".txt")) {
                    f = new File(f.getParentFile(), f.getName() + ".txt");
                }
                IceCreamPersistence.saveState(f, p.mode, p.level, p.timeRemaining, p.score, p.flavor, p.mapa, p.helado, p.enemigos);
                JOptionPane.showMessageDialog(this, "Estado de juego guardado.", "Información", JOptionPane.INFORMATION_MESSAGE);
            }
            resumeActiveTimers();
        } catch (IceCreamExceptions e) {
            resumeActiveTimers();
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void optionImportar() {
        try {
            JFileChooser chooser = new JFileChooser();
            int result = chooser.showOpenDialog(this);
            if (result != JFileChooser.APPROVE_OPTION) return;

            File f = chooser.getSelectedFile();
            String mode = IceCreamPersistence.readMode(f);
            int level = IceCreamPersistence.readLevel(f);
            String flavor = IceCreamPersistence.readFlavor(f);
            int time = IceCreamPersistence.readTime(f);
            int score = IceCreamPersistence.readScore(f);

            setGameMode(mode);

            JPanel panel;
            boolean machine = mode != null && mode.equalsIgnoreCase("mvm");
            if (flavor == null || flavor.isEmpty()) flavor = "vainilla";
            switch (level) {
                case 1:
                    panel = machine ? new Level1MachinePanel(this, flavor) : new Level1Panel(this, flavor);
                    break;
                case 2:
                    panel = machine ? new Level2MachinePanel(this, flavor) : new Level2Panel(this, flavor);
                    break;
                case 3:
                default:
                    panel = machine ? new Level3MachinePanel(this, flavor) : new Level3Panel(this, flavor);
                    break;
            }
            showPanel(panel);
            applyLoadedStateFromFile(f, time, score);
            JOptionPane.showMessageDialog(this, "Partida cargada.", "Información", JOptionPane.INFORMATION_MESSAGE);
        } catch (IceCreamExceptions e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void optionExportarComo() {
        try {
            pauseActiveTimers();
            SnapshotParams p = buildSnapshotParams();
            if (p == null) {
                JOptionPane.showMessageDialog(this, "No hay un estado de juego activo para exportar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                resumeActiveTimers();
                return;
            }
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Texto (*.txt)", "txt"));
            int result = chooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                if (!f.getName().toLowerCase().endsWith(".txt")) {
                    f = new File(f.getParentFile(), f.getName() + ".txt");
                }
                IceCreamPersistence.saveState(f, p.mode, p.level, p.timeRemaining, p.score, p.flavor, p.mapa, p.helado, p.enemigos);
                JOptionPane.showMessageDialog(this, "Estado exportado.", "Información", JOptionPane.INFORMATION_MESSAGE);
            }
            resumeActiveTimers();
        } catch (IceCreamExceptions e) {
            resumeActiveTimers();
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void optionSalir() {
        confirmExit();
    }

    private void confirmExit() {
        int res = JOptionPane.showConfirmDialog(
                this,
                "¿Seguro que deseas salir?",
                "Confirmar salida",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (res == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
    private void abrirVentanaDeJuego() {
        setTitle("BadDopoCream");
        showPanel(new ModeSelectionPanel(this));
    }

    public void showPanel(JPanel panel) {
        setContentPane(panel);
        setSize(800, 600);
        setLocationRelativeTo(null);
        revalidate();
        repaint();
        panel.requestFocusInWindow();
    }

    private static class SnapshotParams {
        String mode; int level; int timeRemaining; int score; String flavor;
        GameMap mapa; IceCream helado; java.util.List<domain.Enemigos> enemigos;
    }

    private SnapshotParams buildSnapshotParams() {
        Component c = getContentPane();
        if (!(c instanceof JPanel)) return null;
        JPanel panel = (JPanel) c;
        try {
            Class<?> cls = panel.getClass();

            int level = 0;
            String name = cls.getSimpleName();
            if (name.contains("Level1")) level = 1;
            else if (name.contains("Level2")) level = 2;
            else if (name.contains("Level3")) level = 3;

            GameMap mapa = (GameMap) getField(panel, cls, "mapa");
            IceCream helado = (IceCream) getField(panel, cls, name.contains("Machine") ? "aiHelado" : "helado");
            if (helado == null) {
                helado = (IceCream) getField(panel, cls, "helado");
            }

            @SuppressWarnings("unchecked")
            java.util.List<domain.Enemigos> enemigos = (java.util.List<domain.Enemigos>) getField(panel, cls, "enemigos");

            Integer score = (Integer) getField(panel, cls, "score");
            Integer timeRemaining = (Integer) getField(panel, cls, "timeRemaining");

            String flavor = null;
            if (helado != null) {
                try {
                    java.lang.reflect.Field fs = IceCream.class.getDeclaredField("sabor");
                    fs.setAccessible(true);
                    flavor = (String) fs.get(helado);
                } catch (Exception ignored) {}
            }

            if (mapa == null || helado == null) return null;

            SnapshotParams p = new SnapshotParams();
            p.mode = getGameMode();
            p.level = level;
            p.timeRemaining = timeRemaining != null ? timeRemaining : 0;
            p.score = score != null ? score : 0;
            p.flavor = flavor != null ? flavor : "vainilla";
            p.mapa = mapa;
            p.helado = helado;
            p.enemigos = enemigos;
            return p;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Object getField(Object instance, Class<?> cls, String fieldName) {
        if (fieldName == null) return null;
        try {
            java.lang.reflect.Field f = cls.getDeclaredField(fieldName);
            f.setAccessible(true);
            return f.get(instance);
        } catch (Exception e) {
            return null;
        }
    }

    private void pauseActiveTimers() {
        Component c = getContentPane();
        if (!(c instanceof JPanel)) return;
        JPanel panel = (JPanel) c;
        Class<?> cls = panel.getClass();
        String[] timerNames = {"enemyTimer", "gameTimer", "aiTimer", "fruitTimer", "botTimer", "slowTimer"};
        for (String tn : timerNames) {
            try {
                java.lang.reflect.Field f = cls.getDeclaredField(tn);
                f.setAccessible(true);
                Object timerObj = f.get(panel);
                if (timerObj instanceof javax.swing.Timer) {
                    javax.swing.Timer t = (javax.swing.Timer) timerObj;
                    if (t.isRunning()) t.stop();
                }
            } catch (Exception ignored) {}
        }
    }

    private void resumeActiveTimers() {
        Component c = getContentPane();
        if (!(c instanceof JPanel)) return;
        JPanel panel = (JPanel) c;
        Class<?> cls = panel.getClass();
        String[] timerNames = {"enemyTimer", "gameTimer", "aiTimer", "fruitTimer", "botTimer", "slowTimer"};
        for (String tn : timerNames) {
            try {
                java.lang.reflect.Field f = cls.getDeclaredField(tn);
                f.setAccessible(true);
                Object timerObj = f.get(panel);
                if (timerObj instanceof javax.swing.Timer) {
                    javax.swing.Timer t = (javax.swing.Timer) timerObj;
                    if (!t.isRunning()) t.start();
                }
            } catch (Exception ignored) {}
        }
    }

    private void applyLoadedStateFromFile(File file, int time, int score) throws IceCreamExceptions {
        Component c = getContentPane();
        if (!(c instanceof JPanel)) return;
        JPanel panel = (JPanel) c;
        Class<?> cls = panel.getClass();
        try {
            GameMap mapa = (GameMap) getField(panel, cls, "mapa");
            IceCream helado = (IceCream) getField(panel, cls, cls.getSimpleName().contains("Machine") ? "aiHelado" : "helado");
            if (helado == null) helado = (IceCream) getField(panel, cls, "helado");
            @SuppressWarnings("unchecked")
            java.util.List<domain.Enemigos> enemigos = (java.util.List<domain.Enemigos>) getField(panel, cls, "enemigos");
            if (mapa == null || helado == null || enemigos == null) return;
            IceCreamPersistence.loadState(file, mapa, helado, enemigos);

            try {
                java.lang.reflect.Field fScore = cls.getDeclaredField("score");
                fScore.setAccessible(true);
                fScore.set(panel, score);
            } catch (Exception ignored) {}
            try {
                java.lang.reflect.Field fTime = cls.getDeclaredField("timeRemaining");
                fTime.setAccessible(true);
                fTime.set(panel, time);
            } catch (Exception ignored) {}

            panel.repaint();
        } catch (Exception ex) {
        }
    }

    public void returnToMainMenu() {
        setContentPane(mainMenuPanel);
        setSize(800, 600);
        setLocationRelativeTo(null);
        revalidate();
        repaint();
        mainMenuPanel.requestFocusInWindow();
    }

    private void makeInvisible(JButton btn) {
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            IceCreamGUI window = new IceCreamGUI();
            window.setVisible(true);
        });
    }
}