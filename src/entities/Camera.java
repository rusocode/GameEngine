package entities;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;

public class Camera {

    private float distanceFromPlayer = 50; // Zoom
    private float angleAroundPlayer;

    private final Vector3f position = new Vector3f(100, 35, 50);
    private float pitch = 10; // Rotacion alrededor de los ejes XYZ, tambien conocido como la inclinacion de la camara
    // Rotacion
    private float yaw = 0;
    private float roll;

    private Player player;

    public Camera(Player player) {
        this.player = player;
    }

    /**
     * Mueve la camara dependiendo la tecla pulsada.
     */
    public void move() {
        calculateZoom();
        calculatePitch();
        calculateAngleAroundPlayer();
        calculateCameraPosition(calculateHorizontalDistance(), calculateVerticalDistance());
        yaw = 180 - (player.getRotY() + angleAroundPlayer);
    }

    /**
     * Calcula la posicion de la camara.
     */
    private void calculateCameraPosition(float horizontalDistance, float verticalDistance) {
        float theta = player.getRotY() + angleAroundPlayer;
        float offsetX = (float) (horizontalDistance * Math.sin(Math.toRadians(theta)));
        float offsetZ = (float) (horizontalDistance * Math.cos(Math.toRadians(theta)));
        // Resta los desplazamientos a la posicion del player para obtener la posicion de la camara por detras de este
        position.x = player.getPosition().x - offsetX;
        position.z = player.getPosition().z - offsetZ;
        position.y = player.getPosition().y + verticalDistance;
    }

    /**
     * Calcula la distancia horizontal de la camara en relacion con el player.
     *
     * @return la distancia horizontal.
     */
    private float calculateHorizontalDistance() {
        return (float) (distanceFromPlayer * Math.cos(Math.toRadians(pitch)));
    }

    /**
     * Calcula la distancia vertical de la camara en relacion con el player.
     *
     * @return la distancia vertical.
     */
    private float calculateVerticalDistance() {
        return (float) (distanceFromPlayer * Math.sin(Math.toRadians(pitch)));
    }

    private void calculateZoom() {
        float zoomLevel = Mouse.getDWheel() * 0.1f; // Lo multiplica por 0.1 para evitar que se aleje o acerque rapidamente
        distanceFromPlayer -= zoomLevel;
    }

    private void calculatePitch() {
        // Si se presiono el boton derecho del mouse
        if (Mouse.isButtonDown(1)) {
            // Calcula cuanto se a movido el mouse hacia arriba o abajo
            float pitchChange = Mouse.getDY() * 0.1f;
            pitch -= pitchChange;
        }
    }

    private void calculateAngleAroundPlayer() {
        // Si se presiono el boton izquierdo del mouse
        if (Mouse.isButtonDown(0)) {
            // Calcula cuanto se a movido el mouse hacia arriba y hacia abajo
            float angleChange = Mouse.getDX() * 0.3f;
            // Calcula cuanto se a movido la camara hacia la izquierda o derecha
            angleAroundPlayer -= angleChange;
        }
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
