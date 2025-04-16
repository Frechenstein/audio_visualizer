package rendering;

import java.util.ArrayList;

import main.Layer;

public class Effects {
	
	Renderer renderer;
	
	private float speed = 450.0f;
	
	private float oszilatorSpeed = 0.35f;
    private float swingAmplitude = 25.0f;
	private float time = 0.0f;
	
	private float layerRotationSpeed = 20.0f;
	private float layerOszilationSpeed = 0.8f;
	private float layerSwingAmplitude = 30.0f;
	
    private float fadeAlpha = 1.0f;
    private float fadeSpeed = 0.25f; // Alpha pro Sekunde
    private boolean initialized = false;
	
	public Effects(Renderer renderer) {
		this.renderer = renderer;
	}
	
	public ArrayList<Layer> createInitialLayers() {
		int currentZ = renderer.initFrontLayerDistance;
		ArrayList<Layer> layers = new ArrayList<>();
		
		while(currentZ < renderer.initZ) {
        	float[] rgba = generateRandomRGBA();
            layers.add(new Layer(rgba, currentZ));
            currentZ += renderer.layerDistance;
		}
		
		return layers;
	}
	
    public void updateFadeAlpha(float deltaTime) {
    	fadeAlpha -= fadeSpeed * deltaTime;
    	if (fadeAlpha <= 0.0f) {
    		fadeAlpha = 0.0f;
            initialized = true;
        }
    	if(renderer.speed < speed) {
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
    	if(alpha < 0.2f) {
    		alpha = 0.25f;
    	}
    	return alpha;
    }
	
	public float calculateRotationAngle(float rotationAngle, float deltaTime) {
		
        switch(renderer.rotationMode) {
    	case 1:
    		rotationAngle -= renderer.rotationSpeed * deltaTime;
    		break;
    	case 2:
    		rotationAngle += renderer.rotationSpeed * deltaTime;
    		break;
    	case 3:
    	    time += deltaTime;
    	    rotationAngle = (float) (Math.sin(time * oszilatorSpeed) * swingAmplitude);
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
		    rotationAngle = (float) (Math.sin(layer.time * layerOszilationSpeed) * layerSwingAmplitude);
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
