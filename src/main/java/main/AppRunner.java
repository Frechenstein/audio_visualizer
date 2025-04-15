package main;

import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetMonitors;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.system.MemoryUtil.NULL;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import rendering.Quad;
import rendering.Renderer;
import rendering.ShaderProgram;

public class AppRunner {

    private long window;
    private int shaderProgram;
    private int vaoId;
    private int textureId;

    // Fensterbreite und -höhe (können dynamisch sein)
    private int windowWidth = 1920;
    private int windowHeight = 1080;
    private boolean fullscreen = true;
    
    private int VIRTUAL_WIDTH = 1920; // 16:9
    //private int VIRTUAL_WIDTH = 1620; // 3:2
    private int VIRTUAL_HEIGHT = 1080;
    
    private boolean debugMode = true;
    
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
        	// Hole alle verfügbaren Monitore
        	PointerBuffer monitors = glfwGetMonitors();
        	if (monitors == null || monitors.limit() == 0) {
        	    throw new RuntimeException("Keine Monitore gefunden");
        	}

        	// Wähle den gewünschten Monitor aus (z.B. 2. Monitor, falls vorhanden)
        	if (monitors.limit() >= 2) {
        		monitor = monitors.get(1); // Index 1 entspricht dem zweiten Monitor
        	} else {
        		monitor = glfwGetPrimaryMonitor(); // Fallback auf den primären Monitor
        	}

        	// Hole den Video-Modus des ausgewählten Monitors
        	var vidMode = glfwGetVideoMode(monitor);

        	// Setze Fensterbreite und -höhe anhand des gewählten Monitors
        	windowWidth = vidMode.width();
        	windowHeight = vidMode.height();
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
        
        this.renderer = new Renderer(this, textureId, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
    }
    
    /**
     * Unfinished - too much lag when using 
     */
    /*
    private void updateViewport() {
    	double targetAspect = (double) VIRTUAL_WIDTH / VIRTUAL_HEIGHT;
    	double windowAspect = (double) windowWidth / windowHeight;
    	
    	int vpX = 0;
    	int vpY = 0;
    	int vpWidth = windowWidth;
    	int vpHeight = windowHeight;
    	
    	if(windowAspect > targetAspect) {
    		vpWidth = (int)(windowHeight * targetAspect);
    		vpX = (windowWidth - vpWidth) / 2;
    	} else if (windowAspect < targetAspect) {
    		vpHeight = (int)(windowWidth / targetAspect);
    		vpY = (windowHeight - vpHeight) / 2;
    	}
    	
    	glViewport(vpX, vpY, vpWidth, vpHeight);
    }
    */
    
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
            while (accumulator >= targetDeltaTime && !debugMode) {
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