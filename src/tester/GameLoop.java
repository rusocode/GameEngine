package tester;

import entities.Camera;
import entities.Entity;
import entities.Light;
import models.OBJLoader;
import models.RawModel;
import models.TexturedModel;
import org.lwjgl.util.vector.Vector3f;
import render.*;
import shaders.StaticShader;
import textures.ModelTexture;

import org.lwjgl.opengl.Display;

/**
 * Bucle principal del juego.
 * <p>
 * <a href="https://betterexplained.com/articles/vector-calculus-understanding-the-dot-product/">Dot product</a>
 */

public class GameLoop {

    public static void main(String[] args) {

        DisplayManager.create();

        Loader loader = new Loader();
        StaticShader shader = new StaticShader();
        Renderer renderer = new Renderer(shader);

        // Carga el modelo crudo
        RawModel rawModel = OBJLoader.loadObjModel("dragon", loader);
        // Ahora el modelo crudo y la textura se "juntan" para crear el modelo texturizado que se usara para aplicarlo a la entidad
        TexturedModel texturedModel = new TexturedModel(rawModel, new ModelTexture(loader.loadTexture("white")));

        Entity entity = getEntity(texturedModel);
        Light light = new Light(new Vector3f(0, 0, -20), new Vector3f(1, 1, 1)); // Fuente de luz
        Camera camera = new Camera();

        while (!Display.isCloseRequested()) {
            entity.increaseRotation(0, 1, 0);
            // entity.increasePosition(0.01f, 0, 0);
            camera.move();
            renderer.prepare();
            shader.start();
            // Carga la fuente de luz y la camara antes de renderizar la entidad
            shader.loadLight(light);
            shader.loadViewMatrix(camera);
            renderer.render(entity, shader);
            shader.stop();
            DisplayManager.update();
        }

        shader.clean();
        loader.clean();
        DisplayManager.close();
    }

    /**
     * Obtiene la entidad texturizada aplicando iluminacion especular y operaciones de transformacion.
     *
     * @param texturedModel modelo texturizado.
     * @return la entidad texturizada con iluminacion especular y transformacion aplicadas.
     */
    private static Entity getEntity(TexturedModel texturedModel) {
        // Aplica iluminacion especular a la textura
        ModelTexture texture = texturedModel.getTexture();
        texture.setShineDamper(10);
        texture.setReflectivity(1);

        // Define las operaciones de transformacion
        Vector3f translation = new Vector3f(0, -5, -35); // Vector de traslacion
        Vector3f scale = new Vector3f(1, 1, 1); // Vector de escala
        float angle = 0; // Angulo de rotacion

        return new Entity(texturedModel, translation, angle, angle, angle, scale);
    }

}
