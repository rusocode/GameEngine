package models;

import textures.ModelTexture;

/**
 * Representa un modelo texturizado, ya que el modelo sin formato solo representa datos.
 */

public class TexturedModel {

    private final RawModel rawModel; // Modelo sin formato o crudo
    private final ModelTexture texture; // Textura del modelo con la que queremos texturizar el modelo especificado

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
