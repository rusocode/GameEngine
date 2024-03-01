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

        // Carga el modelo crudo para usarlo en la clase TexturedModel
        RawModel model = OBJLoader.loadObjModel("dragon", loader);
        // Ahora el modelo crudo y la textura se "juntan" para crear el modelo texturizado (TexturedModel)
        TexturedModel staticModel = new TexturedModel(model, new ModelTexture(loader.loadTexture("white")));

        ModelTexture texture = staticModel.getTexture();
        texture.setShineDamper(10);
        texture.setReflectivity(1);

        // Operaciones de transformacion
        Vector3f translation = new Vector3f(0, -5, -35);
        Vector3f scale = new Vector3f(1, 1, 1);
        float angle = 0;
        // Crea la entidad con el modelo texturizado pasandole por parametro la operaciones de transformacion que se aplicaran al modelo 3D
        Entity entity = new Entity(staticModel, translation, angle, angle, angle, scale);
        Light light = new Light(new Vector3f(0, 0, -20), new Vector3f(1, 1, 1));
        // Crea la camara
        Camera camera = new Camera();

        while (!Display.isCloseRequested()) {
            entity.increaseRotation(0, 1, 0); // Solo rota sobre el eje y
            // entity.increasePosition(0.01f, 0, 0);
            camera.move();
            renderer.prepare();
            shader.start();
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

}
