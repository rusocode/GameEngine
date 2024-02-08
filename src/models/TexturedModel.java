package models;

import textures.ModelTexture;

/**
 * Representa un modelo texturizado, porque el modelo sin formato solo representa datos del modelo.
 */

public class TexturedModel {

    private final RawModel rawModel; // Modelo sin formato
    private final ModelTexture texture; // Textura del modelo con la que queremos texturizar ese modelo a seguir

    public TexturedModel(RawModel rawModel, ModelTexture texture) {
        this.rawModel = rawModel;
        this.texture = texture;
    }

    public RawModel getRawModel() {
        return rawModel;
    }

    public ModelTexture getTexture() {
        return texture;
    }

}
