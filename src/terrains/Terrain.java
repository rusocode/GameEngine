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
    private static final float MAX_PIXEL_COLOUR = 256 * 256 * 256; // Color maximo de pixeles

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
        float gridSquareSize = SIZE / ((float) heights.length - 1); // 800 / 255 = 3.137255
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
            image = ImageIO.read(new File("res/" + (heightMap.equals("heightmap perlin") ? "heightmap perlin.jpg" : heightMap + ".png")));
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        // Obtiene la altura de la imagen, siendo esta el numero de vertices a lo largo de un lado del terreno
        int VERTEX_COUNT = image.getHeight();
        heights = new float[VERTEX_COUNT][VERTEX_COUNT];
        // Calcula la potencia de VERTEX_COUNT para obtener el numero total de vertices
        int count = VERTEX_COUNT * VERTEX_COUNT;
        /* Inicializa varias matrices para almacenar los datos del terreno, incluyendo las coordenadas de los vertices, las
         * normales de los vertices y las coordenadas de textura con los tamaños adecuados para cada uno. */
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

    /**
     * Calcula la normal de un vertice en el terreno utilizando las alturas de los vertices adyacentes en una imagen de altura
     * como referencia. Esto es util para calcular la iluminacion y el sombreado en el terreno durante el renderizado.
     *
     * @param x     coordenada horizontal de la imagen.
     * @param z     coordenada vertical de la imagen.
     * @param image imagen de altura.
     * @return el vector normalizado que representa la normal del vertice en la posicion (x, z) del terreno.
     */
    private Vector3f calculateNormal(int x, int z, BufferedImage image) {
        /* Obtiene las alturas de los vertices adyacentes al vertice en la posicion (x, z) en las direcciones izquierda (heightL),
         * derecha (heightR), abajo (heightD) y arriba (heightU) llamando al metodo getHeight con las coordenadas
         * correspondientes.
         * La razon de restar 1 a la coordenada x es porque se esta buscando la altura del vertice que esta a la izquierda en la
         * imagen de altura. Al moverse hacia la izquierda en una matriz bidimensional (como la representacion de una imagen), se
         * disminuye el valor de la coordenada x. Por lo tanto, se resta 1 a la coordenada x para acceder al vertice adyacente
         * situado a la izquierda.
         * De manera similar, se suman 1 y se restan 1 respectivamente a las coordenadas x y z para acceder a los vertices
         * adyacentes en otras direcciones (derecha, arriba y abajo) en la imagen de altura. Esto garantiza que se obtengan las
         * alturas de los vertices adyacentes correctos para calcular la normal del vertice en la posicion (x, z) del terreno de
         * manera adecuada. */
        float heightL = getHeight(x - 1, z, image);
        float heightR = getHeight(x + 1, z, image);
        float heightD = getHeight(x, z - 1, image);
        float heightU = getHeight(x, z + 1, image);
        /* Calcula la diferencia de altura entre los vertices adyacentes en las direcciones horizontal (izquierda y derecha) y
         * vertical (abajo y arriba) y crea un vector con estas diferencias. Esto se hace restando la altura de los vertices
         * adyacentes al vertice en la posicion (x, z). */
        Vector3f normal = new Vector3f(heightL - heightR, 2f, heightD - heightU);
        /* Normaliza el vector resultante llamando al metodo normalise() de la clase Vector3f. Esto ajusta la magnitud del vector
         * para que tenga una longitud de 1 y lo convierte en una normal unitaria. */
        normal.normalise();
        return normal;
    }

    /**
     * Utiliza el color de un pixel en una imagen de altura para determinar la altura del terreno en una posicion especifica.
     * Luego ajusta y normaliza este valor para que este dentro del rango de alturas deseado para el terreno.
     *
     * @param x     coordenada horizontal de la imagen.
     * @param z     coordenada vertical de la imagen.
     * @param image imagen de altura.
     * @return la altura calculada.
     */
    private float getHeight(int x, int z, BufferedImage image) {
        // Verifica que las coordenadas esten dentro de los limites de la imagen de altura
        if (x < 0 || x >= image.getHeight() || z < 0 || z >= image.getHeight()) return 0;
        /* Obtiene el valor del color del pixel en las coordenadas (x, z) de la imagen utilizando el metodo getRGB(x, z) de la
         * clase BufferedImage. Este valor representa la altura del terreno en ese punto de la imagen. */
        float height = image.getRGB(x, z);
        /* Ajusta el valor de altura para que este en el rango deseado:
         * a. Se suma la mitad del valor maximo de color de un pixel (MAX_PIXEL_COLOUR / 2f) al valor del color del pixel. Esto se
         * hace para centrar los valores de color alrededor de cero, ya que los valores de color normalmente van desde 0 hasta
         * MAX_PIXEL_COLOUR, y se desea que el rango de altura vaya desde -MAX_HEIGHT hasta MAX_HEIGHT.
         * b. Se divide el valor de altura por la mitad del valor maximo de color de un pixel (MAX_PIXEL_COLOUR / 2f). Esto
         * normaliza el valor de altura para que este en el rango [-1, 1].
         * c. Se multiplica el valor de altura por la altura maxima deseada del terreno (MAX_HEIGHT). Esto escala el valor de
         * altura normalizado al rango deseado de alturas del terreno. */
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
