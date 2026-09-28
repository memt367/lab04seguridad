package presentation;

import java.awt.*;
import javax.swing.*;

public class ChooseIceCreamPanel extends JPanel {

    private JButton chocolateButton;
    private JButton vanillaButton;
    private JButton strawberryButton;
    private JButton backButton;

    private Image backgroundImage;
    private IceCreamGUI parentGUI;

    public ChooseIceCreamPanel(IceCreamGUI parent) {
        this.parentGUI = parent;

        backgroundImage = new ImageIcon("src/imagenes/choose_flavour.png").getImage();

        setLayout(null);


        chocolateButton = new JButton("");
        chocolateButton.setBounds(250, 110, 100, 150);
        makeInvisible(chocolateButton);
        add(chocolateButton);

        vanillaButton = new JButton("");
        vanillaButton.setBounds(350, 180, 100, 150);
        makeInvisible(vanillaButton);
        add(vanillaButton);

        strawberryButton = new JButton("");
        strawberryButton.setBounds(450, 110, 100, 150);
        makeInvisible(strawberryButton);
        add(strawberryButton);

        backButton = new JButton("");
        backButton.setBounds(350, 455, 100, 35);
        makeInvisible(backButton);
        add(backButton);


        chocolateButton.addActionListener(e -> seleccionar("chocolate"));
        vanillaButton.addActionListener(e -> seleccionar("vainilla"));
        strawberryButton.addActionListener(e -> seleccionar("fresa"));
        backButton.addActionListener(e -> parentGUI.showPanel(new ModeSelectionPanel(parentGUI)));
    }

    
    private void seleccionar(String sabor) {

        String mode = parentGUI.getGameMode();  

        if (mode == null) {
            
            parentGUI.showPanel(new Level1Panel(parentGUI, sabor));
            return;
        }

        switch (mode) {

            case "pvp":
                parentGUI.showPanel(new Level1Panel(parentGUI, sabor));
                break;

            case "pvm":
                parentGUI.showPanel(new Level1Panel(parentGUI, sabor));
                break;

            case "mvm":
                parentGUI.showPanel(new Level1MachinePanel(parentGUI, sabor));
                break;

            default:
                parentGUI.showPanel(new Level1Panel(parentGUI, sabor));
                break;
        }
    }

    private void makeInvisible(JButton btn) {
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
    }
}
