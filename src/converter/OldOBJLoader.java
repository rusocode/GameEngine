package converter;

import java.io.*;
import java.util.*;

import render.Loader;
import models.RawModel;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

public class OldOBJLoader {

    /**
     * Carga el obj.
     *
     * @param fileName nombre del archivo.
     * @param loader   cargador de modelos.
     * @return el modelo en bruto.
     */
    public static RawModel loadOBJ(String fileName, Loader loader) {
        FileReader fr = null;
        try {
            fr = new FileReader("res/" + fileName + ".obj");
        } catch (FileNotFoundException e) {
            System.err.println("Couldn't load file!/n" + e.getMessage());
        }

        BufferedReader reader = new BufferedReader(fr);
        String line;
        List<Vector3f> vertices = new ArrayList<>();
        List<Vector2f> textures = new ArrayList<>();
        List<Vector3f> normals = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        float[] texturesArray = null;
        float[] normalsArray = null;

        try {
            while (true) {
                line = reader.readLine();
                String[] currentLine = line.split(" ");
                if (line.startsWith("v "))
                    vertices.add(new Vector3f(Float.parseFloat(currentLine[1]), Float.parseFloat(currentLine[2]), Float.parseFloat(currentLine[3])));
                else if (line.startsWith("vt "))
                    textures.add(new Vector2f(Float.parseFloat(currentLine[1]), Float.parseFloat(currentLine[2])));
                else if (line.startsWith("vn "))
                    normals.add(new Vector3f(Float.parseFloat(currentLine[1]), Float.parseFloat(currentLine[2]), Float.parseFloat(currentLine[3])));
                else if (line.startsWith("f ")) {
                    texturesArray = new float[vertices.size() * 2];
                    normalsArray = new float[vertices.size() * 3];
                    break;
                }
            }

            while (line != null && line.startsWith("f ")) {

                String[] currentLine = line.split(" ");
                String[] vertex1 = currentLine[1].split("/");
                String[] vertex2 = currentLine[2].split("/");
                String[] vertex3 = currentLine[3].split("/");

                processVertex(vertex1, indices, textures, normals, texturesArray, normalsArray);
                processVertex(vertex2, indices, textures, normals, texturesArray, normalsArray);
                processVertex(vertex3, indices, textures, normals, texturesArray, normalsArray);

                line = reader.readLine();

            }

            reader.close();

        } catch (IOException e) {
            System.err.println("Error reading the file!/n" + e.getMessage());
        }

        float[] verticesArray = convertVericesListToArray(vertices);
        int[] indicesArray = convertIndicesListToArray(indices);

        return loader.loadToVAO(verticesArray, texturesArray, normalsArray, indicesArray);

    }

    private static void processVertex(String[] vertex, List<Integer> indices, List<Vector2f> textures, List<Vector3f> normals, float[] texturesArray, float[] normalsArray) {
        int i = Integer.parseInt(vertex[0]) - 1;
        indices.add(i);

        Vector2f texture = textures.get(Integer.parseInt(vertex[1]) - 1);
        texturesArray[i * 2] = texture.x;
        texturesArray[i * 2 + 1] = 1 - texture.y;

        Vector3f normal = normals.get(Integer.parseInt(vertex[2]) - 1);
        normalsArray[i * 3] = normal.x;
        normalsArray[i * 3 + 1] = normal.y;
        normalsArray[i * 3 + 2] = normal.z;
    }

    private static float[] convertVericesListToArray(List<Vector3f> vertices) {
        float[] array = new float[vertices.size() * 3];
        int i = 0;
        for (Vector3f vertex : vertices) {
            array[i++] = vertex.x;
            array[i++] = vertex.y;
            array[i++] = vertex.z;
        }
        return array;
    }

    private static int[] convertIndicesListToArray(List<Integer> indices) {
        int[] array = new int[indices.size()];
        for (int i = 0; i < array.length; i++) array[i] = indices.get(i);
        return array;
    }

}

