package java2D;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class WindowPanel extends JPanel implements Runnable {

    // SCREEN SETTINGS
    public int screenWidth = 1280;
    public int screenHeight = 720;
    boolean debugMode = false; 

    // FPS
    int FPS = 60;

    // SYSTEM
    Thread windowThread;
    Renderer renderer;

    // Bild-Objekt
    private BufferedImage image;
    private Image scaledImage;
    private float scaleFactor = 1.0f;
    private float redMultiplier = 1.0f;
    private float greenMultiplier = 1.0f;
    private float blueMultiplier = 1.0f;
    private float alphaMultiplier = 1.0f;  // Transparenz Multiplikator
    
    Layer layer;

    public WindowPanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        this.setFocusable(true);

        // Bild laden
        try {
            image = ImageIO.read(new File("src/main/res/galaxy.png")); 	// Bild laden
            
            renderer = new Renderer(this, image);
            
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startWindowThread() {
        windowThread = new Thread(this);
        windowThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;
        long timer = 0;
        int drawCount = 0;

        while (windowThread != null) {
            currentTime = System.nanoTime();

            delta += (currentTime - lastTime) / drawInterval;
            timer += (currentTime - lastTime);
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }

            if (timer >= 1000000000) {
                drawCount = 0;
                timer = 0;
            }
        }
    }

    public void update() {
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if(debugMode) {
	        g.setColor(Color.white);
	        g.drawRect(0, screenHeight / 2, screenWidth, 1);
	        g.drawRect(screenWidth / 2, 0, 1, screenHeight);
        }
        
        renderer.draw(g);

        
        
    }
}
