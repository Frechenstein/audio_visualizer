package layerdata;

import java.util.ArrayList;

/**
 * Represents a single layer in 3D space, consisting of a fixed set of 2D coordinates,
 * a global depth (z‑value), a color (RGBA), and an optional rotation angle.
 * <p>
 * Each Layer holds its vertex pattern in a List of Coordinate2D and manages
 * its own depth and color independently. Rotations and fade‑in/out times can
 * be animated externally.
 * </p>
 */
public class Layer {
	/** The 2D shape of this layer, in model coordinates. */
	private ArrayList<Coordinate2D> coordinates;

	/** Distance from camera along the z‑axis. */
	private float z;

	/** Color of this layer, as {r,g,b,a}. */
	private float[] rgba;

	/** Current rotation angle in degrees. */
	private float rotationAngle = 0.0f;

	/** Elapsed time counter for per‑layer oscillations. */
	public float time = 0.0f;

	/**
	 * Creates a new Layer with the given color and initial depth.
	 *
	 * @param rgba   an array of four floats {r,g,b,a}, each in [0,1]
	 * @param initZ  the initial z‑distance from the camera
	 */
	public Layer(float[] rgba, int initZ) {
		this.rgba = rgba;
		this.coordinates = new ArrayList<>();
		this.coordinates = createShape();
		this.z = initZ;
	}

	/**
	 * @return the current z‑distance of this layer
	 */
	public float getZ() {
		return z;
	}

	/**
	 * @param z  the new z‑distance of this layer
	 */
	public void setZ(float z) {
		this.z = z;
	}

	/**
	 * @return a mutable list of 2D coordinates defining this layer’s shape
	 */
	public ArrayList<Coordinate2D> getCoordinates() {
		return coordinates;
	}

	/**
	 * @return the current RGBA color array of this layer
	 */
	public float[] getColor() {
		return rgba;
	}

	/**
	 * Sets this layer’s color.
	 *
	 * @param rgba  an array of four floats {r,g,b,a}, each in [0,1]
	 */
	public void setColor(float[] rgba) {
		this.rgba = rgba;
	}

	/**
	 * @return the current rotation angle in degrees
	 */
	public float getRotationAngle() {
		return rotationAngle;
	}

	/**
	 * @param rotAngle new rotation angle in degrees
	 */
	public void setRotationAngle(float rotAngle) {
		this.rotationAngle = rotAngle;
	}

	/**
	 * Constructs the default shape for all layers.
	 * By default, returns a 16‑point “star” shape.
	 *
	 * @return a new List of 2D coordinates
	 */
    public static ArrayList<Coordinate2D> createShape() {
    	return createSquare();
    }

	/**
	 * Produces a symmetric 16‑point pattern around the origin.
	 *
	 * @return coordinates of the pattern
	 */
    public static ArrayList<Coordinate2D> createSquare() {
        ArrayList<Coordinate2D> coordinates = new ArrayList<>();
        
        coordinates.add(new Coordinate2D(260, 0));
        coordinates.add(new Coordinate2D(0, 260));
        coordinates.add(new Coordinate2D(-260, 0));
        coordinates.add(new Coordinate2D(0, -260));
        
        coordinates.add(new Coordinate2D(275, 150));
        coordinates.add(new Coordinate2D(275, -150));
        coordinates.add(new Coordinate2D(150, 275));
        coordinates.add(new Coordinate2D(150, -275));
        coordinates.add(new Coordinate2D(-275, 150));
        coordinates.add(new Coordinate2D(-275, -150));
        coordinates.add(new Coordinate2D(-150, 275));
        coordinates.add(new Coordinate2D(-150, -275));

        coordinates.add(new Coordinate2D(300, 300));
        coordinates.add(new Coordinate2D(-300, 300));
        coordinates.add(new Coordinate2D(300, -300));
        coordinates.add(new Coordinate2D(-300, -300));
        
        return coordinates;
    }

}
