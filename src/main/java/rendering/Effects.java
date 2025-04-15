package rendering;

import main.Layer;

public class Effects {
	
	Renderer renderer;
	
	private float oszilatorSpeed = 0.35f;
    private float swingAmplitude = 25.0f;
	private float time = 0.0f;
	
	private float layerRotationSpeed = 20.0f;
	private float layerOszilationSpeed = 0.69f;
	private float layerSwingAmplitude = 30.0f;
	
	public Effects(Renderer renderer) {
		this.renderer = renderer;
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
