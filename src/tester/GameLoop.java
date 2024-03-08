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
        Light light = new Light(new Vector3f(20000, 20000, 2000), new Vector3f(1, 1, 1));
        MasterRenderer renderer = new MasterRenderer();

        ModelData treeData = OBJFileLoader.loadOBJ("tree");
        RawModel treeModel = loader.loadToVAO(treeData.getVertices(), treeData.getTextureCoords(), treeData.getNormals(), treeData.getIndices());
        // Ahora el modelo en crudo y la textura se "juntan" para crear el modelo texturizado
        TexturedModel tree = new TexturedModel(treeModel, new ModelTexture(loader.loadTexture("tree")));

        ModelData herbData = OBJFileLoader.loadOBJ("herb");
        RawModel herbModel = loader.loadToVAO(herbData.getVertices(), herbData.getTextureCoords(), herbData.getNormals(), herbData.getIndices());
        TexturedModel herb = new TexturedModel(herbModel, new ModelTexture(loader.loadTexture("herb")));
        herb.getTexture().setHasTransparency(true);
        herb.getTexture().setUseFakeLighting(true);

        ModelData fernData = OBJFileLoader.loadOBJ("fern");
        RawModel fernModel = loader.loadToVAO(fernData.getVertices(), fernData.getTextureCoords(), fernData.getNormals(), fernData.getIndices());
        TexturedModel fern = new TexturedModel(fernModel, new ModelTexture(loader.loadTexture("fern")));
        fern.getTexture().setHasTransparency(true);

        for (int i = 0; i < 500; i++) {
            entities.add(getEntity(tree, 3));
            entities.add(getEntity(herb, 1));
            entities.add(getEntity(fern, 0.6f));
        }

        // Crea dos cuadriculas de terreno con diferentes texturas
        Terrain terrain = new Terrain(0, 0, loader, new ModelTexture(loader.loadTexture("grass")));
        Terrain terrain2 = new Terrain(0, 1, loader, new ModelTexture(loader.loadTexture("grass2")));

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
    private static Entity getEntity(TexturedModel texturedModel, float scaleValue) {
        // Aplica iluminacion especular a la textura
        ModelTexture texture = texturedModel.getTexture();
        // texture.setShineDamper(10);
        // texture.setReflectivity(1);

        // Operaciones de transformacion
        Random random = new Random();
        // Vector de traslacion con coordenadas x e z aleatorias
        Vector3f translation = new Vector3f(random.nextFloat() * 800, 0, random.nextFloat() * 800);
        // Angulos de rotacion
        float angleX = 0;
        float angleY = 0;
        float angleZ = 0;
        // Vector de escala
        Vector3f scale = new Vector3f(scaleValue, scaleValue, scaleValue);

        return new Entity(texturedModel, translation, angleX, angleY, angleZ, scale);
    }

}
