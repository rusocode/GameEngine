package shaders;

import java.util.List;

import entities.Camera;
import entities.Light;
import org.lwjgl.util.vector.Vector4f;
import utils.Maths;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class TerrainShader extends ShaderProgram {

    private static final int MAX_LIGHTS = 4;

    private static final String VERTEX_FILE = "src/shaders/terrainVertexShader.glsl";
    private static final String FRAGMENT_FILE = "src/shaders/terrainFragmentShader.glsl";

    private int location_projectionMatrix, location_viewMatrix, location_transformationMatrix;
    private int location_shineDamper, location_reflectivity;
    private int location_skyColor;
    private int location_background, location_r, location_g, location_b, location_blendMap;
    private int location_plane;
    private int[] location_lightPosition, location_lightColour, location_attenuation;

    public TerrainShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
    }

    @Override
    protected void bindAttributes() {
        bindAttribute(0, "position");
        bindAttribute(1, "textureCoords");
        bindAttribute(2, "normal");
    }

    @Override
    protected void getAllUniformLocations() {
        location_projectionMatrix = getUniformLocation("projectionMatrix");
        location_viewMatrix = getUniformLocation("viewMatrix");
        location_transformationMatrix = getUniformLocation("transformationMatrix");
        location_shineDamper = getUniformLocation("shineDamper");
        location_reflectivity = getUniformLocation("reflectivity");
        location_skyColor = getUniformLocation("skyColor");
        location_background = getUniformLocation("background");
        location_r = getUniformLocation("r");
        location_g = getUniformLocation("g");
        location_b = getUniformLocation("b");
        location_blendMap = getUniformLocation("blendMap");
        location_plane = getUniformLocation("plane");

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
     * Carga el color del cielo.
     *
     * @param r canal rojo.
     * @param g canal verde.
     * @param b canal azul.
     */
    public void loadSkyColor(float r, float g, float b) {
        loadVector(location_skyColor, new Vector3f(r, g, b));
    }

    /**
     * Conecta los variables del shader a cada unidad de textura antes de renderizarlas.
     */
    public void connectTextureUnits() {
        loadInt(location_background, 0);
        loadInt(location_r, 1);
        loadInt(location_g, 2);
        loadInt(location_b, 3);
        loadInt(location_blendMap, 4);
    }

    public void loadClipPlane(Vector4f plane) {
        loadVector(location_plane, plane);
    }

    /**
     * Carga las fuentes de luz.
     *
     * @param lights fuentes de luz.
     */
    public void loadLights(List<Light> lights) {
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


}
