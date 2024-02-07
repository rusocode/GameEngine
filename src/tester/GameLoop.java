package tester;

import org.lwjgl.opengl.Display;
import render.*;
import shaders.StaticShader;

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

        RawModel model = loader.loadToVAO(vertices, indices);

        while (!Display.isCloseRequested()) {
            renderer.prepare();
            shader.start();
            renderer.render(model);
            shader.stop();
            DisplayManager.update();
        }

        shader.clean();
        loader.clean();
        DisplayManager.close();
    }

}
