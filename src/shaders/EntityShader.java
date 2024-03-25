package shaders;

import entities.Camera;
import entities.Light;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import utils.Maths;

import java.util.List;

public class EntityShader extends ShaderProgram {

    private static final int MAX_LIGHTS = 4;

    private static final String VERTEX_FILE = "src/shaders/vertexShader.txt";
    private static final String FRAGMENT_FILE = "src/shaders/fragmentShader.txt";

    private int location_projectionMatrix;
    private int location_viewMatrix;
    private int location_transformationMatrix;
    private int location_lightPosition[];
    private int location_lightColour[];
    private int location_attenuation[];
    private int location_shineDamper;
    private int location_reflectivity;
    private int location_useFakeLighting;
    private int location_skyColor;
    private int location_numberOfRows;
    private int location_offset;

    public EntityShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
    }

    @Override
    protected void bindAttributes() {
        // Vincula la variable position al indice 0 para que le de al Vertex Shader acceso a esta posicion en el VAO
        bindAttribute(0, "position"); // Es importante que los nombres de las variables coincidan con los del Vertex Shader
        bindAttribute(1, "textureCoords");
        bindAttribute(2, "normal");
    }

    @Override
    protected void getAllUniformLocations() {
        // Obtiene la ubicacion de la variable uniforme en el shader y la almacena en la variable local
        location_projectionMatrix = getUniformLocation("projectionMatrix");
        location_viewMatrix = getUniformLocation("viewMatrix");
        location_transformationMatrix = getUniformLocation("transformationMatrix");
        location_shineDamper = getUniformLocation("shineDamper");
        location_reflectivity = getUniformLocation("reflectivity");
        location_useFakeLighting = getUniformLocation("useFakeLighting");
        location_skyColor = getUniformLocation("skyColor");
        location_numberOfRows = getUniformLocation("numberOfRows");
        location_offset = getUniformLocation("offset");

        location_lightPosition = new int[MAX_LIGHTS];
        location_lightColour = new int[MAX_LIGHTS];
        location_attenuation = new int[MAX_LIGHTS];

        for (int i = 0; i < MAX_LIGHTS; i++) {
            location_lightPosition[i] = getUniformLocation("lightPosition[" + i + "]");
            location_lightColour[i] = getUniformLocation("lightColour[" + i + "]");
            location_attenuation[i] = getUniformLocation("attenuation[" + i + "]");
        }
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
        Matrix4f matrix = Maths.createViewMatrix(camera);
        loadMatrix(location_viewMatrix, matrix);
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
     * Carga las fuentes de luz.
     *
     * @param lights fuentes de luz.
     */
    public void loadLights(List<Light> lights) {
        /* Carga las primeras cuatro luces de la lista en las variables uniformes del shader y si hay menos de 4 luces en la lista,
         * carga una lista de ceros para llenar esos espacios adicionales en las matrices uniformes. */
        for (int i = 0; i < MAX_LIGHTS; i++) {
            if (i < lights.size()) {
                loadVector(location_lightPosition[i], lights.get(i).getPosition());
                loadVector(location_lightColour[i], lights.get(i).getColour());
                loadVector(location_attenuation[i], lights.get(i).getAttenuation());
            } else {
                loadVector(location_lightPosition[i], new Vector3f(0, 0, 0));
                loadVector(location_lightColour[i], new Vector3f(0, 0, 0));
                loadVector(location_attenuation[i], new Vector3f(1, 0, 0));
            }
        }
    }

    /**
     * Carga la luz especular.
     *
     * @param damper       factor de amortiguacion.
     * @param reflectivity reflectividad.
     */
    public void loadSpecularLight(float damper, float reflectivity) {
        loadFloat(location_shineDamper, damper);
        loadFloat(location_reflectivity, reflectivity);
    }

    /**
     * Carga la iluminacion falsa.
     *
     * @param useFake si la textura usa o no iluminacion falsa.
     */
    public void loadFakeLighting(boolean useFake) {
        loadBoolean(location_useFakeLighting, useFake);
    }

    /**
     * Carga el color del cielo.
     *
     * @param r canal rojo.
     * @param b canal azul.
     * @param g canal verde.
     */
    public void loadSkyColor(float r, float g, float b) {
        loadVector(location_skyColor, new Vector3f(r, g, b));
    }

    public void loadNumberOfRows(int numberOfRows) {
        loadFloat(location_numberOfRows, numberOfRows);
    }

    public void loadOffset(float x, float y) {
        load2DVector(location_offset, new Vector2f(x, y));
    }

}
