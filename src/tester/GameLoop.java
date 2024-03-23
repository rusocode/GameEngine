package tester;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import converter.OldOBJLoader;
import entities.*;
import guis.GuiRenderer;
import guis.GuiTexture;
import models.*;
import org.lwjgl.util.vector.Vector2f;
import render.*;
import terrains.Terrain;
import textures.*;

import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;

/**
 * Bucle principal del juego.
 * <p>
 * <a href="https://betterexplained.com/articles/vector-calculus-understanding-the-dot-product/">Dot product</a>
 * <a href="http://www.lighthouse3d.com/tutorials/view-frustum-culling/view-frustums-shape/">Frustum Culling</a>
 */

public class GameLoop {

    private static final List<Entity> entities = new ArrayList<>();

    public static void main(String[] args) {

        DisplayManager.create();

        Loader loader = new Loader();
        MasterRenderer renderer = new MasterRenderer();

        // *** TERRAIN TEXTURE
        TerrainTexture backgroundTexture = new TerrainTexture(loader.loadTexture("grass4"));
        TerrainTexture backgroundTexture2 = new TerrainTexture(loader.loadTexture("grass2"));
        TerrainTexture rTexture = new TerrainTexture(loader.loadTexture("dirt"));
        TerrainTexture gTexture = new TerrainTexture(loader.loadTexture("grassFlowers"));
        TerrainTexture bTexture = new TerrainTexture(loader.loadTexture("path"));
        TerrainTexture blendMap = new TerrainTexture(loader.loadTexture("blendMap"));
        TerrainTexturePack texturePack = new TerrainTexturePack(backgroundTexture, rTexture, gTexture, bTexture);
        TerrainTexturePack texturePack2 = new TerrainTexturePack(backgroundTexture2, rTexture, gTexture, bTexture);
        // *** TERRAIN TEXTURE

        TexturedModel tree = getTexturedModel(loader, "pine", "pine");
        TexturedModel herb = getTexturedModel(loader, "herb", "herb");
        TexturedModel flower = getTexturedModel(loader, "herb", "flower");
        TexturedModel lamp = getTexturedModel(loader, "lamp", "lamp");

        ModelTexture fernTextureAtlas = new ModelTexture(loader.loadTexture("fern"));
        fernTextureAtlas.setNumberOfRows(2); // Especifica la cantidad de filas para el texture atlas

        TexturedModel fern = new TexturedModel(OldOBJLoader.loadOBJ("fern", loader), fernTextureAtlas);

        fern.getTexture().setHasTransparency(true);
        herb.getTexture().setHasTransparency(true);
        herb.getTexture().setUseFakeLighting(true);
        flower.getTexture().setHasTransparency(true);
        flower.getTexture().setUseFakeLighting(true);
        lamp.getTexture().setUseFakeLighting(true);

        // Crea dos cuadriculas de terreno con diferentes texturas
        Terrain terrain = new Terrain(0, -1, loader, texturePack, blendMap, "heightmap");

        Random random = new Random(676452);

        for (int i = 0; i < 400; i++) {
            if (i % 2 == 0) {
                float x = random.nextFloat() * 800;
                float z = random.nextFloat() * -800;
                float y = terrain.getHeightOfTerrain(x, z);
                entities.add(new Entity(fern, random.nextInt(4), new Vector3f(x, y, z), new Vector3f(0, random.nextFloat() * 360, 0), new Vector3f(0.9f, 0.9f, 0.9f)));
            }
            if (i % 5 == 0) {
                float x = random.nextFloat() * 800;
                float z = random.nextFloat() * -800;
                float y = terrain.getHeightOfTerrain(x, z);
                float scaleTree = random.nextFloat() + 4;
                entities.add(getEntity(tree, new Vector3f(x, y, z), new Vector3f(0, 0, 0), new Vector3f(scaleTree, scaleTree, scaleTree)));
            }
        }

        List<Light> lights = new ArrayList<>();
        lights.add(new Light(new Vector3f(0, 1000, -7000), new Vector3f(0.4f, 0.4f, 0.4f))); // Luz del sol sin atenuacion
        // Luces para cada lampara
        lights.add(new Light(new Vector3f(185, 10, -293), new Vector3f(2, 0, 0), new Vector3f(1, 0.01f, 0.002f)));
        lights.add(new Light(new Vector3f(370, 17, -300), new Vector3f(0, 2, 2), new Vector3f(1, 0.01f, 0.002f)));
        lights.add(new Light(new Vector3f(293, 7, -305), new Vector3f(2, 2, 0), new Vector3f(1, 0.01f, 0.002f)));
        // Modelos de lampara con la misma ubicacion que las luces con la diferencia de que el eje y
        entities.add(getEntity(lamp, new Vector3f(185, -4.7f, -293), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1)));
        entities.add(getEntity(lamp, new Vector3f(370, 4.2f, -300), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1)));
        entities.add(getEntity(lamp, new Vector3f(293, -6.8f, -305), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1)));

        // Especifica el angulo de y a 180 grados para que el player mire al terreno y no a la nada
        Player player = new Player(getTexturedModel(loader, "player", "player"), new Vector3f(100, 0, -100), new Vector3f(0, 180, 0), new Vector3f(0.6f, 0.6f, 0.6f));
        Camera camera = new Camera(player);

        List<GuiTexture> guis = new ArrayList<>();
        /* GuiTexture gui = new GuiTexture(loader.loadTexture("ao"), new Vector2f(0.5f, 0.5f), new Vector2f(0.25f, 0.25f));
        GuiTexture gui2 = new GuiTexture(loader.loadTexture("logo"), new Vector2f(0.4f, 0.6f), new Vector2f(0.25f, 0.25f));
        guis.add(gui);
        guis.add(gui2); */

        GuiRenderer guiRenderer = new GuiRenderer(loader);

        while (!Display.isCloseRequested()) {
            player.move(terrain);
            camera.move();
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
