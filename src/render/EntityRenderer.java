package render;

import entities.Entity;
import models.RawModel;
import models.TexturedModel;
import shaders.EntityShader;
import textures.ModelTexture;
import toolBox.Maths;

import org.lwjgl.opengl.*;
import org.lwjgl.util.vector.Matrix4f;

import java.util.List;
import java.util.Map;

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
                /* Renderiza primitivas graficas, como triangulos, lineas o puntos, mediante el uso de indices almacenados en un VBO. Esta
                 * funcion es esencial en el proceso de renderizado y juega un papel fundamental en la visualizacion de modelos 3D. */
                GL11.glDrawElements(GL11.GL_TRIANGLES, model.getRawModel().getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
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
        GL30.glBindVertexArray(rawModel.getVaoID());
        /* Despues de vincular los atributos en el VAO, los habilita especificando el indice correspondiente al atributo en el
         * Vertex Shader. Al habilitar un atributo, se indica a OpenGL que utilice esos datos durante el renderizado. */
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glEnableVertexAttribArray(2);
        ModelTexture texture = model.getTexture();
        // Deshabilita la seleccion de caras posteriores cada vez que renderiza una textura con transparencia
        if (texture.isHasTransparency()) MasterRenderer.disableCulling();
        shader.loadFakeLighting(texture.isUseFakeLighting());
        shader.loadSpecularLight(texture.getShineDamper(), texture.getReflectivity());
        /* Selecciona la unidad de textura activa entre las disponibles en el contexto. OpenGL permite multiples unidades de
         * textura (generalmente numeradas desde GL_TEXTURE0 hasta GL_TEXTURE31), y esta funcion te permite elegir cual estara
         * activa para las operaciones subsiguientes. Es especialmente util al trabajar con shaders, ya que posibilita asignar
         * diferentes texturas a diferentes unidades y luego utilizar uniformes en los shaders para especificar a cual unidad de
         * textura debe hacer referencia cada textura. */
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        /* Enlaza una textura a una unidad de textura activa. En OpenGL, las texturas se vinculan a unidades de textura, y esta
         * funcion gestiona esa asociacion. En el uso comun de varias unidades de textura en OpenGL, glBindTexture() facilita
         * cambiar entre texturas al activar diferentes unidades y vincular las texturas correspondientes a esas unidades. Ademas,
         * al enlazar texturas, se puede configurar como interactuan con los fragmentos en el shader durante el proceso de
         * renderizado. */
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, model.getTexture().getID());
    }

    /**
     * Carga el modelo de la matriz.
     *
     * @param entity entidad.
     */
    private void loadModelMatrix(Entity entity) {
        Matrix4f transformationMatrix = Maths.createTransformationMatrix(entity.getPosition(), entity.getAngle(), entity.getScale());
        shader.loadTransformationMatrix(transformationMatrix);
    }

    /**
     * Deshabilita el modelo texturizado.
     */
    private void unbindTexturedModel() {
        // Habilita la seleccion nuevamente para que este habilitado para el siguiente modelo
        MasterRenderer.enableCulling();
        // Deshabilita la lista de atributos
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL20.glDisableVertexAttribArray(2);
        // Desvincula el VAO
        GL30.glBindVertexArray(0);
    }

}
