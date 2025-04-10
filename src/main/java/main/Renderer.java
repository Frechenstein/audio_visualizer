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
    
    public Renderer(AppRunner ar, int textureId, int windowWidth, int windowHeight) {
        this.ar = ar;
    	this.textureId = textureId;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        layers = new ArrayList<>();
    }
    
    public Renderer(List<Layer> layers, int textureId, int windowWidth, int windowHeight) {
        this.layers = layers;
        this.textureId = textureId;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
    }
    
    // Aktualisiert ggf. Logik wie Bewegung in Z-Richtung (Zoom, etc.)
    public void update() {
        // Beispiel: update für jeden Layer; z.B. könnte man die z-Koordinate für den Zoom anpassen
        for (Layer layer : layers) {
            for (Layer.Coordinate3D coord : layer.getCoordinates()) {
                // Simuliere, dass jedes Objekt in Richtung Kamera rückt:
                coord.z -= 4.0f;  // Bewegungsgeschwindigkeit in Z-Richtung
                if(coord.z < 50) {
                    coord.z = 800; // Reset, um den unendlichen Zoom-Effekt zu simulieren
                }
            }
        }
    }
    
    // Rendert alle Layer
    public void render() {
        // Beispiel: aktiviere den Shader, binde die Textur etc.
        // (Hier gehst du davon aus, dass du bereits ein Shader-Programm hast, das z. B. "scale" und "aspect" als Uniforms erwartet.)
        glUseProgram(ar.getShaderProgram());  // Ersetze durch deinen Shader-Programm-Handle
        
        // Setze das Fenster-/Seitenverhältnis als Uniform, wenn nötig:
        int aspectLocation = glGetUniformLocation(ar.getShaderProgram(), "aspect");
        glUniform1f(aspectLocation, (float)windowWidth / windowHeight);
        
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
                // Berechne den Skalierungsfaktor anhand der z-Koordinate
                float scale = focalLength / coord.z;
                // Berechne die Bildschirmposition:
                float screenX = centerX + (coord.x * scale);
                float screenY = centerY - (coord.y * scale);  // y-Achse invertiert (OpenGL Koordinatensystem)
                
                // Setze weitere Uniforms für Transformationen (z.B. Position und Scale)
                int positionLocation = glGetUniformLocation(ar.getShaderProgram(), "offset");
                glUniform2f(positionLocation, screenX, screenY);
                int scaleLocation = glGetUniformLocation(ar.getShaderProgram(), "scale");
                glUniform1f(scaleLocation, scale);
                
                // Zeichne dann ein Quad an dieser Position
                // Annahme: Du hast bereits einen VAO/VBO für ein quad erstellt
                glBindVertexArray(ar.getVaoId());
                glDrawArrays(GL_TRIANGLES, 0, 6);
                glBindVertexArray(0);
            }
        }
    }
}
