package entities;

import models.TexturedModel;
import render.DisplayManager;
import terrains.Terrain;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector3f;

/**
 * <a href="https://www.youtube.com/watch?v=F21S9Wpi0y8">Basic Trigonometry</a>
 * <a href="https://www.mathsisfun.com/algebra/trigonometry.html">Introduction to Trigonometry</a>
 */

public class Player extends Entity {

    private static final float RUN_SPEED = 50; // Velocidad de movimiento
    private static final float TURN_SPEED = 130; // Velocidad de giro
    private static final float GRAVITY = -50;
    private static final float JUMP_POWER = 30; // Que tan alto salta

    private float currentSpeed; // Velocidad actual de movimiento
    private float currentTurnSpeed; // Velocidad actual de giro
    private float upwardsSpeed; // Velocidad hacia arriba (determina cuanto aumentara la posicion y del player por segundo)

    private boolean isInAir;

    public Player(TexturedModel model, Vector3f position, Vector3f angle, Vector3f scale) {
        super(model, position, angle, scale);
    }

    /**
     * Mueve el player en distancia y rotacion.
     *
     * @param terrain terreno en el que esta actualmente el player.
     */
    public void move(Terrain terrain) {
        checkInputs();
        increaseRotation(0, currentTurnSpeed * DisplayManager.getFrameTimeSeconds(), 0);
        float distance = currentSpeed * DisplayManager.getFrameTimeSeconds(); // Hipotenusa
        /* Una vez que se conoce la distancia y la rotacion en el eje [y] (angulo) del jugador, se determina la proxima posicion
         * del jugador en los ejes [x] y [z]. Se calcula el desplazamiento en estas direcciones mediante las funciones seno y
         * coseno del angulo [y] multiplicadas por la distancia. La nueva posicion del jugador se obtiene sumando estos
         * desplazamientos a la posicion actual de los ejes [x] y [z]. Este proceso se conoce como "trazar el punto" a lo
         * largo y hacia arriba. */
        /* Teniendo la distancia (hipotenusa) y el angulo [y] del triangulo rectangulo (o del player), queremos saber la distancia
         * en x que va a moverse el player (lado opuesto al angulo [y]). Esto se hace calculando el seno del angulo, ya que el
         * seno es la relacion entre el lado opuesto y la hipotenusa. El resultado del seno es la razon de las longitudes de los
         * lados, por lo que el opuesto es aproximadamente x veces mas largo que la hipotenusa. Ahora sabiendo esto, podemos
         * multiplicar el resultado del seno por la distancia para obtener la nueva posicion en el eje [x] del player, es decir,
         * el lado opuesto. Lo mismo se calcula para el eje [z] pero usando el coseno. */
        float dx = (float) (distance * Math.sin(Math.toRadians(getAngle().y))); // sin(θ) = x / distance
        float dz = (float) (distance * Math.cos(Math.toRadians(getAngle().y))); // cos(θ) = z / distance
        increasePosition(dx, 0, dz);
        // La velocidad hacia arriba disminuira cada segundo en la cantidad especificado por GRAVITY (negativa)
        upwardsSpeed += GRAVITY * DisplayManager.getFrameTimeSeconds();
        increasePosition(0, upwardsSpeed * DisplayManager.getFrameTimeSeconds(), 0);
        float terrainHeight = terrain.getHeightOfTerrain(getPosition().x, getPosition().z);
        // Si la posicion [y] del player es menor a terrainHeight, entonces toco tierra
        if (getPosition().y < terrainHeight) {
            upwardsSpeed = 0; // TODO Parece que no es necesaria esta linea
            isInAir = false;
            getPosition().y = terrainHeight;
        }
    }

    private void checkInputs() {
        if (Keyboard.isKeyDown(Keyboard.KEY_W)) currentSpeed = RUN_SPEED;
        else if (Keyboard.isKeyDown(Keyboard.KEY_S)) currentSpeed = -RUN_SPEED;
        else
            currentSpeed = 0; // Asigna 0 a la velocidad del player para que no se mueva cuando se dejan de presionar las teclas de movimiento
        if (Keyboard.isKeyDown(Keyboard.KEY_D)) currentTurnSpeed = -TURN_SPEED;
        else if (Keyboard.isKeyDown(Keyboard.KEY_A)) currentTurnSpeed = TURN_SPEED;
        else currentTurnSpeed = 0;

        if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) jump();
    }

    private void jump() {
        // Evita que salte mientras esta en el aire
        if (!isInAir) {
            upwardsSpeed = JUMP_POWER;
            isInAir = true;
        }
    }

}
