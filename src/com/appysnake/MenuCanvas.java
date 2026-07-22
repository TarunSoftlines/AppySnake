package com.appysnake;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Font;

public class MenuCanvas extends Canvas {

    private SnakeMIDlet midlet;
    private int selectedIndex = 0; // 0: PLAY, 1: LOAD, 2: SETTINGS
    private final String[] menuOptions = {"PLAY", "LOAD", "SETTINGS"};

    public MenuCanvas(SnakeMIDlet midlet) {
        this.midlet = midlet;
        setFullScreenMode(true);
    }

    protected void paint(Graphics g) {
        int width = getWidth();
        int height = getHeight();

        // Clean background matching artwork mockup
        g.setColor(0xFFFFFF);
        g.fillRect(0, 0, width, height);

        // Header Title
        g.setColor(0x000000);
        Font titleFont = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_LARGE);
        g.setFont(titleFont);
        g.drawString("APPY SNAKE", width / 2, 20, Graphics.HCENTER | Graphics.TOP);

        // Draw Menu Items
        Font menuFont = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_MEDIUM);
        g.setFont(menuFont);

        int startY = height / 3;
        int spacing = 38;

        for (int i = 0; i < menuOptions.length; i++) {
            int optionY = startY + (i * spacing);
            String label = menuOptions[i];

            if (i == 2) { // Settings toggle display
                boolean soundOn = midlet.getAudioManager().isSoundOn();
                label = "SOUND: " + (soundOn ? "ON" : "OFF");
            }

            if (i == selectedIndex) {
                // Highlight selection box
                g.setColor(0xCC2222); // Red theme highlight
                g.fillRect(width / 2 - 80, optionY - 4, 160, 30);
                g.setColor(0xFFFFFF); // Text color on selection
            } else {
                g.setColor(0x000000); // Standard text color
            }

            g.drawString(label, width / 2, optionY, Graphics.HCENTER | Graphics.TOP);
        }

        // Footer copyright note matching your mockup layout
        Font footerFont = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        g.setFont(footerFont);
        g.setColor(0x000000);
        g.drawString("APPYSNAKE © 2026.", width / 2, height - 15, Graphics.HCENTER | Graphics.BOTTOM);
    }

    protected void keyPressed(int keyCode) {
        int action = getGameAction(keyCode);

        if (action == UP || keyCode == KEY_NUM2) {
            selectedIndex--;
            if (selectedIndex < 0) selectedIndex = menuOptions.length - 1;
            repaint();
        } else if (action == DOWN || keyCode == KEY_NUM8) {
            selectedIndex++;
            if (selectedIndex >= menuOptions.length) selectedIndex = 0;
            repaint();
        } else if (action == FIRE || keyCode == KEY_NUM5 || keyCode == -5) {
            executeSelection();
        }
    }

    protected void pointerPressed(int x, int y) {
        int height = getHeight();
        int startY = height / 3;
        int spacing = 38;

        for (int i = 0; i < menuOptions.length; i++) {
            int optionY = startY + (i * spacing);
            if (y >= optionY - 5 && y <= optionY + 30) {
                selectedIndex = i;
                repaint();
                executeSelection();
                break;
            }
        }
    }

    private void executeSelection() {
        switch (selectedIndex) {
            case 0: // PLAY
                midlet.startNewGame();
                break;
            case 1: // LOAD
                midlet.loadSavedGame();
                break;
            case 2: // SETTINGS / SOUND TOGGLE
                boolean currentSound = midlet.getAudioManager().isSoundOn();
                midlet.getAudioManager().setSoundOn(!currentSound);
                repaint();
                break;
        }
    }
}
