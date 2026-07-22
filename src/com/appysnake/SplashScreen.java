package com.appysnake;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import java.io.IOException;

public class SplashScreen extends Canvas implements Runnable {

    private SnakeMIDlet midlet;
    private Image splashImage;
    private boolean skipped = false;

    public SplashScreen(SnakeMIDlet midlet) {
        this.midlet = midlet;
        setFullScreenMode(true);
        try {
            splashImage = Image.createImage("/head.png");
        } catch (IOException e) {
            splashImage = null;
        }
        new Thread(this).start();
    }

    protected void paint(Graphics g) {
        // Light blue background matching the splash artwork
        g.setColor(0x74B8F8); 
        g.fillRect(0, 0, getWidth(), getHeight());

        if (splashImage != null) {
            // Draw opening splash image centered
            g.drawImage(splashImage, getWidth() / 2, getHeight() / 2, Graphics.HCENTER | Graphics.VCENTER);
        } else {
            g.setColor(0xFFFFFF);
            g.drawString("AppySnake Loading...", getWidth() / 2, getHeight() / 2, Graphics.HCENTER | Graphics.TOP);
        }
    }

    public void run() {
        try {
            Thread.sleep(3000); // Show splash for 3 seconds
        } catch (InterruptedException e) {
            // Ignore
        }
        dismiss();
    }

    protected void keyPressed(int keyCode) {
        dismiss();
    }

    protected void pointerPressed(int x, int y) {
        dismiss();
    }

    private synchronized void dismiss() {
        if (!skipped) {
            skipped = true;
            midlet.showMenu();
        }
    }
}
