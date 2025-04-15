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
import main.Layer.Coordinate3D;

public class Renderer {
    
    private int windowWidth, windowHeight;
    AppRunner ar;
    
    private int textureId;
	
    private List<Layer> layers;

    private float focalLength = 300.0f;
    float speed = 480.0f;
    
    private float rotationAngle = 0.0f; 
    float rotationSpeed = 25.0f;
    int rotationMode = 0; // 0: no rotation, 1: whole shape rotates, 2: layers rotate separate
    
    private float baseScale = 0.15f;
    
    public Renderer(AppRunner ar, int textureId, int windowWidth, int windowHeight) {
        this.ar = ar;
    	this.textureId = textureId;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        layers = new ArrayList<>();
        
        float[] rgbaLayer1 = {1.0f, 1.0f, 1.0f, 1.0f};
        layers.add(new Layer(rgbaLayer1));
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
    	int newLayers = 0;
    	Layer removeLayer = null;
    	
        for (Layer layer : layers) {
        	
        	boolean summonNewLayer = false;
        	
            for (Layer.Coordinate3D coord : layer.getCoordinates()) { 
                coord.z -= speed * deltaTime; 
                if(coord.z == 4900 && !summonNewLayer) {
                	summonNewLayer = true;
                	newLayers++;
                }
                if(coord.z < 30) {
                    removeLayer = layer;
                }
            }
            
            if(rotationMode == 2) {
            	float angle = layer.getRotationAngle() + rotationSpeed * deltaTime;
                if (angle >= 360.0f) {
                    angle -= 360.0f;
                }
                layer.setRotationAngle(angle);
            }
        }
        
        for(int l = 0; l < newLayers; l++) {
            //float[] rgba = {1.0f, 1.0f, 1.0f, 1.0f};
        	float[] rgba = {(float) Math.random(), (float) Math.random(), (float) Math.random(), (float) Math.random()};
            layers.add(0, new Layer(rgba));
        }
        
        if(removeLayer != null) {
        	layers.remove(removeLayer);
        }
        
        if(rotationMode == 1) {
            rotationAngle += rotationSpeed * deltaTime;
            if (rotationAngle >= 360.0f) {
                rotationAngle -= 360.0f;
            }
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
        
        float angleRad = 0.0f;
        float cosAngle = 1.0f;
        float sinAngle = 0.0f;
        
        if(rotationMode == 0 || rotationMode == 1) {
            angleRad = (float) Math.toRadians(rotationAngle);
            cosAngle = (float) Math.cos(angleRad);
            sinAngle = (float) Math.sin(angleRad);
            /*System.out.println("RotAngle: " + rotationAngle + " angleRad: " 
                    + angleRad + " cosAngle: " + cosAngle + " sinAngle: " + sinAngle);*/
        }
        
        for (Layer layer : layers) {
            int colorLocation = glGetUniformLocation(ar.getShaderProgram(), "layerColor");
            glUniform4f(colorLocation, layer.getColor()[0], layer.getColor()[1], layer.getColor()[2], layer.getColor()[3]);
            
            for (Layer.Coordinate3D coord : layer.getCoordinates()) {
            	
            	if(rotationMode == 2) {
                    angleRad = (float) Math.toRadians(layer.getRotationAngle());
                    cosAngle = (float) Math.cos(angleRad);
                    sinAngle = (float) Math.sin(angleRad);
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
