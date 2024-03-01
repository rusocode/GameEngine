package models;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import render.Loader;

public class OBJLoader {

    /**
     * @return el modelo en bruto.
     */
    public static RawModel loadObjModel(String fileName, Loader loader) {
        FileReader fr = null;
        try {
            fr = new FileReader("res/" + fileName + ".obj");
        } catch (FileNotFoundException e) {
            System.err.println("Couldn't load file!/n" + e.getMessage());
        }
        // Bufer que lee desde el archivo
        BufferedReader reader = new BufferedReader(fr);
        String line;
        List<Vector3f> vertices = new ArrayList<>(); // Almacena los vertices
        List<Vector2f> textures = new ArrayList<>(); // Almacena las texturas
        List<Vector3f> normals = new ArrayList<>(); // Almacena las normales
        List<Integer> indices = new ArrayList<>(); // Almacena los indices
        // Almacena todos los datos del modelo en matrices para poder cargarlos en el VAO
        float[] verticesArray = null;
        float[] textureArray = null;
        float[] normalsArray = null;
        int[] indicesArray = null;

        try {
            while (true) {
                line = reader.readLine();
                /* Divide la linea especificando el delimitador. En este caso el delimitador es un espacio. Para el ejemplo del
                 * primer vertice dentro del archivo stall.obj la linea "v 3.227124 -0.065127 -1.000000" se divide en 4 cadenas:
                 * currentLine[0] = "v" | currentLine[1] = "3.227124" | currentLine[2] = "-0.065127" | currentLine[3] = "-1.000000" */
                String[] currentLine = line.split(" ");
                if (line.startsWith("v ")) { // TODO Hace falta el espacio?
                    // Crea un vector 3D utilizando los vertices del modelo
                    Vector3f vertex = new Vector3f(Float.parseFloat(currentLine[1]), Float.parseFloat(currentLine[2]), Float.parseFloat(currentLine[3]));
                    vertices.add(vertex);
                } else if (line.startsWith("vt ")) {
                    Vector2f texture = new Vector2f(Float.parseFloat(currentLine[1]), Float.parseFloat(currentLine[2]));
                    textures.add(texture);
                } else if (line.startsWith("vn ")) {
                    Vector3f normal = new Vector3f(Float.parseFloat(currentLine[1]), Float.parseFloat(currentLine[2]), Float.parseFloat(currentLine[3]));
                    normals.add(normal);
                } else if (line.startsWith("f ")) { // Llego al final del archivo (a las caras)
                    textureArray = new float[vertices.size() * 2];
                    normalsArray = new float[vertices.size() * 3];
                    break; // Sale del bucle para iterar las lineas base
                }
            }

            // Itera las lineas base (f)
            while (line != null) {
                // Si la linea no comienza con f, entonces sigue leyendo el archivo hasta llegar a la f
                if (!line.startsWith("f ")) {
                    line = reader.readLine();
                    continue;
                }

                /* La linea base (f 41/1/1 38/2/1 45/3/1) representa un triangulo en el modelo. Cada uno de los bloques (41/1/1)
                 * es un vertice de ese triangulo y los tres numeros (en orden) hacen referencia a la posicion del vertice, a la
                 * coordenada de textura y al normal que usa ese vertice. Entonces, la linea "f 41/1/1 38/2/1 45/3/1" dice que
                 * este triangulo en particular esta compuesto por la posicion 41 del vertice en la matriz de posicion de vertice
                 * que usa la primera coordenada de textura y la primera normal, y ese vertice (41) esta conectado a la posicion
                 * de vertice 38 que usa la segunda coordenada de textura y la primera normal, a su vez este vertice (38) esta
                 * conectado al vertice 45 que usa la tercera coordenada de textura y la primera normal. */

                /* Divide la linea especificando el delimitador. En este caso el delimitador es un espacio. Para el ejemplo del
                 * primer triangulo dentro del archivo stall.obj la linea "f 41/1/1 38/2/1 45/3/1" se divide en 4 cadenas:
                 * currentLine[0] = "f" | currentLine[1] = "41/1/1" | currentLine[2] = "38/2/1" | currentLine[3] = "45/3/1" */
                String[] currentLine = line.split(" ");
                // Divide el bloque "41/1/1" en tres cadenas separadas por el delimitador "/"
                String[] vertex1 = currentLine[1].split("/"); // Ahora el array vertex1 contiene 3 cadenas: "41", "1" y "1"
                String[] vertex2 = currentLine[2].split("/");
                String[] vertex3 = currentLine[3].split("/");

                // Procesa cada vertice del triangulo actual
                processVertex(vertex1, indices, textures, normals, textureArray, normalsArray);
                processVertex(vertex2, indices, textures, normals, textureArray, normalsArray);
                processVertex(vertex3, indices, textures, normals, textureArray, normalsArray);

                line = reader.readLine();

            }

            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Convierte la lista de vertices en una matriz
        verticesArray = new float[vertices.size() * 3];
        // Convierte la lista de indices en una matriz
        indicesArray = new int[indices.size()];

        // Almacena todos los vertices de la lista de vertices en la matriz
        int vertexPointer = 0;
        for (Vector3f vertex : vertices) {
            verticesArray[vertexPointer++] = vertex.x;
            verticesArray[vertexPointer++] = vertex.y;
            verticesArray[vertexPointer++] = vertex.z;
        }

        // Almacena todos los indices en la lista de indices en la matriz
        for (int i = 0; i < indices.size(); i++)
            indicesArray[i] = indices.get(i);

        return loader.loadToVAO(verticesArray, textureArray, normalsArray, indicesArray);

    }

    /**
     * Procesa el vertice.
     */
    private static void processVertex(String[] vertexData, List<Integer> indices, List<Vector2f> textures,
                                      List<Vector3f> normals, float[] textureArray, float[] normalsArray) {
        /* Obtiene el puntero del vertice actual (41). Con este vertice sabemos que coordenada de textura y que normal estan
         * asociados a el, ademas de utilizar el puntero para colocar las coordenadas de texturas y normales en la posicion
         * correcta dentro de las matrices (textureArray y normalsArray). A la posicion del vertice se le resta 1 porque los
         * archivos obj comienzan en 1, y las matrices en 0. */
        int currentVertexPointer = Integer.parseInt(vertexData[0]) - 1;
        indices.add(currentVertexPointer);
        // Obtiene la textura que corresponde al vertice actual
        Vector2f currentTex = textures.get(Integer.parseInt(vertexData[1]) - 1);
        // Agrega la coordenada de textura a la matriz de texturas en la posicion del vertice actual y se multiplica por 2 ya que las coordenadas de texturas son vectores 2D
        textureArray[currentVertexPointer * 2] = currentTex.x;
        textureArray[currentVertexPointer * 2 + 1] = 1 - currentTex.y; // Le resta 1 a la posicion de actual de y de la textura porque OpenGL comienza desde la esquina superior izquierda de una textura
        Vector3f currentNorm = normals.get(Integer.parseInt(vertexData[2]) - 1);
        normalsArray[currentVertexPointer * 3] = currentNorm.x;
        normalsArray[currentVertexPointer * 3 + 1] = currentNorm.y;
        normalsArray[currentVertexPointer * 3 + 2] = currentNorm.z;
        /* En resumen, este metodo ordena las coordenadas de texturas y las normales para el vertice actual, y coloca sus valores
         * en la posicion correcta en las matrices correspondientes. */
    }


}
