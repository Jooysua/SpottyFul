package game;
import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class SoundPlayer {
    public static void playSound(String path) {
        try {
            URL soundURL = SoundPlayer.class.getResource(path);
            if (soundURL == null) {
                System.err.println("Sound not found: " + path);
                return;
            }
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundURL);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();

            // If it's the door slide, stop after 1.5 seconds (1500 ms)
            if (path.contains("door_slide")) {
                new java.util.Timer().schedule(new java.util.TimerTask() {
                    @Override
                    public void run() {
                        clip.stop();
                        clip.close();
                    }
                }, 1500);
            }

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error playing sound: " + e.getMessage());
        }
    }
}