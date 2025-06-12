import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GameOverMenu extends JPanel {

    public GameOverMenu(JFrame frame) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.BLACK);

        JLabel label = new JLabel("You got knocked out");
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Courier New", Font.BOLD, 24));

        JButton retryButton = new JButton("Retry");
        retryButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        retryButton.setFocusPainted(false);
        retryButton.setFont(new Font("Courier New", Font.PLAIN, 16));
        retryButton.addActionListener(e -> {
            frame.setContentPane(new BulletHellTest(frame));
            frame.revalidate();
            frame.repaint();
        });

        JButton leaveButton = new JButton("Leave");
        leaveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        leaveButton.setFocusPainted(false);
        leaveButton.setFont(new Font("Courier New", Font.PLAIN, 16));
        leaveButton.addActionListener(e -> System.exit(0));

        add(Box.createVerticalGlue());
        add(label);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(retryButton);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(leaveButton);
        add(Box.createVerticalGlue());
    }
    // dont know how to implement it bujt we (you) can prob figure it out :) same as victory menu made the leave option thats should
    //trigger to your game but might change my entire cletus fight so that the thinggy does the thing with your thing
    //p.s might have to remove main ? i dunno im assuming its like the menu call codes 
}
