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

    private static final float RUN_SPEED = 50; // Velocidad
    private static final float TURN_SPEED = 130; // Velocidad de giro
    private static final float GRAVITY = -50;
    private static final float JUMP_POWER = 30; // Que tan alto salta

    private float currentSpeed; // Distancia
    private float currentTurnSpeed; // Angulo de giro
    private float upwardsSpeed; // Velocidad hacia arriba

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
        float dx = (float) (distance * Math.sin(Math.toRadians(getAngle().y))); // sin(θ) = x / distance
        float dz = (float) (distance * Math.cos(Math.toRadians(getAngle().y))); // cos(θ) = z / distance
        increasePosition(dx, 0, dz);
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
        else currentSpeed = 0;
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
