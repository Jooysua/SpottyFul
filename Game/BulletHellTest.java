import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.*;
import javax.sound.sampled.*;

public class BulletHellTest extends JPanel implements ActionListener, KeyListener {
    private javax.swing.Timer timer;
    private boolean w, a, s, d, onGround;
    private final int playerSize = 16;
    private int playerX, playerY;
    private int velocityY = 0;
    private final int GRAVITY = 1;
    private final int JUMP_STRENGTH = -12;
    private final Rectangle battleBox = new Rectangle(220, 160, 200, 150);
    private final int bulletSize = 10;
    private ArrayList<Bullet> bullets = new ArrayList<>();
    private ArrayList<Stick> sticks = new ArrayList<>();
    private Random rand = new Random();
    private String[] directions = {"down", "left", "right", "up"};
    private int currentDirectionIndex = 0;
    private long lastDirectionChangeTime = System.currentTimeMillis();
    private Image cletusImage;
    private Image backgroundImage;
    private DialogueBox dialogueBox;
    private boolean showDialogue = true;
    private boolean bossAttacking = false;
    private long attackStartTime;
    private int phase = 1;
    private int bossHealth = 10;
    private int maxHealth = 100;
    private int currentHealth = 100;
    private boolean phase2LeftAttack = true;
    private long phase2SwitchTime;
    private boolean invincible = false;
    private long lastHitTime = 0;
    private final int iFrameDuration = 500;
    private boolean waitingAfterSleep = false;
    private JFrame frame;

    public BulletHellTest(JFrame frame) {
        this.frame = frame;
        setPreferredSize(new Dimension(640, 480));
        setFocusable(true);
        addKeyListener(this);

        URL imgURL = getClass().getResource("/assets/cletus.png");
        if (imgURL != null) cletusImage = new ImageIcon(imgURL).getImage();

        URL bgURL = getClass().getResource("/assets/flame background.gif");
        if (bgURL != null) backgroundImage = new ImageIcon(bgURL).getImage();

        playBackgroundMusic();
        centerPlayer();
        timer = new javax.swing.Timer(15, this);
        timer.start();
        dialogueBox = new DialogueBox("What will you do?");
    }

