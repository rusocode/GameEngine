package entities;

import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;

/**
 * Actualiza la posicion y orientacion de la camara en cada fotograma, teniendo en cuenta la posicion y rotacion del jugador, el
 * zoom y los movimientos del mouse. Esto permite que la camara siga al jugador de manera suave y controlable, ajustando su
 * posicion y angulos de acuerdo con las interacciones del usuario.
 */

public class Camera {

    // Distancia de la camara al player
    private float zoom = 70;
    // Angulos de inclinacion de la camara en los ejes [x] e [y] con respecto al player
    private float xAngle = 20, yAngle = 0;

    private float yaw = 0; // Angulo de rotacion de la camara en el eje [y]

    private final Vector3f position = new Vector3f(0, 0, 0); // No es necesario especificar la posicion de la camara ya que esta depende de la posicion del player

    private float roll;

    private final Player player;

    public Camera(Player player) {
        this.player = player;
    }

    /**
     * Mueve la camara dependiendo de la entrada del usuario.
     */
    public void move() {
        calculateZoom();
        calculateAngles();
        calculateCameraPosition(calculateHorizontalDistance(), calculateVerticalDistance());
        // Le resta el angulo theta a los 180 grados del angulo yaw para que sean iguales y la camara mire en la direccion horizontal correcta
        yaw = 180 - (player.getAngle().y + yAngle);
    }

    public void invertXAngle() {
        this.xAngle = -xAngle;
    }

    /**
     * Calcula la posicion de la camara en relacion con la posicion del jugador, utilizando la distancia horizontal y vertical de
     * la camara al jugador.
     *
     * @param horizontalDistance distancia horizontal.
     * @param verticalDistance   distancia vertical.
     */
    private void calculateCameraPosition(float horizontalDistance, float verticalDistance) {
        /* En una vista desde arriba de una escena 3D, para calcular la posicion de la camara, se necesita determinar el
         * desplazamiento horizontal del jugador. Con esta informacion, se puede calcular la posicion horizontal de la camara. En
         * la esquina superior izquierda, el eje z apunta hacia abajo. Esto contrasta con la vista tradicional, donde el eje x
         * apunta hacia la derecha, el eje y hacia arriba y el eje z hacia nosotros. El jugador esta orientado por su rotacion en
         * y, y la camara se coloca detras de el. La distancia entre la camara y el jugador es la distancia horizontal, aunque la
         * camara puede moverse alrededor del jugador alterando el angulo (yAngle). El angulo total (theta) se calcula sumando la
         * el angulo del player mas el angulo de inclinacion de la camara, y se utiliza para determinar los desplazamientos en [x]
         * e [z] mediante funciones trigonometricas (seno y coseno). */
        float theta = player.getAngle().y + yAngle;
        /* Calcula los offsets usando el angulo theta y la distancia horizontal como informacion. Estos offsets representan la
         * posicion de la camara relativa al jugador en los ejes [x] y [z]. */
        float offsetX = (float) (horizontalDistance * Math.sin(Math.toRadians(theta)));
        float offsetZ = (float) (horizontalDistance * Math.cos(Math.toRadians(theta)));
        // Se restan porque los offsets de la camara parten DESDE la posicion del player dando una direccion negativa
        position.x = player.getPosition().x - offsetX;
        position.z = player.getPosition().z - offsetZ;
        /* Como ya conocemos la distancia vertical de la camara al player y la posicion [y] del player, entonces se suman para
         * obtener la posicion [y] de la camara. */
        position.y = player.getPosition().y + verticalDistance;
    }

    private void calculateZoom() {
        zoom -= Mouse.getDWheel() * 0.1f; // Lo multiplica por 0.1 para evitar que se aleje o acerque rapidamente
    }

    private void calculateAngles() {
        // Si se presiono el boton izquierdo del mouse
        if (Mouse.isButtonDown(0)) {
            // Calcula cuanto se a movido la camara hacia la izquierda o derecha
            yAngle -= Mouse.getDX() * 0.12f;
            // Calcula cuanto se a movido la camara hacia arriba o abajo
            xAngle -= Mouse.getDY() * 0.12f;
            if (xAngle < 1) xAngle = 1; // Evita que la camara pase por debajo del terreno
            else if (xAngle > 90) xAngle = 90; // Evita que la camara pase por encima del player
        }
    }

    /**
     * Calcula la distancia horizontal de la camara al player.
     *
     * @return la distancia horizontal.
     */
    private float calculateHorizontalDistance() {
        return (float) (zoom * Math.cos(Math.toRadians(xAngle)));
    }

    /**
     * Calcula la distancia vertical de la camara al player.
     *
     * @return la distancia vertical.
     */
    private float calculateVerticalDistance() {
        return (float) (zoom * Math.sin(Math.toRadians(xAngle)));
    }

    public Vector3f getPosition() {
        return position;
    }

    public float getXAngle() {
        return xAngle;
    }

    public float getYaw() {
        return yaw;
    }

    public float getRoll() {
        return roll;
    }

}
