package com.appysnake;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.microedition.lcdui.Display;

public class SnakeMIDlet extends MIDlet {

    private Display display;
    private SplashScreen splashScreen;
    private MenuCanvas menuCanvas;
    private GameCanvas gameCanvas;
    private AudioManager audioManager;
    private SaveManager saveManager;

    public SnakeMIDlet() {
        display = Display.getDisplay(this);
        audioManager = new AudioManager();
        saveManager = new SaveManager();
    }

    protected void startApp() throws MIDletStateChangeException {
        if (splashScreen == null) {
            splashScreen = new SplashScreen(this);
            display.setCurrent(splashScreen);
        }
    }

    public void showMenu() {
        if (menuCanvas == null) {
            menuCanvas = new MenuCanvas(this);
        }
        display.setCurrent(menuCanvas);
        
        // Play theme music on home screen if sound is turned on
        if (audioManager.isSoundOn()) {
            audioManager.playThemeSong();
        }
    }

    public void startNewGame() {
        audioManager.stopThemeSong();
        gameCanvas = new GameCanvas(this, 1, 0); // Start at level 1 with score 0
        display.setCurrent(gameCanvas);
        gameCanvas.start();
    }

    public void loadSavedGame() {
        int[] savedData = saveManager.loadProgress();
        if (savedData != null) {
            audioManager.stopThemeSong();
            int level = savedData[0];
            int score = savedData[1];
            gameCanvas = new GameCanvas(this, level, score);
            display.setCurrent(gameCanvas);
            gameCanvas.start();
        } else {
            // If no save data exists, start a new game
            startNewGame();
        }
    }

    public AudioManager getAudioManager() {
        return audioManager;
    }

    public SaveManager getSaveManager() {
        return saveManager;
    }

    protected void pauseApp() {
        if (gameCanvas != null) {
            gameCanvas.pause();
        }
        if (audioManager != null) {
            audioManager.stopThemeSong();
        }
    }

    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
        if (audioManager != null) {
            audioManager.cleanup();
        }
    }

    public void quitApp() {
        try {
            destroyApp(true);
        } catch (MIDletStateChangeException e) {
            // Ignore
        }
        notifyDestroyed();
    }
}