    private void playBackgroundMusic() {
        try {
            URL soundURL = getClass().getResource("/assets/spot 3.wav");
            if (soundURL == null) return;
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundURL);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playHitSound() {
        try {
            URL soundURL = getClass().getResource("/assets/hit.wav");
            if (soundURL == null) return;
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundURL);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void centerPlayer() {
        playerX = battleBox.x + battleBox.width / 2 - playerSize / 2;
        playerY = battleBox.y + battleBox.height / 2 - playerSize / 2;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (currentHealth <= 0) {
            timer.stop();
            frame.setContentPane(new GameOverMenu(frame));
            frame.revalidate();
            return;
        }

        if (bossHealth <= 0) {
            timer.stop();
            frame.setContentPane(new VictoryMenu(frame));
            frame.revalidate();
            return;
        }
        
        // cant get stick to work so im swaping the order 
        if (bossHealth >= 8) phase = 3;
        else if (bossHealth >= 5) phase = 4;
        else if (bossHealth >= 2) phase = 1;
        else phase = 2;

        if (dialogueBox.isShowingSleepMessage()) {
            dialogueBox.update();
        } else if (waitingAfterSleep) {
            // waiting for player to press enter
        } else if (showDialogue) {
            dialogueBox.update();
        } else if (bossAttacking) {
            long now = System.currentTimeMillis();
            if (phase == 3 || phase == 4) {
                if (now - phase2SwitchTime >= 4000) {
                    phase2LeftAttack = !phase2LeftAttack;
                    phase2SwitchTime = now;
                    if (!phase2LeftAttack && (phase == 3 || phase == 4)) {

                        bossAttacking = false;
                        showDialogue = true;
                        dialogueBox = new DialogueBox("What will you do?");
                        sticks.clear();
                        centerPlayer();
                        velocityY = 0;
                    }
                }
                updatePlayerPlatformer();
                spawnSticks();
                updateSticks();
            } else {
                updatePlayerPlatformer();
                if (now - attackStartTime > 10000) {
                    bossAttacking = false;
                    showDialogue = true;
                    dialogueBox = new DialogueBox("What will you do?");
                    bullets.clear();
                    centerPlayer();
                } else {
                    updateDirectionCycle();
                    spawnBullets();
                    updateBullets();
                }
            }
        }

        if (invincible && System.currentTimeMillis() - lastHitTime > iFrameDuration) {
            invincible = false;
        }

        repaint();
    }

    private void updateDirectionCycle() {
        long now = System.currentTimeMillis();
        if (now - lastDirectionChangeTime > 2000) {
            currentDirectionIndex = (currentDirectionIndex + 1) % directions.length;
            lastDirectionChangeTime = now;
        }
    }

    private void updatePlayerPlatformer() {
        int speed = 3;
        if (phase == 3 || phase == 4) {
            if (a && playerX > battleBox.x) playerX -= speed;
            if (d && playerX + playerSize < battleBox.x + battleBox.width) playerX += speed;
            velocityY += GRAVITY;
            playerY += velocityY;
            if (playerY + playerSize >= battleBox.y + battleBox.height) {
                playerY = battleBox.y + battleBox.height - playerSize;
                velocityY = 0;
                onGround = true;
            } else {
                onGround = false;
            }
        } else {
            int nextX = playerX + (d ? speed : 0) - (a ? speed : 0);
            int nextY = playerY + (s ? speed : 0) - (w ? speed : 0);
            if (battleBox.contains(nextX, playerY, playerSize, playerSize)) playerX = nextX;
            if (battleBox.contains(playerX, nextY, playerSize, playerSize)) playerY = nextY;
        }
    }

    private void spawnBullets() {
        int spawnChance = (phase == 1) ? 24 : 12;
        int speed = (phase == 1) ? 2 : 3;
        if (rand.nextInt(spawnChance) == 0) {
            String dir = directions[currentDirectionIndex];
            switch (dir) {
                case "down" -> bullets.add(new Bullet(battleBox.x + rand.nextInt(battleBox.width - bulletSize), battleBox.y, 0, speed));
                case "up" -> bullets.add(new Bullet(battleBox.x + rand.nextInt(battleBox.width - bulletSize), battleBox.y + battleBox.height - bulletSize, 0, -speed));
                case "left" -> bullets.add(new Bullet(battleBox.x + battleBox.width - bulletSize, battleBox.y + rand.nextInt(battleBox.height - bulletSize), -speed, 0));
                case "right" -> bullets.add(new Bullet(battleBox.x, battleBox.y + rand.nextInt(battleBox.height - bulletSize), speed, 0));
            }
        }
    }

    private void updateBullets() {
        Rectangle playerRect = new Rectangle(playerX, playerY, playerSize, playerSize);
        Iterator<Bullet> it = bullets.iterator();
        while (it.hasNext()) {
            Bullet b = it.next();
            b.x += b.dx;
            b.y += b.dy;
            Rectangle bulletRect = new Rectangle(b.x, b.y, bulletSize, bulletSize);
            if (bulletRect.intersects(playerRect)) {
                if (!invincible) {
                    currentHealth -= 2;
                    invincible = true;
                    lastHitTime = System.currentTimeMillis();
                    playHitSound();
                }
                it.remove();
            } else if (!battleBox.intersects(bulletRect)) {
                it.remove();
            }
        }
    }

    private void spawnSticks() {
        if (rand.nextInt(60) == 0) {
            int stickHeight = 30;
            int y = battleBox.y + battleBox.height - stickHeight;
            int x = phase2LeftAttack ? battleBox.x : battleBox.x + battleBox.width;
            int dx = (phase == 4) ? 2 : 1;
            if (!phase2LeftAttack) dx = -dx;
            sticks.add(new Stick(x, y, dx));
        }
    }

    private void updateSticks() {
        Rectangle playerRect = new Rectangle(playerX, playerY, playerSize, playerSize);
        Iterator<Stick> it = sticks.iterator();
        while (it.hasNext()) {
            Stick s = it.next();
            s.x += s.dx;
            Rectangle stickRect = new Rectangle(s.x, s.y, 10, 30);
            if (stickRect.intersects(playerRect)) {
                if (!invincible) {
                    currentHealth -= 4;
                    invincible = true;
                    lastHitTime = System.currentTimeMillis();
                    playHitSound();
                }
                it.remove();
            } else if (!battleBox.intersects(stickRect)) {
                it.remove();
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        if (backgroundImage != null) g2.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        g2.setColor(Color.BLACK);
        g2.fillRect(battleBox.x, battleBox.y, battleBox.width, battleBox.height);
        g2.setColor(Color.WHITE);
        g2.drawRect(battleBox.x, battleBox.y, battleBox.width, battleBox.height);
        g2.setColor(Color.CYAN);
        g2.fillRect(playerX, playerY, playerSize, playerSize);
        g2.setColor(Color.RED);
        for (Bullet b : bullets) g2.fillOval(b.x, b.y, bulletSize, bulletSize);
        g2.setColor(Color.ORANGE);
        for (Stick s : sticks) g2.fillRect(s.x, s.y, 10, 30);
        if (cletusImage != null) g2.drawImage(cletusImage, 470, 180, 120, 160, this);
        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(20, 20, 200, 20);
        g2.setColor(Color.GREEN);
        g2.fillRect(20, 20, (int) (200.0 * currentHealth / maxHealth), 20);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Courier New", Font.PLAIN, 14));
        g2.drawString("HP: " + currentHealth + " / " + maxHealth, 20, 15);
        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(20, 45, 200, 20);
        g2.setColor(Color.MAGENTA);
        g2.fillRect(20, 45, 20 * bossHealth, 20);
        g2.setColor(Color.WHITE);
        g2.drawString("Boss HP: " + bossHealth + " / 10", 20, 60);
        g2.drawString("Phase: " + phase, 250, 60);
        if (showDialogue || dialogueBox.isShowingSleepMessage() || waitingAfterSleep) {
            dialogueBox.draw(g2, getWidth(), getHeight());
        }
    }

    private static class Bullet {
        int x, y, dx, dy;
        public Bullet(int x, int y, int dx, int dy) {
            this.x = x; this.y = y; this.dx = dx; this.dy = dy;
        }
    }

    private static class Stick {
        int x, y, dx;
        public Stick(int x, int y, int dx) {
            this.x = x; this.y = y; this.dx = dx;
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (dialogueBox.isShowingSleepMessage()) {
            if (!dialogueBox.advanceSleepMessage()) {
                waitingAfterSleep = true;
                dialogueBox.doneShowingSleepMessage();
            }
            return;
        }

        if (waitingAfterSleep && e.getKeyCode() == KeyEvent.VK_ENTER) {
            waitingAfterSleep = false;
            showDialogue = false;
            bossAttacking = true;
            attackStartTime = System.currentTimeMillis();
            if (phase == 3 || phase == 4) phase2SwitchTime = attackStartTime;
            bullets.clear();
            sticks.clear();
            centerPlayer();
            velocityY = 0;
            return;
        }

        if (showDialogue && dialogueBox.isComplete()) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT, KeyEvent.VK_A -> dialogueBox.prevOption();
                case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> dialogueBox.nextOption();
                case KeyEvent.VK_UP, KeyEvent.VK_W -> dialogueBox.upOption();
                case KeyEvent.VK_DOWN, KeyEvent.VK_S -> dialogueBox.downOption();
                case KeyEvent.VK_ENTER -> {
                    if (!dialogueBox.isInActMenu()) {
                        String selected = dialogueBox.getCurrentSelection();
                        switch (selected) {
                            case "Bite" -> bossHealth = Math.max(0, bossHealth - 1);
                            case "Act" -> {
                                dialogueBox.enterActMenu();
                                return;
                            }
                        } 
                        showDialogue = false;
                        bossAttacking = true;
                        attackStartTime = System.currentTimeMillis();
                        if (phase == 3 || phase == 4) phase2SwitchTime = attackStartTime;
                        bullets.clear();
                        sticks.clear();
                        centerPlayer();
                        velocityY = 0;
                    } else {
                        String subOption = dialogueBox.getCurrentSelection();
                        if ("Sleep".equals(subOption)) {
                            currentHealth = Math.min(currentHealth + 10, maxHealth);
                            dialogueBox.showSleepMessage();
                            return;
                        }
                        dialogueBox.exitActMenu();
                        showDialogue = false;
                        bossAttacking = true;
                        attackStartTime = System.currentTimeMillis();
                        if (phase == 3 || phase == 4) phase2SwitchTime = attackStartTime;
                        bullets.clear();
                        sticks.clear();
                        centerPlayer();
                        velocityY = 0;
                    }
                }
            }
        } else if ((phase == 3 || phase == 4)) {
            if (e.getKeyChar() == 'w' && onGround) {
                velocityY = JUMP_STRENGTH;
                onGround = false;
            }
            if (e.getKeyChar() == 'a') a = true;
            if (e.getKeyChar() == 'd') d = true;
        } else {
            if (e.getKeyChar() == 'w') w = true;
            if (e.getKeyChar() == 'a') a = true;
            if (e.getKeyChar() == 's') s = true;
            if (e.getKeyChar() == 'd') d = true;
        }
    }

    @Override public void keyReleased(KeyEvent e) {
        if (e.getKeyChar() == 'w') w = false;
        if (e.getKeyChar() == 'a') a = false;
        if (e.getKeyChar() == 's') s = false;
        if (e.getKeyChar() == 'd') d = false;
    }

    @Override public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Bullet Hell Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new BulletHellTest(frame));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
