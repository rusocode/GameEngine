package tester;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import converter.ModelData;
import converter.OBJFileLoader;
import entities.*;
import models.*;
import render.*;
import terrains.Terrain;
import textures.ModelTexture;

import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;
import textures.TerrainTexture;
import textures.TerrainTexturePack;

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
        Camera camera = new Camera();
        Light light = new Light(new Vector3f(20000, 40000, 20000), new Vector3f(1, 1, 1));
        MasterRenderer renderer = new MasterRenderer();

        // *** TERRAIN TEXTURE
        TerrainTexture backgroundTexture = new TerrainTexture(loader.loadTexture("grass"));
        TerrainTexture backgroundTexture2 = new TerrainTexture(loader.loadTexture("grass2"));
        TerrainTexture rTexture = new TerrainTexture(loader.loadTexture("dirt"));
        TerrainTexture gTexture = new TerrainTexture(loader.loadTexture("grassFlowers"));
        TerrainTexture bTexture = new TerrainTexture(loader.loadTexture("path"));

        TerrainTexturePack texturePack = new TerrainTexturePack(backgroundTexture, rTexture, gTexture, bTexture);
        TerrainTexturePack texturePack2 = new TerrainTexturePack(backgroundTexture2, rTexture, gTexture, bTexture);
        TerrainTexture blendMap = new TerrainTexture(loader.loadTexture("blendMap"));
        // *** TERRAIN TEXTURE

        ModelData treeData = OBJFileLoader.loadOBJ("tree");
        RawModel treeModel = loader.loadToVAO(treeData.getVertices(), treeData.getTextureCoords(), treeData.getNormals(), treeData.getIndices());
        // Ahora el modelo en crudo y la textura se "juntan" para crear el modelo texturizado
        TexturedModel tree = new TexturedModel(treeModel, new ModelTexture(loader.loadTexture("tree")));

        ModelData herbData = OBJFileLoader.loadOBJ("herb");
        RawModel herbModel = loader.loadToVAO(herbData.getVertices(), herbData.getTextureCoords(), herbData.getNormals(), herbData.getIndices());
        TexturedModel herb = new TexturedModel(herbModel, new ModelTexture(loader.loadTexture("herb")));
        herb.getTexture().setHasTransparency(true);
        herb.getTexture().setUseFakeLighting(true);

        ModelData flowerData = OBJFileLoader.loadOBJ("herb");
        RawModel flowerModel = loader.loadToVAO(flowerData.getVertices(), flowerData.getTextureCoords(), flowerData.getNormals(), flowerData.getIndices());
        TexturedModel flower = new TexturedModel(flowerModel, new ModelTexture(loader.loadTexture("flower")));
        flower.getTexture().setHasTransparency(true);
        flower.getTexture().setUseFakeLighting(true);

        ModelData fernData = OBJFileLoader.loadOBJ("fern");
        RawModel fernModel = loader.loadToVAO(fernData.getVertices(), fernData.getTextureCoords(), fernData.getNormals(), fernData.getIndices());
        TexturedModel fern = new TexturedModel(fernModel, new ModelTexture(loader.loadTexture("fern")));
        fern.getTexture().setHasTransparency(true);

        Random random = new Random(676452);

        for (int i = 0; i < 400; i++) {
            if (i % 7 == 0) {
                entities.add(getEntity(herb, random.nextFloat() * 800, 0, random.nextFloat() * 800, 0, 0, 0, 1.8f));
                entities.add(getEntity(flower, random.nextFloat() * 800, 0, random.nextFloat() * 800, 0, 0, 0, 2.3f));
            }
            if (i % 3 == 0) {
                entities.add(getEntity(tree, random.nextFloat() * 800, 0, random.nextFloat() * 800, 0, 0, 0, random.nextFloat() * 1 + 4));
                entities.add(getEntity(fern, random.nextFloat() * 800, 0, random.nextFloat() * 800, 0, random.nextFloat() * 360, 0, 0.9f));
            }
        }

        // Crea dos cuadriculas de terreno con diferentes texturas
        Terrain terrain = new Terrain(0, 0, loader, texturePack, blendMap); // 0, -1
        Terrain terrain2 = new Terrain(0, 1, loader, texturePack2, blendMap); // -1, -1

        while (!Display.isCloseRequested()) {
            camera.move();

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

    /**
     * Obtiene una nueva entidad texturizada con iluminacion especular y transformacion.
     *
     * @param texturedModel modelo texturizado.
     * @return la entidad texturizada con iluminacion especular y transformacion.
     */
    private static Entity getEntity(TexturedModel texturedModel, float posX, float posY, float posZ, float angleX, float angleY, float angleZ, float scaleValue) {
        // Aplica iluminacion especular a la textura
        ModelTexture texture = texturedModel.getTexture();
        // texture.setShineDamper(10);
        // texture.setReflectivity(1);

        // Operaciones de transformacion
        Vector3f translation = new Vector3f(posX, posY, posZ); // Vector de traslacion
        Vector3f scale = new Vector3f(scaleValue, scaleValue, scaleValue); // Vector de escala

        return new Entity(texturedModel, translation, angleX, angleY, angleZ, scale);
    }

}
