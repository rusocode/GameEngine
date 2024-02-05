package tester;

import org.lwjgl.opengl.Display;
import render.DisplayManager;
import render.Loader;
import render.RawModel;
import render.Renderer;

public class GameLoop {

    public static void main(String[] args) {
        DisplayManager.create();

        Loader loader = new Loader();
        Renderer renderer = new Renderer();

        // OpenGL espera que los vertices se definan en sentido antihorario de forma predeterminada
        float[] vertices = {
                // Triangulo inferior izquierdo
                -0.5f, 0.5f, 0f,
                -0.5f, -0.5f, 0f,
                0.5f, -0.5f, 0f,
                // Triangulo superior derecho
                0.5f, -0.5f, 0f,
                0.5f, 0.5f, 0f,
                -0.5f, 0.5f, 0f,
        };

        RawModel model = loader.loadToVAO(vertices);

        while (!Display.isCloseRequested()) {
            renderer.prepare();
            renderer.render(model);
            DisplayManager.update();
        }

        loader.clean();
        DisplayManager.close();
    }

}
