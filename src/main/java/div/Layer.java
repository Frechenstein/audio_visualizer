package div;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.ArrayList;
import java.util.List;

import java2D.WindowPanel;

public class Layer {

    WindowPanel wp;

    private List<Coordinate3D> coordinates;    // 3D-Koordinaten mit Z
    private float redMultiplier = 1.0f;
    private float greenMultiplier = 1.0f;
    private float blueMultiplier = 1.0f;
    private float alphaMultiplier = 1.0f;
    private float imageSizeMultiplier = 1.0f;
    private BufferedImage image;
    private float focalLength = 250.0f;         // Je größer, desto flacher die Perspektive
    private float movementSpeed = 4.0f;         // Wie schnell das Viereck auf die Kamera zukommt

    public Layer(List<Coordinate3D> coordinates, BufferedImage image, WindowPanel wp) {
        this.coordinates = coordinates;
        this.image = image;
        this.wp = wp;
    }

    public void setColorMultiplier(float red, float green, float blue) {
        this.redMultiplier = red;
        this.greenMultiplier = green;
        this.blueMultiplier = blue;
    }

    public void setAlphaMultiplier(float alpha) {
        this.alphaMultiplier = alpha;
    }
    
    public void setImageSizeMultiplier(float multiplier) {
        this.imageSizeMultiplier = multiplier;
    }

    public void draw(Graphics g) {
        for (Coordinate3D coord : coordinates) {
            coord.z -= movementSpeed;

            // Reset wenn Punkt zu nah ist (quasi durchgeflogen)
            if (coord.z < 50) {
                coord.z = 800; // Reset an entfernte Position
            }

            // Perspektivische Projektion
            float scale = focalLength / coord.z;
            int screenX = (int)(wp.screenWidth / 2 + coord.x * scale);
            int screenY = (int)(wp.screenHeight / 2 - coord.y * scale);
            int imageSize = (int)(image.getWidth() * scale * imageSizeMultiplier);

            // Skalierung & Farbanpassung
            Image scaledImage = image.getScaledInstance(imageSize, imageSize, Image.SCALE_SMOOTH);
            BufferedImage coloredImage = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);
            Graphics g2 = coloredImage.getGraphics();
            g2.drawImage(scaledImage, 0, 0, null);
            g2.dispose();

            RescaleOp rescaleOp = new RescaleOp(
                new float[]{redMultiplier, greenMultiplier, blueMultiplier, alphaMultiplier}, new float[4], null);
            rescaleOp.filter(coloredImage, coloredImage);

            // Zeichnen zentriert an projizierter Position
            g.drawImage(coloredImage, screenX - imageSize / 2, screenY - imageSize / 2, null);
        }
    }

    // Neue Klasse für 3D-Koordinaten mit Z
    public static class Coordinate3D {
        public float x, y, z;

        public Coordinate3D(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    // Erzeugt die typische Vierecks-Form mit Tiefe
    public static List<Coordinate3D> createShape() {
        List<Coordinate3D> coordinates = new ArrayList<>();
        float z = 800; // Anfangs-Z-Position (weit weg)

        coordinates.add(new Coordinate3D(260, 0, z));
        coordinates.add(new Coordinate3D(0, 260, z));
        coordinates.add(new Coordinate3D(-260, 0, z));
        coordinates.add(new Coordinate3D(0, -260, z));
        
        coordinates.add(new Coordinate3D(275, 150, z));
        coordinates.add(new Coordinate3D(275, -150, z));
        coordinates.add(new Coordinate3D(150, 275, z));
        coordinates.add(new Coordinate3D(150, -275, z));
        coordinates.add(new Coordinate3D(-275, 150, z));
        coordinates.add(new Coordinate3D(-275, -150, z));
        coordinates.add(new Coordinate3D(-150, 275, z));
        coordinates.add(new Coordinate3D(-150, -275, z));

        coordinates.add(new Coordinate3D(300, 300, z));
        coordinates.add(new Coordinate3D(-300, 300, z));
        coordinates.add(new Coordinate3D(300, -300, z));
        coordinates.add(new Coordinate3D(-300, -300, z));

        return coordinates;
    }
}
