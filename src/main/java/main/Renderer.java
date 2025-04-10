package main;

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

public class Renderer {
    
    private List<Layer> layers;
    // Beispielhafte Parameter für die Perspektive (z. B. focalLength und den Bildschirmmittelpunkt)
    private float focalLength = 300.0f;
    private int windowWidth, windowHeight;
    AppRunner ar;
    
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
    public void update() {
        // Beispiel: update für jeden Layer; z.B. könnte man die z-Koordinate für den Zoom anpassen
    	
    	int newLayers = 0;
    	Layer removeLayer = null;
    	
        for (Layer layer : layers) {
        	
        	boolean summonNewLayer = false;
        	
            for (Layer.Coordinate3D coord : layer.getCoordinates()) {
                // Simuliere, dass jedes Objekt in Richtung Kamera rückt:
                coord.z -= 2.0f;  // Bewegungsgeschwindigkeit in Z-Richtung
                if(coord.z == 2900 && !summonNewLayer) {
                	summonNewLayer = true;
                	newLayers++;
                }
                if(coord.z < 30) {
                    removeLayer = layer; // Reset, um den unendlichen Zoom-Effekt zu simulieren
                }
            }
        }
        
        for(int l = 0; l < newLayers; l++) {
            float[] rgbaLayer1 = {1.0f, 1.0f, 1.0f, 1.0f};
            layers.add(new Layer(rgbaLayer1));
        }
        
        if(removeLayer != null) {
        	layers.remove(removeLayer);
        }
        
        
    }
    
    // Rendert alle Layer
    public void render() {
        // Beispiel: aktiviere den Shader, binde die Textur etc.
        // (Hier gehst du davon aus, dass du bereits ein Shader-Programm hast, das z. B. "scale" und "aspect" als Uniforms erwartet.)
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
        // Hier kannst du z.B. eine Uniform "layerColor" erwarten:
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
                // Annahme: Du hast bereits einen VAO/VBO für ein quad erstellt
                glBindVertexArray(ar.getVaoId());
                glDrawArrays(GL_TRIANGLES, 0, 6);
                glBindVertexArray(0);
            }
        }
    }
}
