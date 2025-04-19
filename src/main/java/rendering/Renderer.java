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
import main.Config;
import utility.Timer;

public class Renderer {

    AppRunner ar;
    Effects effects;
    Timer timer;

    private int windowWidth, windowHeight;
    
    private final int textureId;
    private float baseScale;
	
    private List<Layer> layers;
    private int initZ;
    private int layerDistance;
    private int removeLayerDistance;
    private float zAccumulator;

    private float focalLength;
    private float rotationAngle;
    float speed;

    /**
     * 0: no rotation
     * 1-3: whole shape rotates; 1: clockwise; 2: counterclockwise; 3: back and forth
     * 4-6: every layer has  rotation; 4: counterclockwise; 5; clockwise; 6: back and forth
     */
    int rotationMode = 0;
    
    public Renderer(AppRunner ar, int textureId, Config cfg) {
        this.ar = ar;
    	this.textureId = textureId;

        initializeSettings(cfg);

        timer = new Timer(15.0);
        
        effects = new Effects(this, cfg);
        
        if(ar.debugMode) {
        	layers = new ArrayList<>();
        	layers.add(new Layer(cfg.DEBUG_RGBA, cfg.debugInitZ));
        } else {
            layers = effects.createInitialLayers();
        }
    }

    private void initializeSettings(Config cfg) {
        this.windowWidth = cfg.virtualWidth;
        this.windowHeight = cfg.virtualHeight;

        this.baseScale = cfg.BASE_IMAGE_SCALE;

        this.initZ = cfg.initZ;
        this.layerDistance = cfg.LAYER_DISTANCE;
        this.removeLayerDistance = cfg.REMOVE_LAYER_DISTANCE;

        this.focalLength = cfg.FOCAL_LENGTH;

        this.speed = cfg.START_SPEED;

        this.zAccumulator = 0.0f;
        this.rotationAngle = 0.0f;

        if(rotationMode < 0) {
            rotationMode = 0;
        }
        else if(rotationMode > 6) {
            rotationMode = 0;
        }
    }

    public void update(float deltaTime) {


    	if(timer.isElapsed()) {
    		timer.reset();
    		if(rotationMode < 7) {
    			rotationMode++;
    		}
    	}

    	
    	if(!effects.isInitialized()) {
    		effects.updateFadeAlpha(deltaTime);
    	}
    	
    	int newLayers = 0;
    	Layer removeLayer = null;
    	
    	float zMovement = speed * deltaTime;
    	zAccumulator += zMovement;

    	while (zAccumulator >= layerDistance) {
    	    zAccumulator -= layerDistance;
    	    newLayers++;
    	}
    	
    	for (Layer layer : layers) {

    	    boolean removeThis = false;

    	    for (Layer.Coordinate3D coord : layer.getCoordinates()) {
    	        coord.z -= zMovement;
    	        if (coord.z < removeLayerDistance) {
    	            removeThis = true;
    	        }
    	    }

    	    if (removeThis) {
    	        removeLayer = layer;
    	    }

    	    if (rotationMode - 3 > 0) {
    	        layer.setRotationAngle(effects.calculateLayerAngle(layer, deltaTime));
    	    }
    	}
        
        for(int l = 0; l < newLayers; l++) {
        	float[] rgba = effects.generateRandomRGBA();
            layers.add(0, new Layer(rgba, initZ));
        }
        
        if(removeLayer != null) {
        	layers.remove(removeLayer);
        }
        
        if(rotationMode - 4 < 0) {
        	rotationAngle = effects.calculateRotationAngle(rotationAngle, deltaTime);
        }
    }

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
        
        if(!effects.isInitialized() && !ar.debugMode) {
        	renderFadeLayer();
        }
        
    }
    
    public void renderFadeLayer() {
        float fadeAlpha = effects.getFadeAlpha();

        // Kein Texturbild verwenden – nur Farbe
        glBindTexture(GL_TEXTURE_2D, 0);

        // Hole Uniform-Locations
        int shader = ar.getShaderProgram();
        int colorLocation = glGetUniformLocation(shader, "layerColor");
        int offsetLocation = glGetUniformLocation(shader, "offset");
        int scaleLocation = glGetUniformLocation(shader, "scale");
        int aspectLocation = glGetUniformLocation(shader, "aspect");

        // 🟡 Wichtig: Werte "neutral" setzen, damit du den kompletten Bildschirm überdeckst
        glUniform4f(colorLocation, 0.0f, 0.0f, 0.0f, fadeAlpha); // Schwarz mit transparenz
        glUniform2f(offsetLocation, 0.0f, 0.0f);                 // Kein Versatz
        glUniform1f(scaleLocation, 2.0f);                        // Keine Skalierung
        glUniform1f(aspectLocation, 1.0f);                       // Kein Seitenverhältnis-Einfluss

        // Zeichne Vollbild-Quad (VAO ist das gleiche wie für normale Layer)
        glBindVertexArray(ar.getVaoId());
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindVertexArray(0);
    }

}
