package tester;

import entities.Camera;
import entities.Entity;
import entities.Light;
import models.OBJLoader;
import models.RawModel;
import models.TexturedModel;
import org.lwjgl.util.vector.Vector3f;
import render.*;
import terrains.Terrain;
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

        // Carga el modelo en crudo
        RawModel rawModel = OBJLoader.loadObjModel("tree", loader);
        // Ahora el modelo en crudo y la textura se "juntan" para crear el modelo texturizado que se usara para aplicarlo a la entidad
        TexturedModel texturedModel = new TexturedModel(rawModel, new ModelTexture(loader.loadTexture("tree")));

        Entity entity = getEntity(texturedModel);
        Light light = new Light(new Vector3f(3000, 2000, 2000), new Vector3f(1, 1, 1)); // Fuente de luz

        Terrain terrain = new Terrain(0, 0, loader, new ModelTexture(loader.loadTexture("grass")));
        Terrain terrain2 = new Terrain(1, 0, loader, new ModelTexture(loader.loadTexture("grass")));

        Camera camera = new Camera();

        MasterRenderer renderer = new MasterRenderer();

        while (!Display.isCloseRequested()) {
            entity.increaseRotation(0, 1, 0);
            camera.move();

            renderer.processTerrain(terrain);
            renderer.processTerrain(terrain2);
            renderer.processEntity(entity);

            renderer.render(light, camera);

            DisplayManager.update();
        }

        renderer.clean();
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
        Vector3f translation = new Vector3f(400, 0, 400); // Vector de traslacion
        int scaleValue = 3;
        Vector3f scale = new Vector3f(scaleValue, scaleValue, scaleValue); // Vector de escala
        // Angulos de rotacion
        float angleX = 0;
        float angleY = 0;
        float angleZ = 0;

        return new Entity(texturedModel, translation, angleX, angleY, angleZ, scale);
    }

}
