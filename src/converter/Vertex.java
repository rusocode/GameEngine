package converter;

import org.lwjgl.util.vector.Vector3f;

/**
 * Objeto vertice con un indice (identificador) y posicion (Vector3f).
 */

public class Vertex {

    private final int index; // Identificador del vertice
    private final Vector3f position;

    private final float length; // Longitud del vertice, pj: 3.227124

    private int textureIndex = -1, normalIndex = -1;
    private Vertex duplicateVertex;

    public Vertex(int index, Vector3f position) {
        this.index = index;
        this.position = position;
        this.length = position.length();
    }

    /**
     * Si los datos del vertice (coordenadas de textura y normales) ya se establecieron.
     *
     * @return verdadero si los los datos del vertice se establecieron o false.
     */
    public boolean isSet() {
        return textureIndex != -1 && normalIndex != -1;
    }

    /**
     * Si es la misma textura y normal.
     *
     * @return verdadero si es la misma textura y normal o false.
     */
    public boolean hasSameTextureAndNormal(int textureIndexOther, int normalIndexOther) {
        return textureIndexOther == textureIndex && normalIndexOther == normalIndex;
    }

    public int getIndex() {
        return index;
    }

    public float getLength() {
        return length;
    }

    public void setTextureIndex(int textureIndex) {
        this.textureIndex = textureIndex;
    }

    public void setNormalIndex(int normalIndex) {
        this.normalIndex = normalIndex;
    }

    public Vector3f getPosition() {
        return position;
    }

    public int getTextureIndex() {
        return textureIndex;
    }

    public int getNormalIndex() {
        return normalIndex;
    }

    public Vertex getDuplicateVertex() {
        return duplicateVertex;
    }

    public void setDuplicateVertex(Vertex duplicateVertex) {
        this.duplicateVertex = duplicateVertex;
    }

}