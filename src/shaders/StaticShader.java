package shaders;

import entities.Camera;
import entities.Light;
import org.lwjgl.util.vector.Matrix;
import org.lwjgl.util.vector.Matrix4f;
import toolBox.Maths;

public class StaticShader extends ShaderProgram {

    private static final String VERTEX_FILE = "src/shaders/vertexShader.txt";
    private static final String FRAGMENT_FILE = "src/shaders/fragmentShader.txt";

    private int location_projectionMatrix;
    private int location_viewMatrix;
    private int location_transformationMatrix;
    private int location_lightPosition;
    private int location_lightColour;
    private int location_shineDamper;
    private int location_reflectivity;

    public StaticShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
    }

    @Override
    protected void bindAttributes() {
        bindAttribute(0, "position");
        // Vincula las coordenadas de texturas para que le de al sombreador de vertices acceso a estas coordenadas en el VAO
        bindAttribute(1, "textureCoords");
        bindAttribute(2, "normal");
    }

    /**
     * Carga todas las ubicaciones uniformes declaradas en el vertexShader.
     */
    @Override
    protected void getAllUniformLocations() {
        // Obtiene la ubicacion de la variable uniforme
        location_projectionMatrix = getUniformLocation("projectionMatrix");
        location_viewMatrix = getUniformLocation("viewMatrix");
        location_transformationMatrix = getUniformLocation("transformationMatrix");
        location_lightPosition = getUniformLocation("lightPosition");
        location_lightColour = getUniformLocation("lightColour");
        location_shineDamper = getUniformLocation("shineDamper");
        location_reflectivity = getUniformLocation("reflectivity");
    }

    /**
     * Carga la matriz de proyeccion.
     *
     * @param matrix matriz de proyeccion.
     */
    public void loadProjectionMatrix(Matrix4f matrix) {
        loadMatrix(location_projectionMatrix, matrix);
    }

    /**
     * Carga la matriz de vista.
     *
     * @param camera camara.
     */
    public void loadViewMatrix(Camera camera) {
        Matrix4f viewMatrix = Maths.createViewMatrix(camera);
        loadMatrix(location_viewMatrix, viewMatrix);
    }

    /**
     * Carga la matriz de transformacion.
     *
     * @param matrix matriz de transformacion.
     */
    public void loadTransformationMatrix(Matrix4f matrix) {
        loadMatrix(location_transformationMatrix, matrix);
    }

    /**
     * Carga las variables de luz.
     */
    public void loadLight(Light light) {
        loadVector(location_lightPosition, light.getPosition());
        loadVector(location_lightColour, light.getColour());
    }

    /**
     * Carga las variables de brillo.
     */
    public void loadShineVariables(float damper, float reflectivity) {
        loadFloat(location_shineDamper, damper);
        loadFloat(location_reflectivity, reflectivity);
    }

}
