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

/**
 * Entry point and main loop of the application.
 * Manages initialization of OpenGL, window creation, and the rendering loop.
 */
public class AppRunner {

    /**
     * GLFW window handle.
     */
    private long window;

    /**
     * Shader program handle.
     */
    private int shaderProgram;

    /**
     * Vertex Array Object handle.
     */
    private int vaoId;

    /**
     * OpenGL texture handle.
     */
    private int textureId;

    /**
     * Target FPS for the render loop.
     */
    private float FPS;

    /**
     * Main renderer instance.
     */
    private Renderer renderer;

    /**
     * Shader program manager.
     */
    private ShaderProgram shader;

    /**
     * Enables debug mode if true.
     */
    public boolean debugMode;

    /**
     * Starts the application. Initializes context, enters render loop, and cleans up.
     */
    public void run() {
        init();
        loop();
        cleanup();
    }

    /**
     * Initializes GLFW, OpenGL context, shader, VAO and texture resources.
     */
    private void init() {
        AppInitializer initializer = new AppInitializer();
        initializer.init();

        this.window = initializer.getWindow();
        this.shader = initializer.getShader();
        this.shaderProgram = shader.getShaderProgram();
        this.vaoId = initializer.getVaoId();
        this.textureId = initializer.getTextureId();
        this.debugMode = initializer.isDebugMode();
        this.FPS = initializer.getFPS();

        this.renderer = new Renderer(this, textureId, Config.get());
    }

    /**
     * Main rendering loop. Manages frame timing and calls update/render logic.
     */
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

    /**
     * Cleans up all OpenGL and GLFW resources before shutdown.
     */
    private void cleanup() {
        glDeleteProgram(shaderProgram);
        glDeleteVertexArrays(vaoId);
        glDeleteTextures(textureId);
        glfwDestroyWindow(window);
        glfwTerminate();
    }

    /**
     * Returns the OpenGL shader program ID.
     */
    public int getShaderProgram() {
        return shaderProgram;
    }

    /**
     * Returns the VAO ID used for rendering.
     */
    public int getVaoId() {
        return vaoId;
    }

    /**
     * Application entry point.
     */
    public static void main(String[] args) {
        new AppRunner().run();
    }
}