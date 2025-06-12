package game;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/** Hyper-realistic metal door that can slide upward. */
public class MetalDoor {
    private int x, y;
    private boolean powered = false;
    private boolean lifted  = false;
    private BufferedImage image;

    public MetalDoor(int x, int y) {
        this.x = x;
        this.y = y;
        try {
            image = ImageIO.read(getClass().getResourceAsStream(
                     "/assets/door_metallic.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void draw(Graphics2D g) {
        if (!lifted && image != null)
            g.drawImage(image, x, y, null);
    }

    public void draw(Graphics2D g, int offsetY) {
        if (!lifted && image != null)
            g.drawImage(image, x, y - offsetY, null);
    }

    public void lift() { lifted = true; }

    public void powerUp() { powered = true; }

    public boolean isPowered() { return powered; }

    public Rectangle getBounds() {
        if (image == null) return new Rectangle(x, y, 32, 64);
        return new Rectangle(x, y, image.getWidth(), image.getHeight());
    }

    public int getX() { return x; }

    public int getY() { return y; }
}