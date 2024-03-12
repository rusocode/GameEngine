package converter;

import java.io.*;
import java.util.*;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

/**
 * Este analizador a diferencia del anterior soluciona el problema de la costura de textura (mencionado en el episodio 9), por lo
 * que puede cargar modelos que contienen costuras de textura sin que tengas que dividir los bordes a lo largo de la costura de
 * textura en Blender antes de exportar. La mayoria de los modelos que encuentras en linea tienen costuras de textura y no se
 * habrian cargado correctamente con el cargador OBJ anterior.
 */

public class OBJLoader {

    /**
     * Carga el obj.
     *
     * @param fileName nombre del archivo.
     * @return los datos del modelo.
     */
    public static ModelData loadOBJ(String fileName) {
        FileReader fr = null;
        try {
            fr = new FileReader("res/" + fileName + ".obj");
        } catch (FileNotFoundException e) {
            System.err.println("Couldn't load file!/n" + e.getMessage());
        }
        // Bufer que lee desde el archivo
        BufferedReader reader = new BufferedReader(fr);
        String line;
        List<Vertex> vertices = new ArrayList<>(); // Lista de objetos Vertex (vertices)
        List<Vector2f> textures = new ArrayList<>(); // Lista de texturas
        List<Vector3f> normals = new ArrayList<>(); // Lista de normales
        List<Integer> indices = new ArrayList<>(); // Lista de indices

        try {
            // Almacena las posiciones, coordenadas de textura y normales del modelo en listas
            while (true) {
                line = reader.readLine();
                if (line.startsWith("v ")) {
                    /* Divide la linea especificando el espacio como delimitador. Tomando como ejemplo el primer vertice del
                     * archivo fern.obj, la linea "v -1.668906 1.421207 -4.981303" se divide en 4 cadenas:
                     * currentLine[0] = "v" | currentLine[1] = "-1.668906" | currentLine[2] = "1.421207" | currentLine[3] = "-4.981303" */
                    String[] currentLine = line.split(" ");
                    /* Crea un objeto Vertex especificando el indice (aumenta cada vez que se agrega un objeto a la lista) y la
                     * posicion, y lo agrega a la lista. */
                    vertices.add(new Vertex(vertices.size(), new Vector3f(Float.parseFloat(currentLine[1]), Float.parseFloat(currentLine[2]), Float.parseFloat(currentLine[3]))));
                } else if (line.startsWith("vt ")) {
                    String[] currentLine = line.split(" ");
                    textures.add(new Vector2f(Float.parseFloat(currentLine[1]), Float.parseFloat(currentLine[2])));
                } else if (line.startsWith("vn ")) {
                    String[] currentLine = line.split(" ");
                    normals.add(new Vector3f(Float.parseFloat(currentLine[1]), Float.parseFloat(currentLine[2]), Float.parseFloat(currentLine[3])));
                } else if (line.startsWith("f ")) break;
                // Llego al final del archivo, por lo que sale del bucle para iterar las lineas base
            }

            // Si la linea es distinta a null y comienza con f
            while (line != null && line.startsWith("f ")) {

                /* La primera linea base de fern.obj "f 3/1/1 4/2/2 1/3/3" representa un triangulo en el modelo. Cada uno de los
                 * bloques "1/3/3" es un vertice de ese triangulo y los tres numeros (en orden) hacen referencia a la posicion,
                 * coordenada de textura y normal. Entonces, el ultimo bloque de la linea base (1/3/3) dice que este triangulo en
                 * particular esta compuesto por el vertice 1, que usa la tercera coordenada de textura y la tercera normal, y ese
                 * vertice (1) esta conectado al vertice 4 que usa la segunda coordenada de textura y la segunda normal, a su vez
                 * este vertice (4) esta conectado al vertice 3 que usa la primera coordenada de textura y la primera normal.
                 * Tecnicamente hablando, en el ultimo bloque (1/3/3), 1 hace referencia al primer vertice del archivo .obj: "v -1.668906 1.421207 -4.981303".
                 * Luego, 3 hace referencia a la tercera coordenada de textura "vt 0.261788 0.999931" y el ultimo numero del
                 * bloque (3) hace referencia a la tercera normal "vn 0.008454 0.417798 -0.908475". */

                /* Divide la linea especificando el espacio como delimitador. Tomando como ejemplo el primer triangulo del archivo
                 * fern.obj, la linea "f 3/1/1 4/2/2 1/3/3" se divide en 4 cadenas:
                 * currentLine[0] = "f" | currentLine[1] = "3/1/1" | currentLine[2] = "4/2/2" | currentLine[3] = "1/3/3" */
                String[] currentLine = line.split(" ");
                String[] vertex1 = currentLine[1].split("/");
                String[] vertex2 = currentLine[2].split("/");
                // Divide el bloque "1/3/3" en tres cadenas separadas por el delimitador "/"
                String[] vertex3 = currentLine[3].split("/"); // Ahora el array vertex3 contiene 3 cadenas: "1", "3" y "3"

                // Procesa los 3 vertices del triangulo
                processVertex(vertex1, vertices, indices);
                processVertex(vertex2, vertices, indices);
                processVertex(vertex3, vertices, indices);

                line = reader.readLine();
            }

            reader.close();

        } catch (IOException e) {
            System.err.println("Error reading the file!/n" + e.getMessage());
        }

        removeUnusedVertices(vertices);

        // Crea los arrays (para poder cargarlos en el VAO) con los tamaños adecuados para cada lista de datos
        float[] verticesArray = new float[vertices.size() * 3];
        float[] texturesArray = new float[vertices.size() * 2];
        float[] normalsArray = new float[vertices.size() * 3];

        // TODO Se pasa la direccion de memoria y no el valor?
        float furthest = convertDataListToArrays(vertices, textures, normals, verticesArray, texturesArray, normalsArray);

        int[] indicesArray = convertIndicesListToArray(indices);

        return new ModelData(verticesArray, texturesArray, normalsArray, indicesArray, furthest);
    }

