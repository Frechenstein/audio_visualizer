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
import utility.Utils;

public class AppRunner {

    private long window;
    private int shaderProgram;
    private int vaoId;
    private int textureId;

    private float FPS;

    private Renderer renderer;
    private ShaderProgram shader;

    public boolean debugMode;

    public void run() {
        init();
        loop();
        cleanup();
    }

    private void init() {
        Config cfg = Config.get();

        this.debugMode = cfg.DEBUG_MODE;

        // create error callback
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
        
        if(cfg.fullscreen && !debugMode) {
        	if(cfg.SURFACE_MODE) cfg.virtualWidth = cfg.surfaceVirtualWidth;

        	PointerBuffer monitors = glfwGetMonitors();
        	if (monitors == null || monitors.limit() == 0) {
        	    throw new RuntimeException("Keine Monitore gefunden");
        	}

        	// choose screen for fullscreen if present - else primary screen
        	if (monitors.limit() >= 2) {
        		monitor = monitors.get(cfg.screenIndex);
        	} else {
        		monitor = glfwGetPrimaryMonitor();
        	}

        	var vidMode = glfwGetVideoMode(monitor);

        	// dimensions based on chosen screen
        	cfg.windowWidth = vidMode.width();
        	cfg.windowHeight = vidMode.height();
        }

        // create window
        window = glfwCreateWindow(cfg.windowWidth, cfg.windowHeight, cfg.WINDOW_TITLE, monitor, NULL);
        if (window == NULL) {
            throw new RuntimeException("Creation of window failed");
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(cfg.V_SYNC); // V-Sync
        glfwShowWindow(window);

        GL.createCapabilities();

        glViewport(0, 0, cfg.windowWidth, cfg.windowHeight);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // create shader program
        shader = new ShaderProgram();
        shaderProgram = shader.getShaderProgram();

        // create quad (VBO/VAO) for the texture
        vaoId = Quad.createQuad();

        // loading texture
        textureId = Utils.loadTexture(cfg.TEXTURE_PATH);
        
        if(debugMode) {
            this.FPS = cfg.debugFps;
        	cfg.initZ = cfg.debugInitZ;
        } else {
            this.FPS = cfg.fps;
        }
        
        this.renderer = new Renderer(this, textureId, cfg);
    }
    
    public int getShaderProgram() {
    	return shaderProgram;
    }
    
    public int getVaoId() {
    	return vaoId;
    }

    private void loop() {
        double targetDeltaTime = 1.0 / FPS;
        double lastTime = glfwGetTime();
        double accumulator = 0.0;
        
        while (!glfwWindowShouldClose(window)) {
            double currentTime = glfwGetTime();
            double frameTime = currentTime - lastTime;
            lastTime = currentTime;
            accumulator += frameTime;
            
            // update
            while (accumulator >= targetDeltaTime && !debugMode) {
                renderer.update((float) targetDeltaTime);
                accumulator -= targetDeltaTime;
            }
            
            // rendering
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