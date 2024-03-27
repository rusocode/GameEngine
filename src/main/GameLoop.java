package main;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import converter.OldOBJLoader;
import entities.*;
import guis.GuiRenderer;
import guis.GuiTexture;
import models.*;
import org.lwjgl.util.vector.Vector4f;
import render.*;
import terrains.Terrain;
import textures.*;

import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;
import utils.MousePicker;

/**
 * Bucle principal del juego.
 * <br><br>
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

public class GameLoop {

    private static final List<Entity> entities = new ArrayList<>();

    public static void main(String[] args) {

        DisplayManager.create();

        Loader loader = new Loader();
        MasterRenderer renderer = new MasterRenderer(loader);

        // TERRAIN TEXTURE
        TerrainTexture backgroundTexture = new TerrainTexture(loader.loadTexture("grass4"));
        TerrainTexture rTexture = new TerrainTexture(loader.loadTexture("dirt"));
        TerrainTexture gTexture = new TerrainTexture(loader.loadTexture("grassFlowers"));
        TerrainTexture bTexture = new TerrainTexture(loader.loadTexture("path"));
        TerrainTexturePack texturePack = new TerrainTexturePack(backgroundTexture, rTexture, gTexture, bTexture);
        TerrainTexture blendMap = new TerrainTexture(loader.loadTexture("blendMap"));
        // TERRAIN TEXTURE

        TexturedModel playerModel = getTexturedModel(loader, "player", "player");
        TexturedModel treeModel = getTexturedModel(loader, "pine", "pine");
        TexturedModel bobbleTreeModel = getTexturedModel(loader, "bobbleTree", "bobbleTree");
        TexturedModel toonRockModel = getTexturedModel(loader, "toonRocks", "toonRocks");
        TexturedModel herbModel = getTexturedModel(loader, "herb", "herb");
        TexturedModel flowerModel = getTexturedModel(loader, "herb", "flower");
        TexturedModel lampModel = getTexturedModel(loader, "lamp", "lamp");

        ModelTexture fernTextureAtlas = new ModelTexture(loader.loadTexture("fern"));
        fernTextureAtlas.setNumberOfRows(2); // Especifica la cantidad de filas para el texture atlas

        TexturedModel fern = new TexturedModel(OldOBJLoader.loadOBJ("fern", loader), fernTextureAtlas);

        fern.getTexture().setHasTransparency(true);
        herbModel.getTexture().setHasTransparency(true);
        herbModel.getTexture().setUseFakeLighting(true);
        flowerModel.getTexture().setHasTransparency(true);
        flowerModel.getTexture().setUseFakeLighting(true);
        lampModel.getTexture().setUseFakeLighting(true);

        // Crea una cuadricula de terreno
        Terrain terrain = new Terrain(0, -1, loader, texturePack, blendMap, "heightmap");

        Random random = new Random(676452);

        for (int i = 0; i < 400; i++) {
            if (i % 3 == 0) {
                float x = random.nextFloat() * 800;
                float z = random.nextFloat() * -800;
                float y = terrain.getHeightOfTerrain(x, z);
                entities.add(new Entity(toonRockModel, random.nextInt(4), new Vector3f(x, y, z), new Vector3f(0, random.nextFloat() * 360, 0), new Vector3f(1.9f, 1.9f, 1.9f)));
            }
            if (i % 2 == 0) {
                float x = random.nextFloat() * 800;
                float z = random.nextFloat() * -800;
                float y = terrain.getHeightOfTerrain(x, z);
                float sacale = random.nextFloat() * 0.2f + 0.6f; // float scaleTree = random.nextFloat() + 4;
                entities.add(getEntity(bobbleTreeModel, new Vector3f(x, y, z), new Vector3f(0, 0, 0), new Vector3f(sacale, sacale, sacale)));
            }
        }

        List<Light> lights = new ArrayList<>();
        // new Vector3f(20000, 40000, 20000), new Vector3f(1, 1, 1) // Dia
        // new Vector3f(0, 1000, -7000), new Vector3f(0.4f, 0.4f, 0.4f) // Noche
        lights.add(new Light(new Vector3f(20000, 40000, 20000), new Vector3f(1, 1, 1))); // Fuente de luz principal
        // Luces para cada lampara
        lights.add(new Light(new Vector3f(185, 10, -293), new Vector3f(2, 0, 0), new Vector3f(1, 0.01f, 0.002f)));
        lights.add(new Light(new Vector3f(370, 17, -300), new Vector3f(0, 2, 2), new Vector3f(1, 0.01f, 0.002f)));
        // Lamparas con la misma ubicacion que las luces pero diferente posicion en [y]
        entities.add(getEntity(lampModel, new Vector3f(185, -4.7f, -293), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1)));
        entities.add(getEntity(lampModel, new Vector3f(370, 4.2f, -300), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1)));

        Player player = new Player(playerModel, new Vector3f(100, 0, -100), new Vector3f(0, 180, 0), new Vector3f(0.7f, 0.7f, 0.7f));
        Camera camera = new Camera(player);

        List<GuiTexture> guis = new ArrayList<>();
        /* GuiTexture gui = new GuiTexture(loader.loadTexture("ao"), new Vector2f(0.5f, 0.5f), new Vector2f(0.25f, 0.25f));
        GuiTexture gui2 = new GuiTexture(loader.loadTexture("logo"), new Vector2f(0.4f, 0.6f), new Vector2f(0.25f, 0.25f));
        guis.add(gui);
        guis.add(gui2); */

        GuiRenderer guiRenderer = new GuiRenderer(loader);

        MousePicker picker = new MousePicker(camera, renderer.getProjectionMatrix(), terrain);

        Entity lamp = new Entity(lampModel, new Vector3f(293, -6.8f, -305), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1));
        entities.add(lamp);
        Light light = new Light(new Vector3f(293, 7, -305), new Vector3f(0, 2, 2), new Vector3f(1, 0.01f, 0.002f));
        lights.add(light);

        while (!Display.isCloseRequested()) {
            player.move(terrain);
            camera.move();

            /* picker.update();
            Vector3f terrainPoint = picker.getCurrentTerrainPoint();
            if (terrainPoint != null) {
                lamp.setPosition(terrainPoint);
                light.setPosition(new Vector3f(terrainPoint.x, terrainPoint.y + 15, terrainPoint.z));
            } */

            renderer.processEntity(player);
            renderer.processTerrain(terrain);
            for (Entity entity : entities) renderer.processEntity(entity);
            renderer.render(lights, camera);
            // guiRenderer.render(guis);
            DisplayManager.update();
        }

        guiRenderer.clean();
        renderer.clean();
        loader.clean();
        DisplayManager.close();
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

}
