package main;

import java.util.ArrayList;
import java.util.List;

import java2D.Layer.Coordinate3D;

public class Layer {
	
	private ArrayList<Coordinate3D> coordinates;
	
	private float[] rgba = new float[4];
	private float rotationAngle = 0.0f;
	public float time = 0.0f;
	
	
	public Layer(float[] rgba, int initZ) {
		for(int i = 0; i < this.rgba.length; i++) {
			this.rgba[i] = rgba[i];
		}
		this.coordinates = new ArrayList<>();
		this.coordinates = createShape(initZ);
	}
	
	public ArrayList<Coordinate3D> getCoordinates() {
		return coordinates;
	}
	
	// GETTER AND SETTER FOR COLORS
	public float[] getColor() {
		return rgba;
	}
	
	public void setColor(float[] rgba) {
		for(int i = 0; i < this.rgba.length; i++) {
			this.rgba[i] = rgba[i];
		}
	}
	
	// GETTER AND SETTER FOR ROTATION
	public float getRotationAngle() {
		return rotationAngle;
	}
	
	public void setRotationAngle(float rotAngle) {
		this.rotationAngle = rotAngle;
	}
	
	public static class Coordinate3D  {
		public float x,y,z;
		
		public Coordinate3D(float x, float y, float z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}
	
    public static ArrayList<Coordinate3D> createShape(int initZ) {
    	return createSquare(initZ);
    }
    
    public static ArrayList<Coordinate3D> createSquare(int initZ) {
        ArrayList<Coordinate3D> coordinates = new ArrayList<>();
        
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
