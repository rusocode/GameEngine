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

        /* Para este ejemplo, los datos de vertices (OpenGL espera que los vertices se definan en sentido antihorario de forma
         * predeterminada) representan las caras de un cuadrado 3D. Cada cara esta compuesta por dos triangulos (o poligonos) en
         * donde cada triangulo esta formado por tres vertices. El cuarto vertice (V3) representa el segundo triangulo en la otra
         * esquina. Cada vertice esta formado por los tres ejes de coordenadas (x,y,z). */
        float[] vertices = {
                // Cara
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

        // Indices utilizados por el Index Buffer
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

        // Carga el modelo crudo para usarlo en la clase TexturedModel
        RawModel model = loader.loadToVAO(vertices, textureCoords, indices);
        // Ahora el modelo crudo y la textura se "juntan" para crear el modelo texturizado (TexturedModel)
        TexturedModel staticModel = new TexturedModel(model, new ModelTexture(loader.loadTexture("image")));

        // Operaciones de transformacion
        Vector3f translation = new Vector3f(0, 0, -5);
        Vector3f scale = new Vector3f(1, 1, 1);
        float angle = 0;
        // Crea la entidad con el modelo texturizado pasandole por parametro la operaciones de transformacion que se aplicaran al modelo 3D
        Entity entity = new Entity(staticModel, translation, angle, angle, angle, scale);
        // Crea la camara
        Camera camera = new Camera();

        while (!Display.isCloseRequested()) {
            entity.increaseRotation(1, 1, 0);
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
