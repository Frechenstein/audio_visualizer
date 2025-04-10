package java2D;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.ArrayList;
import java.util.List;

import div.Layer.Coordinate3D;

public class Layer {

    // Referenz auf das WindowPanel, um die Bildschirmmitte zu bestimmen
    WindowPanel wp;
    
    // Liste der 3D-Koordinaten (x, y, z)
    private List<Coordinate3D> coordinates;
    
    // Farb- und Transparenzmodifikatoren
    private float redMultiplier = 1.0f;
    private float greenMultiplier = 1.0f;
    private float blueMultiplier = 1.0f;
    private float alphaMultiplier = 1.0f;
    
    
    // Das Basisbild, das als Texture dient
    private BufferedImage image;
    private float baseScale = 1.0f; // Standardwert kleiner als 1 -> Bild kleiner

    
    // Parameter für den 3D-Effekt:
    // focalLength entspricht der Brennweite – je größer, desto flacher erscheint die Perspektive
    private float focalLength = 300.0f;
    // Bewegungsgeschwindigkeit in Z-Richtung (Simuliert den Zoom/Bewegung)
    private float movementSpeed = 4.0f;
    
    /**
     * Konstruktor
     * @param coordinates Liste der 3D-Punkte
     * @param image Basisbild, welches transformiert wird
     * @param wp Referenz auf das WindowPanel (für Bildschirmmitte, etc.)
     */
    public Layer(List<Coordinate3D> coordinates, BufferedImage image, WindowPanel wp) {
        this.coordinates = coordinates;
        this.image = image;
        this.wp = wp;
    }
    
    // Setzt die Farbmodifikatoren
    public void setColorMultiplier(float red, float green, float blue) {
        this.redMultiplier = red;
        this.greenMultiplier = green;
        this.blueMultiplier = blue;
    }
    
    public void setAlphaMultiplier(float alpha) {
        this.alphaMultiplier = alpha;
    }
    
    public void setBaseScale(float baseScale) {
        this.baseScale = baseScale;
    }
    
    /**
     * Zeichnet alle Bildinstanzen an den 3D-Punkten.
     * Für jeden Punkt wird das Bild entsprechend der Perspektive (Skalierung) und Position
     * transformiert gezeichnet.
     */
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        
        // Setze RenderingHints, um eine weiche Skalierung und gutes Antialiasing zu erreichen
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int imgWidth = image.getWidth();
        int imgHeight = image.getHeight();
        
        for (Coordinate3D point : coordinates) {
            // Aktualisiere die Z-Koordinate (bewegt den Punkt auf die Kamera zu)
            point.z -= movementSpeed;
            // Reset des Punktes, wenn er zu nah gekommen ist, damit der Effekt endlos wird
            if (point.z < 50) {
                point.z = 800;
            }
            
            // Berechne den Skalierungsfaktor für die Position (Projektion)
            float computedScale = focalLength / point.z;
            
            // Bildschirmpositionen bleiben anhand computedScale konstant:
            int screenX = (int) (wp.screenWidth / 2 + point.x * computedScale);
            int screenY = (int) (wp.screenHeight / 2 - point.y * computedScale);
            
            // Berechne den Faktor für die eigentliche Bildgröße:
            // Dadurch werden die Abstände beibehalten, aber das Bild selbst kleiner gezeichnet.
            float drawScale = computedScale * baseScale;
            
            // Transformation aufbauen:
            AffineTransform transform = new AffineTransform();
            transform.translate(screenX, screenY);                  // Positionieren
            transform.scale(drawScale, drawScale);                    // Bild skalieren (kleiner als computedScale)
            transform.translate(-imgWidth / 2.0, -imgHeight / 2.0);     // Zentrieren des Bildes
            
            BufferedImage coloredImage = applyColorModifiers(image);
            g2d.drawImage(coloredImage, transform, null);
        }
    }
    
    /**
     * Wendet Farbanpassungen (RGB und Alpha) auf ein Bild an.
     * Falls alle Multiplikatoren 1.0 sind, wird das Originalbild zurückgegeben.
     */
    private BufferedImage applyColorModifiers(BufferedImage baseImage) {
        if (redMultiplier == 1.0f && greenMultiplier == 1.0f &&
            blueMultiplier == 1.0f && alphaMultiplier == 1.0f) {
            return baseImage;
        }
        
        BufferedImage modifiedImage = new BufferedImage(
                baseImage.getWidth(), baseImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = modifiedImage.createGraphics();
        g2d.drawImage(baseImage, 0, 0, null);
        g2d.dispose();

        float[] scales = { redMultiplier, greenMultiplier, blueMultiplier, alphaMultiplier };
        float[] offsets = new float[4];  // offsets bleiben 0
        RescaleOp op = new RescaleOp(scales, offsets, null);
        op.filter(modifiedImage, modifiedImage);
        return modifiedImage;
    }
    
    /**
     * Innere Klasse für 3D-Koordinaten.
     * x und y definieren die Position in der Ebene, z ist die Tiefe (Abstand zur Kamera).
     */
    public static class Coordinate3D {
        public float x, y, z;
        
        public Coordinate3D(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
    
    /**
     * Beispielhafte Fabrikmethode zur Erzeugung des hohlen Vierecks.
     * Hier werden 8 Punkte erzeugt, die ein Viereck darstellen, wobei
     * x und y aus der 2D-Definition übernommen werden und z als initialer
     * Wert gesetzt wird.
     * @param wp Referenz auf das WindowPanel (zur Nutzung der Bildschirmmaße)
     * @return Liste der 3D-Koordinaten
     */
    public static List<Coordinate3D> createShape() {
        List<Coordinate3D> coordinates = new ArrayList<>();
        float initZ = 1000; // Start-Tiefe, je weiter, desto kleiner erscheint das Bild
        
        coordinates.add(new Coordinate3D(260, 0, initZ));
        coordinates.add(new Coordinate3D(0, 260, initZ));
        coordinates.add(new Coordinate3D(-260, 0, initZ));
        coordinates.add(new Coordinate3D(0, -260, initZ));
        
        coordinates.add(new Coordinate3D(275, 150, initZ));
        coordinates.add(new Coordinate3D(275, -150, initZ));
        coordinates.add(new Coordinate3D(150, 275, initZ));
        coordinates.add(new Coordinate3D(150, -275, initZ));
        coordinates.add(new Coordinate3D(-275, 150, initZ));
        coordinates.add(new Coordinate3D(-275, -150, initZ));
        coordinates.add(new Coordinate3D(-150, 275, initZ));
        coordinates.add(new Coordinate3D(-150, -275, initZ));

        coordinates.add(new Coordinate3D(300, 300, initZ));
        coordinates.add(new Coordinate3D(-300, 300, initZ));
        coordinates.add(new Coordinate3D(300, -300, initZ));
        coordinates.add(new Coordinate3D(-300, -300, initZ));
        
        return coordinates;
    }
}
