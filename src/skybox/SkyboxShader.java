package skybox;

import render.DisplayManager;
import entities.Camera;
import shaders.ShaderProgram;
import utils.Maths;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Matrix4f;

public class SkyboxShader extends ShaderProgram {

    private static final String VERTEX_FILE = "src/skybox/skyboxVertexShader.glsl";
    private static final String FRAGMENT_FILE = "src/skybox/skyboxFragmentShader.glsl";

    private static final float ROTATE_SPEED = 1f;

    private int location_viewMatrix, location_projectionMatrix;
    private int location_fogColour;
    private int location_cubeMap;
    private int location_cubeMap2;
    private int location_blendFactor;

    private float rotation;

    public SkyboxShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
    }

    @Override
    protected void bindAttributes() {
        bindAttribute(0, "position");
    }

    @Override
    protected void getAllUniformLocations() {
        location_viewMatrix = getUniformLocation("viewMatrix");
        location_projectionMatrix = getUniformLocation("projectionMatrix");
        location_fogColour = getUniformLocation("fogColour");
        location_cubeMap = getUniformLocation("cubeMap");
        location_cubeMap2 = getUniformLocation("cubeMap2");
        location_blendFactor = getUniformLocation("blendFactor");
    }

    public void loadProjectionMatrix(Matrix4f matrix) {
        loadMatrix(location_projectionMatrix, matrix);
    }

    public void loadViewMatrix(Camera camera) {
        Matrix4f matrix = Maths.createViewMatrix(camera);
        /* La ultima columa de la matriz determina la traslacion, asi que al configurar esa traslacion en 0, la matriz de vista no
         * hara que el skybox se mueva en relacion con la camara. */
        matrix.m30 = 0;
        matrix.m31 = 0;
        matrix.m32 = 0;
        rotation += ROTATE_SPEED * DisplayManager.getFrameTimeSeconds();
        // Rota la matriz en el eje y usando el valor de rotacion calculado
        Matrix4f.rotate((float) Math.toRadians(rotation), new Vector3f(0, 1, 0), matrix, matrix);
        loadMatrix(location_viewMatrix, matrix);
    }

    public void loadFogColour(float r, float g, float b) {
        loadVector(location_fogColour, new Vector3f(r, g, b));
    }

    public void loadBlendFactor(float blend) {
        loadFloat(location_blendFactor, blend);
    }

    public void connectTextureUnits() {
        loadInt(location_cubeMap, 0);
        loadInt(location_cubeMap2, 1);
    }

}
