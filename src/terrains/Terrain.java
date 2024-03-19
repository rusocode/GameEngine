package terrains;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import models.RawModel;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import render.Loader;
import textures.TerrainTexture;
import textures.TerrainTexturePack;
import toolBox.Maths;

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
    private static final float MAX_HEIGHT = 40; // Altura maxima del terreno
    private static final float MAX_PIXEL_COLOUR = 256 * 256 * 256; // Representa los colores de los canales RGB

    // El eje y no es necesario ya que el terreno permanece en la misma altura
    private final float x, z;
    private final RawModel model;
    private final TerrainTexturePack texturePack;
    private final TerrainTexture blendMap;

    // Almacena las alturas de cada vertice del terreno
    private float[][] heights;

    public Terrain(float gridX, float gridZ, Loader laoder, TerrainTexturePack texturePack, TerrainTexture blendMap, String heightMap) {
        this.x = gridX * SIZE;
        this.z = gridZ * SIZE;
        this.model = generateTerrain(laoder, heightMap);
        this.texturePack = texturePack;
        this.blendMap = blendMap;
    }

    /**
     * Obtiene la altura del terreno en la posicion del player.
     *
     * @param worldX posicion x del player.
     * @param worldZ posicion z del player.
     * @return la altura del terreno.
     */
    public float getHeightOfTerrain(float worldX, float worldZ) {
        // Convierte la coordenada mundial en una posicion relativa al terreno
        float terrainX = worldX - this.x;
        float terrainZ = worldZ - this.z;
        // Calcula el tamanio de cada cuadrado de la cuadricula del terreno
        float gridSquareSize = SIZE / ((float) heights.length - 1);
        /* Averigua en que cuadrado de la cuadricula esta la coordenada (x,z). Por ejemplo, si cada cuadrado de la cuadricula es
         * de 5x5 y estamos en la posicion del terreno (13,8), al dividir esta posicion por la longitud del cuadrado de la
         * cuadricula y calcular el floor de la division, el resultado dara que estamos en la posicion (2,1). */
        int gridX = (int) Math.floor(terrainX / gridSquareSize);
        int gridZ = (int) Math.floor(terrainZ / gridSquareSize);
        // Verifica si la posicion esta dentro de los limites del terreno
        if (gridX >= heights.length - 1 || gridZ >= heights.length - 1 || gridX < 0 || gridZ < 0) return 0;
        /* Averigua en que lugar de la cuadricula esta el jugador usando el operador de modulo para averiguar la distancia del
         * jugador desde la esquina superior izquierda del cuadrado de la cuadricula para encontrar la distancia x y la distancia
         * z. Luego las divide por el tamaño del cuadrado de la cuadricula que dara una coordenada x y una coordenada z entre 0 y
         * 1. */
        float xCoord = (terrainX % gridSquareSize) / gridSquareSize;
        float zCoord = (terrainZ % gridSquareSize) / gridSquareSize;
        /* Por lo que si este es el cuadrado de la cuadricula en el que estamos actualmente, la parte superior izquierda es (0,0)
         * y la parte inferior derecha es (1,1), y acabamos de calcular la coordenada (x,z) del jugador en este cuadrado, que
         * seria algo como (0.75,0.25). Como sabes, todo esta hecho de triangulos en el mundo 3D, por lo que cada cuadrado de la
         * cuadricula es en realidad dos triangulos. Entonces, ¿como podemos encontrar en que triangulo el player esta ubicado?.
         * La linea que separa los dos triangulos, es la linea x, que es igual a (x = 1 - z). Todo en esa linea tendra una
         * coordenada x que es iugal a 1 menos la coordenada z. En el lado inferior del triangulo, la coordenada x es (x > 1 - z)
         * y en el lado superior es lo contrario (x < 1 - z). Al probar si la coordenda x es mayor que uno menos la coordenada z,
         * podemos determinar en que triangulo esta parado el player. */
        float answer;
        /* Ahora que sabemos en que triangulo esta el jugador y sabemos su posicion (x,z) en el triangulo. Tambien sabemos la
         * altura de cada punto en el triangulo porque hemos almacenado las alturas de todos los vertices del terreno en la matriz
         * de alturas (heights). Todo lo que necesitamos ahora es encontrar la altura del triangulo en la posicion (x,z) del
         * player. Una forma de hacerlo es la interpolacion centrada en bary. */
        if (xCoord <= (1 - zCoord)) {
            answer = Maths.barryCentric(new Vector3f(0, heights[gridX][gridZ], 0), new Vector3f(1,
                    heights[gridX + 1][gridZ], 0), new Vector3f(0,
                    heights[gridX][gridZ + 1], 1), new Vector2f(xCoord, zCoord));
        } else {
            answer = Maths.barryCentric(new Vector3f(1, heights[gridX + 1][gridZ], 0), new Vector3f(1,
                    heights[gridX + 1][gridZ + 1], 1), new Vector3f(0,
                    heights[gridX][gridZ + 1], 1), new Vector2f(xCoord, zCoord));
        }
        return answer;
    }

    private RawModel generateTerrain(Loader loader, String heightMap) {

        BufferedImage image = null;
        try {
            image = ImageIO.read(new File("res/" + heightMap + ".png"));
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        int VERTEX_COUNT = image.getHeight();
        heights = new float[VERTEX_COUNT][VERTEX_COUNT];
        // Calcula la potencia de VERTEX_COUNT para obtener el numero total de vertices
        int count = VERTEX_COUNT * VERTEX_COUNT;
        float[] vertices = new float[count * 3];
        float[] normals = new float[count * 3];
        float[] textureCoords = new float[count * 2];
        int[] indices = new int[6 * (VERTEX_COUNT - 1) * (VERTEX_COUNT - 1)];
        int vertexPointer = 0;
        for (int i = 0; i < VERTEX_COUNT; i++) {
            for (int j = 0; j < VERTEX_COUNT; j++) {
                vertices[vertexPointer * 3] = (float) j / (VERTEX_COUNT - 1) * SIZE;
                float height = getHeight(j, i, image);
                heights[j][i] = height; // Almacena el vertice de altura en la matriz
                vertices[vertexPointer * 3 + 1] = height;
                vertices[vertexPointer * 3 + 2] = (float) i / (VERTEX_COUNT - 1) * SIZE;
                Vector3f normal = calculateNormal(j, i, image);
                normals[vertexPointer * 3] = normal.x;
                normals[vertexPointer * 3 + 1] = normal.y;
                normals[vertexPointer * 3 + 2] = normal.z;
                textureCoords[vertexPointer * 2] = (float) j / (VERTEX_COUNT - 1);
                textureCoords[vertexPointer * 2 + 1] = (float) i / (VERTEX_COUNT - 1);
                vertexPointer++;
            }
        }
        int i = 0;
        for (int gz = 0; gz < VERTEX_COUNT - 1; gz++) {
            for (int gx = 0; gx < VERTEX_COUNT - 1; gx++) {
                int topLeft = (gz * VERTEX_COUNT) + gx;
                int topRight = topLeft + 1;
                int bottomLeft = ((gz + 1) * VERTEX_COUNT) + gx;
                int bottomRight = bottomLeft + 1;
                indices[i++] = topLeft;
                indices[i++] = bottomLeft;
                indices[i++] = topRight;
                indices[i++] = topRight;
                indices[i++] = bottomLeft;
                indices[i++] = bottomRight;
            }
        }
        return loader.loadToVAO(vertices, textureCoords, normals, indices);
    }

    private Vector3f calculateNormal(int x, int z, BufferedImage image) {
        float heightL = getHeight(x - 1, z, image);
        float heightR = getHeight(x + 1, z, image);
        float heightD = getHeight(x, z - 1, image);
        float heightU = getHeight(x, z + 1, image);
        Vector3f normal = new Vector3f(heightL - heightR, 2f, heightD - heightU);
        normal.normalise();
        return normal;
    }

    private float getHeight(int x, int z, BufferedImage image) {
        if (x < 0 || x >= image.getHeight() || z < 0 || z >= image.getHeight()) return 0;
        float height = image.getRGB(x, z); // Devuelve un valor que representa el color del pixel
        height += MAX_PIXEL_COLOUR / 2f;
        height /= MAX_PIXEL_COLOUR / 2f;
        height *= MAX_HEIGHT;
        return height;
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
