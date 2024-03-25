package render;

import java.util.*;

import entities.*;
import models.TexturedModel;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import shaders.EntityShader;
import shaders.TerrainShader;
import skybox.SkyboxRenderer;
import terrains.Terrain;

public class MasterRenderer {

    private static final int FOV = 70; // Angulo de vision
    private static final float NEAR_PLANE = 0.1f; // Plano cercano
    private static final float FAR_PLANE = 1000; // Plano lejano

    // https://rgbcolorpicker.com/0-1 o usar los colores del skybox
    private static final float RED = 0.592f; // 0.5444f
    private static final float GREEN = 0.871f; // 0.62f
    private static final float BLUE = 0.949f; // 0.69f

    private Matrix4f projectionMatrix;

    private final EntityShader entityShader = new EntityShader();
    private final EntityRenderer entityRenderer;
    private final TerrainShader terrainShader = new TerrainShader();
    private final TerrainRenderer terrainRenderer;
    private final SkyboxRenderer skyboxRenderer;

    private final Map<TexturedModel, List<Entity>> entities = new HashMap<>();
    private final List<Terrain> terrains = new ArrayList<>();

    public MasterRenderer(Loader loader) {
        enableCulling();
        createProjectionMatrix();
        entityRenderer = new EntityRenderer(entityShader, projectionMatrix);
        terrainRenderer = new TerrainRenderer(terrainShader, projectionMatrix);
        skyboxRenderer = new SkyboxRenderer(loader, projectionMatrix);
    }

    public static void enableCulling() {
        // Evita que se rendericen las caras posteriores del modelo (Culling Faces)
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BACK);
    }

    // Desactiva la seleccion de caras posteriores cada vez que renderiza un objeto con transparencia
    public static void disableCulling() {
        GL11.glDisable(GL11.GL_CULL_FACE);
    }

    public void render(List<Light> lights, Camera camera) {
        prepare();
        entityShader.start();
        entityShader.loadSkyColor(RED, GREEN, BLUE); // Lo carga en cada frame para el ciclo dia/noche
        entityShader.loadLights(lights);
        entityShader.loadViewMatrix(camera);
        entityRenderer.render(entities);
        entityShader.stop();
        terrainShader.start();
        terrainShader.loadSkyColor(RED, GREEN, BLUE);
        terrainShader.loadLights(lights);
        terrainShader.loadViewMatrix(camera);
        terrainRenderer.render(terrains);
        terrainShader.stop();
        skyboxRenderer.render(camera);
        terrains.clear();
        entities.clear(); // Limpia las entidades, de lo contrario se acumularan y se terminaran renderizando millones de entidades
    }

    public void processTerrain(Terrain terrain) {
        terrains.add(terrain);
    }

    /**
     * Coloca las entidades en el HashMap de entidades.
     *
     * @param entity entidad.
     */
    public void processEntity(Entity entity) {
        TexturedModel entityModel = entity.getModel();
        List<Entity> batch = entities.get(entityModel);
        if (batch != null) batch.add(entity);
        else {
            List<Entity> newBatch = new ArrayList<>();
            newBatch.add(entity);
            entities.put(entityModel, newBatch);
        }
    }

    public void clean() {
        entityShader.clean();
        terrainShader.clean();
    }

    /**
     * Se llama una vez en cada fotograma y simplemente prepara a OpenGL para renderizar el juego.
     */
    public void prepare() {
        GL11.glEnable(GL11.GL_DEPTH_TEST); // Para que OpenGL pruebe que triangulo esta por encima del otro evitando que se superpongan
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        // Borra el color del ultimo fotograma
        GL11.glClearColor(RED, GREEN, BLUE, 1);
    }

    /**
     * Una matriz de proyeccion es una matriz utilizada en graficos 3D que representa la transformacion de las coordenadas
     * tridimensionales de un objeto en el espacio tridimensional a coordenadas bidimensionales en un plano de proyeccion. Esta
     * transformacion es fundamental para simular la perspectiva y la profundidad en una escena 3D cuando se proyecta en una
     * pantalla bidimensional, como en un monitor o una ventana de visualizacion.
     * <p>
     * Existen dos tipos principales de matrices de proyeccion en graficos 3D: proyeccion ortografica y proyeccion perspectiva.
     * <p>
     * <b>Proyeccion Ortografica:</b>
     * <ul>
     * <li>En una proyeccion ortografica, los rayos de luz son paralelos entre si al proyectar la escena en el plano de proyeccion.
     * <li>Esta proyeccion no simula el efecto de la perspectiva y mantiene el tamaño de los objetos constante independientemente
     * de su distancia a la camara.
     * <li>La matriz de proyeccion ortografica suele ser representada por una matriz ortogonal.
     * </ul>
     * <b>Proyeccion Perspectiva:</b>
     * <ul>
     * <li>En una proyeccion perspectiva, los rayos de luz convergen hacia un punto de fuga, lo que simula el efecto de la
     * perspectiva en la visualizacion.
     * <li>Los objetos mas cercanos a la camara aparecen mas grandes, mientras que los objetos mas lejanos se ven mas pequeños,
     * creando una sensacion de profundidad.
     * <li>La matriz de proyeccion perspectiva generalmente se representa mediante una matriz de perspectiva.
     * </ul>
     * La forma especifica de la matriz de proyeccion depende de la proyeccion utilizada y del sistema de coordenadas en uso. La
     * multiplicacion de la matriz de proyeccion con la matriz de vista y la matriz de modelo (si se utiliza) forma la conocida
     * matriz MVP (Model-View-Projection), que transforma las coordenadas de los objetos desde el espacio del modelo al espacio de
     * la pantalla.
     * <p>
     * En resumen, una matriz de proyeccion es esencial para representar de manera adecuada la perspectiva y la profundidad en
     * graficos 3D, facilitando la transformacion de las coordenadas 3D a coordenadas 2D para su representacion en una pantalla.
     */
    private void createProjectionMatrix() {
        // Obtiene la relacion de aspecto de la ventana de visualizacion
        float aspectRatio = (float) Display.getWidth() / Display.getHeight();
        // Calcula la escala en Y (yScale) basada en el campo de vision (FOV)
        float yScale = (float) (1f / Math.tan(Math.toRadians(FOV / 2f)) * aspectRatio);
        // Calcula la escala en X (xScale) en funcion de la escala en Y y la relacion de aspecto
        float xScale = yScale / aspectRatio;
        // Calcula la longitud del frustum en el eje Z (profundidad del volumen de visualizacion)
        float frustumLength = FAR_PLANE - NEAR_PLANE;
        // Crea una nueva matriz de 4x4 para almacenar la matriz de proyeccion
        projectionMatrix = new Matrix4f();
        // Configura los elementos especificos de la matriz de proyeccion
        projectionMatrix.m00 = xScale; // Escala en X
        projectionMatrix.m11 = yScale; // Escala en Y
        projectionMatrix.m22 = -((FAR_PLANE + NEAR_PLANE) / frustumLength); // Proyeccion en el eje Z
        projectionMatrix.m23 = -1; // Desplazamiento en el eje Z
        projectionMatrix.m32 = -((2 * NEAR_PLANE * FAR_PLANE) / frustumLength); // Desplazamiento en el eje Z
        projectionMatrix.m33 = 0; // Perspectiva
    }

}
