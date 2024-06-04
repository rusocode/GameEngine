package render;

import java.util.List;

import models.RawModel;
import shaders.TerrainShader;
import terrains.Terrain;
import textures.TerrainTexturePack;
import utils.Maths;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class TerrainRenderer {

    private final TerrainShader shader;

    public TerrainRenderer(TerrainShader shader, Matrix4f projectionMatrix) {
        this.shader = shader;
        shader.start();
        shader.loadProjectionMatrix(projectionMatrix);
        shader.connectTextureUnits();
        shader.stop();
    }

    public void render(List<Terrain> terrains) {
        for (Terrain terrain : terrains) {
            prepareTexturedModel(terrain);
            loadModelMatrix(terrain);
            glDrawElements(GL_TRIANGLES, terrain.getModel().getVertexCount(), GL_UNSIGNED_INT, 0);
            unbindTexturedModel();
        }
    }

    private void prepareTexturedModel(Terrain terrain) {
        RawModel rawModel = terrain.getModel();
        /* A partir del modelo en crudo se obtiene el id para vincular el VAO de ese modelo. Un VAO es un objeto de OpenGL que
         * encapsula el estado de multiples buffers de atributos de vertices (como posiciones, colores, coordenadas de textura,
         * etc.) necesarios para renderizar una malla o modelo. Cuando se llama a glBindVertexArray(), se activa el VAO
         * especificado, lo que significa que los comandos subsiguientes que afectan a los atributos de vertices (como
         * glVertexAttribPointer()) operaran en el VAO activado.
         * En resumen, glBindVertexArray() se utiliza para vincular un VAO especifico, lo que permite a OpenGL recordar los
         * estados de los atributos de vertices asociados a ese VAO para su uso posterior durante el proceso de renderizado. */
        glBindVertexArray(rawModel.getVaoID());
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
        shader.loadSpecularLight(1, 0);
        bindTextures(terrain);
    }

    /**
     * Activa y vincula las texturas del pack de texturas a las unidades de texturas correctas.
     *
     * @param terrain terreno.
     */
    private void bindTextures(Terrain terrain) {
        TerrainTexturePack pack = terrain.getTexturePack();
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, pack.getBackground().getID());
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, pack.getR().getID());
        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D, pack.getG().getID());
        glActiveTexture(GL_TEXTURE3);
        glBindTexture(GL_TEXTURE_2D, pack.getB().getID());
        glActiveTexture(GL_TEXTURE4);
        glBindTexture(GL_TEXTURE_2D, terrain.getBlendMap().getID());
    }

    private void loadModelMatrix(Terrain terrain) {
        Matrix4f matrix = Maths.createTransformationMatrix(new Vector3f(terrain.getX(), 0, terrain.getZ()), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1));
        shader.loadTransformationMatrix(matrix);
    }

    private void unbindTexturedModel() {
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        glBindVertexArray(0);
    }

}
