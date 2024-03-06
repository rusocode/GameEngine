package tester;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import entities.*;
import models.*;
import render.*;
import terrains.Terrain;
import textures.ModelTexture;

import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;

/**
 * Bucle principal del juego.
 * <p>
 * <a href="https://betterexplained.com/articles/vector-calculus-understanding-the-dot-product/">Dot product</a>
 */

public class GameLoop {

    private static final List<Entity> entities = new ArrayList<>();

    public static void main(String[] args) {

        DisplayManager.create();

        Loader loader = new Loader();

        // Carga el modelo en crudo
        RawModel rawModel = OBJLoader.loadObjModel("tree", loader);
        // Ahora el modelo en crudo y la textura se "juntan" para crear el modelo texturizado que se usara para aplicarlo a la entidad
        TexturedModel texturedModel = new TexturedModel(rawModel, new ModelTexture(loader.loadTexture("tree")));

        for (int i = 0; i < 500; i++) entities.add(getEntity(texturedModel));

        // Entity entity = getEntity(texturedModel);

        Light light = new Light(new Vector3f(3000, 2000, 2000), new Vector3f(1, 1, 1)); // Fuente de luz
        // Light light = new Light(new Vector3f(20000, 20000, 2000), new Vector3f(1, 1, 1));

        Terrain terrain = new Terrain(0, 0, loader, new ModelTexture(loader.loadTexture("grass2")));
        Terrain terrain2 = new Terrain(0, 1, loader, new ModelTexture(loader.loadTexture("grass")));

        Camera camera = new Camera();

        MasterRenderer renderer = new MasterRenderer();

        while (!Display.isCloseRequested()) {
            // entity.increasePosition(0.1f, 0, 0); // Por que se mueve hacia la izquierda?
            camera.move();

            renderer.processTerrain(terrain);
            renderer.processTerrain(terrain2);

            for (Entity entity : entities) renderer.processEntity(entity);

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

        // Operaciones de transformacion
        Random random = new Random();
        // Vector de traslacion con coordenadas x e z aleatorias
        Vector3f translation = new Vector3f(random.nextFloat() * 800, 0, random.nextFloat() * 600);
        // Vector de escala
        int scaleValue = 3;
        Vector3f scale = new Vector3f(scaleValue, scaleValue, scaleValue);
        // Angulos de rotacion
        float angleX = 0;
        float angleY = 0;
        float angleZ = 0;

        return new Entity(texturedModel, translation, angleX, angleY, angleZ, scale);
    }

}
