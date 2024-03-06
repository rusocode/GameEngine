package textures;

/**
 * Representa una textura que se utiliza para texturizar los modelos.
 */

public class ModelTexture {

    private final int textureID;

    private float shineDamper = 1; // Factor de amortiguacion
    private float reflectivity = 0; // Luz reflejada
    private boolean hasTransparency;
    /* La textura "herb" consiste en dos quads con normales orientadas en direcciones distintas, lo que causa variaciones notables
     * en la iluminacion. Para corregir esto, se ajustan todas las normales para que apunten hacia arriba, creando una iluminacion
     * falsa. */
    private boolean useFakeLighting;

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

    public boolean isHasTransparency() {
        return hasTransparency;
    }

    public void setHasTransparency(boolean hasTransparency) {
        this.hasTransparency = hasTransparency;
    }

    public boolean isUseFakeLighting() {
        return useFakeLighting;
    }

    public void setUseFakeLighting(boolean useFakeLighting) {
        this.useFakeLighting = useFakeLighting;
    }

}
