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
 * <p>
 * La matriz de vista controla la posicion y orientacion de la camara, mientras que la matriz de proyeccion controla como se
 * proyectan los objetos en la pantalla. Ambas son cruciales para lograr una representacion precisa y realista en un entorno 3D.
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
        GL11.glClearColor(0, 0, 0, 1);
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
         * Este mecanismo es especialmente util cuando trabajas con sombreadores (shaders) en OpenGL, ya que te permite asignar
         * diferentes texturas a diferentes unidades de textura y luego usar uniformes en tus shaders para especificar a cual
         * unidad de textura debe hacer referencia cada textura. */
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        /* La funcion glBindTexture() en OpenGL se utiliza para enlazar (bind) una textura a una unidad de textura activa. En
         * OpenGL, las texturas se asocian a unidades de textura, y glBindTexture() se encarga de esa asociacion.
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

    /**
     * Una matriz de proyeccion es una matriz utilizada en graficos 3D que representa la transformacion de las coordenadas
     * tridimensionales de un objeto en el espacio tridimensional a coordenadas bidimensionales en un plano de proyeccion. Esta
     * transformacion es fundamental para simular la perspectiva y la profundidad en una escena 3D cuando se proyecta en una
     * pantalla bidimensional, como en un monitor o una ventana de visualizacion.
     * <p>
     * Existen dos tipos principales de matrices de proyeccion en graficos 3D: proyeccion ortografica y proyeccion perspectiva.
     * <p>
     * <b>Proyeccion Ortografica:</b>
     * <ul>
     * <li>En una proyeccion ortografica, los rayos de luz son paralelos entre si al proyectar la escena en el plano de proyeccion.
     * <li>Esta proyeccion no simula el efecto de la perspectiva y mantiene el tamaño de los objetos constante independientemente
     * de su distancia a la camara.
     * <li>La matriz de proyeccion ortografica suele ser representada por una matriz ortogonal.
     * </ul>
     * <b>Proyeccion Perspectiva:</b>
     * <ul>
     * <li>En una proyeccion perspectiva, los rayos de luz convergen hacia un punto de fuga, lo que simula el efecto de la
     * perspectiva en la visualizacion.
     * <li>Los objetos mas cercanos a la camara aparecen mas grandes, mientras que los objetos mas lejanos se ven mas pequeños,
     * creando una sensacion de profundidad.
     * <li>La matriz de proyeccion perspectiva generalmente se representa mediante una matriz de perspectiva.
     * </ul>
     * La forma especifica de la matriz de proyeccion depende de la proyeccion utilizada y del sistema de coordenadas en uso. La
     * multiplicacion de la matriz de proyeccion con la matriz de vista y la matriz de modelo (si se utiliza) forma la conocida
     * matriz MVP (Model-View-Projection), que transforma las coordenadas de los objetos desde el espacio del modelo al espacio de
     * la pantalla.
     * <p>
     * En resumen, una matriz de proyeccion es esencial para representar de manera adecuada la perspectiva y la profundidad en
     * graficos 3D, facilitando la transformacion de las coordenadas 3D a coordenadas 2D para su representacion en una pantalla.
     */
    private void createProjectionMatrix() {
        // Obtiene la relacion de aspecto de la ventana de visualizacion
        float aspectRatio = (float) Display.getWidth() / Display.getHeight();
        // Calcula la escala en Y (yScale) basada en el campo de vision (FOV)
        float yScale = (float) (1f / Math.tan(Math.toRadians(FOV / 2f)) * aspectRatio);
        // Calcula la escala en X (xScale) en funcion de la escala en Y y la relacion de aspecto
        float xScale = yScale / aspectRatio;
        // Calcula la longitud del frustum en el eje Z (profundidad del volumen de visualizacion)
        float frustumLength = FAR_PLANE - NEAR_PLANE;
        // Crea una nueva matriz de 4x4 para almacenar la matriz de proyeccion
        projectionMatrix = new Matrix4f();
        // Configura los elementos especificos de la matriz de proyeccion
        projectionMatrix.m00 = xScale; // Escala en X
        projectionMatrix.m11 = yScale; // Escala en Y
        projectionMatrix.m22 = -((FAR_PLANE + NEAR_PLANE) / frustumLength); // Proyeccion en el eje Z
        projectionMatrix.m23 = -1; // Desplazamiento en el eje Z
        projectionMatrix.m32 = -((2 * NEAR_PLANE * FAR_PLANE) / frustumLength); // Desplazamiento en el eje Z
        projectionMatrix.m33 = 0; // Perspectiva
    }

}
