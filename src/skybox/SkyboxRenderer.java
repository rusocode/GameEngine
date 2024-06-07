package skybox;

import entities.Camera;
import models.RawModel;
import render.DisplayManager;
import render.Loader;

import org.lwjgl.util.vector.Matrix4f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class SkyboxRenderer {

    private static final float SIZE = 500f;

    // Caras del SkyBox
    private static final float[] VERTICES = {
            -SIZE, SIZE, -SIZE,
            -SIZE, -SIZE, -SIZE,
            SIZE, -SIZE, -SIZE,
            SIZE, -SIZE, -SIZE,
            SIZE, SIZE, -SIZE,
            -SIZE, SIZE, -SIZE,

            -SIZE, -SIZE, SIZE,
            -SIZE, -SIZE, -SIZE,
            -SIZE, SIZE, -SIZE,
            -SIZE, SIZE, -SIZE,
            -SIZE, SIZE, SIZE,
            -SIZE, -SIZE, SIZE,

            SIZE, -SIZE, -SIZE,
            SIZE, -SIZE, SIZE,
            SIZE, SIZE, SIZE,
            SIZE, SIZE, SIZE,
            SIZE, SIZE, -SIZE,
            SIZE, -SIZE, -SIZE,

            -SIZE, -SIZE, SIZE,
            -SIZE, SIZE, SIZE,
            SIZE, SIZE, SIZE,
            SIZE, SIZE, SIZE,
            SIZE, -SIZE, SIZE,
            -SIZE, -SIZE, SIZE,

            -SIZE, SIZE, -SIZE,
            SIZE, SIZE, -SIZE,
            SIZE, SIZE, SIZE,
            SIZE, SIZE, SIZE,
            -SIZE, SIZE, SIZE,
            -SIZE, SIZE, -SIZE,

            -SIZE, -SIZE, -SIZE,
            -SIZE, -SIZE, SIZE,
            SIZE, -SIZE, -SIZE,
            SIZE, -SIZE, -SIZE,
            -SIZE, -SIZE, SIZE,
            SIZE, -SIZE, SIZE
    };

    private static final String skybox_dir = "skybox/";

    private static final String[] DAY_TEXTURE_FILES = {skybox_dir + "right", skybox_dir + "left", skybox_dir + "top", skybox_dir + "bottom", skybox_dir + "back", skybox_dir + "front"};
    private static final String[] NIGHT_TEXTURE_FILES = {skybox_dir + "night_right", skybox_dir + "night_left", skybox_dir + "night_top", skybox_dir + "night_bottom", skybox_dir + "night_back", skybox_dir + "night_front"};

    private final RawModel cube;
    private final int dayTexture;
    private final int nightTexture;
    private final SkyboxShader shader;
    private float time;

    public SkyboxRenderer(Loader loader, Matrix4f projectionMatrix) {
        /* Carga los vertices del SkyBox en el VAO y lo alamacena el modelo en crudo. Luego ese modelo en crudo se utiliza para
         * enlazar el VAO al contexto de renderizado actual a travez del ID. */
        cube = loader.loadToVAO(VERTICES, 3);
        // Carga las texturas que forman al skybox
        dayTexture = loader.loadCubeMap(DAY_TEXTURE_FILES);
        nightTexture = loader.loadCubeMap(NIGHT_TEXTURE_FILES);
        shader = new SkyboxShader(); // Crea el programa shader para el skybox
        shader.start(); // Inicia el programa del shader
        shader.connectTextureUnits();
        shader.loadProjectionMatrix(projectionMatrix);
        shader.stop();
    }

    /**
     * @param camera camara.
     * @param r      canal rojo del color de la niebla.
     * @param g      canal verde del color de la niebla.
     * @param b      canal azul del color de la niebla.
     */
    public void render(Camera camera, float r, float g, float b) {
        shader.start();
        shader.loadViewMatrix(camera);
        shader.loadFogColour(r, g, b);
        glBindVertexArray(cube.getVaoID());
        glEnableVertexAttribArray(0);
        bindTextures();
        glDrawArrays(GL_TRIANGLES, 0, cube.getVertexCount());
        glDisableVertexAttribArray(0);
        glBindVertexArray(0);
        shader.stop();
    }

    private void bindTextures() {
        time += DisplayManager.getFrameTimeSeconds() * 1000;
        time %= 24000; // Si es mayor que 24000 vuelve a 0
        int texture1, texture2;
        float blendFactor;
        if (time >= 0 && time < 5000) {
            texture1 = dayTexture; // n
            texture2 = dayTexture; // n
            blendFactor = (time - 0) / 5000;
        } else if (time >= 5000 && time < 8000) {
            texture1 = dayTexture; // n
            texture2 = dayTexture;
            blendFactor = (time - 5000) / (8000 - 5000);
        } else if (time >= 8000 && time < 21000) {
            texture1 = dayTexture;
            texture2 = dayTexture;
            blendFactor = (time - 8000) / (21000 - 8000);
        } else {
            texture1 = dayTexture;
            texture2 = dayTexture; // n
            blendFactor = (time - 21000) / (24000 - 21000);
        }

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_CUBE_MAP, texture1);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_CUBE_MAP, texture2);
        shader.loadBlendFactor(blendFactor);
    }

}
