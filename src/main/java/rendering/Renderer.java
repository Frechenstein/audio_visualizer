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

import layerdata.Coordinate2D;
import main.AppRunner;
import layerdata.Layer;
import main.Config;
import utility.Timer;

/**
 * Main renderer for the audio visualizer.
 * Controls layer spawning, movement along Z, global rotation, fade‑in/out overlay,
 * and submits draw calls to OpenGL.
 */
public class Renderer {

    AppRunner ar;
    Effects effects;
    Timer timer;

    private int uAspect;
    private int uOffset;
    private int uScale;
    private int uLayerColor;

    private int windowWidth, windowHeight;
    
    private final int textureId;
    private float baseScale;

    /** All active layers in depth order (0 = frontmost) */
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
     * 4-6: every layer has rotation; 4: counterclockwise; 5; clockwise; 6: back and forth
     * 7: no rotation but keeps layer rotation
     */
    int rotationMode = 0;

    // animation calibrated for initZ = 10000 and timer 10.0 seconds
    int[] rotationAnimation = {0, 1, 2, 2, 1, 4, 5, 4, 5, 7, 7, 6, 6, 7, 7};
    int animationCounter = 0;

    /**
     * Constructs the renderer, loads settings, spawns initial layers or debug layer.
     *
     * @param ar         the main application runner (provides GL handle)
     * @param textureId  OpenGL ID of the texture to use
     * @param cfg        global configuration object
     */
    public Renderer(AppRunner ar, int textureId, Config cfg) {
        this.ar = ar;
    	this.textureId = textureId;

        initializeSettings(cfg);

        timer = new Timer(10.0);
        
        effects = new Effects(this, cfg);
        
        if(ar.debugMode) {
        	layers = new ArrayList<>();
        	layers.add(new Layer(cfg.DEBUG_RGBA, cfg.debugInitZ));
        } else {
            layers = effects.createInitialLayers();
        }
    }

    /**
     * Initializes all parameters according to global configuration
     *
     * @param cfg global configuration object
     */
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

        int prog = ar.getShaderProgram();
        uAspect     = glGetUniformLocation(prog, "aspect");
        uOffset     = glGetUniformLocation(prog, "offset");
        uScale      = glGetUniformLocation(prog, "scale");
        uLayerColor = glGetUniformLocation(prog, "layerColor");

    }

    /**
     * Updates scene state: fade, rotation, layer movement & spawn.
     *
     * @param deltaTime  seconds since last frame
     */
    public void update(float deltaTime) {

        // Rotate mode cycling
    	if(timer.isElapsed()) {
    		timer.reset();
            /*
    		if(rotationMode < 7) {
    			rotationMode++;
    		} else {
                rotationMode = 0;
            }*/
            if(animationCounter >= rotationAnimation.length) animationCounter = 0;
            rotationMode = rotationAnimation[animationCounter];
            animationCounter++;
    	}

        // Fade-in until complete
    	if(!effects.isInitialized()) {
    		effects.updateFadeAlpha(deltaTime);
    	}
    	
    	int newLayers = 0;
    	Layer removeLayer = null;

        // Move layers and spawn new ones at fixed Z intervals
    	float zMovement = speed * deltaTime;
    	zAccumulator += zMovement;

    	while (zAccumulator >= layerDistance) {
    	    zAccumulator -= layerDistance;
    	    newLayers++;
    	}
    	
    	for (Layer layer : layers) {

            layer.setZ(layer.getZ() - zMovement);
            if(layer.getZ() < removeLayerDistance) {
                removeLayer = layer;
            }

    	    if (rotationMode >= 4) {
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
        
        if(rotationMode >= 1 && rotationMode <= 3) {
        	rotationAngle = effects.calculateRotationAngle(rotationAngle, deltaTime);
        }
    }

    /**
     * Renders all layers and (if still fading) the fullscreen fade overlay.
     */
    public void render() {
        glUseProgram(ar.getShaderProgram()); 

        glUniform1f(uAspect, (float) windowWidth / windowHeight);
        
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureId);
        
        float centerX = windowWidth / 2.0f;
        float centerY = windowHeight / 2.0f;
        
        float cosAngle = 1.0f;
        float sinAngle = 0.0f;

        // Precompute global rotation if needed
        if(rotationMode >= 1 && rotationMode <= 3) {
    		float[] angles = effects.getAngles(rotationAngle);
            cosAngle = angles[0];
            sinAngle = angles[1];
        }

        // Draw each layer
        for (Layer layer : layers) {
            glUniform4f(uLayerColor, layer.getColor()[0], layer.getColor()[1], layer.getColor()[2], layer.getColor()[3]);

            float layerZ = layer.getZ();
            float computedScale = focalLength / layerZ;
            float finalScale = computedScale * baseScale;

            // Per-layer override rotation
            if(rotationMode >= 4) {
                float[] angles = effects.getAngles(layer.getRotationAngle());
                cosAngle = angles[0];
                sinAngle = angles[1];
            }

            for (Coordinate2D coord : layer.getCoordinates()) {

                // Rotate point
                float rotatedX = cosAngle * coord.x - sinAngle * coord.y;
                float rotatedY = sinAngle * coord.x + cosAngle * coord.y;

                // Project & offset to screen
                float screenX = centerX + (rotatedX * computedScale);
                float screenY = centerY - (rotatedY * computedScale);
                
                float ndcX = (screenX / (windowWidth / 2.0f)) - 1.0f;
                float ndcY = 1.0f - (screenY / (windowHeight / 2.0f));

                glUniform2f(uOffset, ndcX, ndcY);
                glUniform1f(uScale, finalScale);
                
                glBindVertexArray(ar.getVaoId());
                glDrawArrays(GL_TRIANGLES, 0, 6);
                glBindVertexArray(0);
            }
        }

        // Draw fade overlay last
        if(!effects.isInitialized() && !ar.debugMode) {
        	renderFadeLayer();
        }
        
    }
    
    public void renderFadeLayer() {
        float fadeAlpha = effects.getFadeAlpha();

        glBindTexture(GL_TEXTURE_2D, 0);

        glUniform4f(uLayerColor, 0.0f, 0.0f, 0.0f, fadeAlpha);
        glUniform2f(uOffset, 0.0f, 0.0f);
        glUniform1f(uScale, 2.0f);
        glUniform1f(uAspect, 1.0f);

        glBindVertexArray(ar.getVaoId());
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindVertexArray(0);
    }

}
