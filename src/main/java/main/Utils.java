package main;

import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_LINEAR_MIPMAP_LINEAR;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_UNPACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glPixelStorei;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.system.MemoryStack.stackPush;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

public class Utils {
	
    /**
     * Lädt eine PNG-Datei mithilfe von STBImage und erstellt eine OpenGL-Textur.
     * Es wird mit 4 Kanälen (RGBA) geladen, damit dein Bild einen Alphakanal hat.
     * Der Pfad muss relativ zum Projekt sein.
     */
	public static int loadTexture(String filePath) {
        int texId;
        try (MemoryStack stack = stackPush()) {
            IntBuffer width  = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            // Bild vertikal spiegeln (damit die Textur richtig angezeigt wird)
            STBImage.stbi_set_flip_vertically_on_load(true);
            // Erzwinge 4 Kanäle (RGBA)
            ByteBuffer imageData = STBImage.stbi_load(filePath, width, height, channels, 4);
            if(imageData == null) {
                throw new RuntimeException("Failed to load texture file: " + STBImage.stbi_failure_reason());
            }

            texId = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, texId);

            // Sicherstellen, dass die Zeilenbreite korrekt aufgelöst wird
            glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

            // Setze Textur-Parameter:
            // Verwende CLAMP_TO_EDGE, damit an den Rändern nichts wiederholt wird
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

            // Lade die Texturdaten in die GPU
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width.get(0), height.get(0),
                    0, GL_RGBA, GL_UNSIGNED_BYTE, imageData);
            glGenerateMipmap(GL_TEXTURE_2D);

            STBImage.stbi_image_free(imageData);
        }
        return texId;
	}

}
