package game;

import javax.swing.*;

public class GameWindow {
    private static JFrame frame;
    private static JPanel currentPanel;

    public static void init() {
        frame = new JFrame("Spotty Life – Bones & Wires");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPanel(new GamePanel());   // start room
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void setPanel(JPanel panel) {
        if (frame == null) {
            System.err.println("GameWindow.init() was never called!");
            return;
        }

        // 🛑 STOP any previous GamePanel thread
        if (currentPanel instanceof GamePanel gp) {
            gp.stop(); // This safely ends the game loop in the old panel
        }

        frame.setContentPane(panel);
        frame.revalidate();
        frame.repaint();
        panel.requestFocusInWindow();

        currentPanel = panel; // 🔁 Track the active panel
    }
}