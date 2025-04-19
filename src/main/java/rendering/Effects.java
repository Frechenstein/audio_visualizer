package rendering;

import java.util.ArrayList;

import layerdata.Layer;
import main.Config;

/**
 * Encapsulates all non‑rendering effects: fade‑in, whole‑scene rotation,
 * per‑layer rotation/oscillation, and random RGBA generation.
 */
public class Effects {
	
	Renderer renderer;

	private float fadeAlpha;
	private float fadeSpeed;
	private boolean initialized = false;

	private float alphaClamp;
	
	private float idleSpeed;

	int initFrontLayerDistance;

	int initZ;
	private int layerDistance;

	private float wholeRotationSpeed, wholeOscillationSpeed, wholeSwingAmplitude;
	private float wholeRotationTime;

	private float layerRotationSpeed, layerOscillationSpeed, layerSwingAmplitude;
	
	public Effects(Renderer renderer, Config cfg) {
		this.renderer = renderer;
		initializeSettings(cfg);


	}

	/**
	 * Initializes all parameters according to global configuration
	 *
	 * @param cfg global configuration object
	 */
	private void initializeSettings(Config cfg) {
		this.fadeAlpha = cfg.INIT_FADE_ALPHA;
		this.fadeSpeed = cfg.FADE_SPEED;

		this.idleSpeed = cfg.IDLE_SPEED;
		this.initFrontLayerDistance = cfg.INIT_FRONT_DISTANCE;

		this.alphaClamp = cfg.idleAlphaClamp;

		this.initZ = cfg.initZ;
		this.layerDistance = cfg.LAYER_DISTANCE;

		this.wholeRotationSpeed = cfg.wholeRotationSpeed;
		this.wholeOscillationSpeed = cfg.wholeOscillationSpeed;
		this.wholeSwingAmplitude = cfg.wholeSwingAmplitude;
		this.wholeRotationTime = 0.0f;

		this.layerRotationSpeed = cfg.layerRotationSpeed;
		this.layerOscillationSpeed = cfg.layerOscillationSpeed;
		this.layerSwingAmplitude = cfg.layerSwingAmplitude;

	}

	/**
	 * Spawns initial layers evenly spaced between initFrontZ and renderer.initZ.
	 */
	public ArrayList<Layer> createInitialLayers() {
		int currentZ = initFrontLayerDistance;
		ArrayList<Layer> layers = new ArrayList<>();
		
		while(currentZ < initZ) {
        	float[] rgba = generateRandomRGBA();
            layers.add(new Layer(rgba, currentZ));
            currentZ += layerDistance;
		}
		
		return layers;
	}

	/**
	 * Advances the fade‑in animation and ramps renderer.speed toward idleSpeed.
	 */
    public void updateFadeAlpha(float deltaTime) {
    	fadeAlpha -= fadeSpeed * deltaTime;
    	if (fadeAlpha <= 0.0f) {
    		fadeAlpha = 0.0f;
            initialized = true;
        }
    	if(renderer.speed < idleSpeed) {
    		renderer.speed += 1.0f;
        }
    }

	/**
	 * @return true once fade‑in has completed
	 */
    public boolean isInitialized() {
        return initialized;
    }

	/**
	 * @return current alpha of fade overlay in [0,1]
	 */
    public float getFadeAlpha() {
        return fadeAlpha;
    }

	/**
	 * @return a random RGBA array with alpha clamped to a minimum
	 */
	public float[] generateRandomRGBA() {
    	float alpha = clampAlpha((float) Math.random());
    	float[] rgba = {(float) Math.random(), 
    			(float) Math.random(), 
    			(float) Math.random(),
				alpha};
    	return rgba;
    }

	/**
	 * Ensures the alpha channel for a layer is not below a configured minimum.
	 *
	 * @param alpha  the original alpha value in the range [0,1]
	 * @return       the clamped alpha, never less than {@link #alphaClamp}
	 */
	public float clampAlpha(float alpha) {
    	if(alpha < alphaClamp) {
    		alpha = alphaClamp;
    	}
    	return alpha;
    }

