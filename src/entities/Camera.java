package entities;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector3f;

/**
 * En realidad la camara no se mueve, solo las entidades.
 */

public class Camera {

    private final Vector3f position = new Vector3f(0, 0, 0);
    private float pitch; // Rotacion alrededor de los ejes XYZ, tambien conocido como la inclinacion de la camara
    private float yaw; // Hacia arriba o abajo
    private float roll;

    public Camera() {

    }

    public void move() {
        if (Keyboard.isKeyDown(Keyboard.KEY_W)) position.z -= 0.02f;
        if (Keyboard.isKeyDown(Keyboard.KEY_D)) position.x += 0.02f;
        if (Keyboard.isKeyDown(Keyboard.KEY_A)) position.x -= 0.02f;
    }

    public Vector3f getPosition() {
        return position;
    }

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public float getRoll() {
        return roll;
    }
}
