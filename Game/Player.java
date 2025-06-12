package game;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

public class Player {
    private int x, y;
    private int speed = 2;
    private boolean up, down, left, right;
    private BufferedImage spriteSheet;
    private int frame = 0;
    private int direction = 0;
    private int frameCounter = 0;
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return SPRITE_WIDTH; }
    public int getHeight() { return SPRITE_HEIGHT; }

    private final int SPRITE_WIDTH = 24;
    private final int SPRITE_HEIGHT = 32;

    public Player(int x, int y) {
        this.x = x;
        this.y = y;
        try {
            spriteSheet = ImageIO.read(getClass().getResourceAsStream("/assets/spot_sprites.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 🧠 Update Spotty with wall collision logic
    public void update(List<Rectangle> walls) {
        boolean moving = false;

        int nextX = x;
        int nextY = y;

        if (up) {
            nextY -= speed;
            direction = 3;
            moving = true;
        }
        if (down) {
            nextY += speed;
            direction = 0;
            moving = true;
        }
        if (left) {
            nextX -= speed;
            direction = 1;
            moving = true;
        }
        if (right) {
            nextX += speed;
            direction = 2;
            moving = true;
        }

        Rectangle nextBounds = new Rectangle(nextX, nextY, SPRITE_WIDTH, SPRITE_HEIGHT);
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

        if (moving) {
            frameCounter++;
            if (frameCounter >= 10) {
                frame = (frame + 1) % 4;
                frameCounter = 0;
            }
        } else {
            frame = 0;
        }
    }

    public void draw(Graphics2D g2) {
    if (spriteSheet != null) {
        // 🕶 Draw soft shadow under Spotty
        g2.setColor(new Color(0, 0, 0, 120)); // transparent black
        g2.fillOval(x - 2, y + SPRITE_HEIGHT - 6, SPRITE_WIDTH + 4, 10);

        // 🐾 Draw Spotty sprite
        int sx = frame * SPRITE_WIDTH;
        int sy = direction * SPRITE_HEIGHT;
        g2.drawImage(spriteSheet, x, y, x + SPRITE_WIDTH, y + SPRITE_HEIGHT,
                sx, sy, sx + SPRITE_WIDTH, sy + SPRITE_HEIGHT, null);
    }
}

    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_UP) up = true;
        if (code == KeyEvent.VK_DOWN) down = true;
        if (code == KeyEvent.VK_LEFT) left = true;
        if (code == KeyEvent.VK_RIGHT) right = true;
    }

    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_UP) up = false;
        if (code == KeyEvent.VK_DOWN) down = false;
        if (code == KeyEvent.VK_LEFT) left = false;
        if (code == KeyEvent.VK_RIGHT) right = false;
    }
    
    public void resetKeys() {
    up = false;
    down = false;
    left = false;
    right = false;
}
}