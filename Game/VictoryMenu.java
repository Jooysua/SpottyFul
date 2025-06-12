import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
// https://looka.com/blog/types-of-fonts/ https://www.indeed.com/career-advice/career-development/business-fonts
public class VictoryMenu extends JPanel {
    public VictoryMenu(JFrame frame) {
        setLayout(new GridBagLayout());
        setBackground(Color.BLACK);

        JLabel label = new JLabel("Victory!");
        label.setFont(new Font("Courier New", Font.BOLD, 32));
        label.setForeground(Color.GREEN);

        JButton leaveButton = new JButton("Leave");
        leaveButton.setFont(new Font("Courier New", Font.PLAIN, 20));
        leaveButton.addActionListener(e -> System.exit(0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);

        gbc.gridy = 0;
        add(label, gbc);

        gbc.gridy = 1;
        add(leaveButton, gbc);
    }
}
