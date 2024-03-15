package entities;

import models.TexturedModel;
import org.lwjgl.util.vector.Vector3f;

/**
 * Entidad.
 */

public class Entity {

    private TexturedModel model;
    private Vector3f position;
    private Vector3f angle;
    private final Vector3f scale;

    public Entity(TexturedModel model, Vector3f position, Vector3f angle, Vector3f scale) {
        this.model = model;
        this.position = position;
        this.angle = angle;
        this.scale = scale;
    }

    public void increasePosition(float dx, float dy, float dz) {
        position.x += dx;
        position.y += dy;
        position.z += dz;
    }

    public void increaseRotation(float dx, float dy, float dz) {
        angle.x += dx;
        angle.y += dy;
        angle.z += dz;
    }

    public TexturedModel getModel() {
        return model;
    }

    public void setModel(TexturedModel model) {
        this.model = model;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public Vector3f getAngle() {
        return angle;
    }

    public void setAngle(Vector3f angle) {
        this.angle = angle;
    }

    public Vector3f getScale() {
        return scale;
    }
}
