package java2D;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.ArrayList;

import java2D.Layer.Coordinate3D;

public class Renderer {
	
	WindowPanel wp;
	
    // Das Basisbild, das als Texture dient
    private BufferedImage image;
    private float baseScale = 0.5f; // Standardwert kleiner als 1 -> Bild kleiner
    
    // Parameter für den 3D-Effekt:
    // focalLength entspricht der Brennweite – je größer, desto flacher erscheint die Perspektive
    private float focalLength = 300.0f;
    // Bewegungsgeschwindigkeit in Z-Richtung (Simuliert den Zoom/Bewegung)
    private float movementSpeed = 4.0f;
    
    private ArrayList<Layer> layers = new ArrayList<>();
    
    public Renderer(WindowPanel wp, BufferedImage image) {
    	this.wp = wp;
    	this.image = image;
    	
    	float[] rgba = {0.0f, 1.0f, 0.0f, 1.0f};
    	layers.add(new Layer(rgba));
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
        
        for(Layer layer : layers) {
        	
        	
            for (Coordinate3D point : layer.coordinates) {
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
                
                BufferedImage coloredImage = applyColorModifiers(image, layer.rgba);
                g2d.drawImage(coloredImage, transform, null);
            }
            
            
        }
    }
    
    /**
     * Wendet Farbanpassungen (RGB und Alpha) auf ein Bild an.
     * Falls alle Multiplikatoren 1.0 sind, wird das Originalbild zurückgegeben.
     */
    private BufferedImage applyColorModifiers(BufferedImage baseImage, float[] rgba) {
        if (rgba[0] == 1.0f && rgba[1] == 1.0f &&
        		rgba[2] == 1.0f && rgba[3] == 1.0f) {
            return baseImage;
        }
        
        BufferedImage modifiedImage = new BufferedImage(
                baseImage.getWidth(), baseImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = modifiedImage.createGraphics();
        g2d.drawImage(baseImage, 0, 0, null);
        g2d.dispose();
        
        float[] offsets = new float[4];  // offsets bleiben 0
        RescaleOp op = new RescaleOp(rgba, offsets, null);
        op.filter(modifiedImage, modifiedImage);
        return modifiedImage;
    }

}
