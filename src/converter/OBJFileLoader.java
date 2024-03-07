package converter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

/**
 * Este analizador a diferencia del anterior soluciona el problema de la costura de textura (mencionado en el episodio 9), por lo
 * que puede cargar modelos que contienen costuras de textura sin que tengas que dividir los bordes a lo largo de la costura de
 * textura en Blender antes de exportar. La mayoria de los modelos que encuentras en linea tienen costuras de textura y no se
 * habrian cargado correctamente con el cargador OBJ anterior.
 */

public class OBJFileLoader {

    private static final String RES_LOC = "res/";

    /**
     * Carga el modelo.
     * <p>
     * TODO No seria mejor nombrarlo loadModel?
     *
     * @param fileName archivo del modelo.
     * @return los datos del modelo.
     */
    public static ModelData loadOBJ(String fileName) {
        FileReader fr = null;
        try {
            fr = new FileReader(RES_LOC + fileName + ".obj");
        } catch (FileNotFoundException e) {
            System.err.println("Couldn't load file!/n" + e.getMessage());
        }
        // Bufer que lee desde el archivo
        BufferedReader reader = new BufferedReader(fr);
        String line;
        List<Vertex> vertices = new ArrayList<>(); // Lista de vertices
        List<Vector2f> textures = new ArrayList<>(); // Lista de texturas
        List<Vector3f> normals = new ArrayList<>(); // Lista de normales
        List<Integer> indices = new ArrayList<>(); // Lista de indices

        try {
            while (true) {
                line = reader.readLine();
                if (line.startsWith("v ")) {
                    /* Divide la linea especificando el delimitador. En este caso el delimitador es un espacio. Para el ejemplo del
                     * primer vertice dentro del archivo stall.obj la linea "v 3.227124 -0.065127 -1.000000" se divide en 4 cadenas:
                     * currentLine[0] = "v" | currentLine[1] = "3.227124" | currentLine[2] = "-0.065127" | currentLine[3] = "-1.000000" */
                    String[] currentLine = line.split(" ");
                    vertices.add(new Vertex(vertices.size(), new Vector3f(Float.parseFloat(currentLine[1]), Float.parseFloat(currentLine[2]), Float.parseFloat(currentLine[3]))));
                } else if (line.startsWith("vt ")) {
                    String[] currentLine = line.split(" ");
                    textures.add(new Vector2f(Float.parseFloat(currentLine[1]), Float.parseFloat(currentLine[2])));
                } else if (line.startsWith("vn ")) {
                    String[] currentLine = line.split(" ");
                    normals.add(new Vector3f(Float.parseFloat(currentLine[1]), Float.parseFloat(currentLine[2]), Float.parseFloat(currentLine[3])));
                } else if (line.startsWith("f ")) break;
                // Llego al final del archivo (caras), por lo que sale del bucle para iterar las lineas base
            }

            // Si la linea es distinta a null y comienza con f
            while (line != null && line.startsWith("f ")) {

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
                processVertex(vertex1, vertices, indices);
                processVertex(vertex2, vertices, indices);
                processVertex(vertex3, vertices, indices);

                line = reader.readLine();
            }

            reader.close();

        } catch (IOException e) {
            System.err.println("Error reading the file");
        }

        removeUnusedVertices(vertices);

        float[] verticesArray = new float[vertices.size() * 3];
        float[] texturesArray = new float[vertices.size() * 2];
        float[] normalsArray = new float[vertices.size() * 3];
        float furthest = convertDataToArrays(vertices, textures, normals, verticesArray, texturesArray, normalsArray);
        int[] indicesArray = convertIndicesListToArray(indices);

        return new ModelData(verticesArray, texturesArray, normalsArray, indicesArray, furthest);
    }

    /**
     * Procesa el vertice.
     */
    private static void processVertex(String[] vertex, List<Vertex> vertices, List<Integer> indices) {
        /* Obtiene el indice del vertice actual (41). Con este vertice sabemos que coordenada de textura y que normal estan
         * asociados a el. A la posicion del vertice se le resta 1 porque los archivos .obj comienzan en 1, y las matrices en 0. */
        int index = Integer.parseInt(vertex[0]) - 1;
        Vertex currentVertex = vertices.get(index);
        int textureIndex = Integer.parseInt(vertex[1]) - 1;
        int normalIndex = Integer.parseInt(vertex[2]) - 1;
        if (!currentVertex.isSet()) {
            currentVertex.setTextureIndex(textureIndex);
            currentVertex.setNormalIndex(normalIndex);
            indices.add(index);
        } else dealWithAlreadyProcessedVertex(currentVertex, textureIndex, normalIndex, indices, vertices);
    }

    /**
     * Trata con el vertice ya procesado.
     */
    private static void dealWithAlreadyProcessedVertex(Vertex previousVertex, int newTextureIndex, int newNormalIndex, List<Integer> indices, List<Vertex> vertices) {
        // Si es la misma textura, entonces la agrega a la lista de indices
        if (previousVertex.hasSameTextureAndNormal(newTextureIndex, newNormalIndex)) indices.add(previousVertex.getIndex());
        else {
            Vertex anotherVertex = previousVertex.getDuplicateVertex();
            if (anotherVertex != null)
                dealWithAlreadyProcessedVertex(anotherVertex, newTextureIndex, newNormalIndex, indices, vertices);
            else {
                Vertex duplicateVertex = new Vertex(vertices.size(), previousVertex.getPosition());
                duplicateVertex.setTextureIndex(newTextureIndex);
                duplicateVertex.setNormalIndex(newNormalIndex);
                previousVertex.setDuplicateVertex(duplicateVertex);
                vertices.add(duplicateVertex);
                indices.add(duplicateVertex.getIndex());
            }
        }
    }

    private static int[] convertIndicesListToArray(List<Integer> indices) {
        int[] indicesArray = new int[indices.size()];
        for (int i = 0; i < indicesArray.length; i++) indicesArray[i] = indices.get(i);
        return indicesArray;
    }

    private static float convertDataToArrays(List<Vertex> vertices, List<Vector2f> textures,
                                             List<Vector3f> normals, float[] verticesArray, float[] texturesArray,
                                             float[] normalsArray) {
        float furthestPoint = 0;
        for (int i = 0; i < vertices.size(); i++) {
            Vertex currentVertex = vertices.get(i);
            if (currentVertex.getLength() > furthestPoint) furthestPoint = currentVertex.getLength();
            Vector3f position = currentVertex.getPosition();
            Vector2f textureCoord = textures.get(currentVertex.getTextureIndex());
            Vector3f normalVector = normals.get(currentVertex.getNormalIndex());
            verticesArray[i * 3] = position.x;
            verticesArray[i * 3 + 1] = position.y;
            verticesArray[i * 3 + 2] = position.z;
            texturesArray[i * 2] = textureCoord.x;
            texturesArray[i * 2 + 1] = 1 - textureCoord.y;
            normalsArray[i * 3] = normalVector.x;
            normalsArray[i * 3 + 1] = normalVector.y;
            normalsArray[i * 3 + 2] = normalVector.z;
        }
        return furthestPoint;
    }

    private static void removeUnusedVertices(List<Vertex> vertices) {
        for (Vertex vertex : vertices) {
            if (!vertex.isSet()) {
                vertex.setTextureIndex(0);
                vertex.setNormalIndex(0);
            }
        }
    }

}
