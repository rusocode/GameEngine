package entities;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector3f;

public class Camera {

    private final Vector3f position = new Vector3f(0, 20, 0);
    private float pitch; // Rotacion alrededor de los ejes XYZ, tambien conocido como la inclinacion de la camara
    private float yaw = 180; // Hacia arriba o abajo
    private float roll;

    public Camera() {

    }

    /**
     * Mueve la camara dependiendo la tecla pulsada.
     */
    public void move() {
        if (Keyboard.isKeyDown(Keyboard.KEY_W)) position.y += 0.1f;
        if (Keyboard.isKeyDown(Keyboard.KEY_S)) position.y -= 0.1f;
        if (Keyboard.isKeyDown(Keyboard.KEY_D)) position.x += 0.1f;
        if (Keyboard.isKeyDown(Keyboard.KEY_A)) position.x -= 0.1f;
        if (Keyboard.isKeyDown(Keyboard.KEY_Q)) position.z -= 0.1f;
        if (Keyboard.isKeyDown(Keyboard.KEY_E)) position.z += 0.1f;
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
