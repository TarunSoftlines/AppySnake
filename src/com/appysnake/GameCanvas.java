package com.appysnake;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Image;
import java.io.IOException;
import java.util.Random;

public class GameCanvas extends Canvas implements Runnable {

    private SnakeMIDlet midlet;
    private Thread gameThread;
    private boolean running = false;
    private boolean paused = false;

    // Game States
    private static final int STATE_PLAYING = 0;
    private static final int STATE_LEVEL_COMPLETE = 1;
    private static final int STATE_GAME_OVER = 2;
    private static final int STATE_VICTORY = 3;
    private int gameState = STATE_PLAYING;

    // Grid Configuration
    private int tileSize = 16;
    private int gridWidth;
    private int gridHeight;
    private int xOffset;
    private int yOffset;

    // Snake Logic
    private static final int MAX_SNAKE_LENGTH = 1000;
    private int[] snakeX = new int[MAX_SNAKE_LENGTH];
    private int[] snakeY = new int[MAX_SNAKE_LENGTH];
    private int snakeLength;

    // Movement Directions
    private static final int DIR_UP = 0;
    private static final int DIR_RIGHT = 1;
    private static final int DIR_DOWN = 2;
    private static final int DIR_LEFT = 3;
    private int direction;
    private int nextDirection;

    // Apple Food logic
    private int foodX;
    private int foodY;
    private int applesEatenInLevel;
    private int totalScore;
    private int currentLevel;

    // Exact Apple Target sequence per level (Levels 1 to 20)
    private static final int[] LEVEL_TARGETS = {
        6, 12, 14, 16, 18, 20, 22, 24, 26, 28,
        30, 32, 34, 36, 38, 40, 42, 44, 46, 48
    };

    // 20 Level Themes (Background Color & Border Color)
    private static final int[] THEME_BG_COLORS = {
        0x1B4D3E, // L1: Emerald Forest
        0xD2B48C, // L2: Desert Sands
        0x0F0F23, // L3: Neon Cyber
        0x4A0E0E, // L4: Lava Cavern
        0x1C3144, // L5: Frozen Tundra
        0x0B3C5D, // L6: Deep Jungle
        0x2B1B17, // L7: Toxic Swamp
        0x3A2E39, // L8: Golden Temple
        0x111111, // L9: Midnight City
        0x4B0082, // L10: Dark Violet
        0x002B36, // L11: Ocean Abyss
        0x3B3A3A, // L12: Volcanic Ash
        0x220B38, // L13: Alien Nebula
        0x2A1810, // L14: Rustic Redwood
        0x5C2018, // L15: Sunset Crimson
        0x1A1C23, // L16: Graveyard
        0x2C3E50, // L17: Dungeon Pass
        0x0B132B, // L18: Celestial Space
        0x3F4E4F, // L19: Retro Slate
        0x1C0A00  // L20: Ultimate Realm
    };

    private static final int[] THEME_BORDER_COLORS = {
        0x2ECC71, 0xE67E22, 0x00FFFF, 0xFF4500, 0x00BFFF,
        0x27AE60, 0x8E44AD, 0xF1C40F, 0x95A5A6, 0x9B59B6,
        0x1ABC9C, 0x7F8C8D, 0xE84393, 0xD35400, 0xFF7675,
        0x6C5CE7, 0x34495E, 0xFD79A8, 0xBDC3C7, 0xFFD700
    };

    // Assets
    private Image snakeImg;
    private Image foodImg;
    private Random random = new Random();

    public GameCanvas(SnakeMIDlet midlet, int startLevel, int initialScore) {
        this.midlet = midlet;
        this.currentLevel = startLevel;
        this.totalScore = initialScore;

        setFullScreenMode(true);
        loadAssets();
        calculateGrid();
        initLevel();
    }

    private void loadAssets() {
        try {
            snakeImg = Image.createImage("/snake.png");
        } catch (IOException e) {
            snakeImg = null;
        }

        try {
            foodImg = Image.createImage("/food.png");
        } catch (IOException e) {
            foodImg = null;
        }
    }

    private void calculateGrid() {
        int screenWidth = getWidth();
        int screenHeight = getHeight();

        // Calculate available area reserving 30px top for HUD
        int hudHeight = 30;
        gridWidth = (screenWidth - 8) / tileSize;
        gridHeight = (screenHeight - hudHeight - 8) / tileSize;

        xOffset = (screenWidth - (gridWidth * tileSize)) / 2;
        yOffset = hudHeight + ((screenHeight - hudHeight - (gridHeight * tileSize)) / 2);
    }

