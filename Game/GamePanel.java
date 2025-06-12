package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel implements Runnable, KeyListener {
    private static final int WIDTH = 640, HEIGHT = 480;
    private Thread gameThread;
    private volatile boolean running = false;
    private BufferedImage background;
    private Player player;
    private Battery battery;
    private MetalDoor metalDoor;
    private Rectangle teleportTile;
    private boolean doorSliding = false;
    private int doorSlideY = 0;
    private final int DOOR_SLIDE_TARGET = 64;
    private boolean doorOpen = false;
    private int shakeTimer = 0;
    private final int shakeMagnitude = 4;
    private final List<Rectangle> walls = new ArrayList<>();
    private final List<Confetti> confetti = new ArrayList<>();
    private DialogueBox dialogueBox;
    private boolean dialogueActive = false;

    public GamePanel() {
        init();
    }

    private void init() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addKeyListener(this);
        teleportTile = new Rectangle(200, DOOR_SLIDE_TARGET + 30, 32, 32);

        walls.clear();
        walls.add(new Rectangle(0, 0, WIDTH, 75));
        walls.add(new Rectangle(0, HEIGHT - 32, WIDTH, 32));
        walls.add(new Rectangle(0, 0, 32, HEIGHT));
        walls.add(new Rectangle(WIDTH - 32, 0, 32, HEIGHT));

        int[][] holeTiles = {
            {9, 7}, {10, 7}, {9, 6}, {10, 6},
            {8, 8}, {9, 8}, {10, 8}, {11, 8},
            {8, 7}, {11, 7}, {9, 9}, {10, 9}
        };
        for (int[] t : holeTiles) {
            walls.add(new Rectangle(t[0] * 32, t[1] * 32, 32, 32));
        }

        battery = new Battery(200, 200);
        metalDoor = new MetalDoor(128, 53);
        player = new Player(100, 100);

        if (GameState.batteryDelivered) {
            battery.deactivate();
            metalDoor.powerUp();
            doorSliding = true;
            doorOpen = true;
            shakeTimer = 30;
        }

        try {
            background = ImageIO.read(getClass().getResourceAsStream("/assets/tutorial_floor1.3.png"));
        } catch (IOException e) {
            System.err.println("Cannot load background image.");
        }

        confetti.clear();
        dialogueBox = null;
        dialogueActive = false;
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        final double ns = 1_000_000_000.0 / 60;
        long last = System.nanoTime();

        while (running && Thread.currentThread() == gameThread) {
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

    public void stop() {
        running = false;
        gameThread = null;
    }

    private void update() {
        if (!dialogueActive) {
            player.update(walls);
            Rectangle pb = new Rectangle(player.getX(), player.getY(), player.getWidth(), player.getHeight());

            if (battery.isActive() && !battery.isPickedUp() && pb.intersects(battery.getBounds())) {
                battery.pickUp();
            }

            if (battery.isActive() && battery.isPickedUp() && pb.intersects(metalDoor.getBounds())) {
                if (!GameState.batteryDelivered) {
                    metalDoor.powerUp();
                    battery.deactivate();
                    GameState.batteryDelivered = true;

                    if (!GameState.batteryPowerSoundPlayed) {
                        SoundPlayer.playSound("/assets/sounds/door_powerup.wav");
                        GameState.batteryPowerSoundPlayed = true;
                    }

                    if (dialogueBox == null) {
                        dialogueBox = new DialogueBox(
                            "THE DOOR H O W L S WITH POWER!",
                            "Hack Door", "Quit(becomes unplayable)"
                        );
                        dialogueActive = true;
                    }
                }
            }

            if (GameState.mazeCompleted && !GameState.mazeSoundsPlayed) {
                GameState.mazeSoundsPlayed = true;
                shakeTimer = 60;
                doorSliding = true;
                doorOpen = true;

                if (!GameState.doorSlidePlayed) {
                    GameState.doorSlidePlayed = true;
                    new java.util.Timer().schedule(new java.util.TimerTask() {
                        @Override
                        public void run() {
                            SoundPlayer.playSound("/assets/sounds/door_slide.wav");
                        }
                    }, 600);
                }
            }

            if (GameState.mazeCompleted && doorOpen && teleportTile.intersects(pb)) {
                GameWindow.setPanel(new NextRoomPanel());
            }

            for (Confetti c : confetti) {
                c.update();
            }

            confetti.removeIf(c -> !c.isAlive());
        }

        if (doorSliding) {
            if (doorSlideY < DOOR_SLIDE_TARGET) doorSlideY++;
            else doorSliding = false;
        }

        if (shakeTimer > 0) shakeTimer--;

        if (dialogueActive && dialogueBox != null) {
            dialogueBox.update();
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
        }
        g2.translate(sx, sy);

        if (background != null)
            g2.drawImage(background, 0, 0, WIDTH, HEIGHT, null);

        record Z(int y, Runnable r) {}
        List<Z> zList = new ArrayList<>();

        zList.add(new Z(metalDoor.getY() + metalDoor.getBounds().height - doorSlideY,
            () -> metalDoor.draw(g2, doorSlideY)));

        if (battery.isActive() && !battery.isPickedUp()) {
            Rectangle b = battery.getBounds();
            zList.add(new Z(b.y + b.height, () -> battery.draw(g2, player.getX(), player.getY())));
        }

        zList.add(new Z(player.getY() + player.getHeight(), () -> {
            player.draw(g2);
            if (battery.isPickedUp())
                battery.draw(g2, player.getX(), player.getY());
        }));

        zList.sort((a, b) -> Integer.compare(a.y(), b.y()));
        for (Z z : zList) z.r().run();

        for (Confetti c : confetti) c.draw(g2);

        if (GameState.mazeCompleted && doorOpen) {
            g2.setColor(new Color(0, 255, 255, 100));
            g2.fill(teleportTile);
        }

        g2.translate(-sx, -sy);

        if (dialogueBox != null)
            dialogueBox.draw(g2, WIDTH, HEIGHT);
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {
        if (!dialogueActive) player.keyReleased(e);
    }
    @Override public void keyPressed(KeyEvent e) {
        if (dialogueActive && dialogueBox != null) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_DOWN -> dialogueBox.nextOption();
                case KeyEvent.VK_UP -> dialogueBox.prevOption();
                case KeyEvent.VK_Z -> {
                    if (dialogueBox.isComplete()) {
                        int choice = dialogueBox.getSelectedOption();
                        if (choice == 0) GameWindow.setPanel(new MazePanel());
                        dialogueActive = false;
                        dialogueBox = null;
                    } else {
                        dialogueBox.forceFinish();
                    }
                }
            }
        } else {
            player.keyPressed(e);
        }
    }

    public Player getPlayer() {
        return player;
    }

    public void openDialogue(DialogueBox box) {
        this.dialogueBox = box;
        this.dialogueActive = true;
    }

    private static class Confetti {
        int x, y, life = 60;
        double vy = 1 + Math.random() * 1.5;
        Color col = new Color((int) (Math.random() * 0xFFFFFF));
        Confetti(int x, int y) { this.x = x; this.y = y; }
        void update() { y += vy; life--; }
        void draw(Graphics2D g) { g.setColor(col); g.fillRect(x, y, 4, 4); }
        boolean isAlive() { return life > 0; }
    }
}