package rendering;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;

public class ShaderProgram {
	
	int shaderProgram;
	
	public ShaderProgram() {
		createShaderProgram();
	}
	
	public int getShaderProgram() {
		return shaderProgram;
	}
	
    /**
     * Erstellt ein einfaches Shader-Programm mit Vertex- und Fragment-Shader.
     * Der Vertex-Shader erhält eine Uniform "scale" (Zoom) sowie "aspect" für die Korrektur des Seitenverhältnisses.
     */
    private void createShaderProgram() {
    	String vertexShaderSource =
    		    "#version 330 core\n" +
    		    "layout(location = 0) in vec2 position;\n" +
    		    "layout(location = 1) in vec2 texCoords;\n" +
    		    "uniform float scale;\n" +
    		    "uniform float aspect;\n" +       // aspect = windowWidth / windowHeight
    		    "uniform vec2 offset;  // Offset in NDC\n" +
    		    "out vec2 passTexCoords;\n" +
    		    "void main(){\n" +
    		    "    // Teile die x-Komponente durch aspect, um das Seitenverhältnis zu korrigieren\n" +
    		    "    vec2 pos = vec2(position.x / aspect, position.y);\n" +
    		    "    gl_Position = vec4(pos * scale + offset, 0.0, 1.0);\n" +
    		    "    passTexCoords = texCoords;\n" +
    		    "}\n";


        String fragmentShaderSource =
                "#version 330 core\n" +
                "in vec2 passTexCoords;\n" +
                "out vec4 outColor;\n" +
                "uniform sampler2D texSampler;\n" +
                "uniform vec4 layerColor;\n" + 
                "void main(){\n" +
                "    outColor = texture(texSampler, passTexCoords) * layerColor;\n" +
                "}\n";

        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexShaderSource);
        glCompileShader(vertexShader);
        if (glGetShaderi(vertexShader, GL_COMPILE_STATUS) == GL_FALSE) {
            System.err.println("Vertex shader compile error: " + glGetShaderInfoLog(vertexShader));
        }

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentShaderSource);
        glCompileShader(fragmentShader);
        if (glGetShaderi(fragmentShader, GL_COMPILE_STATUS) == GL_FALSE) {
            System.err.println("Fragment shader compile error: " + glGetShaderInfoLog(fragmentShader));
        }

        int program = glCreateProgram();
        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);
        glLinkProgram(program);
        if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
            System.err.println("Shader program linking error: " + glGetProgramInfoLog(program));
        }

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);

        this.shaderProgram = program;
    }

}
