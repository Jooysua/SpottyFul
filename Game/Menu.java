import javax.swing.*;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
//https://looka.com/blog/types-of-fonts/ https://www.indeed.com/career-advice/career-development/business-fonts
public class Menu extends JFrame {
    private final MusicPlayer musicPlayer = new MusicPlayer();
    private ImageIcon titleImage;

    public Menu() {
        
        URL imageURL = getClass().getResource("/assets/Title_Screen.jpg");
        if (imageURL != null) {
            titleImage = new ImageIcon(imageURL);
        } else {
            System.out.println("Title image not found."); // so know if image isnt being dtected becasue i dont see it but no error ??
        }

       
        setTitle("Spotty Life");
        setSize(640, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center window
        setResizable(false);

        
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                setBackground(Color.WHITE); // White background
                if (titleImage != null) {
                    // Center the image at top
                    int imgX = (getWidth() - titleImage.getIconWidth()) / 2;
                    g.drawImage(titleImage.getImage(), imgX, 40, this);
                }
            }
        };
        panel.setLayout(null);
        
        JButton startButton = new JButton("START");
        startButton.setFont(new Font("Courier New", Font.BOLD, 24));
        startButton.setBackground(Color.BLACK);
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);
        startButton.setBounds(240, 300, 160, 50);
        startButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                startButton.setForeground(Color.LIGHT_GRAY);
            }

            public void mouseExited(MouseEvent e) {
                startButton.setForeground(Color.WHITE);
            }
        });
        startButton.addActionListener(e -> {
            System.out.println("Starting Spotty Life...");
            musicPlayer.stop();
           
            dispose(); 
        });


        JButton optionsButton = new JButton("OPTIONS");
        optionsButton.setFont(new Font("Courier New", Font.BOLD, 20));
        optionsButton.setBackground(Color.WHITE);
        optionsButton.setForeground(Color.BLACK);
        optionsButton.setFocusPainted(false);
        optionsButton.setBounds(240, 370, 160, 45);
        optionsButton.addActionListener(e -> showOptionsDialog());

        panel.add(startButton);
        panel.add(optionsButton);
        add(panel);

        setVisible(true);

        musicPlayer.play("/assets/menu_music.wav");
    }

    private void showOptionsDialog() {
        JDialog optionsDialog = new JDialog(this, "Options", true);
        optionsDialog.setSize(300, 150);
        optionsDialog.setLocationRelativeTo(this);
        optionsDialog.setLayout(new BorderLayout());

        JLabel volumeLabel = new JLabel("Volume", JLabel.CENTER);
        JSlider volumeSlider = new JSlider(0, 100, 100);
        volumeSlider.setMajorTickSpacing(25);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintLabels(true);

        volumeSlider.addChangeListener(e -> {
            float volume = volumeSlider.getValue() / 100f;
            musicPlayer.setVolume(volume);
        });

        optionsDialog.add(volumeLabel, BorderLayout.NORTH);
        optionsDialog.add(volumeSlider, BorderLayout.CENTER);
        optionsDialog.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Menu::new);
    }
}
