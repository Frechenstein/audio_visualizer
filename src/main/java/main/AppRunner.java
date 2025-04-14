package main;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import rendering.Quad;
import rendering.Renderer;
import rendering.ShaderProgram;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.*;

public class AppRunner {

    private long window;
    private int shaderProgram;
    private int vaoId;
    private int textureId;

    // Fensterbreite und -höhe (können dynamisch sein)
    private int windowWidth = 1920;
    private int windowHeight = 1080;
    private boolean fullscreen = false;
    
    private Renderer renderer;
    private ShaderProgram shader;

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
        
        Long monitor = NULL;
        
        if(fullscreen) {
            long primaryMonitor = glfwGetPrimaryMonitor();
            var vidMode = glfwGetVideoMode(primaryMonitor);
            
            windowWidth = vidMode.width();
            windowHeight = vidMode.height();
            
            monitor = primaryMonitor;
        }

        // Fenster erstellen
        window = glfwCreateWindow(windowWidth, windowHeight, "AudioVis", monitor, NULL);
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
        shader = new ShaderProgram();
        shaderProgram = shader.getShaderProgram();

        // Erstelle ein Quad (VBO/VAO) für die Textur
        vaoId = Quad.createQuad();

        // Lade die Textur (128x128, mit transparentem Hintergrund)
        textureId = Utils.loadTexture("src/main/res/galaxy.png");
        
        this.renderer = new Renderer(this, textureId, windowWidth, windowHeight);
    }
    
    public int getShaderProgram() {
    	return shaderProgram;
    }
    
    public int getVaoId() {
    	return vaoId;
    }

    private void loop() {
        double targetDeltaTime = 1.0 / 120.0; // 60 Updates pro Sekunde
        double lastTime = glfwGetTime();
        double accumulator = 0.0;
        
        while (!glfwWindowShouldClose(window)) {
            double currentTime = glfwGetTime();
            double frameTime = currentTime - lastTime;
            lastTime = currentTime;
            accumulator += frameTime;
            
            // Führe so lange update-Schritte aus, wie das Akkumulator-Zeit-Fenster füllt:
            while (accumulator >= targetDeltaTime) {
                renderer.update((float) targetDeltaTime);
                accumulator -= targetDeltaTime;
            }
            
            // Den Frame rendern
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            renderer.render();
            
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