package terrains;

import models.RawModel;
import render.Loader;
import textures.TerrainTexture;
import textures.TerrainTexturePack;

/**
 * Un terreno es basicamente una entidad.
 * <p>
 * Es importante que las texturas sean potencia de 2 para evitar bordes negros.
 * <p>
 * xGrid y zGrid son coordenadas que determinan en que cuadricula del mundo aparece el terreno. El mundo esta formado por una
 * cuadricula donde cada cuadrado tiene bordes del tamaño del terreno (SIZE).
 */

public class Terrain {

    private static final float SIZE = 800; // Tamaño del terreno
    private static final int VERTEX_COUNT = 128; // Cantidad de vertices del terreno

    // El eje y no es necesario ya que el terreno permanece en la misma altura
    private final float x, z;
    private final RawModel model;
    private final TerrainTexturePack texturePack;
    private final TerrainTexture blendMap;

    public Terrain(float gridX, float gridZ, Loader laoder, TerrainTexturePack texturePack, TerrainTexture blendMap) {
        this.x = gridX * SIZE;
        this.z = gridZ * SIZE;
        this.model = generateTerrain(laoder);
        this.texturePack = texturePack;
        this.blendMap = blendMap;
    }

    private RawModel generateTerrain(Loader loader) {
        // Calcula la potencia de VERTEX_COUNT para obtener el numero total de vertices
        int count = VERTEX_COUNT * VERTEX_COUNT;
        float[] vertices = new float[count * 3];
        float[] normals = new float[count * 3];
        float[] textureCoords = new float[count * 2];
        int[] indices = new int[6 * (VERTEX_COUNT - 1) * (VERTEX_COUNT - 1)];
        int vertexPointer = 0;
        for (int i = 0; i < VERTEX_COUNT; i++) {
            for (int j = 0; j < VERTEX_COUNT; j++) {
                vertices[vertexPointer * 3] = (float) j / ((float) VERTEX_COUNT - 1) * SIZE;
                vertices[vertexPointer * 3 + 1] = 0;
                vertices[vertexPointer * 3 + 2] = (float) i / ((float) VERTEX_COUNT - 1) * SIZE;
                normals[vertexPointer * 3] = 0;
                normals[vertexPointer * 3 + 1] = 1;
                normals[vertexPointer * 3 + 2] = 0;
                textureCoords[vertexPointer * 2] = (float) j / ((float) VERTEX_COUNT - 1);
                textureCoords[vertexPointer * 2 + 1] = (float) i / ((float) VERTEX_COUNT - 1);
                vertexPointer++;
            }
        }
        int pointer = 0;
        for (int gz = 0; gz < VERTEX_COUNT - 1; gz++) {
            for (int gx = 0; gx < VERTEX_COUNT - 1; gx++) {
                int topLeft = (gz * VERTEX_COUNT) + gx;
                int topRight = topLeft + 1;
                int bottomLeft = ((gz + 1) * VERTEX_COUNT) + gx;
                int bottomRight = bottomLeft + 1;
                indices[pointer++] = topLeft;
                indices[pointer++] = bottomLeft;
                indices[pointer++] = topRight;
                indices[pointer++] = topRight;
                indices[pointer++] = bottomLeft;
                indices[pointer++] = bottomRight;
            }
        }
        return loader.loadToVAO(vertices, textureCoords, normals, indices);
    }

    public float getX() {
        return x;
    }

    public float getZ() {
        return z;
    }

    public RawModel getModel() {
        return model;
    }

    public TerrainTexturePack getTexturePack() {
        return texturePack;
    }

    public TerrainTexture getBlendMap() {
        return blendMap;
    }
}
