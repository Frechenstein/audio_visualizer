package main;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class AppRunner {

    private long window;
    private int shaderProgram;
    private int vaoId;
    private int textureId;
    private float zoom = 1.0f; // Zoomwert, wird im Laufe der Zeit verändert

    // Fensterbreite und -höhe (können dynamisch sein)
    private int windowWidth = 1280;
    private int windowHeight = 720;
    
    private Renderer renderer;

    public void run() {
        init();
        loop();
        cleanup();
    }

    private void init() {
        // Fehler-Callback einrichten
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) {
            throw new IllegalStateException("GLFW initialisation failed");
        }

        // Fenster-Hinweise (OpenGL 3.3 Core)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        // Für MacOS: glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

        // Fenster erstellen
        window = glfwCreateWindow(windowWidth, windowHeight, "OpenGL LWJGL", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Creation of window failed");
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1); // V-Sync
        glfwShowWindow(window);

        // OpenGL-Funktionen laden
        GL.createCapabilities();

        // Setze den Viewport explizit
        glViewport(0, 0, windowWidth, windowHeight);

        // Blending aktivieren (für transparente Bereiche)
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // Shader-Programm erstellen
        shaderProgram = createShaderProgram();

        // Erstelle ein Quad (VBO/VAO) für die Textur
        vaoId = createQuad();

        // Lade die Textur (128x128, mit transparentem Hintergrund)
        textureId = loadTexture("src/main/res/galaxy.png");
    }

    /**
     * Erstellt ein einfaches Shader-Programm mit Vertex- und Fragment-Shader.
     * Der Vertex-Shader erhält eine Uniform "scale" (Zoom) sowie "aspect" für die Korrektur des Seitenverhältnisses.
     */
    private int createShaderProgram() {
        String vertexShaderSource =
                "#version 330 core\n" +
                "layout(location = 0) in vec2 position;\n" +
                "layout(location = 1) in vec2 texCoords;\n" +
                "uniform float scale;\n" +
                "uniform float aspect;\n" +  // aspect = windowWidth / windowHeight
                "out vec2 passTexCoords;\n" +
                "void main(){\n" +
                "    // Korrigiere die x-Komponente, sodass ein quadratischer Effekt entsteht\n" +
                "    gl_Position = vec4(scale * (position.x / aspect), scale * position.y, 0.0, 1.0);\n" +
                "    passTexCoords = texCoords;\n" +
                "}\n";

        String fragmentShaderSource =
                "#version 330 core\n" +
                "in vec2 passTexCoords;\n" +
                "out vec4 outColor;\n" +
                "uniform sampler2D texSampler;\n" +
                "void main(){\n" +
                "    outColor = texture(texSampler, passTexCoords);\n" +
                "}\n";

        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexShaderSource);
        glCompileShader(vertexShader);
        if (glGetShaderi(vertexShader, GL_COMPILE_STATUS) == GL_FALSE) {
            System.err.println("Vertex shader compile error: " + glGetShaderInfoLog(vertexShader));
        }

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentShaderSource);
        glCompileShader(fragmentShader);
        if (glGetShaderi(fragmentShader, GL_COMPILE_STATUS) == GL_FALSE) {
            System.err.println("Fragment shader compile error: " + glGetShaderInfoLog(fragmentShader));
        }

        int program = glCreateProgram();
        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);
        glLinkProgram(program);
        if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
            System.err.println("Shader program linking error: " + glGetProgramInfoLog(program));
        }

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);

        return program;
    }
    
    public int getShaderProgram() {
    	return shaderProgram;
    }

    /**
     * Erzeugt ein VAO und VBO für ein einfaches Quad, bestehend aus zwei Dreiecken.
     * Die Vertex-Daten beinhalten die Position und die Texturkoordinaten.
     */
    private int createQuad() {
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
    
    public int getVaoId() {
    	return vaoId;
    }

    /**
     * Lädt eine PNG-Datei mithilfe von STBImage und erstellt eine OpenGL-Textur.
     * Es wird mit 4 Kanälen (RGBA) geladen, damit dein Bild einen Alphakanal hat.
     * Der Pfad muss relativ zum Projekt sein.
     */
    private int loadTexture(String filePath) {
        int texId;
        try (MemoryStack stack = stackPush()) {
            IntBuffer width  = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            // Bild vertikal spiegeln (damit die Textur richtig angezeigt wird)
            STBImage.stbi_set_flip_vertically_on_load(true);
            // Erzwinge 4 Kanäle (RGBA)
            ByteBuffer imageData = STBImage.stbi_load(filePath, width, height, channels, 4);
            if(imageData == null) {
                throw new RuntimeException("Failed to load texture file: " + STBImage.stbi_failure_reason());
            }

            texId = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, texId);

            // Sicherstellen, dass die Zeilenbreite korrekt aufgelöst wird
            glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

            // Setze Textur-Parameter:
            // Verwende CLAMP_TO_EDGE, damit an den Rändern nichts wiederholt wird
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

            // Lade die Texturdaten in die GPU
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width.get(0), height.get(0),
                    0, GL_RGBA, GL_UNSIGNED_BYTE, imageData);
            glGenerateMipmap(GL_TEXTURE_2D);

            STBImage.stbi_image_free(imageData);
        }
        return texId;
    }

    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            // Aktiviere das Shader-Programm
            glUseProgram(shaderProgram);

            // Setze den Zoom-Wert (scale) und das Aspect-Verhältnis (width/height)
            zoom += 0.001f;  // Passe diesen Wert nach Bedarf an
            int scaleLocation = glGetUniformLocation(shaderProgram, "scale");
            glUniform1f(scaleLocation, zoom);
            int aspectLocation = glGetUniformLocation(shaderProgram, "aspect");
            glUniform1f(aspectLocation, (float) windowWidth / windowHeight);

            // Binde die Textur
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, textureId);

            // Zeichne das Quad
            glBindVertexArray(vaoId);
            glDrawArrays(GL_TRIANGLES, 0, 6);
            glBindVertexArray(0);

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    private void cleanup() {
        glDeleteProgram(shaderProgram);
        glDeleteVertexArrays(vaoId);
        glDeleteTextures(textureId);
        glfwDestroyWindow(window);
        glfwTerminate();
    }

    public static void main(String[] args) {
        new AppRunner().run();
    }
}
