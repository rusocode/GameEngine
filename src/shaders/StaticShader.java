package shaders;

/**
 * Implementacion de los shaders.
 */

public class StaticShader extends ShaderProgram {

    private static final String VERTEX_FILE = "src/shaders/vertexShader.txt";
    private static final String FRAGMENT_FILE = "src/shaders/fragmentShader.txt";

    public StaticShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
    }

    @Override
    protected void bindAttributes() {
        /* Vincula el atributo 0 del VAO porque ahi es donde se almacena la posicion de vertice y lo conecta a la variable de
         * posicion de entrada en el vertexShader. */
        super.bindAttribute(0, "position");
    }
}
