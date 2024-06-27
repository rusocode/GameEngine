package render;

import entities.Entity;
import models.RawModel;
import models.TexturedModel;
import shaders.EntityShader;
import textures.ModelTexture;
import utils.Maths;

import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Matrix4f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * Renderiza la entidad.
 * <p>
 * La matriz de vista controla la posicion y orientacion de la camara, mientras que la matriz de proyeccion controla como se
 * proyectan los objetos en la pantalla. Ambas son cruciales para lograr una representacion precisa y realista en un entorno 3D.
 * <a href="https://www.youtube.com/watch?v=1KGwhqhJlDI&list=RD_xYNJlx03_M&index=4">:D</a>
 */

public class EntityRenderer {

    private final EntityShader shader;

    public EntityRenderer(EntityShader shader, Matrix4f projectionMatrix) {
        this.shader = shader;
        shader.start();
        shader.loadProjectionMatrix(projectionMatrix);
        shader.stop();
    }

    /**
     * Renderiza las entidades.
     *
     * @param entities lista de entidades.
     */
    public void render(Map<TexturedModel, List<Entity>> entities) {
        // Itera todos los modelos
        for (TexturedModel model : entities.keySet()) {
            prepareTexturedModel(model);
            // Obtiene la lista de entidades de ese modelo texturizado y las itera
            List<Entity> batch = entities.get(model);
            for (Entity entity : batch) {
                loadModelMatrix(entity);
                /* Renderiza triangulos mediante el uso de indices almacenados en un vbo. Como le estamos pasando un buffer de
                 * indices que contiene ints, entonces se especifica con GL_UNSIGNED_INT comenzando desde el principio. */
                glDrawElements(GL_TRIANGLES, model.getRawModel().getVertexCount(), GL_UNSIGNED_INT, 0);
            }
            unbindTexturedModel();
        }
    }

    /**
     * Prepara el modelo texturizado.
     *
     * @param model modelo texturizado.
     */
    private void prepareTexturedModel(TexturedModel model) {
        RawModel rawModel = model.getRawModel();
        /* Obtiene el id para vincular el vao de ese modelo, lo que permite a OpenGL recordar los estados de los atributos de
         * vertices asociados a ese vao para su uso posterior durante el proceso de renderizado. */
        glBindVertexArray(rawModel.getID());
        /* Habilita la lista de atributos especificando el numero correspondiente. Al habilitar un atributo, se indica a OpenGL que
         * utilice esos datos durante el renderizado. La lista de atributos viene deshabilitada por defecto. */
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
        // Obtiene la textura del modelo para poder cargar los datos especificos en el shader
        ModelTexture texture = model.getTexture();
        shader.loadNumberOfRows(texture.getNumberOfRows());
        // Deshabilita la seleccion de caras posteriores cada vez que renderiza una textura con transparencia
        if (texture.isHasTransparency()) MasterRenderer.disableCulling();
        shader.loadFakeLighting(texture.isUseFakeLighting());
        // Carga los valores de luz especular en el shader antes de renderizar el objeto
        shader.loadSpecularLight(texture.getShineDamper(), texture.getReflectivity());
        /* Selecciona la unidad de textura activa entre las disponibles en el contexto. OpenGL permite multiples unidades de
         * textura (generalmente numeradas desde GL_TEXTURE0 hasta GL_TEXTURE31), y esta funcion te permite elegir cual estara
         * activa para las operaciones subsiguientes. Es especialmente util al trabajar con shaders, ya que posibilita asignar
         * diferentes texturas a diferentes unidades y luego utilizar uniformes en los shaders para especificar a cual unidad de
         * textura debe hacer referencia cada textura. */
        glActiveTexture(GL_TEXTURE0);
        /* Enlaza una textura a una unidad de textura activa. En OpenGL, las texturas se vinculan a unidades de textura, y esta
         * funcion gestiona esa asociacion. En el uso comun de varias unidades de textura en OpenGL, glBindTexture() facilita
         * cambiar entre texturas al activar diferentes unidades y vincular las texturas correspondientes a esas unidades. Ademas,
a         * al enlazar texturas, se puede configurar como interactuan con los fragmentos en el shader durante el proceso de
         * renderizado. */
        glBindTexture(GL_TEXTURE_2D, model.getTexture().getID());
    }

    /**
     * Carga el modelo de la matriz.
     *
     * @param entity entidad.
     */
    private void loadModelMatrix(Entity entity) {
        Matrix4f matrix = Maths.createTransformationMatrix(entity.getPosition(), entity.getAngle(), entity.getScale());
        shader.loadTransformationMatrix(matrix);
        shader.loadOffset(entity.getTextureXOffset(), entity.getTextureYOffset());
    }

    /**
     * Deshabilita el modelo texturizado.
     */
    private void unbindTexturedModel() {
        // Habilita la seleccion nuevamente para que este habilitado para el siguiente modelo
        MasterRenderer.enableCulling();
        // Deshabilita la lista de atributos
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        // Desvincula el vao
        glBindVertexArray(0);
    }

}