	/**
	 * Computes the new rotation angle for the entire scene based on the current mode.
	 * <ul>
	 *   <li>Mode 1: rotate clockwise at constant speed.</li>
	 *   <li>Mode 2: rotate counter‑clockwise at constant speed.</li>
	 *   <li>Mode 3: oscillate back‑and‑forth using a sine wave.</li>
	 * </ul>
	 * The result is normalized into [0,360).
	 *
	 * @param rotationAngle  the current rotation angle in degrees
	 * @param deltaTime      elapsed time since last frame, in seconds
	 * @return               the updated rotation angle in degrees, wrapped to [0,360)
	 */
	public float calculateRotationAngle(float rotationAngle, float deltaTime) {
		
        switch(renderer.rotationMode) {
    	case 1:
			// Clockwise constant rotation
    		rotationAngle -= wholeRotationSpeed * deltaTime;
    		break;
    	case 2:
			// Counter‑clockwise constant rotation
    		rotationAngle += wholeRotationSpeed * deltaTime;
    		break;
    	case 3:
			// Oscillating rotation: sin wave between ±swingAmplitude
    	    wholeRotationTime += deltaTime;
    	    rotationAngle = (float) (Math.sin(wholeRotationTime * wholeOscillationSpeed) * wholeSwingAmplitude);
    		break;
        }

		// Wrap into [0,360)
	    if(rotationAngle >= 360.0f) rotationAngle -= 360.0f;
	    if(rotationAngle < 0.0f) rotationAngle += 360.0f;
		
		return rotationAngle;
	}

	/**
	 * Computes the new rotation angle for an individual layer based on the current mode.
	 * <ul>
	 *   <li>Mode 4: rotate each layer clockwise at constant speed.</li>
	 *   <li>Mode 5: rotate each layer counter‑clockwise at constant speed.</li>
	 *   <li>Mode 6: oscillate each layer back‑and‑forth using a sine wave.</li>
	 * </ul>
	 * The result is normalized into [0,360).
	 *
	 * @param layer      the layer whose rotation to update; its internal time field
	 *                   is advanced for oscillation modes
	 * @param deltaTime  elapsed time since last frame, in seconds
	 * @return           the updated rotation angle in degrees for this layer, wrapped to [0,360)
	 */
	public float calculateLayerAngle(Layer layer, float deltaTime) {
		float rotationAngle = layer.getRotationAngle();
		switch(renderer.rotationMode) {
		case 4:
			// Clockwise rotation per layer
	    	rotationAngle -= layerRotationSpeed * deltaTime;
	        break;
		case 5:
			// Counter‑clockwise rotation per layer
			rotationAngle += layerRotationSpeed * deltaTime;
	        break;
		case 6:
			// Oscillating rotation per layer: sin wave
			layer.time += deltaTime;
		    rotationAngle = (float) (Math.sin(layer.time * layerOscillationSpeed) * layerSwingAmplitude);
		    break;
	    }

		// Wrap into [0,360)
	    if(rotationAngle >= 360.0f) rotationAngle -= 360.0f;
	    if(rotationAngle < 0.0f) rotationAngle += 360.0f;
	    
		return rotationAngle;
	}

	/**
	 * Precomputes cos/sin for a given angle in degrees.
	 * @return float[]{cos(angle), sin(angle)}
	 */
	public float[] getAngles(float rotationAngle) {
        if(renderer.rotationMode > 0) {
        	return calculateAngles(rotationAngle);
        }
        return new float[]{1.0f, 0.0f};
	}
	
	public float[] calculateAngles(float rotationAngle) {
		float[] angles = new float[2];
		float angleRad = (float) Math.toRadians(rotationAngle);
		angles[0] = (float) Math.cos(angleRad);
		angles[1] = (float) Math.sin(angleRad);
		return angles;
	}

}