    private void initLevel() {
        applesEatenInLevel = 0;
        snakeLength = 4;
        direction = DIR_RIGHT;
        nextDirection = DIR_RIGHT;

        int startX = gridWidth / 2;
        int startY = gridHeight / 2;

        for (int i = 0; i < snakeLength; i++) {
            snakeX[i] = startX - i;
            snakeY[i] = startY;
        }

        spawnFood();
        gameState = STATE_PLAYING;
    }

    private void spawnFood() {
        boolean valid = false;
        while (!valid) {
            foodX = random.nextInt(gridWidth);
            foodY = random.nextInt(gridHeight);

            valid = true;
            for (int i = 0; i < snakeLength; i++) {
                if (snakeX[i] == foodX && snakeY[i] == foodY) {
                    valid = false;
                    break;
                }
            }
        }
    }

    public void start() {
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void pause() {
        paused = true;
    }

    public void resume() {
        paused = false;
    }

    public void run() {
        while (running) {
            if (!paused && gameState == STATE_PLAYING) {
                updateGame();
            }
            repaint();
            
            // Adjust game speed per level
            int speedDelay = Math.max(60, 150 - (currentLevel * 4));
            try {
                Thread.sleep(speedDelay);
            } catch (InterruptedException e) {
                // Ignore interruption
            }
        }
    }

    private void updateGame() {
        direction = nextDirection;

        // Move body
        for (int i = snakeLength - 1; i > 0; i--) {
            snakeX[i] = snakeX[i - 1];
            snakeY[i] = snakeY[i - 1];
        }

        // Move head
        switch (direction) {
            case DIR_UP:    snakeY[0]--; break;
            case DIR_RIGHT: snakeX[0]++; break;
            case DIR_DOWN:  snakeY[0]++; break;
            case DIR_LEFT:  snakeX[0]--; break;
        }

        // Screen boundaries wrap around
        if (snakeX[0] < 0) snakeX[0] = gridWidth - 1;
        if (snakeX[0] >= gridWidth) snakeX[0] = 0;
        if (snakeY[0] < 0) snakeY[0] = gridHeight - 1;
        if (snakeY[0] >= gridHeight) snakeY[0] = 0;

        // Self-collision Check (Rule 3)
        for (int i = 1; i < snakeLength; i++) {
            if (snakeX[0] == snakeX[i] && snakeY[0] == snakeY[i]) {
                handleGameOver();
                return;
            }
        }

        // Apple Eating Check (Rule 2)
        if (snakeX[0] == foodX && snakeY[0] == foodY) {
            applesEatenInLevel++;
            totalScore += 10 * currentLevel;

            if (snakeLength < MAX_SNAKE_LENGTH) {
                snakeLength++; // Snake grows
            }

            midlet.getAudioManager().playEatSound();

            // Check level goal (Rule 1)
            int targetApples = LEVEL_TARGETS[currentLevel - 1];
            if (applesEatenInLevel >= targetApples) {
                midlet.getAudioManager().playDieSound(); // Play completion sound
                
                if (currentLevel >= 20) {
                    gameState = STATE_VICTORY;
                } else {
                    gameState = STATE_LEVEL_COMPLETE;
                }
                
                // Auto-save game progress
                midlet.getSaveManager().saveProgress(currentLevel + 1, totalScore);
            } else {
                spawnFood();
            }
        }
    }

    private void handleGameOver() {
        gameState = STATE_GAME_OVER;
        midlet.getAudioManager().playDieSound();
    }

    protected void paint(Graphics g) {
        int width = getWidth();
        int height = getHeight();

        int themeIdx = Math.min(currentLevel - 1, 19);
        int bgColor = THEME_BG_COLORS[themeIdx];
        int borderColor = THEME_BORDER_COLORS[themeIdx];

        // Draw Screen Background
        g.setColor(0x000000);
        g.fillRect(0, 0, width, height);

        // Draw HUD Header
        g.setColor(0xFFFFFF);
        Font hudFont = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_SMALL);
        g.setFont(hudFont);
        g.drawString("LVL: " + currentLevel + "/20", 5, 5, Graphics.LEFT | Graphics.TOP);
        g.drawString("APPLES: " + applesEatenInLevel + "/" + LEVEL_TARGETS[themeIdx], width / 2, 5, Graphics.HCENTER | Graphics.TOP);
        g.drawString("SCORE: " + totalScore, width - 5, 5, Graphics.RIGHT | Graphics.TOP);

        // Draw Grid Playfield
        g.setColor(bgColor);
        g.fillRect(xOffset, yOffset, gridWidth * tileSize, gridHeight * tileSize);

        g.setColor(borderColor);
        g.drawRect(xOffset - 1, yOffset - 1, (gridWidth * tileSize) + 1, (gridHeight * tileSize) + 1);

        // Draw Apple (Food)
        if (gameState == STATE_PLAYING || gameState == STATE_LEVEL_COMPLETE) {
            int drawFoodX = xOffset + (foodX * tileSize);
            int drawFoodY = yOffset + (foodY * tileSize);
            if (foodImg != null) {
                g.drawImage(foodImg, drawFoodX, drawFoodY, Graphics.LEFT | Graphics.TOP);
            } else {
                g.setColor(0xFF0000);
                g.fillArc(drawFoodX, drawFoodY, tileSize, tileSize, 0, 360);
            }
        }

        // Draw Snake
        for (int i = 0; i < snakeLength; i++) {
            int drawSnakeX = xOffset + (snakeX[i] * tileSize);
            int drawSnakeY = yOffset + (snakeY[i] * tileSize);

            if (snakeImg != null) {
                g.drawImage(snakeImg, drawSnakeX, drawSnakeY, Graphics.LEFT | Graphics.TOP);
            } else {
                if (i == 0) g.setColor(0x00FF00); // Snake Head
                else g.setColor(0xCC0000);        // Snake Body
                g.fillRect(drawSnakeX, drawSnakeY, tileSize - 1, tileSize - 1);
            }
        }

        // Draw Overlay Screens
        if (gameState == STATE_LEVEL_COMPLETE) {
            drawOverlay(g, "LEVEL " + currentLevel + " CLEARED!", "Press 5 / Touch to Continue");
        } else if (gameState == STATE_GAME_OVER) {
            drawOverlay(g, "GAME OVER", "Press 5 / Touch to Retry");
        } else if (gameState == STATE_VICTORY) {
            drawOverlay(g, "YOU BEAT ALL 20 LEVELS!", "Congratulations! Score: " + totalScore);
        } else if (paused) {
            drawOverlay(g, "PAUSED", "Press 5 to Resume");
        }
    }

