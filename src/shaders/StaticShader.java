package shaders;

import entities.Camera;
import org.lwjgl.util.vector.Matrix;
import org.lwjgl.util.vector.Matrix4f;
import toolBox.Maths;

public class StaticShader extends ShaderProgram {

    private static final String VERTEX_FILE = "src/shaders/vertexShader.txt";
    private static final String FRAGMENT_FILE = "src/shaders/fragmentShader.txt";

    private int location_projectionMatrix;
    private int location_viewMatrix;
    private int location_transformationMatrix;

    public StaticShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
    }

    /**
     * Carga todas las ubicaciones uniformes declaradas en el vertexShader.
     */
    @Override
    protected void getAllUniformLocations() {
        // Obtiene la ubicacion de la variable uniforme
        location_projectionMatrix = super.getUniformLocation("projectionMatrix");
        location_viewMatrix = super.getUniformLocation("viewMatrix");
        location_transformationMatrix = super.getUniformLocation("transformationMatrix");
    }

    @Override
    protected void bindAttributes() {
        super.bindAttribute(0, "position");
        // Vincula las coordenadas de texturas para que le de al sombreador de vertices acceso a estas coordenadas en el VAO
        super.bindAttribute(1, "textureCoords");
    }

    /**
     * Carga la matriz de proyeccion.
     *
     * @param matrix matriz de proyeccion.
     */
    public void loadProjectionMatrix(Matrix4f matrix) {
        super.loadMatrix(location_projectionMatrix, matrix);
    }

    /**
     * Carga la matriz de vista.
     *
     * @param camera camara.
     */
    public void loadViewMatrix(Camera camera) {
        Matrix4f viewMatrix = Maths.createViewMatrix(camera);
        super.loadMatrix(location_viewMatrix, viewMatrix);
    }

    /**
     * Carga la matriz de transformacion.
     *
     * @param matrix matriz de transformacion.
     */
    public void loadTransformationMatrix(Matrix4f matrix) {
        super.loadMatrix(location_transformationMatrix, matrix);
    }


}
