package java2D;

import java.util.ArrayList;
import java.util.List;

import java2D.Layer.Coordinate3D;

public class BasicGeometry {
	
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
