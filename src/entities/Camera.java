package entities;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;

public class Camera {

    private float zoom = 70; // Distancia de la camara al player
    private float angleAroundPlayer; // Angulo alrededor del player
    private final Vector3f position = new Vector3f(0, 0, 0); // No es necesario especificar la posicion de la camara ya que esta depende de la posicion del player
    private float pitch = 20; // Rotacion alrededor de los ejes [x,y,z], tambien conocido como la inclinacion de la camara
    private float yaw = 0; // Rotacion?
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
        calculatePitch();
        calculateAngleAroundPlayer();
        calculateCameraPosition(calculateHorizontalDistance(), calculateVerticalDistance());
        /* Usando lo que sabemos sobre angulos en lineas paralelas, sabemos que el angulo de la camara (yaw) debe ser igual a
         * theta. Por lo que tu angulo es igual a todo ese angulo que es 180 grados menos theta. Asi puedes rotar la camara
         * alrededor del player y cuando el player gira, la camara tambien gira correctamente. */
        yaw = 180 - (player.getAngle().y + angleAroundPlayer); // Ver de nuevo
    }

    public void invertPitch() {
        this.pitch = -pitch;
    }

    /**
     * Calcula la posicion de la camara.
     *
     * @param horizontalDistance distancia horizontal.
     * @param verticalDistance   distancia vertical.
     */
    private void calculateCameraPosition(float horizontalDistance, float verticalDistance) {
        /* En una vista desde arriba de una escena 3D, para calcular la posicion de la camara, se necesita determinar el
         * desplazamiento horizontal del jugador. Con esta informacion, se puede calcular la posicion horizontal de la camara. En
         * la esquina superior izquierda, el eje z apunta hacia abajo. Esto contrasta con la vista tradicional, donde el eje x
         * apunta hacia la derecha, el eje y hacia arriba y el eje z hacia nosotros. El jugador esta orientado por su rotacion en
         * y (rotY), y la camara se coloca detras de el. La distancia entre la camara y el jugador es la distancia horizontal,
         * aunque la camara puede moverse alrededor del jugador alterando el angulo (angleAroundPlayer). El angulo total (theta)
         * se calcula sumando la rotacion y el angulo del jugador, y se utiliza para determinar los desplazamientos en [x] e [z]
         * mediante funciones trigonometricas (seno y coseno). */
        float theta = player.getAngle().y + angleAroundPlayer;
        float offsetX = (float) (horizontalDistance * Math.sin(Math.toRadians(theta)));
        float offsetZ = (float) (horizontalDistance * Math.cos(Math.toRadians(theta)));
        /* Resta los desplazamientos a la posicion del player para obtener la posicion correcta de la camara por detras de este.
         * ¿Por que se restan los desplazamientos a la posicion del player en vez de sumarlos? Si miras bien en el grafico del
         * video, el desplazamiento x de la camara desde la vista desde arriba esta en la direccion x negativa y el desplazamiento
         * z de la camara desde el player tambien esta en la direccion z negativa. */
        position.x = player.getPosition().x - offsetX;
        position.z = player.getPosition().z - offsetZ;
        /* Como ya conocemos la distancia vertical de la camara al player y la posicion [y] del player, entonces se suman para
         * obtener la posicion [y] de la camara. */
        position.y = player.getPosition().y + verticalDistance;
    }

    /**
     * Calcula la distancia horizontal de la camara al player.
     *
     * @return la distancia horizontal.
     */
    private float calculateHorizontalDistance() {
        return (float) (zoom * Math.cos(Math.toRadians(pitch)));
    }

    /**
     * Calcula la distancia vertical de la camara al player.
     *
     * @return la distancia vertical.
     */
    private float calculateVerticalDistance() {
        return (float) (zoom * Math.sin(Math.toRadians(pitch)));
    }

    private void calculateZoom() {
        float zoomLevel = 0;
        if (Keyboard.isKeyDown(Keyboard.KEY_P)) zoomLevel--;
        if (Keyboard.isKeyDown(Keyboard.KEY_O)) zoomLevel++;
        // float zoomLevel = Mouse.getDWheel() * 0.1f; // Lo multiplica por 0.1 para evitar que se aleje o acerque rapidamente
        zoom -= zoomLevel;
    }

    private void calculatePitch() {
        // Si se presiono el boton derecho del mouse
        if (Mouse.isButtonDown(1)) {
            // Calcula cuanto se a movido la camara hacia arriba o abajo
            float pitchChange = Mouse.getDY() * 0.1f;
            pitch -= pitchChange;
            if (pitch < 1) pitch = 1; // Evita que la camara pase por debajo del terreno
            else if (pitch > 90) pitch = 90; // Evita que la camara pase por encima del player
        }
    }

    private void calculateAngleAroundPlayer() {
        // Si se presiono el boton izquierdo del mouse
        if (Mouse.isButtonDown(0)) {
            // Calcula cuanto se a movido la camara hacia la izquierda o derecha
            float angleChange = Mouse.getDX() * 0.07f;
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
