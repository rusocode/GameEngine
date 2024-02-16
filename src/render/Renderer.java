package render;

import entities.Entity;
import models.RawModel;
import models.TexturedModel;
import org.lwjgl.opengl.*;
import shaders.StaticShader;
import toolBox.Maths;

import org.lwjgl.util.vector.Matrix4f;

/**
 * Representa el modelo texturizado del VAO.
 */

public class Renderer {

    private static final int FOV = 70;
    private static final float NEAR_PLANE = 0.1f;
    private static final float FAR_PLANE = 1000;

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
        GL11.glEnable(GL11.GL_DEPTH_TEST); // Para que OpenGL pruebe que triangulo esta por encima del otro
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
        // Vincula el VAO
        GL30.glBindVertexArray(rawModel.getVaoID());
        // Activa la lista de atributos en la que se almacenan los datos
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        // Carga la transformacion de la entidad
        Matrix4f transformationMatrix = Maths.createTransformationMatrix(entity.getPosition(), entity.getRotX(), entity.getRotY(), entity.getRotZ(), entity.getScale());
        shader.loadTransformationMatrix(transformationMatrix);
        // Le dice a OpenGL cual textura queremos renderizar en el cuadrado, asi que la forma en que lo hacemos es ponerla en uno de los bancos de texturas que OpenGL nos ofrece
        GL13.glActiveTexture(GL13.GL_TEXTURE0); // Seleccionar unidad de textura activa
        /* Aca es donde el muestrador 2D recuerda la suma del uniforme 2D en el sombreador de fragmentos que usa de forma
         * predeterminada la textura en el banco de texturas cero. Luego solo tenemos algo para vincular nuestra textura, que es
         * una textura 2D y luego toma el ID de textura que queremos vincular. */
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, model.getTexture().getTextureID());
        GL11.glDrawElements(GL11.GL_TRIANGLES, rawModel.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
        // Deshabilita la lista de atributos cuando termine de usar todo
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
