package java2D;

import java.util.List;

public class Layer {
    
    // Liste der 3D-Koordinaten (x, y, z)
    public List<Coordinate3D> coordinates;
    
    // Farb- und Transparenzmodifikatoren
    float[] rgba;
    
    
    /**
     * Konstruktor
     */
    public Layer(float[] rgba) {
        this.rgba = rgba;
        coordinates = BasicGeometry.createShape();
    }

    public float[] getRGBA() {
    	return rgba;
    }
    
    public void setRGBA(float[] rgba) {
		this.rgba = rgba;
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
}
