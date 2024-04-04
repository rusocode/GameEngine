package entities;

import models.TexturedModel;

import org.lwjgl.util.vector.Vector3f;

/**
 * Entidad.
 */

public class Entity {

    private TexturedModel model;
    private Vector3f position;
    private Vector3f angle;
    private final Vector3f scale;

    /* Cuando usamos un texture atlas, es necesario recalcular las coordenadas de texturas del objeto dependiendo del tamanio del
     * atlas y el indice de textura de la entidad. Para calcular las nuevas coordenadas de textura, primero se reducen las
     * coordenadas de texturas originales usando la variable numberOfRows. Luego se agrega un desplazamiento [x] e [y] a cada
     * coordenada de textura dependiendo de que textura se use en el atlas. Es importante aclarar que los texture atlas deben
     * ser potencia de 2 y la cantidad de filas tiene que ser igual a la cantidad de columnas. */
    private int textureIndex = 0; // Indica que texture atlas usa esta entidad

    public Entity(TexturedModel model, Vector3f position, Vector3f angle, Vector3f scale) {
        this.model = model;
        this.position = position;
        this.angle = angle;
        this.scale = scale;
    }

    public Entity(TexturedModel model, int textureIndex, Vector3f position, Vector3f angle, Vector3f scale) {
        this.model = model;
        this.textureIndex = textureIndex;
        this.position = position;
        this.angle = angle;
        this.scale = scale;
    }

    public float getTextureXOffset() {
        int column = textureIndex % model.getTexture().getNumberOfRows();
        return (float) column / model.getTexture().getNumberOfRows();
    }

    public float getTextureYOffset() {
        int row = textureIndex / model.getTexture().getNumberOfRows();
        return (float) row / model.getTexture().getNumberOfRows();
    }

    public void increasePosition(float dx, float dy, float dz) {
        position.x += dx;
        position.y += dy;
        position.z += dz;
    }

    public void increaseRotation(float dx, float dy, float dz) {
        angle.x += dx;
        angle.y += dy;
        angle.z += dz;
    }

    public TexturedModel getModel() {
        return model;
    }

    public void setModel(TexturedModel model) {
        this.model = model;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public Vector3f getAngle() {
        return angle;
    }

    public void setAngle(Vector3f angle) {
        this.angle = angle;
    }

    public Vector3f getScale() {
        return scale;
    }
}
