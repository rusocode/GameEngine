package render;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import models.RawModel;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

import javax.swing.*;

/**
 * Carga modelos 3D en memoria, almacenando posiciones y datos sobre el modelo en VBO.
 */

public class Loader {

    // Crea una lista de seguimientos de los VAO y VBO para poder eliminarlos cuando se cierre el juego
    private final List<Integer> vaos = new ArrayList<>();
    private final List<Integer> vbos = new ArrayList<>();
    private final List<Integer> textures = new ArrayList<>();

    /**
     * Obtiene las posiciones de los vertices y los carga en un VAO.
     *
     * @param positions     posicion de los vertices.
     * @param textureCoords coordenadas de texturas.
     * @param indices       indices.
     * @return la informacion sobre el VAO como modelo sin procesar.
     */
    public RawModel loadToVAO(float[] positions, float[] textureCoords, int[] indices) {
        int vaoID = createVAO();
        // Vincula el buffer de indices
        bindIndicesBuffer(indices);
        storeDataInAttributeList(0, 3, positions); // Posiciones
        storeDataInAttributeList(1, 2, textureCoords); // Coordenadas de texturas
        unbindVAO();
        return new RawModel(vaoID, indices.length);
    }

    public int loadTexture(String fileName) {
        Texture texture = null;
        try {
            texture = TextureLoader.getTexture("PNG", new FileInputStream("res/" + fileName + ".png"));
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, "No se pudo encontrar la imagen", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error de I/O", "Error", JOptionPane.ERROR_MESSAGE);
        }
        int textureID = texture.getTextureID();
        textures.add(textureID);
        return textureID;
    }

    private int createVAO() {
        // Crea un VAO vacio
        int vaoID = GL30.glGenVertexArrays();
        vaos.add(vaoID);
        // Activa el VAO
        GL30.glBindVertexArray(vaoID);
        return vaoID;
    }

    /**
     * Carga el buffer de indices y lo vincula a un VAO.
     */
    private void bindIndicesBuffer(int[] indices) {
        // Crea un VBO vacio usando los buffers
        int vboID = GL15.glGenBuffers();
        vbos.add(vboID);
        // Vincula el buffer usando un buffer de matriz de elementos y eso le dice a OpenGL que lo use como buffer de indices
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboID);
        IntBuffer buffer = storeDataInIntBuffer(indices);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
    }

    /**
     * Almacena los datos la lista del VAO.
     *
     * @param attributeNumber numero de atributo.
     * @param coordinateSize  tamanio de coordenadas.
     * @param data            datos.
     */
    private void storeDataInAttributeList(int attributeNumber, int coordinateSize, float[] data) {
        int vboID = GL15.glGenBuffers();
        vbos.add(vboID);
        // Almacena los datos del VBO en el buffer de array
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
        FloatBuffer buffer = storeDataInFloatBuffer(data);
        /* Una vez que tengamos un buffer con los datos, podemos almacenarlos en el VBO especifiando para que se usaran los datos,
         * en este caso se utiliza GL_STATIC_DRAW para que los datos no se puedan editar una vez que esten almacenados. */
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        /* Coloca el VBO en la lista de atributos pasandole el numero de atributo para la lista, la longitud de cada vertice, el
         * tipo de datos, si los datos estan normalizados o no, la distancia entra cada uno de sus vertices y el desplazamiento. */
        GL20.glVertexAttribPointer(attributeNumber, coordinateSize, GL11.GL_FLOAT, false, 0, 0);
        // Una vez terminado se desvincula el VBO actual
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    /**
     * Desvincula el VAO.
     */
    private void unbindVAO() {
        GL30.glBindVertexArray(0);
    }

    /**
     * Almacena los datos del array en un buffer.
     *
     * @param data datos.
     * @return buffer de datos.
     */
    private FloatBuffer storeDataInFloatBuffer(float[] data) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
        buffer.put(data);
        buffer.flip();
        return buffer;
    }

    /**
     * Almacena los indices en el VAO.
     *
     * @param data datos.
     * @return buffer de datos.
     */
    private IntBuffer storeDataInIntBuffer(int[] data) {
        IntBuffer buffer = BufferUtils.createIntBuffer(data.length);
        buffer.put(data);
        buffer.flip();
        return buffer;
    }

    /**
     * Elimina todos los VAO y VBO.
     */
    public void clean() {
        for (int vao : vaos) GL30.glDeleteVertexArrays(vao);
        for (int vbo : vbos) GL15.glDeleteBuffers(vbo);
        for (int texture : textures) GL11.glDeleteTextures(texture);
    }

}
