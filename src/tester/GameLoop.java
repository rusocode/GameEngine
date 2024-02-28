package tester;

import entities.Camera;
import entities.Entity;
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
 */

public class GameLoop {

    public static void main(String[] args) {

        DisplayManager.create();

        Loader loader = new Loader();
        StaticShader shader = new StaticShader();
        Renderer renderer = new Renderer(shader);

        // Carga el modelo crudo para usarlo en la clase TexturedModel
        RawModel model = OBJLoader.loadObjModel("stall", loader);
        // Ahora el modelo crudo y la textura se "juntan" para crear el modelo texturizado (TexturedModel)
        TexturedModel staticModel = new TexturedModel(model, new ModelTexture(loader.loadTexture("stallTexture")));

        // Operaciones de transformacion
        Vector3f translation = new Vector3f(0, 0, -50);
        Vector3f scale = new Vector3f(1, 1, 1);
        float angle = 0;
        // Crea la entidad con el modelo texturizado pasandole por parametro la operaciones de transformacion que se aplicaran al modelo 3D
        Entity entity = new Entity(staticModel, translation, angle, angle, angle, scale);
        // Crea la camara
        Camera camera = new Camera();

        while (!Display.isCloseRequested()) {
            entity.increaseRotation(0, 1, 0); // Solo rota sobre el eje y
            // entity.increasePosition(0.01f, 0, 0);
            camera.move();
            renderer.prepare();
            shader.start();
            shader.loadViewMatrix(camera);
            renderer.render(entity, shader);
            shader.stop();
            DisplayManager.update();
        }

        shader.clean();
        loader.clean();
        DisplayManager.close();
    }

}
