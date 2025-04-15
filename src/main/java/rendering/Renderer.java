package rendering;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform2f;
import static org.lwjgl.opengl.GL20.glUniform4f;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

import java.util.ArrayList;
import java.util.List;

import main.AppRunner;
import main.Layer;
import utility.Timer;

public class Renderer {
    
    private int windowWidth, windowHeight;
    AppRunner ar;
    
    Timer timer;
    
    private int textureId;
	
    private List<Layer> layers;
    public int initZ = 5000;
    int layerDistance = 100;

    private float focalLength = 300.0f;
    float speed = 480.0f;
    
    Effects effects;
    
    private float rotationAngle = 0.0f; 
    float rotationSpeed = 20.0f;
    /**
     * 0: no rotation
     * 1-3: whole shape rotates; 1: clockwise; 2: counterclockwise; 3: back and forth
     * 4-6: every layer has  rotation; 4: counterclockwise; 5; clockwise; 6: back and forth
     */
    int rotationMode = 6; 
    
    private float baseScale = 0.15f;
    
    public Renderer(AppRunner ar, int textureId, int windowWidth, int windowHeight, int initZ) {
        this.ar = ar;
    	this.textureId = textureId;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        
        timer = new Timer(15.0);
        
        layers = new ArrayList<>();
        effects = new Effects(this);
        
        this.initZ = initZ;
        
        float[] rgba = {1.0f, 1.0f, 1.0f, 1.0f};
        layers.add(new Layer(rgba, initZ));
        
        if(rotationMode < 0) {
        	rotationMode = 0;
        }
    }
    
    public Renderer(List<Layer> layers, int textureId, int windowWidth, int windowHeight) {
        this.layers = layers;
        this.textureId = textureId;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
    }
    
    public void setBaseScale(float baseScale) {
        this.baseScale = baseScale;
    }
    
    // Aktualisiert ggf. Logik wie Bewegung in Z-Richtung (Zoom, etc.)
    public void update(float deltaTime) {

    	/*
    	if(timer.isElapsed()) {
    		timer.reset();
    		if(rotationMode < 7) {
    			rotationMode++;
    		}
    	}
    	*/
    	
    	int newLayers = 0;
    	Layer removeLayer = null;
    	
        for (Layer layer : layers) {
        	
        	boolean summonNewLayer = false;
        	
            for (Layer.Coordinate3D coord : layer.getCoordinates()) { 
                coord.z -= speed * deltaTime; 
                if(coord.z == initZ - layerDistance && !summonNewLayer) {
                	summonNewLayer = true;
                	newLayers++;
                }
                if(coord.z < 30) {
                    removeLayer = layer;
                }
            }
            
            if(rotationMode - 3 > 0) {
            	layer.setRotationAngle(effects.calculateLayerAngle(layer, deltaTime));
            }
        }
        
        for(int l = 0; l < newLayers; l++) {
            //float[] rgba = {1.0f, 1.0f, 1.0f, 1.0f};
        	float[] rgba = {(float) Math.random(), (float) Math.random(), (float) Math.random(), (float) Math.random()};
            layers.add(0, new Layer(rgba, initZ));
        }
        
        if(removeLayer != null) {
        	layers.remove(removeLayer);
        }
        
        if(rotationMode - 4 < 0) {
        	rotationAngle = effects.calculateRotationAngle(rotationAngle, deltaTime);
        }
    }
    
    // Rendert alle Layer
    public void render() {
        glUseProgram(ar.getShaderProgram()); 
        
        int aspectLocation = glGetUniformLocation(ar.getShaderProgram(), "aspect");
        glUniform1f(aspectLocation, (float) windowWidth / windowHeight);
        
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureId);
        
        float centerX = windowWidth / 2.0f;
        float centerY = windowHeight / 2.0f;
        
        float cosAngle = 1.0f;
        float sinAngle = 0.0f;
        
        if(rotationMode - 4 < 0) {
    		float[] angles = effects.getAngles(rotationAngle);
            cosAngle = angles[0];
            sinAngle = angles[1];
        }
        
        for (Layer layer : layers) {
            int colorLocation = glGetUniformLocation(ar.getShaderProgram(), "layerColor");
            glUniform4f(colorLocation, layer.getColor()[0], layer.getColor()[1], layer.getColor()[2], layer.getColor()[3]);
            
            for (Layer.Coordinate3D coord : layer.getCoordinates()) {

            	if(rotationMode - 3 > 0) {
            		float[] angles = effects.getAngles(layer.getRotationAngle());
                    cosAngle = angles[0];
                    sinAngle = angles[1];
            	}

                float computedScale = focalLength / coord.z;
                float finalScale = computedScale * baseScale; 
                
                float rotatedX = cosAngle * coord.x - sinAngle * coord.y;
                float rotatedY = sinAngle * coord.x + cosAngle * coord.y;

                float screenX = centerX + (rotatedX * computedScale);
                float screenY = centerY - (rotatedY * computedScale);
                
                float ndcX = (screenX / (windowWidth / 2.0f)) - 1.0f;
                float ndcY = 1.0f - (screenY / (windowHeight / 2.0f));

                int offsetLocation = glGetUniformLocation(ar.getShaderProgram(), "offset");
                glUniform2f(offsetLocation, ndcX, ndcY);
                int scaleLocation = glGetUniformLocation(ar.getShaderProgram(), "scale");
                glUniform1f(scaleLocation, finalScale);
                
             
                glBindVertexArray(ar.getVaoId());
                glDrawArrays(GL_TRIANGLES, 0, 6);
                glBindVertexArray(0);
            }
        }
    }
}
