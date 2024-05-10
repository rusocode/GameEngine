package water;

import java.util.List;

import models.RawModel;
import org.lwjgl.opengl.GL13;
import render.DisplayManager;
import render.Loader;
import utils.Maths;
import entities.Camera;
import entities.Light;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class WaterRenderer {

    private static final String DUDV_MAP = "dudv";
    private static final String NORMAL_MAP = "normal";
    // Velocidad de movimiento de las ondas del agua
    private static final float WAVE_SPEED = 0.03f;

    private RawModel quad;
    private final WaterShader shader;
    private final WaterFrameBuffers fbos;

    private float moveFactor;

    private final int dudvTexture;
    private final int normalMap;

    public WaterRenderer(Loader loader, WaterShader shader, Matrix4f projectionMatrix, WaterFrameBuffers fbos) {
        this.shader = shader;
        this.fbos = fbos;
        dudvTexture = loader.loadTexture(DUDV_MAP);
        normalMap = loader.loadTexture(NORMAL_MAP);
        shader.start();
        shader.connectTextureUnits();
        shader.loadProjectionMatrix(projectionMatrix);
        shader.stop();
        setUpVAO(loader);
    }

    public void render(List<WaterTile> water, Camera camera, Light sun) {
        prepareRender(camera, sun);
        for (WaterTile tile : water) {
            Matrix4f modelMatrix = Maths.createTransformationMatrix(new Vector3f(tile.getX(), tile.getHeight(), tile.getZ()), new Vector3f(0, 0, 0), new Vector3f(WaterTile.TILE_SIZE, WaterTile.TILE_SIZE, WaterTile.TILE_SIZE));
            shader.loadModelMatrix(modelMatrix);
            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, quad.getVertexCount());
        }
        unbind();
    }

    private void prepareRender(Camera camera, Light sun) {
        shader.start();
        shader.loadViewMatrix(camera);
        // Aumenta el movimiento de las ondas del agua en cada frame
        moveFactor += WAVE_SPEED * DisplayManager.getFrameTimeSeconds();
        // Vuelve a 0 cuando llega a 1
        moveFactor %= 1;
        // Carga el movimiento en el shader
        shader.loadMoveFactor(moveFactor);
        shader.loadLight(sun);
        GL30.glBindVertexArray(quad.getVaoID());
        GL20.glEnableVertexAttribArray(0);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbos.getReflectionTexture());
        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbos.getRefractionTexture());
        GL13.glActiveTexture(GL13.GL_TEXTURE2);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, dudvTexture);
        GL13.glActiveTexture(GL13.GL_TEXTURE3);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, normalMap);
        GL13.glActiveTexture(GL13.GL_TEXTURE4);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbos.getRefractionDepthTexture());

        // Habilita la combinacion alfa
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    private void unbind() {
        GL11.glDisable(GL11.GL_BLEND);
        GL20.glDisableVertexAttribArray(0);
        GL30.glBindVertexArray(0);
        shader.stop();
    }

    /**
     * Carga un quad en un VAO.
     */
    private void setUpVAO(Loader loader) {
        // Solo las posiciones de [x] y [z] vertex aqui, [y] se establece en 0 en v.shader
        float[] vertices = {-1, -1, -1, 1, 1, -1, 1, -1, -1, 1, 1, 1};
        quad = loader.loadToVAO(vertices, 2);
    }

}
