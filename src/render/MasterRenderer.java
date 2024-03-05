package render;

import java.util.*;

import entities.*;
import models.TexturedModel;
import shaders.StaticShader;

public class MasterRenderer {

    private final StaticShader shader = new StaticShader();
    private final Renderer renderer = new Renderer(shader);

    private final Map<TexturedModel, List<Entity>> entities = new HashMap<>();

    public void render(Light sun, Camera camera) {
        renderer.prepare();
        shader.start();
        shader.loadLight(sun);
        shader.loadViewMatrix(camera);
        renderer.render(entities);
        shader.stop();
        entities.clear(); // Limpia las entidades, de lo contrario se acumularan y se terminaran renderizando millones de entidades
    }

    /**
     * Coloca las entidades en la HashMap de entidades.
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
        shader.clean();
    }

}
