package game;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DialogueBox {

    /* ---------- page handling ---------- */
    private final List<String> pages = new ArrayList<>();
    private final List<Integer> pageDelays = new ArrayList<>();
    private int currentPage = 0;

    /* ---------- typing effect ---------- */
    private String fullText;          // text of current page
    private String displayedText = "";/* what is visible  */
    private int charIndex  = 0;
    private int frameDelay = 2;       // per-char delay
    private int frameCount = 0;

    /* ---------- state ---------- */
    private boolean isComplete = false;
    private final List<String> options;
    private int selectedOption = 0;

    /* === constructors === */

    // old 1-page constructor (compatibility)
    public DialogueBox(String text, String... opts) {
        this(new String[]{text}, null, opts);
    }
    public DialogueBox(String[] texts) {
    this(texts, null, new String[]{});
    }

    // new multi-page constructor
    public DialogueBox(String[] texts, int[] delays, String... opts) {
        pages.addAll(Arrays.asList(texts));
        options = List.of(opts);

        // fill delays (default 2 if not supplied)
        for (int i = 0; i < pages.size(); i++) {
            int d = (delays != null && i < delays.length) ? delays[i] : 2;
            pageDelays.add(d);
        }
        loadPage(0);
    }

    /* ---------- page loader ---------- */
    private void loadPage(int index) {
        currentPage   = index;
        fullText      = pages.get(index);
        displayedText = "";
        charIndex     = 0;
        frameDelay    = pageDelays.get(index);
        frameCount    = 0;
        // do not mark complete yet
    }

    /* ---------- update/typing ---------- */
    public void update() {
        if (isComplete) return;

        frameCount++;
        if (charIndex < fullText.length() && frameCount >= frameDelay) {
            displayedText += fullText.charAt(charIndex++);
            frameCount = 0;
        }

        if (charIndex >= fullText.length() && currentPage == pages.size() - 1) {
            isComplete = true; // last page fully typed
        }
    }

    /* ---------- drawing ---------- */
    public void draw(Graphics2D g2, int panelWidth, int panelHeight) {
        int boxW = panelWidth - 40;
        int boxH = 100;
        int x = 20, y = panelHeight - boxH - 20;

        g2.setColor(Color.BLACK);
        g2.fillRect(x, y, boxW, boxH);
        g2.setColor(Color.WHITE);
        g2.drawRect(x, y, boxW, boxH);

        g2.setFont(new Font("Courier New", Font.PLAIN, 18));
        g2.drawString(displayedText, x + 10, y + 30);

        // options only on final page & after typing done
        if (isComplete) {
            for (int i = 0; i < options.size(); i++) {
                String prefix = (i == selectedOption) ? "> " : "  ";
                g2.drawString(prefix + options.get(i), x + 20, y + 60 + i * 20);
            }
        }
    }

    /* ---------- navigation ---------- */
    public void nextOption() {
        if (isComplete && !options.isEmpty())
            selectedOption = (selectedOption + 1) % options.size();
    }

    public void prevOption() {
        if (isComplete && !options.isEmpty())
            selectedOption = (selectedOption - 1 + options.size()) % options.size();
    }

    public int getSelectedOption() { return selectedOption; }
    public boolean isComplete()    { return isComplete; }

    /* ---------- force-finish / next ---------- */
    public void forceFinish() {
        if (charIndex < fullText.length()) {
            // finish current line instantly
            displayedText = fullText;
            charIndex = fullText.length();
        } else if (currentPage < pages.size() - 1) {
            // advance to next page
            loadPage(currentPage + 1);
        } else {
            // last page already finished
            isComplete = true;
        }
    }
}