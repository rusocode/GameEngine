package shaders;

import entities.Camera;
import entities.Light;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import toolBox.Maths;

public class TerrainShader extends ShaderProgram {


    private static final String VERTEX_FILE = "src/shaders/terrainVertexShader.txt";
    private static final String FRAGMENT_FILE = "src/shaders/terrainFragmentShader.txt";

    private int location_projectionMatrix;
    private int location_viewMatrix;
    private int location_transformationMatrix;
    private int location_lightPosition;
    private int location_lightColour;
    private int location_shineDamper;
    private int location_reflectivity;
    private int location_skyColor;
    private int location_backgroundTexture;
    private int location_rTexture;
    private int location_gTexture;
    private int location_bTexture;
    private int location_blendMap;


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
        location_lightPosition = getUniformLocation("lightPosition");
        location_lightColour = getUniformLocation("lightColour");
        location_shineDamper = getUniformLocation("shineDamper");
        location_reflectivity = getUniformLocation("reflectivity");
        location_skyColor = getUniformLocation("skyColor");
        location_backgroundTexture = getUniformLocation("backgroundTexture");
        location_rTexture = getUniformLocation("rTexture");
        location_gTexture = getUniformLocation("gTexture");
        location_bTexture = getUniformLocation("bTexture");
        location_blendMap = getUniformLocation("blendMap");
    }

    /**
     * Conecta los samplers del shader a cada unidad de textura.
     */
    public void connectTextureUnits() {
        loadInt(location_backgroundTexture, 0);
        loadInt(location_rTexture, 1);
        loadInt(location_gTexture, 2);
        loadInt(location_bTexture, 3);
        loadInt(location_blendMap, 4);
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
     * Carga la fuente de luz.
     *
     * @param light fuente de luz.
     */
    public void loadLight(Light light) {
        loadVector(location_lightPosition, light.getPosition());
        loadVector(location_lightColour, light.getColour());
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

}
