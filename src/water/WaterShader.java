package water;

import entities.Light;
import shaders.ShaderProgram;
import utils.Maths;
import entities.Camera;

import org.lwjgl.util.vector.Matrix4f;

public class WaterShader extends ShaderProgram {

    private final static String VERTEX_FILE = "src/water/waterVertex.glsl";
    private final static String FRAGMENT_FILE = "src/water/waterFragment.glsl";

    private int location_modelMatrix, location_viewMatrix, location_projectionMatrix;
    private int location_reflectionTexture, location_refractionTexture;
    private int location_dudvMap, location_normalMap, location_depthMap;
    private int location_moveFactor;
    private int location_cameraPosition;
    private int location_lightColour, location_lightPosition;

    public WaterShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
    }

    @Override
    protected void bindAttributes() {
        bindAttribute(0, "position");
    }

    @Override
    protected void getAllUniformLocations() {
        location_modelMatrix = getUniformLocation("modelMatrix");
        location_viewMatrix = getUniformLocation("viewMatrix");
        location_projectionMatrix = getUniformLocation("projectionMatrix");
        location_reflectionTexture = getUniformLocation("reflectionTexture");
        location_refractionTexture = getUniformLocation("refractionTexture");
        location_dudvMap = getUniformLocation("dudvMap");
        location_normalMap = getUniformLocation("normalMap");
        location_depthMap = getUniformLocation("depthMap");
        location_moveFactor = getUniformLocation("moveFactor");
        location_cameraPosition = getUniformLocation("cameraPosition");
        location_lightColour = getUniformLocation("lightColour");
        location_lightPosition = getUniformLocation("lightPosition");
    }

    public void connectTextureUnits() {
        loadInt(location_reflectionTexture, 0);
        loadInt(location_refractionTexture, 1);
        loadInt(location_dudvMap, 2);
        loadInt(location_normalMap, 3);
        loadInt(location_depthMap, 4);
    }

    public void loadProjectionMatrix(Matrix4f projection) {
        loadMatrix(location_projectionMatrix, projection);
    }

    public void loadViewMatrix(Camera camera) {
        Matrix4f matrix = Maths.createViewMatrix(camera);
        loadMatrix(location_viewMatrix, matrix);
        loadVector(location_cameraPosition, camera.getPosition());
    }

    public void loadModelMatrix(Matrix4f modelMatrix) {
        loadMatrix(location_modelMatrix, modelMatrix);
    }

    public void loadMoveFactor(float factor) {
        loadFloat(location_moveFactor, factor);
    }

    public void loadLight(Light sun) {
        loadVector(location_lightColour, sun.getColour());
        loadVector(location_lightPosition, sun.getPosition());
    }

}
