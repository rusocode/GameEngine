package guis;

import java.util.List;

import models.RawModel;
import render.Loader;
import utils.Maths;

import org.lwjgl.util.vector.Matrix4f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class GuiRenderer {

    private final RawModel quad;
    private final GuiShader shader;

    public GuiRenderer(Loader loader) {
        float[] positions = {
                -1, 1,
                -1, -1,
                1, 1,
                1, -1
        };
        quad = loader.loadToVAO(positions, 2);
        shader = new GuiShader();
    }

    public void render(List<GuiTexture> guis) {
        shader.start();
        glBindVertexArray(quad.getVaoID());
        glEnableVertexAttribArray(0);
        // Le saca la transparencia a la gui (fondo negro de la imagen)
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        // Deshabilita las pruebas de profundidad para que se vean las guis que estan por debajo de esta
        glDisable(GL_DEPTH_TEST);
        for (GuiTexture gui : guis) {
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, gui.getTexture());
            Matrix4f matrix = Maths.createTransformationMatrix(gui.getPosition(), gui.getScale());
            shader.loadTransformationMatrix(matrix);
            glDrawArrays(GL_TRIANGLE_STRIP, 0, quad.getVertexCount());
        }
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        glDisableVertexAttribArray(0);
        glBindVertexArray(0);
        shader.stop();
    }

    public void clean() {
        shader.clean();
    }

}
