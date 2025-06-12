package game;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Battery {
    private int x, y;
    private boolean pickedUp = false;
    private boolean active = true;
    private BufferedImage sprite;

    public Battery(int x, int y) {
        this.x = x;
        this.y = y;
        try {
            sprite = ImageIO.read(getClass().getResourceAsStream("/assets/battery.png"));
        } catch (IOException e) {
            System.err.println("Battery sprite missing!");
        }
    }

    public void draw(Graphics2D g2, int playerX, int playerY) {
        if (!active) return;

        if (pickedUp && sprite != null) {
            // Draw above Spot's head!
            g2.drawImage(sprite, playerX, playerY - 34, 24, 32, null);
        } else if (sprite != null) {
            g2.drawImage(sprite, x, y, 24, 32, null);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, 24, 32);
    }

    public boolean isPickedUp() {
        return pickedUp;
    }

    public void pickUp() {
        pickedUp = true;
    }

    public void deactivate() {
        active = false;
    }

    public boolean isActive() {
        return active;
    }
}