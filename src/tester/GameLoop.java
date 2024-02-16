package tester;

import entities.Camera;
import entities.Entity;
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

        // Vertex Data (OpenGL espera que los vertices se definan en sentido antihorario de forma predeterminada)
        float[] vertices = {
                -0.5f, 0.5f, -0.5f, // V0
                -0.5f, -0.5f, -0.5f, // V1
                0.5f, -0.5f, -0.5f, // V2
                0.5f, 0.5f, -0.5f, // V3

                -0.5f, 0.5f, 0.5f,
                -0.5f, -0.5f, 0.5f,
                0.5f, -0.5f, 0.5f,
                0.5f, 0.5f, 0.5f,

                0.5f, 0.5f, -0.5f,
                0.5f, -0.5f, -0.5f,
                0.5f, -0.5f, 0.5f,
                0.5f, 0.5f, 0.5f,

                -0.5f, 0.5f, -0.5f,
                -0.5f, -0.5f, -0.5f,
                -0.5f, -0.5f, 0.5f,
                -0.5f, 0.5f, 0.5f,

                -0.5f, 0.5f, 0.5f,
                -0.5f, 0.5f, -0.5f,
                0.5f, 0.5f, -0.5f,
                0.5f, 0.5f, 0.5f,

                -0.5f, -0.5f, 0.5f,
                -0.5f, -0.5f, -0.5f,
                0.5f, -0.5f, -0.5f,
                0.5f, -0.5f, 0.5f

        };

        // Coordenadas de texturas
        float[] textureCoords = {

                0, 0,
                0, 1,
                1, 1,
                1, 0,
                0, 0,
                0, 1,
                1, 1,
                1, 0,
                0, 0,
                0, 1,
                1, 1,
                1, 0,
                0, 0,
                0, 1,
                1, 1,
                1, 0,
                0, 0,
                0, 1,
                1, 1,
                1, 0,
                0, 0,
                0, 1,
                1, 1,
                1, 0


        };

        // El Index Buffer indica la posicion de los vertices que forman el primer y segundo triangulo
        int[] indices = {
                0, 1, 3,
                3, 1, 2,
                4, 5, 7,
                7, 5, 6,
                8, 9, 11,
                11, 9, 10,
                12, 13, 15,
                15, 13, 14,
                16, 17, 19,
                19, 17, 18,
                20, 21, 23,
                23, 21, 22

        };

        // Carga el modelo crudo y la textura para usarlos en la clase TexturedModel
        RawModel model = loader.loadToVAO(vertices, textureCoords, indices);
        TexturedModel staticModel = new TexturedModel(model, new ModelTexture(loader.loadTexture("image")));

        Entity entity = new Entity(staticModel, new Vector3f(0, 0, -5), 0, 0, 0, 1);
        Camera camera = new Camera();

        while (!Display.isCloseRequested()) {
            entity.increaseRotation(1, 1, 0);
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
