package tester;

import models.RawModel;
import models.TexturedModel;
import render.*;
import shaders.StaticShader;

import org.lwjgl.opengl.Display;
import textures.ModelTexture;

/**
 * El renderizado con index buffer renderiza el juego un poco mas rapido, ademas de garantizar que los modelos no ocupen
 * demasiado espacio en la memoria.
 */

public class GameLoop {

    public static void main(String[] args) {

        DisplayManager.create();

        Loader loader = new Loader();
        Renderer renderer = new Renderer();
        StaticShader shader = new StaticShader();

        // Vertex Data (OpenGL espera que los vertices se definan en sentido antihorario de forma predeterminada)
        float[] vertices = {
                -0.5f, 0.5f, 0, // V0
                -0.5f, -0.5f, 0, // V1
                0.5f, -0.5f, 0, // V2
                0.5f, 0.5f, 0, // V3
        };

        // El Index Buffer indica la posicion de los vertices que forman el primer y segundo triangulo
        int[] indices = {
                0, 1, 3, // Triangulo superior izquierdo (V0, V1, V3)
                3, 1, 2 // Triangulo inferior derecho (V3, V1, V2)
        };

        // Coordenadas de texturas
        float[] textureCoords = {
                0, 0, // V0
                0, 1, // V1
                1, 1, // V2
                1, 0 // V3
        };

        RawModel model = loader.loadToVAO(vertices, textureCoords, indices);
        ModelTexture texture = new ModelTexture(loader.loadTexture("image"));
        TexturedModel texturedModel = new TexturedModel(model, texture);

        while (!Display.isCloseRequested()) {
            renderer.prepare();
            shader.start();
            renderer.render(texturedModel);
            shader.stop();
            DisplayManager.update();
        }

        shader.clean();
        loader.clean();
        DisplayManager.close();
    }

}
