package main;

public final class Config {

    private static final Config INSTANCE = new Config();
    public static Config get() { return INSTANCE; }

    /**
     * ===== SYSTEM SETTINGS =====
     */
    public boolean fullscreen = false;
    // <editor-fold desc="SYSTEM SETTINGS">
    public final String WINDOW_TITLE = "AudioVis";

    public int windowWidth = 1920;
    public int windowHeight = 1080;
    public int virtualWidth = 1920; // 16:9
    public int virtualHeight = 1080;
    public int surfaceVirtualWidth = 1620; // 3:2
    public final boolean SURFACE_MODE = false;

    public int screenIndex = 0;

    public float fps = 120.0f;

    public final int V_SYNC = 1;

    // paths available: "src/main/res/~.png" ~ galaxy, time, kai;
    public final String TEXTURE_PATH = "src/main/res/galaxy.png";
    // </editor-fold>

    /**
     * ===== DEBUG SETTINGS =====
     */
    public final boolean DEBUG_MODE = false;
    // <editor-fold desc="DEBUG SETTINGS">
    public float debugFps = 60.0f;

    public int debugInitZ = 500;
    public final float[] DEBUG_RGBA = {1.0f, 1.0f, 1.0f, 1.0f};
    // </editor-fold>

    /**
     * ===== CONSTANTS =====
     */
    // <editor-fold desc="CONSTANTS">
    public final float BASE_IMAGE_SCALE = 0.15f;

    public final int INIT_FRONT_DISTANCE = 500;
    public final int LAYER_DISTANCE = 100;
    public final int REMOVE_LAYER_DISTANCE = 30;

    public final float FOCAL_LENGTH = 300.0f;

    public final float START_SPEED = 30.0f;
    public final float IDLE_SPEED = 450.0f;
    // </editor-fold>

    /**
     * ===== DYNAMIC PARAMETERS =====
     */
    // <editor-fold desc="DYNAMIC PARAMETERS">
    public int initZ = 5000;

    // </editor-fold>

    /**
     * ===== EFFECT SETTINGS =====
     */
    // <editor-fold desc="EFFECT SETTINGS">
    public final float INIT_FADE_ALPHA = 1.0f;
    public final float FADE_SPEED = 0.25f;

    public float idleAlphaClamp = 0.35f;

    public float wholeRotationSpeed = 20.0f;
    public float wholeOscillationSpeed = 0.35f;
    public float wholeSwingAmplitude = 25.0f;

    public float layerRotationSpeed = 20.0f;
    public float layerOscillationSpeed = 0.8f;
    public float layerSwingAmplitude = 30.0f;

    // </editor-fold>

    private Config() {}

}