    /**
     * Procesa el vertice.
     *
     * @param vertex   array con los numeros del bloque.
     * @param vertices lista de vertices.
     * @param indices  lista de indices.
     */
    private static void processVertex(String[] vertex, List<Vertex> vertices, List<Integer> indices) {
        /* Tomando como ejemplo el bloque "1/3/3", obtiene la primera cadena del array vertex (vertex[0]) que es 1 y le resta 1
         * porque los archivos .obj comienzan en 1 y los arrays en 0. Por lo tanto se almacena 0 en la variable index. */
        int i = Integer.parseInt(vertex[0]) - 1;
        // Obtiene el objeto Vertex pasandole el index 0, es decir, el primer objeto Vertex creado anteriormente
        Vertex currentVertex = vertices.get(i);
        // Hace lo mismo para el indice de la coordenada de textura y la normal
        int textureIndex = Integer.parseInt(vertex[1]) - 1;
        int normalIndex = Integer.parseInt(vertex[2]) - 1;
        // Si el vertice no se establecio
        if (!currentVertex.isSet()) {
            // Establece los indices de la coordenada de textura y la normal del vertice actual
            currentVertex.setTextureIndex(textureIndex);
            currentVertex.setNormalIndex(normalIndex);
            // Agrega el indice del vertice actual a la lista de indices
            indices.add(i);
        } else dealWithAlreadyProcessedVertex(currentVertex, textureIndex, normalIndex, vertices, indices);
    }

    /**
     * Trata con el vertice ya procesado.
     */
    private static void dealWithAlreadyProcessedVertex(Vertex previousVertex, int newTextureIndex, int newNormalIndex, List<Vertex> vertices, List<Integer> indices) {
        // Si es la misma textura, entonces la agrega a la lista de indices
        if (previousVertex.hasSameTextureAndNormal(newTextureIndex, newNormalIndex)) indices.add(previousVertex.getIndex());
        else {
            Vertex anotherVertex = previousVertex.getDuplicateVertex();
            if (anotherVertex != null)
                dealWithAlreadyProcessedVertex(anotherVertex, newTextureIndex, newNormalIndex, vertices, indices);
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

    /**
     * Convierte la lista de indices en un array.
     *
     * @param indices lista de indices.
     * @return el array con los indices.
     */
    private static int[] convertIndicesListToArray(List<Integer> indices) {
        int[] array = new int[indices.size()];
        for (int i = 0; i < array.length; i++) array[i] = indices.get(i);
        return array;
    }

    /**
     * Convierte la lista de datos en arrays.
     *
     * @param vertices      lista de vertices.
     * @param textures      lista de coordenadas de textura.
     * @param normals       lista de normales.
     * @param verticesArray array para la lista de vertices.
     * @param texturesArray array para la lista de coordenadas de textura.
     * @param normalsArray  array para la lista de normales.
     * @return el punto mas lejano.
     */
    private static float convertDataListToArrays(List<Vertex> vertices, List<Vector2f> textures,
                                                 List<Vector3f> normals, float[] verticesArray, float[] texturesArray,
                                                 float[] normalsArray) {
        float furthestPoint = 0;

        for (int i = 0; i < vertices.size(); i++) {

            // Obtiene el vertice actual
            Vertex currentVertex = vertices.get(i);

            if (currentVertex.getLength() > furthestPoint) furthestPoint = currentVertex.getLength();

            // A partir del vertice actual se obtiene la posicion
            Vector3f vertex = currentVertex.getPosition();
            // A partir del vertice actual se obtiene el indice de la coordenada de textura y a partir de ese indice se obtiene la coordenada de textura
            Vector2f textureCoord = textures.get(currentVertex.getTextureIndex());
            Vector3f normalVector = normals.get(currentVertex.getNormalIndex());

            /* Desde los vectores se obtienen los ejes correspondientes que se usaran para llenar los arrays con los datos del
             * .obj. Es importante aclarar que el indice del verticesArray se multiplica por 3 en cada iteracion del for para
             * cambiar de posicion correctamente ya que es un vector 3D. Para la primera iteracion, la operacion "i * 3" da 0, ese
             * indice se usa para almacenar la coordenada x, despues se suma 1 al indice para alamacenar la coordenada y. Por
             * ultimo, se suma 2 al indice para almacenar la coordenada z en la posicion correcta del array. */
            verticesArray[i * 3] = vertex.x;
            verticesArray[i * 3 + 1] = vertex.y;
            verticesArray[i * 3 + 2] = vertex.z;
            // El indice de textureArray se multiplica por 2 ya que es un vector 2D
            texturesArray[i * 2] = textureCoord.x;
            texturesArray[i * 2 + 1] = 1 - textureCoord.y; // Le resta 1 a la coordenada y de la textura porque OpenGL comienza desde la esquina superior izquierda de una textura
            normalsArray[i * 3] = normalVector.x;
            normalsArray[i * 3 + 1] = normalVector.y;
            normalsArray[i * 3 + 2] = normalVector.z;
        }
        return furthestPoint;
    }

    /**
     * Elimina los vertices no utilizados.
     *
     * @param vertices lista de vertices.
     */
    private static void removeUnusedVertices(List<Vertex> vertices) {
        for (Vertex vertex : vertices) {
            // Si el vertice no se establecio
            if (!vertex.isSet()) {
                vertex.setTextureIndex(0);
                vertex.setNormalIndex(0);
            }
        }
    }

}
