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

        // Ahora el modelo en crudo y la textura se "juntan" para crear el modelo texturizado
        TexturedModel tree = getTexturedModel(loader, "tree", "tree");
        TexturedModel fern = getTexturedModel(loader, "fern", "fern");
        TexturedModel herb = getTexturedModel(loader, "herb", "herb");
        TexturedModel flower = getTexturedModel(loader, "herb", "flower");
        TexturedModel box = getTexturedModel(loader, "box", "box");

        fern.getTexture().setHasTransparency(true);
        herb.getTexture().setHasTransparency(true);
        herb.getTexture().setUseFakeLighting(true);
        flower.getTexture().setHasTransparency(true);
        flower.getTexture().setUseFakeLighting(true);

        Random random = new Random(676452);

        entities.add(getEntity(box, new Vector3f(100, 0, -60), new Vector3f(0, 0, 0), new Vector3f(5, 8, 5)));
        entities.add(getEntity(box, new Vector3f(100, 0, -240), new Vector3f(0, 0, 0), new Vector3f(5, 8, 5)));
        entities.add(getEntity(box, new Vector3f(120, 0, -360), new Vector3f(0, 0, 0), new Vector3f(5, 8, 5)));

        for (int i = 0; i < 400; i++) {
            if (i % 7 == 0) {
                entities.add(getEntity(herb, new Vector3f(random.nextFloat() * 400 - 200, 0, random.nextFloat() * -400), new Vector3f(0, 0, 0), new Vector3f(1.8f, 1.8f, 1.8f)));
                entities.add(getEntity(flower, new Vector3f(random.nextFloat() * 400 - 200, 0, random.nextFloat() * -400), new Vector3f(0, 0, 0), new Vector3f(2.3f, 2.3f, 2.3f)));
            }
            if (i % 3 == 0) {
                float scaleTree = random.nextFloat() + 4;
                entities.add(getEntity(tree, new Vector3f(random.nextFloat() * 800 - 400, 0, random.nextFloat() * -600), new Vector3f(0, 0, 0), new Vector3f(scaleTree, scaleTree, scaleTree)));
                entities.add(getEntity(fern, new Vector3f(random.nextFloat() * 400 - 200, 0, random.nextFloat() * -400), new Vector3f(0, random.nextFloat() * 360, 0), new Vector3f(0.9f, 0.9f, 0.9f)));
            }
        }

        // Crea dos cuadriculas de terreno con diferentes texturas
        Terrain terrain = new Terrain(0, -1, loader, texturePack, blendMap); // 0, 0
        Terrain terrain2 = new Terrain(-1, -1, loader, texturePack2, blendMap); // 0, 1

        // Especifica el angulo de y a 180 grados para que el player mire el terreno y no a la nada
        Player player = new Player(getTexturedModel(loader, "player", "player"), new Vector3f(100, 0, -100), new Vector3f(0, 180, 0), new Vector3f(1, 1, 1));
        Camera camera = new Camera(player);

        while (!Display.isCloseRequested()) {
            camera.move();

            player.move();
            renderer.processEntity(player);

            renderer.processTerrain(terrain);
            renderer.processTerrain(terrain2);

            for (Entity entity : entities) renderer.processEntity(entity);

            renderer.render(light, camera);

            DisplayManager.update();
        }

        renderer.clean();
        loader.clean();
        DisplayManager.close();
    }


    private static TexturedModel getTexturedModel(Loader loader, String obj, String texture) {
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
