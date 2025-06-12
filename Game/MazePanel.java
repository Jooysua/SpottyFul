package game;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class MazePanel extends JPanel implements Runnable, KeyListener {
    private static final int TILE_SIZE = 32;
    private static final int COLS = 20;
    private static final int ROWS = 15;
    private static final int WIDTH = COLS * TILE_SIZE;
    private static final int HEIGHT = ROWS * TILE_SIZE;

    private final List<Rectangle> mazeWalls = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();
    private Player player;
    private Point goalTile;
    private int score = 0;
    private int level = 1;
    private Thread gameThread;
    private final Random random = new Random();

    public MazePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addKeyListener(this);
        player = new Player(32, 32);
        setupMaze();
        spawnGoalTile();
        spawnEnemies();
        gameThread = new Thread(this, "MazePanelThread");
        gameThread.start();
    }

    private void setupMaze() {
        mazeWalls.clear();
        int[][] layout = {
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
            {1,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,1,1},
            {1,0,1,1,1,0,1,0,1,1,1,0,1,0,1,0,1,0,0,1},
            {1,0,1,0,1,0,0,0,1,0,0,0,0,0,1,0,0,1,0,1},
            {1,0,1,0,1,1,1,1,1,0,1,1,0,1,1,1,0,1,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,1,0,1,1,0,1,0,1,1,1,0,1,1,1,1,0,1,1,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1},
            {1,0,1,1,1,1,1,1,1,1,0,1,1,0,1,1,1,0,0,1},
            {1,0,1,0,1,0,1,0,0,0,0,0,1,0,1,0,1,1,0,1},
            {1,0,1,0,1,0,1,0,1,1,1,0,1,0,1,0,0,1,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,1,0,1,0,1,1,0,1,0,1,0,1,1,0,1,1,0,1,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}
        };
        for (int row = 0; row < layout.length; row++) {
            for (int col = 0; col < layout[row].length; col++) {
                if (layout[row][col] == 1) {
                    mazeWalls.add(new Rectangle(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE));
                }
            }
        }
    }

    private void spawnGoalTile() {
        int gx, gy;
        do {
            gx = random.nextInt(COLS) * TILE_SIZE;
            gy = random.nextInt(ROWS) * TILE_SIZE;
        } while (collidesWithWall(new Rectangle(gx, gy, TILE_SIZE, TILE_SIZE)));
        goalTile = new Point(gx, gy);
    }

    private void spawnEnemies() {
        enemies.clear();
        for (int i = 0; i < level; i++) {
            int ex, ey;
            do {
                ex = random.nextInt(COLS) * TILE_SIZE;
                ey = random.nextInt(ROWS) * TILE_SIZE;
            } while (collidesWithWall(new Rectangle(ex, ey, TILE_SIZE, TILE_SIZE)));
            Enemy enemy = new Enemy(ex, ey);
            enemy.setLevel(level);
            enemies.add(enemy);
        }
    }

    private boolean collidesWithWall(Rectangle r) {
        for (Rectangle w : mazeWalls)
            if (w.intersects(r)) return true;
        return false;
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
        player.update(mazeWalls);
        Rectangle pBox = new Rectangle(player.getX(), player.getY(), player.getWidth(), player.getHeight());
        Rectangle gBox = new Rectangle(goalTile.x, goalTile.y, TILE_SIZE, TILE_SIZE);

        if (pBox.intersects(gBox)) {
            score++;
            if (score >= 3) {
                GameState.mazeCompleted = true;
                GameWindow.setPanel(new GamePanel());
                return;
            }
            level++;
            spawnGoalTile();
            spawnEnemies();
        }

        for (Enemy e : enemies) {
            e.update(mazeWalls, player);
            Rectangle eBox = e.getBounds();
            if (pBox.intersects(eBox)) {
                score = 0;
                level = 1;
                player = new Player(32, 32);
                spawnGoalTile();
                spawnEnemies();
                return;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        g2.setColor(Color.GRAY);
        for (Rectangle w : mazeWalls) g2.fill(w);

        g2.setColor(Color.YELLOW);
        g2.fillRect(goalTile.x, goalTile.y, TILE_SIZE, TILE_SIZE);

        for (Enemy e : enemies) e.draw(g2);
        player.draw(g2);

        g2.setColor(Color.WHITE);
        g2.drawString("Hack Level " + level + " | Score: " + score + "/3", 10, 20);
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyPressed(KeyEvent e) { player.keyPressed(e); }
    @Override public void keyReleased(KeyEvent e) { player.keyReleased(e); }
}