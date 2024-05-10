package main;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import converter.OldOBJLoader;
import entities.*;
import guis.*;
import models.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector4f;
import render.*;
import terrains.Terrain;
import textures.*;
import water.*;
import utils.MousePicker;

import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

/**
 * <h3>World Space</h3>
 * Todos los vertices de un cubo o cualquier modelo 3D se especifican en coordenadas locales (local space) o en relacion con el
 * origen del modelo [0,0,0]. Para colocar estos modelos en diferentes lugares del mundo 3D, se transforma cada vertice unsando
 * una <b>matriz de transformacion</b> (model matrix transform) para que todos los vertices esten especificados en coordenadas
 * mundiales, de modo que las posiciones de todos los vertices ahora esten en relacion con el origen del mundo. Luego se
 * transforman todos los vertices usando una <b>matriz de vista</b> (view matrix transform) para que todos los vertices esten en
 * relacion con la posicion de la camara, llamando a esto <b>eye space</b>. Finalmente se usa la <b>matriz de proyeccion</b>
 * (projection matrix transform) para escalar objetos distantes para hacerlos parecer mas pequenios y, en general, para hacer que
 * nuestra vista tenga una forma de frustum (view frustum’s shape). OpenGL luego lleva a cabo la <b>division de perspectiva</b> en
 * todos los vertices para convertirlos en un <b>espacio de dispositivo normalizado</b> (normalised device space), lo que coloca
 * todos los vertices en terminos del sistema de coordenadas de OpenGL. Finalmente, OpenGL convierte estas posiciones a
 * coordenadas de pixeles 2D en la pantalla para que puedan ser renderizadas y este espacio se llama <b>viewport space</b>.
 * <p>
 * <a href="https://betterexplained.com/articles/vector-calculus-understanding-the-dot-product/">Dot product</a>
 * <a href="http://www.lighthouse3d.com/tutorials/view-frustum-culling/view-frustums-shape/">Frustum Culling</a>
 */

public class Game {

    private static TexturedModel playerModel, treeModel, herbModel, flowerModel, lampModel, fernModel;
    private static TerrainTexturePack texturePack;
    private static TerrainTexture blendMap;

    private static final String obj_dir = "obj/";
    private static final String heightmap_dir = "terrain/heightmap/";
    private static final String terrain_dir = "terrain/";

    public static void main(String[] args) {

        DisplayManager.create();

        Loader loader = new Loader();
        MasterRenderer renderer = new MasterRenderer(loader);

        createTextures(loader);
        createModels(loader);

        List<Terrain> terrains = new ArrayList<>();
        List<Entity> entities = new ArrayList<>();
        List<Light> lights = new ArrayList<>();

        Terrain terrain = new Terrain(0, -1, loader, texturePack, blendMap, heightmap_dir + "heightmap_water");
        terrains.add(terrain);

        placeModels(entities, terrain);

        Light sun = new Light(new Vector3f(20000, 40000, 20000), new Vector3f(1, 1, 1));
        lights.add(sun);

        Player player = new Player(playerModel, new Vector3f(0, 0, 0), new Vector3f(0, 180, 0), new Vector3f(0.7f, 0.7f, 0.7f));
        entities.add(player);
        Camera camera = new Camera(player);

        List<GuiTexture> guis = new ArrayList<>();
        // guis.add(new GuiTexture(loader.loadTexture("gui/ao"), new Vector2f(0.5f, 0.5f), new Vector2f(0.25f, 0.25f)));
        // guis.add(new GuiTexture(loader.loadTexture("gui/logo"), new Vector2f(0.4f, 0.6f), new Vector2f(0.25f, 0.25f)));

        GuiRenderer guiRenderer = new GuiRenderer(loader);

        /* MousePicker picker = new MousePicker(camera, renderer.getProjectionMatrix(), terrain);
        Entity lamp = new Entity(lampModel, new Vector3f(293, -6.8f, -305), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1));
        entities.add(lamp);
        Light light = new Light(new Vector3f(293, 7, -305), new Vector3f(0, 2, 2), new Vector3f(1, 0.01f, 0.002f));
        lights.add(light); */

        WaterFrameBuffers buffers = new WaterFrameBuffers();
        WaterShader waterShader = new WaterShader();
        WaterRenderer waterRenderer = new WaterRenderer(loader, waterShader, renderer.getProjectionMatrix(), buffers);
        List<WaterTile> waters = new ArrayList<>();
        WaterTile water = new WaterTile(75, -75, 0);
        waters.add(water);

        while (!Display.isCloseRequested()) {
            player.move(terrain);
            camera.move();

            // updatePicker(picker, lamp, light);

            /* Una vez habilitado un plano de recorte, se debe especificar su ecuacion en el Vertex Shader para que se aplique
             * correctamente durante el renderizado. Esto permite crear efectos complejos, como renderizar escenas desde el
             * interior de un objeto o mostrar unicamente las partes visibles a traves de un agujero. */
            GL11.glEnable(GL30.GL_CLIP_DISTANCE0);

            // Renderiza las texturas de reflexion
            buffers.bindReflectionFrameBuffer();
            // Mueva la camara por debajo del agua para simular el efecto de reflejo en los objetos
            float distance = 2 * (camera.getPosition().y - water.getHeight());
            camera.getPosition().y -= distance;
            camera.invertPitch();
            /* Recorta todo lo que esta por encima de la altura del agua. Esto nos deja mucho margen de error y es lo que causa
             * los fallos, especialmente cuando el agua estaba distorcionada. Ahora que hemos amortiguado la distorsion, el error
             * casi ha desaparecido, pero todavia se ve algun pixel ocasional, por lo que en lugar de hacer que el plano de recorte
             * se corte exactamente en la superficie del agua, podemos agregar un pequenio desplazamiento para crea una pequenia
             * superposicion. */
            renderer.renderScene(entities, terrains, lights, camera, new Vector4f(0, 1, 0, -water.getHeight() + 1f));
            // Vuelve la camara a su posicion original
            camera.getPosition().y += distance;
            camera.invertPitch();

            // Renderiza las texturas de refraccion
            buffers.bindRefractionFrameBuffer();
            // Recorta todo lo que esta por debajo de la altura del agua
            renderer.renderScene(entities, terrains, lights, camera, new Vector4f(0, -1, 0, water.getHeight()));

            // Renderiza en pantalla
            GL11.glDisable(GL30.GL_CLIP_DISTANCE0); // Para el renderizado final, solo queremos renderizar toda la escena en pantalla sin recortar nada
            buffers.unbindCurrentFrameBuffer();
            renderer.renderScene(entities, terrains, lights, camera, new Vector4f(0, 0, 0, 0)); // Deshabilita el plano de recorte
            waterRenderer.render(waters, camera, sun);
            guiRenderer.render(guis);

            DisplayManager.update();
        }

        buffers.clean();
        waterShader.clean();
        guiRenderer.clean();
        renderer.clean();
        loader.clean();
        DisplayManager.close();
    }

