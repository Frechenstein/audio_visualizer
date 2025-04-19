package rendering;

import java.util.ArrayList;

import main.Layer;
import main.Config;

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

	private float wholeRotationSpeed;
	private float wholeOscillationSpeed;
    private float wholeSwingAmplitude;
	private float wholeRotationTime;

	private float layerRotationSpeed;
	private float layerOscillationSpeed;
	private float layerSwingAmplitude;
	
	public Effects(Renderer renderer, Config cfg) {
		this.renderer = renderer;
		initializeSettings(cfg);


	}

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

    public boolean isInitialized() {
        return initialized;
    }

    public float getFadeAlpha() {
        return fadeAlpha;
    }
    
    public float[] generateRandomRGBA() {
    	float alpha = clampAlpha((float) Math.random());
    	float[] rgba = {(float) Math.random(), 
    			(float) Math.random(), 
    			(float) Math.random(),
				alpha};
    	return rgba;
    }
    
    public float clampAlpha(float alpha) {
    	if(alpha < alphaClamp) {
    		alpha = alphaClamp;
    	}
    	return alpha;
    }
	
	public float calculateRotationAngle(float rotationAngle, float deltaTime) {
		
        switch(renderer.rotationMode) {
    	case 1:
    		rotationAngle -= wholeRotationSpeed * deltaTime;
    		break;
    	case 2:
    		rotationAngle += wholeRotationSpeed * deltaTime;
    		break;
    	case 3:
    	    wholeRotationTime += deltaTime;
    	    rotationAngle = (float) (Math.sin(wholeRotationTime * wholeOscillationSpeed) * wholeSwingAmplitude);
    		break;
        }
    		
	    if(rotationAngle >= 360.0f) rotationAngle -= 360.0f;
	    if(rotationAngle < 0.0f) rotationAngle += 360.0f;
		
		return rotationAngle;
	}
	
	public float calculateLayerAngle(Layer layer, float deltaTime) {
		float rotationAngle = layer.getRotationAngle();
		switch(renderer.rotationMode) {
		case 4:
	    	rotationAngle -= layerRotationSpeed * deltaTime;
	        break;
		case 5:
			rotationAngle += layerRotationSpeed * deltaTime;
	        break;
		case 6:
			layer.time += deltaTime;
		    rotationAngle = (float) (Math.sin(layer.time * layerOscillationSpeed) * layerSwingAmplitude);
		    break;
	    }
	    
	    if(rotationAngle >= 360.0f) rotationAngle -= 360.0f;
	    if(rotationAngle < 0.0f) rotationAngle += 360.0f;
	    
		return rotationAngle;
	}
	
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
