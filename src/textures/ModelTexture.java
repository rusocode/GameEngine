package textures;

/**
 * Representa una textura que se utiliza para texturizar los modelos 3D.
 */

public class ModelTexture {

    private final int textureID;

    private float shineDamper = 1;
    private float reflectivity = 0;

    public ModelTexture(int textureID) {
        this.textureID = textureID;
    }

    public int getTextureID() {
        return textureID;
    }

    public float getShineDamper() {
        return shineDamper;
    }

    public void setShineDamper(float shineDamper) {
        this.shineDamper = shineDamper;
    }

    public float getReflectivity() {
        return reflectivity;
    }

    public void setReflectivity(float reflectivity) {
        this.reflectivity = reflectivity;
    }
}
