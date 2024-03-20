package tester;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import converter.OldOBJLoader;
import entities.*;
import models.*;
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
        Light light = new Light(new Vector3f(20000, 40000, 20000), new Vector3f(1, 1, 1));
        MasterRenderer renderer = new MasterRenderer();

        // *** TERRAIN TEXTURE
        TerrainTexture backgroundTexture = new TerrainTexture(loader.loadTexture("grass"));
        TerrainTexture backgroundTexture2 = new TerrainTexture(loader.loadTexture("grass2"));
        TerrainTexture rTexture = new TerrainTexture(loader.loadTexture("dirt"));
        TerrainTexture gTexture = new TerrainTexture(loader.loadTexture("grassFlowers"));
        TerrainTexture bTexture = new TerrainTexture(loader.loadTexture("moss_path"));
        TerrainTexture blendMap = new TerrainTexture(loader.loadTexture("blendMap"));
        TerrainTexturePack texturePack = new TerrainTexturePack(backgroundTexture, rTexture, gTexture, bTexture);
        TerrainTexturePack texturePack2 = new TerrainTexturePack(backgroundTexture2, rTexture, gTexture, bTexture);
        // *** TERRAIN TEXTURE

        TexturedModel tree = getTexturedModel(loader, "tree", "tree");
        TexturedModel herb = getTexturedModel(loader, "herb", "herb");
        TexturedModel flower = getTexturedModel(loader, "herb", "flower");

        ModelTexture fernTextureAtlas = new ModelTexture(loader.loadTexture("fern"));
        fernTextureAtlas.setNumberOfRows(2); // Especifica la cantidad de filas para el texture atlas

        TexturedModel fern = new TexturedModel(OldOBJLoader.loadOBJ("fern", loader), fernTextureAtlas);

        fern.getTexture().setHasTransparency(true);
        herb.getTexture().setHasTransparency(true);
        herb.getTexture().setUseFakeLighting(true);
        flower.getTexture().setHasTransparency(true);
        flower.getTexture().setUseFakeLighting(true);

        // Crea dos cuadriculas de terreno con diferentes texturas
        Terrain terrain = new Terrain(0, -1, loader, texturePack, blendMap, "heightmap");

        Random random = new Random(676452);

        for (int i = 0; i < 400; i++) {
            if (i % 2 == 0) {
                float x = random.nextFloat() * 800 - 400;
                float z = random.nextFloat() * -600;
                float y = terrain.getHeightOfTerrain(x, z);
                entities.add(new Entity(fern, random.nextInt(4), new Vector3f(x, y, z), new Vector3f(0, random.nextFloat() * 360, 0), new Vector3f(0.9f, 0.9f, 0.9f)));
            }
            if (i % 5 == 0) {
                float x = random.nextFloat() * 800 - 400;
                float z = random.nextFloat() * -600;
                float y = terrain.getHeightOfTerrain(x, z);
                float scaleTree = random.nextFloat() + 4;
                entities.add(getEntity(tree, new Vector3f(x, y, z), new Vector3f(0, 0, 0), new Vector3f(scaleTree, scaleTree, scaleTree)));
            }
        }

        // Especifica el angulo de y a 180 grados para que el player mire al terreno y no a la nada
        Player player = new Player(getTexturedModel(loader, "player", "player"), new Vector3f(100, 0, -100), new Vector3f(0, 180, 0), new Vector3f(1, 1, 1));
        Camera camera = new Camera(player);

        while (!Display.isCloseRequested()) {
            player.move(terrain);
            camera.move();
            renderer.processEntity(player);
            renderer.processTerrain(terrain);
            for (Entity entity : entities) renderer.processEntity(entity);
            renderer.render(light, camera);
            DisplayManager.update();
        }

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
