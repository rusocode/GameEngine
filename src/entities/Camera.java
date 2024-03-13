package entities;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector3f;

public class Camera {

    private final Vector3f position = new Vector3f(100, 35, 50);
    private float pitch = 10; // Rotacion alrededor de los ejes XYZ, tambien conocido como la inclinacion de la camara
    // Rotacion
    private float yaw = 0;
    private float roll;

    public Camera() {

    }

    /**
     * Mueve la camara dependiendo la tecla pulsada.
     */
    public void move() {
        /* float speed = 1f;
        if (Keyboard.isKeyDown(Keyboard.KEY_W)) position.y += speed;
        if (Keyboard.isKeyDown(Keyboard.KEY_S)) position.y -= speed;
        // TODO Por que la camara se mueve hacia la izq? Es como que esta invertida
        if (Keyboard.isKeyDown(Keyboard.KEY_D)) position.x += speed;
        if (Keyboard.isKeyDown(Keyboard.KEY_A)) position.x -= speed;
        if (Keyboard.isKeyDown(Keyboard.KEY_Q)) position.z -= speed;
        if (Keyboard.isKeyDown(Keyboard.KEY_E)) position.z += speed; */
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
