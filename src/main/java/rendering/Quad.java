package rendering;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

/**
 * Provides a method to generate a simple textured quad for rendering.
 */
public class Quad {

    /**
     * Creates a VAO and VBO for a quad made of two triangles with position and texture coordinates.
     *
     * @return The generated VAO ID.
     */
    public static int createQuad() {
        // Vertex-Daten: Position (x, y) und TexCoords (s, t)
        float[] vertices = {
            // Position      // TexCoords
            -0.5f,  0.5f,    0.0f, 1.0f,  // oben links
            -0.5f, -0.5f,    0.0f, 0.0f,  // unten links
             0.5f, -0.5f,    1.0f, 0.0f,  // unten rechts

             0.5f, -0.5f,    1.0f, 0.0f,  // unten rechts
             0.5f,  0.5f,    1.0f, 1.0f,  // oben rechts
            -0.5f,  0.5f,    0.0f, 1.0f   // oben links
        };

        int vao = glGenVertexArrays();
        int vbo = glGenBuffers();

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        // Attribut 0: Position (2 float-Werte)
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        // Attribut 1: TexCoords (2 float-Werte)
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);
        glEnableVertexAttribArray(1);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        return vao;
    }

}
