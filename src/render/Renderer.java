package render;

import models.RawModel;
import models.TexturedModel;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
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
     * @param texturedModel modelo sin formato.
     */
    public void render(TexturedModel texturedModel) {
        RawModel model = texturedModel.getRawModel();
        // Vincula el VAO
        GL30.glBindVertexArray(model.getVaoID());
        // Activa la lista de atributos en la que se almacenan los datos
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        // Le dice a OpenGL cual textura queremos renderizar en el cuadrado, asi que la forma en que lo hacemos es ponerla en uno de los bancos de texturas que OpenGL nos ofrece
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        /* Aca es donde el muestrador 2D recuerda la suma del uniforme 2D en el sombreador de fragmentos que usa de forma
         * predeterminada la textura en le banco de texturas cero. Luego solo tenemos algo para vincular nuestra textura, que es
         * una textura 2D y luego toma el ID de textura que queremos vincular. */
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texturedModel.getTexture().getTextureID());
        GL11.glDrawElements(GL11.GL_TRIANGLES, model.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
        // Deshabilita la lista de atributos cuando termine de usar todo
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        // Desvincula el VAO
        GL30.glBindVertexArray(0);
    }

}
