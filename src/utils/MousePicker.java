package utils;

import entities.Camera;
import terrains.Terrain;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

/**
 * <a href="https://antongerdelan.net/opengl/raycasting.html">Mouse Picking with Ray Casting</a>
 */

public class MousePicker {

    // Cantidad de veces que comprueba la busqueda del punto de interseccion del rayo, mientras mas alto sea este valor mas precisa sera la interseccion
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
        Vector2f normalizedCoords = getNormalizedDeviceCoords(Mouse.getX(), Mouse.getY());
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
     * Convierte el espacio de clip al espacio ocular.
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

    /**
     * Basicamente en cada frame intenta encontrar el punto de interseccion del rayo con el terrreno en algun lugar entre su punto
     * inicial en la camara y dentro de un cierto rango. Intenta encontrar el punto de interseccion usando una busqueda binaria,
     * por lo que primero verifica el punto medio del rango del rayo y descubre si este punto esta por encima o por debajo del
     * terreno. Si el punto esta por debajo del terreno, entonces la interseccion debe estar en la mitad superior del rango del
     * rayo, pero si el punto esta sobre el terreno entonces la interseccion debe estar en la mitad inferior del rango del rayo.
     * Entonces toma la mitad del rango del rayo en la que esta la interseccion y repite el proceso. Esto se repite una cantidad
     * de veces determinada por RECURSION_COUNT y una vez que haya terminado devuelve el punto medio de la seccion del rayo
     * sobrante, que en ese momento sera muy pequenio y dara una posicion bastante precisa para la interseccion.
     *
     * @param count  contador de busquedas (iterador).
     * @param start  inicio del rango del rayo.
     * @param finish final del rango del rayo.
     * @param ray    rayo.
     * @return el punto de interseccion del rayo con el terreno.
     */
    private Vector3f binarySearch(int count, float start, float finish, Vector3f ray) {
        float half = start + ((finish - start) / 2);
        // Si se termino de buscar el punto de interseccion del rayo
        if (count >= RECURSION_COUNT) {
            Vector3f endPoint = getPointOnRay(ray, half);
            Terrain terrain = getTerrain(endPoint.getX(), endPoint.getZ());
            // Si el terreno es distinto a nulo, devuelve el punto de interseccion
            if (terrain != null) return endPoint;
            else return null;
        }
        // Si la interseccion esta en la mitad superior del rango del rayo
        if (intersectionInRange(start, half, ray))
            return binarySearch(count + 1, start, half, ray); // Mitad superior del rango del rayo
        else
            return binarySearch(count + 1, half, finish, ray); // Mitad inferior del rango del rayo
    }

    /**
     * Verifica la interseccion en el rango del rayo.
     *
     * @param start  punto inicial del rayo.
     * @param finish punto final del rayo.
     * @param ray    rayo.
     * @return si el punto inicial no esta bajo tierra y si el punto final esta bajo tierra.
     */
    private boolean intersectionInRange(float start, float finish, Vector3f ray) {
        // Obtiene el punto del rayo de inicio y final
        Vector3f startPoint = getPointOnRay(ray, start);
        Vector3f endPoint = getPointOnRay(ray, finish);
        return !isUnderGround(startPoint) && isUnderGround(endPoint);
    }

    /**
     * Obtiene el punto en el rayo.
     *
     * @param ray      rayo.
     * @param distance distancia del rayo.
     * @return el punto en el rayo.
     */
    private Vector3f getPointOnRay(Vector3f ray, float distance) {
        Vector3f camPos = camera.getPosition();
        // Posicion de la camara
        Vector3f start = new Vector3f(camPos.x, camPos.y, camPos.z);
        /* Escala el rayo (que inicia desde la posicion de la camara) a la distancia especificada (la distancia es el rango
         * especificado que esta entre el inicio y final del rango del rayo). */
        Vector3f scaledRay = new Vector3f(ray.x * distance, ray.y * distance, ray.z * distance);
        return Vector3f.add(start, scaledRay, null);
    }

    /**
     * Verifica si el punto esta bajo tierra.
     *
     * @param testPoint punto.
     * @return si el punto esta bajo tierra.
     */
    private boolean isUnderGround(Vector3f testPoint) {
        Terrain terrain = getTerrain(testPoint.getX(), testPoint.getZ());
        float height = 0;
        // Almacena el alto del terreno en ese punto
        if (terrain != null) height = terrain.getHeightOfTerrain(testPoint.getX(), testPoint.getZ());
        // Verifica si el alto del punto es menor al alto del terreno
        return testPoint.y < height;
    }

    /**
     * Obtiene el terreno actual.
     *
     * @param worldX grilla x del terreno.
     * @param worldZ grilla z del terreno.
     * @return el terreno actual.
     */
    private Terrain getTerrain(float worldX, float worldZ) {
        // int x = (int) (worldX / Terrain.SIZE);
        // int z = (int) (worldZ / Terrain.SIZE);
        // return terrains[x][z];
        return terrain;
    }

}
