package game;

import java.awt.*;
import java.util.*;
import java.util.List;

public class Enemy {
    private int x, y;
    private final int SIZE = 32;
    private int level = 1;
    private int moveCooldown = 0;
    private List<Point> path = new ArrayList<>();

    public Enemy(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void update(List<Rectangle> walls, Player player) {
    if (moveCooldown > 0) {
        moveCooldown--;
        return;
    }

    if (path.isEmpty() || new Random().nextInt(10) < 2) {
        path = calculatePath(walls, player);
    }

    if (!path.isEmpty()) {
        Point next = path.remove(0);
        x = next.x;
        y = next.y;
    }

    // Clamp bounds
    x = Math.max(0, Math.min(x, 19 * SIZE));
    y = Math.max(0, Math.min(y, 14 * SIZE));

    // Slow down level 3 enemy a bit
    if (level == 3) {
        moveCooldown = 6;  // was 4
    } else {
        moveCooldown = Math.max(10 - level * 2, 3);
    }
}

    private List<Point> calculatePath(List<Rectangle> walls, Player player) {
        boolean[][] visited = new boolean[15][20];
        Map<Point, Point> cameFrom = new HashMap<>();
        Queue<Point> queue = new LinkedList<>();
        List<Point> finalPath = new ArrayList<>();

        Point start = new Point(x, y);
        Point goal = new Point(player.getX(), player.getY());

        queue.add(start);
        visited[y / SIZE][x / SIZE] = true;

        while (!queue.isEmpty()) {
            Point current = queue.poll();

            if (current.equals(goal)) break;

            for (Point dir : directions()) {
                int nx = current.x + dir.x * SIZE;
                int ny = current.y + dir.y * SIZE;

                if (inBounds(nx, ny) && !visited[ny / SIZE][nx / SIZE]) {
                    Rectangle nextBox = new Rectangle(nx, ny, SIZE, SIZE);
                    if (!collides(nextBox, walls)) {
                        Point neighbor = new Point(nx, ny);
                        queue.add(neighbor);
                        visited[ny / SIZE][nx / SIZE] = true;
                        cameFrom.put(neighbor, current);
                    }
                }
            }
        }

        Point step = goal;
        if (!cameFrom.containsKey(goal)) return finalPath;

        // Rebuild path backwards
        while (!step.equals(start)) {
            finalPath.add(0, step); // Add to front
            step = cameFrom.get(step);
        }

        return finalPath;
    }

    private boolean inBounds(int x, int y) {
        return x >= 0 && x < 640 && y >= 0 && y < 480;
    }

    private List<Point> directions() {
        return List.of(
            new Point(0, -1), new Point(1, 0),
            new Point(0, 1), new Point(-1, 0)
        );
    }

    private boolean collides(Rectangle r, List<Rectangle> walls) {
        for (Rectangle wall : walls)
            if (wall.intersects(r)) return true;
        return false;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, SIZE, SIZE);
    }

    public void draw(Graphics2D g2) {
        g2.setColor(Color.RED);
        g2.fillRect(x, y, SIZE, SIZE);
    }

    public int getX() { return x; }
    public int getY() { return y; }
}