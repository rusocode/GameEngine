package entities;

import models.TexturedModel;
import org.lwjgl.util.vector.Vector3f;

/**
 * Entidad.
 */

public class Entity {

    private TexturedModel model;
    private Vector3f translation;
    private float rotX, rotY, rotZ;
    private final Vector3f scale;

    public Entity(TexturedModel model, Vector3f translation, float rotX, float rotY, float rotZ, Vector3f scale) {
        this.model = model;
        this.translation = translation;
        this.rotX = rotX;
        this.rotY = rotY;
        this.rotZ = rotZ;
        this.scale = scale;
    }

    public void increaseTranslation(float dx, float dy, float dz) {
        translation.x += dx;
        translation.y += dy;
        translation.z += dz;
    }

    public void increaseRotation(float dx, float dy, float dz) {
        rotX += dx;
        rotY += dy;
        rotZ += dz;
    }

    public TexturedModel getModel() {
        return model;
    }

    public void setModel(TexturedModel model) {
        this.model = model;
    }

    public Vector3f getTranslation() {
        return translation;
    }

    public void setTranslation(Vector3f translation) {
        this.translation = translation;
    }

    public float getRotX() {
        return rotX;
    }

    public void setRotX(float rotX) {
        this.rotX = rotX;
    }

    public float getRotY() {
        return rotY;
    }

    public void setRotY(float rotY) {
        this.rotY = rotY;
    }

    public float getRotZ() {
        return rotZ;
    }

    public void setRotZ(float rotZ) {
        this.rotZ = rotZ;
    }

    public Vector3f getScale() {
        return scale;
    }
}
