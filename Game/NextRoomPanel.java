package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NextRoomPanel extends JPanel implements Runnable, KeyListener {
    private static final int WIDTH = 640;
    private static final int HEIGHT = 480;

    private DialogueBox dialogueBox;
    private boolean showingDialogue = false;
    private Rectangle triggerZone = new Rectangle(200, 100, 64, 64);
    private boolean hasTriggeredDialogue = false;

    private BufferedImage background;
    private Player player;
    private Thread gameThread;
    private final List<Rectangle> walls = new ArrayList<>();

    private int shakeTimer = 0;
    private final int shakeMagnitude = 4;

    public NextRoomPanel() {
        init();
    }

    private void init() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addKeyListener(this);

        try {
            background = ImageIO.read(getClass().getResourceAsStream("/assets/room2.png"));
        } catch (IOException e) {
            System.err.println("Failed to load room2 background.");
        }

        player = new Player(100, 100);
        dialogueBox = new DialogueBox("You feel an odd, warm breeze... but there’s no wind here.");

        walls.clear();
        walls.add(new Rectangle(0, 0, WIDTH, 75));
        walls.add(new Rectangle(0, HEIGHT - 32, WIDTH, 32));
        walls.add(new Rectangle(0, 0, 32, HEIGHT));
        walls.add(new Rectangle(WIDTH - 32, 0, 32, HEIGHT));

        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        final double ns = 1_000_000_000.0 / 60;
        long last = System.nanoTime();

        while (true) {
            long now = System.nanoTime();
            while (now - last >= ns) {
                update();
                last += ns;
            }
            repaint();
            try {
                Thread.sleep(2);
            } catch (InterruptedException ignored) {}
        }
    }

    private void update() {
        if (showingDialogue) {
            dialogueBox.update();
            return; // Skip player update while dialogue is active
        }

        player.update(walls);

        // Trigger dialogue once when player enters the zone
        if (!hasTriggeredDialogue && player.getBounds().intersects(triggerZone)) {
            showingDialogue = true;
            hasTriggeredDialogue = true;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        int sx = 0, sy = 0;
        if (shakeTimer > 0) {
            sx = (int) (Math.random() * shakeMagnitude * 2) - shakeMagnitude;
            sy = (int) (Math.random() * shakeMagnitude * 2) - shakeMagnitude;
            shakeTimer--;
        }

        g2.translate(sx, sy);

        if (background != null)
            g2.drawImage(background, 0, 0, WIDTH, HEIGHT, null);

        player.draw(g2);

        g2.translate(-sx, -sy);

        if (showingDialogue) {
            dialogueBox.draw(g2, WIDTH, HEIGHT);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {
        if (!showingDialogue) {
            player.keyReleased(e);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (showingDialogue) {
            if (e.getKeyCode() == KeyEvent.VK_Z) {
                if (!dialogueBox.isComplete()) {
                    dialogueBox.forceFinish();
                } else {
                    showingDialogue = false;
                }
            }
            return; // Prevent player movement while in dialogue
        }

        player.keyPressed(e);
    }
}