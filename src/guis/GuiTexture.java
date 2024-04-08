package guis;

import org.lwjgl.util.vector.Vector2f;

/**
 * Las GUI solo tiene posicion y escala.
 */

public class GuiTexture {

    private final int texture;
    private final Vector2f position, scale;

    public GuiTexture(int texture, Vector2f position, Vector2f scale) {
        this.texture = texture;
        this.position = position;
        this.scale = scale;
    }

    public int getTexture() {
        return texture;
    }

    public Vector2f getPosition() {
        return position;
    }

    public Vector2f getScale() {
        return scale;
    }
}
