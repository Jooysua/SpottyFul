package game;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

public class SpotMazePlayer {
    private int x, y, size = 24;
    private int speed = 4;

    public SpotMazePlayer(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void keyPressed(KeyEvent e, List<Rectangle> walls) {
        int nextX = x, nextY = y;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:    nextY -= speed; break;
            case KeyEvent.VK_DOWN:  nextY += speed; break;
            case KeyEvent.VK_LEFT:  nextX -= speed; break;
            case KeyEvent.VK_RIGHT: nextX += speed; break;
        }

        Rectangle nextBounds = new Rectangle(nextX, nextY, size, size);
        boolean collision = false;
        for (Rectangle wall : walls) {
            if (nextBounds.intersects(wall)) {
                collision = true;
                break;
            }
        }

        if (!collision) {
            x = nextX;
            y = nextY;
        }
    }

    public void draw(Graphics2D g2) {
        g2.setColor(Color.CYAN);
        g2.fillRect(x, y, size, size);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, size, size);
    }
}