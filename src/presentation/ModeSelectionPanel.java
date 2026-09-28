package presentation;

import java.awt.*;
import javax.swing.*;

public class ModeSelectionPanel extends JPanel {

    private JButton pvpButton;
    private JButton pvmButton;
    private JButton mvmButton;
    private Image background;
    private IceCreamGUI parent;

    public ModeSelectionPanel(IceCreamGUI parent) {
        this.parent = parent;
        setLayout(null);


        background = new ImageIcon("src/imagenes/select_mode_bg.png").getImage();


        pvpButton = new JButton();
        pvpButton.setBounds(140, 250, 300, 60);
        makeInvisible(pvpButton);
        add(pvpButton);

        pvmButton = new JButton();
        pvmButton.setBounds(140, 300, 300, 60);
        makeInvisible(pvmButton);
        add(pvmButton);

        mvmButton = new JButton();
        mvmButton.setBounds(140, 370, 300, 60);
        makeInvisible(mvmButton);
        add(mvmButton);


        pvpButton.addActionListener(e -> parent.showPanel(new ChooseIceCreamPanel(parent)));
        pvmButton.addActionListener(e -> parent.showPanel(new ChooseIceCreamPanel(parent)));
        mvmButton.addActionListener(e -> parent.showPanel(new Level1MachinePanel(parent,"Vainilla")));
    }

    private void makeInvisible(JButton btn) {
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setText("");
    }


    private void styleButton(JButton btn) {
        btn.setFont(new Font("Arial", Font.BOLD, 24));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(75, 140, 220));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 3));
        btn.setOpaque(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, getWidth(), getHeight(), null);
    }
}
