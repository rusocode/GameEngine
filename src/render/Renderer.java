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
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        // Borra el color del ultimo fotograma
        GL11.glClearColor(1, 0, 0, 1);
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
        GL11.glDrawElements(GL11.GL_TRIANGLES, model.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
        // Deshabilita la lista de atributos cuando termine de usar todo
        GL20.glDisableVertexAttribArray(0);
        // Desvincula el VAO
        GL30.glBindVertexArray(0);
    }

}
