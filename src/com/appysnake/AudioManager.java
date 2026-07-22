package com.appysnake;

import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import java.io.InputStream;

public class AudioManager {

    private boolean soundOn = true;
    private Player themePlayer;

    public boolean isSoundOn() {
        return soundOn;
    }

    public void setSoundOn(boolean soundOn) {
        this.soundOn = soundOn;
        if (!soundOn) {
            stopThemeSong();
        }
    }

    public void playThemeSong() {
        if (!soundOn) return;
        try {
            stopThemeSong();
            InputStream is = getClass().getResourceAsStream("/theme.mp3");
            if (is != null) {
                themePlayer = Manager.createPlayer(is, "audio/mpeg");
                themePlayer.setLoopCount(-1); // Loop indefinitely
                themePlayer.start();
            }
        } catch (Exception e) {
            // Audio device busy or unsupported format fallback
        }
    }

    public void stopThemeSong() {
        try {
            if (themePlayer != null) {
                themePlayer.stop();
                themePlayer.deallocate();
                themePlayer.close();
                themePlayer = null;
            }
        } catch (Exception e) {
            themePlayer = null;
        }
    }

    public void playEatSound() {
        playSoundEffect("/eat.wav", "audio/x-wav");
    }

    public void playDieSound() {
        playSoundEffect("/die.wav", "audio/x-wav");
    }

    private void playSoundEffect(final String path, final String type) {
        if (!soundOn) return;
        new Thread(new Runnable() {
            public void run() {
                try {
                    InputStream is = getClass().getResourceAsStream(path);
                    if (is != null) {
                        Player sfxPlayer = Manager.createPlayer(is, type);
                        sfxPlayer.start();
                    }
                } catch (Exception e) {
                    // Ignore sound effect errors
                }
            }
        }).start();
    }

    public void cleanup() {
        stopThemeSong();
    }
}