    private void drawOverlay(Graphics g, String title, String subtitle) {
        int width = getWidth();
        int height = getHeight();

        g.setColor(0x000000);
        g.fillRect(20, height / 2 - 40, width - 40, 80);

        g.setColor(0xFFD700);
        g.drawRect(20, height / 2 - 40, width - 40, 80);

        Font largeFont = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_MEDIUM);
        g.setFont(largeFont);
        g.drawString(title, width / 2, height / 2 - 25, Graphics.HCENTER | Graphics.TOP);

        Font smallFont = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        g.setFont(smallFont);
        g.setColor(0xFFFFFF);
        g.drawString(subtitle, width / 2, height / 2 + 10, Graphics.HCENTER | Graphics.TOP);
    }

    protected void keyPressed(int keyCode) {
        int action = getGameAction(keyCode);

        if (gameState == STATE_PLAYING) {
            if ((action == UP || keyCode == KEY_NUM2) && direction != DIR_DOWN) {
                nextDirection = DIR_UP;
            } else if ((action == DOWN || keyCode == KEY_NUM8) && direction != DIR_UP) {
                nextDirection = DIR_DOWN;
            } else if ((action == LEFT || keyCode == KEY_NUM4) && direction != DIR_RIGHT) {
                nextDirection = DIR_LEFT;
            } else if ((action == RIGHT || keyCode == KEY_NUM6) && direction != DIR_LEFT) {
                nextDirection = DIR_RIGHT;
            } else if (keyCode == KEY_NUM5 || action == FIRE) {
                paused = !paused;
            }
        } else if (gameState == STATE_LEVEL_COMPLETE) {
            if (keyCode == KEY_NUM5 || action == FIRE || keyCode == -5) {
                currentLevel++;
                initLevel();
            }
        } else if (gameState == STATE_GAME_OVER) {
            if (keyCode == KEY_NUM5 || action == FIRE || keyCode == -5) {
                initLevel();
            }
        } else if (gameState == STATE_VICTORY) {
            if (keyCode == KEY_NUM5 || action == FIRE || keyCode == -5) {
                midlet.showMenu();
            }
        }
    }

    protected void pointerPressed(int x, int y) {
        if (gameState == STATE_LEVEL_COMPLETE) {
            currentLevel++;
            initLevel();
            return;
        } else if (gameState == STATE_GAME_OVER) {
            initLevel();
            return;
        } else if (gameState == STATE_VICTORY) {
            midlet.showMenu();
            return;
        }

        // On-screen touch direction controls
        int width = getWidth();
        int height = getHeight();

        if (y < height / 3 && direction != DIR_DOWN) {
            nextDirection = DIR_UP;
        } else if (y > (height * 2) / 3 && direction != DIR_UP) {
            nextDirection = DIR_DOWN;
        } else if (x < width / 2 && direction != DIR_RIGHT) {
            nextDirection = DIR_LEFT;
        } else if (x >= width / 2 && direction != DIR_LEFT) {
            nextDirection = DIR_RIGHT;
        }
    }
}
