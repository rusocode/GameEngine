package render;

import entities.Entity;
import models.RawModel;
import models.TexturedModel;
import org.lwjgl.opengl.*;
import shaders.StaticShader;
import toolBox.Maths;

import org.lwjgl.util.vector.Matrix4f;

/**
 * Renderiza el modelo texturizado.
 *
 * <a href="https://www.youtube.com/watch?v=1KGwhqhJlDI&list=RD_xYNJlx03_M&index=4">:D</a>
 */

public class Renderer {

    private static final int FOV = 70; // Angulo de vision
    private static final float NEAR_PLANE = 0.1f; // Plano cercano
    private static final float FAR_PLANE = 1000; // Plano lejano

    private Matrix4f projectionMatrix;

    public Renderer(StaticShader shader) {
        createProjectionMatrix();
        shader.start();
        shader.loadProjectionMatrix(projectionMatrix);
        shader.stop();
    }

    /**
     * Se llama una vez en cada fotograma y simplemente prepara a OpenGL para renderizar el juego.
     */
    public void prepare() {
        GL11.glEnable(GL11.GL_DEPTH_TEST); // Para que OpenGL pruebe que triangulo esta por encima del otro evitando que se superpongan
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        // Borra el color del ultimo fotograma
        GL11.glClearColor(1, 0, 0, 1);
    }

    /**
     * Renderiza el modelo.
     *
     * @param entity entidad.
     * @param shader shader.
     */
    public void render(Entity entity, StaticShader shader) {
        TexturedModel model = entity.getModel();
        RawModel rawModel = model.getRawModel();
        GL30.glBindVertexArray(rawModel.getVaoID());
        /* Habilita los atributos de vertice almacenados en el VAO especificando el indice (este indice corresponde al indice del
         * atributo en el shader de vertices). Estas llamadas son comunes despues de haber configurado los atributos de vertices
         * en un VAO y preceden al renderizado.
         * Al habilitar un atributo de vertice, le estas indicando a OpenGL que debe usar los datos asociados a ese atributo
         * durante el proceso de renderizado. Es importante destacar que despues de habilitar un atributo, deberias deshabilitarlo
         * cuando ya no lo necesites para evitar problemas inesperados. Esto se hace mediante la funcion glDisableVertexAttribArray(). */
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        Matrix4f transformationMatrix = Maths.createTransformationMatrix(entity.getPosition(), entity.getRotX(), entity.getRotY(), entity.getRotZ(), entity.getScale());
        shader.loadTransformationMatrix(transformationMatrix);
        /* La funcion glActiveTexture() en OpenGL se utiliza para especificar que unidad de textura activar entre las disponibles
         * en el contexto de OpenGL. En OpenGL, puedes tener multiples unidades de textura (usualmente numeradas desde GL_TEXTURE0
         * hasta GL_TEXTURE31, dependiendo de la implementacion) y glActiveTexture() te permite seleccionar cual de ellas estara
         * activa para las operaciones subsiguientes.
         *
         * Este mecanismo es especialmente util cuando trabajas con sombreadores (shaders) en OpenGL, ya que te permite asignar
         * diferentes texturas a diferentes unidades de textura y luego usar uniformes en tus shaders para especificar a cual
         * unidad de textura debe hacer referencia cada textura. */
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        /* La funcion glBindTexture() en OpenGL se utiliza para enlazar (bind) una textura a una unidad de textura activa. En
         * OpenGL, las texturas se asocian a unidades de textura, y glBindTexture() se encarga de esa asociacion.
         *
         * Es comun usar varias unidades de textura en OpenGL, y glBindTexture() permite cambiar facilmente entre texturas al
         * activar diferentes unidades de textura y enlazar las texturas apropiadas a esas unidades. Ademas, al enlazar texturas,
         * se puede configurar como interactuan con los fragmentos en el shader durante el proceso de renderizado. */
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, model.getTexture().getTextureID());
        /* La funcion glDrawElements() en OpenGL se utiliza para renderizar primitivas graficas (como triangulos, lineas o puntos)
         * utilizando indices almacenados en un Vertex Buffer Object (VBO). Este metodo es parte del proceso de renderizado y es
         * fundamental para la visualizacion de modelos 3D. */
        GL11.glDrawElements(GL11.GL_TRIANGLES, rawModel.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
        // Deshabilita la lista de atributos
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        // Desvincula el VAO
        GL30.glBindVertexArray(0);
    }

    private void createProjectionMatrix() {
        float aspectRatio = (float) Display.getWidth() / Display.getHeight();
        float yScale = (float) (1f / Math.tan(Math.toRadians(FOV / 2f)) * aspectRatio);
        float xScale = yScale / aspectRatio;
        float frustumLength = FAR_PLANE - NEAR_PLANE;

        projectionMatrix = new Matrix4f();
        projectionMatrix.m00 = xScale;
        projectionMatrix.m11 = yScale;
        projectionMatrix.m22 = -((FAR_PLANE + NEAR_PLANE) / frustumLength);
        projectionMatrix.m23 = -1;
        projectionMatrix.m32 = -((2 * NEAR_PLANE * FAR_PLANE) / frustumLength);
        projectionMatrix.m33 = 0;
    }

}