    private static void createTextures(Loader loader) {
        TerrainTexture background = new TerrainTexture(loader.loadTexture(terrain_dir + "grass"));
        TerrainTexture r = new TerrainTexture(loader.loadTexture(terrain_dir + "dirt"));
        TerrainTexture g = new TerrainTexture(loader.loadTexture(terrain_dir + "grass_flowers"));
        TerrainTexture b = new TerrainTexture(loader.loadTexture(terrain_dir + "path"));
        texturePack = new TerrainTexturePack(background, r, g, b);
        blendMap = new TerrainTexture(loader.loadTexture(terrain_dir + "blend_map"));
    }

    private static void createModels(Loader loader) {
        playerModel = getTexturedModel(loader, obj_dir + "player", obj_dir + "player");
        treeModel = getTexturedModel(loader, obj_dir + "pine", obj_dir + "pine");
        herbModel = getTexturedModel(loader, obj_dir + "herb", obj_dir + "herb");
        flowerModel = getTexturedModel(loader, obj_dir + "herb", obj_dir + "flower");
        lampModel = getTexturedModel(loader, obj_dir + "lamp", obj_dir + "lamp");

        ModelTexture fernTextureAtlas = new ModelTexture(loader.loadTexture(obj_dir + "fern"));
        fernTextureAtlas.setNumberOfRows(2); // Especifica la cantidad de filas para el texture atlas
        fernModel = new TexturedModel(OldOBJLoader.loadOBJ(obj_dir + "fern", loader), fernTextureAtlas);

        fernModel.getTexture().setHasTransparency(true);
        herbModel.getTexture().setHasTransparency(true);
        herbModel.getTexture().setUseFakeLighting(true);
        flowerModel.getTexture().setHasTransparency(true);
        flowerModel.getTexture().setUseFakeLighting(true);
        lampModel.getTexture().setUseFakeLighting(true);

    }

    private static void placeModels(List<Entity> entities, Terrain terrain) {
        Random random = new Random(120);
        for (int i = 0; i < 12; i++) {
            if (i % 3 == 0) {
                float x = random.nextFloat() * Terrain.SIZE, z = random.nextFloat() * -Terrain.SIZE;
                float y = terrain.getHeightOfTerrain(x, z);
                entities.add(new Entity(fernModel, random.nextInt(4), new Vector3f(x, y, z), new Vector3f(0, random.nextFloat() * 360, 0), new Vector3f(0.9f, 0.9f, 0.9f)));
            }
            if (i % 2 == 0) {
                float x = random.nextFloat() * Terrain.SIZE, z = random.nextFloat() * -Terrain.SIZE;
                float y = terrain.getHeightOfTerrain(x, z);
                float sacale = random.nextFloat() * 0.6f + 0.8f;
                entities.add(getEntity(treeModel, new Vector3f(x, y, z), new Vector3f(0, 0, 0), new Vector3f(sacale, sacale, sacale)));
            }
        }
    }

    private static TexturedModel getTexturedModel(Loader loader, String obj, String texture) {
        // Ahora el modelo en crudo y la textura se "juntan" para crear el modelo texturizado
        return new TexturedModel(OldOBJLoader.loadOBJ(obj, loader), new ModelTexture(loader.loadTexture(texture)));
    }

    /**
     * Obtiene una nueva entidad texturizada con iluminacion especular y transformacion.
     *
     * @param texturedModel modelo texturizado.
     * @return la entidad texturizada con iluminacion especular y transformacion.
     */
    private static Entity getEntity(TexturedModel texturedModel, Vector3f position, Vector3f angle, Vector3f scale) {
        // Aplica iluminacion especular a la textura
        ModelTexture texture = texturedModel.getTexture();
        // texture.setShineDamper(10);
        // texture.setReflectivity(1);
        return new Entity(texturedModel, position, angle, scale);
    }

    private static void updatePicker(MousePicker picker, Entity lamp, Light light) {
        picker.update();
        Vector3f terrainPoint = picker.getCurrentTerrainPoint();
        if (terrainPoint != null) {
            lamp.setPosition(terrainPoint);
            light.setPosition(new Vector3f(terrainPoint.x, terrainPoint.y + 15, terrainPoint.z));
        }
    }

}
