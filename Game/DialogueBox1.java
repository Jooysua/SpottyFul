import java.awt.*;
import java.util.List;

public class DialogueBox {
    private String fullText;
    private String displayedText = "";
    private int charIndex = 0;
    private int frameDelay = 2;
    private int frameCount = 0;
    private boolean isComplete = false;
    private boolean inActMenu = false;
    private boolean showingSleepMessage = false;
    private boolean waitingForFinalEnter = false;
// removing act stuff and mercy dont have code for it to do anything dont have time will move one 
    private final List<String> mainOptions = List.of("Bite", "Act"); //"Act", "Mercy"
    private final List<String> actOptions = List.of("Sleep"); //"Beg", "Stare", "Sleep", "Sniff"

    private int selectedOption = 0;

    private final List<String> sleepMessages = List.of(
        "Cletus doesn't want to be rude. He watches you nap.",
        "You feel refreshed."
    );
    private int sleepMessageIndex = 0;

    public DialogueBox(String text) {
        this.fullText = text;
    }

    public void setText(String text) {
        this.fullText = text;
        this.displayedText = "";
        this.charIndex = 0;
        this.frameCount = 0;
        this.isComplete = false;
    }

    public void update() {
        if (!isComplete) {
            frameCount++;
            if (frameCount >= frameDelay && charIndex < fullText.length()) {
                displayedText += fullText.charAt(charIndex++);
                frameCount = 0;
            }
            if (charIndex >= fullText.length()) {
                isComplete = true;
            }
        }
    }

    public void draw(Graphics2D g2, int panelWidth, int panelHeight) {
        int boxWidth = panelWidth - 40;
        int boxHeight = 120;
        int x = 20;
        int y = panelHeight - boxHeight - 20;

        g2.setColor(new Color(0, 0, 0, 200));
        g2.fillRect(x, y, boxWidth, boxHeight);
        g2.setColor(Color.WHITE);
        g2.drawRect(x, y, boxWidth, boxHeight);

        g2.setFont(new Font("Courier New", Font.PLAIN, 18));
        g2.setColor(Color.WHITE);
        g2.drawString(displayedText, x + 10, y + 30);

        if (isComplete && !showingSleepMessage) {
            List<String> options = inActMenu ? actOptions : mainOptions;
            int cols = 2;
            int colSpacing = 180;
            int rowSpacing = 20;
            for (int i = 0; i < options.size(); i++) {
                int col = i % cols;
                int row = i / cols;
                int optX = x + 30 + col * colSpacing;
                int optY = y + 60 + row * rowSpacing;

                if (i == selectedOption) {
                    g2.setColor(Color.YELLOW);
                    g2.drawString("> " + options.get(i), optX, optY);
                } else {
                    g2.setColor(Color.WHITE);
                    g2.drawString("  " + options.get(i), optX, optY);
                }
            }
        }
    }

    public void nextOption() {
        List<String> options = inActMenu ? actOptions : mainOptions;
        if (isComplete) {
            selectedOption = (selectedOption + 1) % options.size();
        }
    }

    public void prevOption() {
        List<String> options = inActMenu ? actOptions : mainOptions;
        if (isComplete) {
            selectedOption = (selectedOption - 1 + options.size()) % options.size();
        }
    }

    public void downOption() {
        List<String> options = inActMenu ? actOptions : mainOptions;
        if (isComplete) {
            selectedOption = (selectedOption + 2) % options.size();
        }
    }

    public void upOption() {
        List<String> options = inActMenu ? actOptions : mainOptions;
        if (isComplete) {
            selectedOption = (selectedOption - 2 + options.size()) % options.size();
        }
    }

    public int getSelectedOption() {
        return selectedOption;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void forceFinish() {
        displayedText = fullText;
        isComplete = true;
    }

    public boolean isInActMenu() {
        return inActMenu;
    }

    public void enterActMenu() {
        inActMenu = true;
        selectedOption = 0;
    }

    public void exitActMenu() {
        inActMenu = false;
        selectedOption = 0;
    }

    public String getCurrentSelection() {
        return (inActMenu ? actOptions : mainOptions).get(selectedOption);
    }

    public boolean selectedSleepInAct() {
        return inActMenu && actOptions.get(selectedOption).equals("Sleep");
    }

    public void showSleepMessage() {
        sleepMessageIndex = 0;
        showingSleepMessage = true;
        waitingForFinalEnter = false;
        setText(sleepMessages.get(sleepMessageIndex));
    }

    public boolean advanceSleepMessage() {
        if (showingSleepMessage && isComplete) {
            sleepMessageIndex++;
            if (sleepMessageIndex < sleepMessages.size()) {
                setText(sleepMessages.get(sleepMessageIndex));
                return true;
            } else {
                waitingForFinalEnter = true;
                return false;
            }
        }
        return false;
    }

    public boolean isShowingSleepMessage() {
        return showingSleepMessage;
    }

    public void doneShowingSleepMessage() {
        showingSleepMessage = false;
        waitingForFinalEnter = false;
        exitActMenu();
    }

    public boolean waitForFinalEnter() {
        return waitingForFinalEnter;
    }
    
    
}
