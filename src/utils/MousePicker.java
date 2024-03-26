package utils;

import entities.Camera;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import terrains.Terrain;

/**
 * <a href="https://antongerdelan.net/opengl/raycasting.html">Mouse Picking with Ray Casting</a>
 */

public class MousePicker {

    private static final int RECURSION_COUNT = 200;
    private static final float RAY_RANGE = 600; // Rango del rayo

    private final Camera camera;
    private final Matrix4f projectionMatrix;
    private Matrix4f viewMatrix;

    private final Terrain terrain;
    private Vector3f currentTerrainPoint;

    public MousePicker(Camera camera, Matrix4f projectionMatrix, Terrain terrain) {
        this.camera = camera;
        this.projectionMatrix = projectionMatrix;
        this.viewMatrix = Maths.createViewMatrix(camera);
        this.terrain = terrain;
    }

    public Vector3f getCurrentTerrainPoint() {
        return currentTerrainPoint;
    }

    public void update() {
        viewMatrix = Maths.createViewMatrix(camera);
        Vector3f currentRay = calculateMouseRay();
        if (intersectionInRange(0, RAY_RANGE, currentRay)) currentTerrainPoint = binarySearch(0, 0, RAY_RANGE, currentRay);
        else currentTerrainPoint = null;
    }

    /**
     * Calcula la direccion de los rayos proyectados en funcion de la posicion del mouse en pantalla.
     *
     * @return el rayo en el espacio mundial.
     */
    private Vector3f calculateMouseRay() {
        float mouseX = Mouse.getX();
        float mouseY = Mouse.getY();
        Vector2f normalizedCoords = getNormalizedDeviceCoords(mouseX, mouseY);
        // Resta -1 al eje [z] para que apunte hacia la pantalla y agrega un componente w para convertirlo en un vector 4D
        Vector4f clipCoords = new Vector4f(normalizedCoords.x, normalizedCoords.y, -1f, -1f);
        Vector4f eyeCoords = toEyeCoords(clipCoords);
        Vector3f worldRay = toWorldCoords(eyeCoords);
        return worldRay;
    }

    /**
     * Convierte las coordenadas oculares a coordenadas mundiales.
     *
     * @param eyeCoords coordenadas del espacio ocular.
     * @return las coordenadas del mundo.
     */
    private Vector3f toWorldCoords(Vector4f eyeCoords) {
        Matrix4f invertedView = Matrix4f.invert(viewMatrix, null);
        Vector4f rayWorld = Matrix4f.transform(invertedView, eyeCoords, null);
        Vector3f mouseRay = new Vector3f(rayWorld.x, rayWorld.y, rayWorld.z);
        // Normaliza el rayo porque es una direccion y queremos que sea un vector
        mouseRay.normalise();
        return mouseRay;
    }

    /**
     * Convierte el espacio de clip al espacion ocular.
     *
     * @param clipCoords coordenadas del espacio de clip.
     * @return las coordenadas 4D en el espacio ocular.
     */
    private Vector4f toEyeCoords(Vector4f clipCoords) {
        Matrix4f invertedProjection = Matrix4f.invert(projectionMatrix, null);
        Vector4f eyeCoords = Matrix4f.transform(invertedProjection, clipCoords, null);
        return new Vector4f(eyeCoords.x, eyeCoords.y, -1f, 0f);
    }

    /**
     * Normaliza (convierte) las coordenadas del mouse en pantalla al sistema de coordenadas de OpenGL.
     *
     * @param mouseX coordenada [x] del mouse.
     * @param mouseY coordenada [y] del mouse.
     * @return la posicion [x,y] del mouse en el sistema de coordenadas de OpenGL.
     */
    private Vector2f getNormalizedDeviceCoords(float mouseX, float mouseY) {
        float x = (2f * mouseX) / Display.getWidth() - 1;
        float y = (2f * mouseY) / Display.getHeight() - 1;
        return new Vector2f(x, y);
    }

    // ***

    private Vector3f getPointOnRay(Vector3f ray, float distance) {
        Vector3f camPos = camera.getPosition();
        Vector3f start = new Vector3f(camPos.x, camPos.y, camPos.z);
        Vector3f scaledRay = new Vector3f(ray.x * distance, ray.y * distance, ray.z * distance);
        return Vector3f.add(start, scaledRay, null);
    }

    private Vector3f binarySearch(int count, float start, float finish, Vector3f ray) {
        float half = start + ((finish - start) / 2f);
        if (count >= RECURSION_COUNT) {
            Vector3f endPoint = getPointOnRay(ray, half);
            Terrain terrain = getTerrain(endPoint.getX(), endPoint.getZ());
            if (terrain != null) return endPoint;
            else return null;
        }
        if (intersectionInRange(start, half, ray)) return binarySearch(count + 1, start, half, ray);
        else return binarySearch(count + 1, half, finish, ray);
    }

    private boolean intersectionInRange(float start, float finish, Vector3f ray) {
        Vector3f startPoint = getPointOnRay(ray, start);
        Vector3f endPoint = getPointOnRay(ray, finish);
        return !isUnderGround(startPoint) && isUnderGround(endPoint);
    }

    private boolean isUnderGround(Vector3f testPoint) {
        Terrain terrain = getTerrain(testPoint.getX(), testPoint.getZ());
        float height = 0;
        if (terrain != null) height = terrain.getHeightOfTerrain(testPoint.getX(), testPoint.getZ());
        return testPoint.y < height;
    }

    private Terrain getTerrain(float worldX, float worldZ) {
        return terrain;
    }

}
