package main;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import rendering.Quad;
import rendering.ShaderProgram;
import utility.Utils;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Handles the initialization of GLFW, OpenGL, shader, texture and VAO.
 * Collects relevant objects and settings for use in AppRunner.
 */
public class AppInitializer {

    private long window;
    private int vaoId;
    private int textureId;
    private ShaderProgram shader;
    private float FPS;
    private boolean debugMode;

    /**
     * Initializes the window, OpenGL context, shader program, texture and quad.
     */
    public void init() {
        Config cfg = Config.get();
        this.debugMode = cfg.DEBUG_MODE;

        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) {
            throw new IllegalStateException("Failed to initialize GLFW");
        }

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        // For macOS: glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

        long monitor = NULL;

        if (cfg.fullscreen && !debugMode) {
            if (cfg.SURFACE_MODE) cfg.virtualWidth = cfg.surfaceVirtualWidth;

            PointerBuffer monitors = glfwGetMonitors();
            if (monitors == null || monitors.limit() == 0) {
                throw new RuntimeException("No monitors found");
            }

            monitor = (monitors.limit() >= 2)
                    ? monitors.get(cfg.screenIndex)
                    : glfwGetPrimaryMonitor();

            GLFWVidMode vidMode = glfwGetVideoMode(monitor);
            cfg.windowWidth = vidMode.width();
            cfg.windowHeight = vidMode.height();
        }

        window = createWindow(cfg.windowWidth, cfg.windowHeight, cfg.WINDOW_TITLE, monitor);

        glfwMakeContextCurrent(window);
        glfwSwapInterval(cfg.V_SYNC);
        glfwShowWindow(window);

        GL.createCapabilities();

        glViewport(0, 0, cfg.windowWidth, cfg.windowHeight);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        shader = new ShaderProgram();
        vaoId = Quad.createQuad();
        textureId = Utils.loadTexture(cfg.TEXTURE_PATH);

        FPS = debugMode ? cfg.debugFps : cfg.fps;
        if (debugMode) cfg.initZ = cfg.debugInitZ;
    }

    /**
     * Creates a GLFW window and handles errors.
     *
     * @param width Window width in pixels
     * @param height Window height in pixels
     * @param title Title of the window
     * @param monitor Monitor handle (NULL for windowed mode)
     * @return GLFW window handle
     */
    private long createWindow(int width, int height, String title, long monitor) {
        long handle = glfwCreateWindow(width, height, title, monitor, NULL);
        if (handle == NULL) {
            glfwTerminate();
            throw new RuntimeException("Failed to create GLFW window");
        }
        System.out.println("Window created: " + width + "x" + height);
        return handle;
    }

    /**
     * @return The GLFW window handle
     */
    public long getWindow() {
        return window;
    }

    /**
     * @return The VAO ID used for rendering
     */
    public int getVaoId() {
        return vaoId;
    }

    /**
     * @return The OpenGL texture ID
     */
    public int getTextureId() {
        return textureId;
    }

    /**
     * @return The compiled shader program object
     */
    public ShaderProgram getShader() {
        return shader;
    }

    /**
     * @return Target frames per second
     */
    public float getFPS() {
        return FPS;
    }

    /**
     * @return True if the application is running in debug mode
     */
    public boolean isDebugMode() {
        return debugMode;
    }
}
