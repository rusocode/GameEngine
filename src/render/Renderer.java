package render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

/**
 * Representa el modelo del VAO.
 */

public class Renderer {

    /**
     * Se llama una vez en cada fotograma y simplemente prepara a OpenGL para renderizar el juego.
     */
    public void prepare() {
        // Borra el color del ultimo fotograma
        GL11.glClearColor(1, 0, 0, 1);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
    }

    /**
     * Renderiza un modelo.
     *
     * @param model modelo sin formato.
     */
    public void render(RawModel model) {
        // Vincula el VAO
        GL30.glBindVertexArray(model.getVaoID());
        // Activa la lista de atributos en la que se almacenan los datos
        GL20.glEnableVertexAttribArray(0);
        // Renderiza el modelo usando triangulos, especificando desde donde comienzan a renderizar y cuantos vertices debe representar
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, model.getVertexCount());
        // Deshabilita la lista de atributos cuando termine de usar todo
        GL20.glDisableVertexAttribArray(0);
        // Desvincula el VAO
        GL30.glBindVertexArray(0);
    }

}
