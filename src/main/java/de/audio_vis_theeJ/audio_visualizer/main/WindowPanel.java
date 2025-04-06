package de.audio_vis_theeJ.audio_visualizer.main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class WindowPanel extends JPanel implements Runnable {

    // SCREEN SETTINGS
    int screenWidth = 1280;
    int screenHeight = 720;

    // FPS
    int FPS = 60;

    // SYSTEM
    Thread windowThread;

    // Bild-Objekt
    private BufferedImage image;
    private Image scaledImage;
    private float scaleFactor = 1.0f;
    private float redMultiplier = 1.0f;
    private float greenMultiplier = 1.0f;
    private float blueMultiplier = 1.0f;
    private float alphaMultiplier = 1.0f;  // Transparenz Multiplikator

    public WindowPanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        this.setFocusable(true);

        // Bild laden
        try {
            image = ImageIO.read(new File("res/galaxy.png")); // Bild laden
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
        // Hier könntest du die Skalierung, Farb- und Transparenzänderung dynamisch anpassen
        scaleFactor = (float) (1); // Beispielhafte Skalierung
        redMultiplier = (float) Math.abs(Math.sin(System.currentTimeMillis() * 0.001));  // Beispiel für Farbänderung
        greenMultiplier = (float) Math.abs(Math.cos(System.currentTimeMillis() * 0.001));
        blueMultiplier = (float) Math.abs(Math.sin(System.currentTimeMillis() * 0.002));
        alphaMultiplier = (float) (Math.sin(System.currentTimeMillis() * 0.001) * 0.5 + 1.0); // Transparenz (Alpha) anpassen
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Skalieren des Bildes
        scaledImage = image.getScaledInstance((int) (image.getWidth() * scaleFactor),
                (int) (image.getHeight() * scaleFactor), Image.SCALE_SMOOTH);

        // Farbänderung auf das Bild anwenden
        BufferedImage coloredImage = new BufferedImage(scaledImage.getWidth(null), scaledImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics g2 = coloredImage.getGraphics();
        g2.drawImage(scaledImage, 0, 0, null);
        g2.dispose();

        // Farbskalierung anwenden
        RescaleOp rescaleOp = new RescaleOp(new float[]{redMultiplier, greenMultiplier, blueMultiplier, 1.0f}, new float[4], null);
        rescaleOp.filter(coloredImage, coloredImage);

        // Transparenz anpassen (Alpha-Wert)
        for (int x = 0; x < coloredImage.getWidth(); x++) {
            for (int y = 0; y < coloredImage.getHeight(); y++) {
                int rgba = coloredImage.getRGB(x, y);
                int alpha = (rgba >> 24) & 0xff; // Extrahiere den Alpha-Wert
                alpha = (int) (alpha * alphaMultiplier); // Anpassen der Transparenz

                if (alpha > 255) alpha = 255;  // Alpha-Wert sollte max. 255 sein
                if (alpha < 0) alpha = 0;      // Alpha-Wert sollte nicht negativ sein

                // Setze den neuen Alpha-Wert
                rgba = (alpha << 24) | (rgba & 0x00ffffff);
                coloredImage.setRGB(x, y, rgba);
            }
        }

        // Zeichne das bearbeitete Bild auf dem Panel
        g.drawImage(coloredImage, 100, 100, this); // Position anpassen, wo du das Bild zeichnen möchtest
    }
}
