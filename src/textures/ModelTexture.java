package textures;

/**
 * Representa una textura que se utiliza para texturizar los modelos.
 */

public class ModelTexture {

    private final int textureID;

    public ModelTexture(int textureID) {
        this.textureID = textureID;
    }

    public int getTextureID() {
        return textureID;
    }
}