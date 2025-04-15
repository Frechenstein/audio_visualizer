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
	
    private List<Layer> layers;

    private float focalLength = 300.0f;
    float speed = 480.0f;
    
    // Hier übergibst du auch, welche Textur genutzt werden soll (dies könnte je nach Layer variieren)
    private int textureId;
    
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
        }
        
        for(int l = 0; l < newLayers; l++) {
            //float[] rgba = {1.0f, 1.0f, 1.0f, 1.0f};
        	float[] rgba = {(float) Math.random(), (float) Math.random(), (float) Math.random(), (float) Math.random()};
            layers.add(0, new Layer(rgba));
        }
        
        if(removeLayer != null) {
        	layers.remove(removeLayer);
        }
        
        
    }
    
    // Rendert alle Layer
    public void render() {
        glUseProgram(ar.getShaderProgram());  // Ersetze durch deinen Shader-Programm-Handle
        
        // Setze das Fenster-/Seitenverhältnis als Uniform, wenn nötig:
        int aspectLocation = glGetUniformLocation(ar.getShaderProgram(), "aspect");
        glUniform1f(aspectLocation, (float) windowWidth / windowHeight);
        
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureId);
        
        // Mittlere Bildschirmkoordinaten
        float centerX = windowWidth / 2.0f;
        float centerY = windowHeight / 2.0f;
        
        // Für jeden Layer die Farbe als Uniform setzen
        for (Layer layer : layers) {
            int colorLocation = glGetUniformLocation(ar.getShaderProgram(), "layerColor");
            glUniform4f(colorLocation, layer.getColor()[0], layer.getColor()[1], layer.getColor()[2], layer.getColor()[3]);
            
            // Nun iteriere über jede 3D-Koordinate in diesem Layer:
            for (Layer.Coordinate3D coord : layer.getCoordinates()) {
                // Berechne den perspektivischen Skalierungsfaktor
                float computedScale = focalLength / coord.z;
                // Multipliziere mit dem globalen Basis-Skalierungsfaktor:
                float finalScale = computedScale * baseScale;
                
                // Berechne die Bildschirmposition:
                float screenX = centerX + (coord.x * computedScale);
                float screenY = centerY - (coord.y * computedScale);  // y-Achse invertiert (OpenGL Koordinatensystem)
                
             // Beispiel für die Berechnung des Offsets aus den Bildschirmkoordinaten:
                float ndcX = (screenX / (windowWidth / 2.0f)) - 1.0f;
                float ndcY = 1.0f - (screenY / (windowHeight / 2.0f));
                // Jetzt setze den Uniform "offset"
                int offsetLocation = glGetUniformLocation(ar.getShaderProgram(), "offset");
                glUniform2f(offsetLocation, ndcX, ndcY);
                int scaleLocation = glGetUniformLocation(ar.getShaderProgram(), "scale");
                glUniform1f(scaleLocation, finalScale);
                
                // Zeichne dann ein Quad an dieser Position
                glBindVertexArray(ar.getVaoId());
                glDrawArrays(GL_TRIANGLES, 0, 6);
                glBindVertexArray(0);
            }
        }
    }
}
