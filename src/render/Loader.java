package render;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import models.RawModel;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

/**
 * Carga modelos 3D en memoria.
 * <p>
 * El renderizado con Index Buffer (bufer de indices) es una tecnica utilizada en graficos por computadora para optimizar la
 * representacion de geometria 3D. Esta tecnica se aplica especialmente al utilizar primitivas graficas como triangulos.
 * <p>
 * En el contexto de OpenGL o Direct3D, el Index Buffer es un tipo de bufer que almacena indices que apuntan a los vertices en un
 * Vertex Buffer Object (VBO). En lugar de enviar cada vertice de forma independiente, se utilizan indices para referenciar a los
 * vertices en el VBO, permitiendo la reutilizacion de vertices comunes entre multiples triangulos.
 * <p>
 * El proceso general del renderizado con Index Buffer implica los siguientes pasos:
 * <ol>
 * <li><b>Creacion de Vertex Buffer Object (VBO):</b>
 * Se almacenan los datos de los vertices, como posiciones, normales y coordenadas de textura, en un bufer de vertices.
 * <li><b>Creacion de Index Buffer:</b>
 * Se crea un bufer de indices que contiene informacion sobre como se conectan los vertices para formar triangulos.
 * <li><b>Envio de Datos a la GPU:</b>
 * Ambos buferes (VBO e Index Buffer) se envian a la GPU.
 * <li><b>Proceso de Renderizado:</b>
 * Durante el proceso de renderizado, la GPU utiliza los datos del Index Buffer para determinar como los vertices deben ser
 * ensamblados en triangulos.
 * <li><b>Optimizacion de Uso de Memoria:</b>
 * La principal ventaja del uso de Index Buffer es la optimizacion del uso de memoria, ya que permite la reutilizacion de vertices
 * compartidos por multiples triangulos, reduciendo asi la cantidad de datos que se deben enviar y almacenar.
 * </ol>
 * Esta tecnica es eficaz para modelos 3D con geometria repetitiva, ya que reduce la redundancia en los datos de vertices y mejora
 * la eficiencia en la representacion de modelos complejos.
 */

public class Loader {

    // Lista de seguimiento de los VAO y VBO para poder eliminarlos cuando se cierre el juego
    private final List<Integer> vaos = new ArrayList<>();
    private final List<Integer> vbos = new ArrayList<>();
    private final List<Integer> textures = new ArrayList<>();

    /**
     * Carga el modelo en el VAO.
     *
     * @param positions     posicion de los vertices. TODO Aunque se podria llamar vertex o vertexPosition
     * @param textureCoords coordenadas de texturas.
     * @param indices       indices.
     * @return el modelo sin procesar.
     */
    public RawModel loadToVAO(float[] positions, float[] textureCoords, float[] normals, int[] indices) {
        int vaoID = createVAO();
        bindIndicesBuffer(indices);
        storeDataInAttributeList(0, 3, positions);
        storeDataInAttributeList(1, 2, textureCoords);
        storeDataInAttributeList(2, 3, normals);
        unbindVAO();
        return new RawModel(vaoID, indices.length);
    }

    /**
     * Crea y vincula el VAO.
     *
     * @return el identificador del VAO.
     */
    private int createVAO() {
        // Genera un identificador para el VAO
        int vaoID = GL30.glGenVertexArrays();
        vaos.add(vaoID);
        // Enlaza el identificador del VAO
        GL30.glBindVertexArray(vaoID);
        return vaoID;
    }

    /**
     * Vincula el bufer de indices.
     *
     * @param indices indices.
     */
    private void bindIndicesBuffer(int[] indices) {
        // Genera un identificador para el VBO
        int vboID = GL15.glGenBuffers();
        vbos.add(vboID);
        /* Enlaza el VBO especifico. El primer parametro determina el tipo de destino al cual se vinculara el VBO, pudiendo ser
         * GL_ARRAY_BUFFER (para datos de atributos de vertices) o GL_ELEMENT_ARRAY_BUFFER (para indices de elementos en un VAO,
         * generalmente asociado con glDrawElements). */
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboID);
        IntBuffer buffer = storeIndicesInBuffer(indices);
        /* Carga el bufer de indices en el VBO actualmente enlazado. El tercer parametro especifica el uso previsto de los datos
         * en el VBO, pudiendo ser GL_STATIC_DRAW (datos que rara vez cambian), GL_DYNAMIC_DRAW (datos que cambian ocasionalmente)
         * o GL_STREAM_DRAW (datos que cambian frecuentemente). */
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
    }

    /**
     * Almacena los datos del VBO en la lista de atributos del VAO.
     *
     * @param index indice del atributo del vertice.
     * @param size  numero de componentes por atributo.
     * @param data  datos del vertice.
     */
    private void storeDataInAttributeList(int index, int size, float[] data) {
        int vboID = GL15.glGenBuffers();
        vbos.add(vboID);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
        FloatBuffer buffer = storeDataInBuffer(data);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        /* Define la organizacion y almacenamiento de los datos de los atributos de los vertices en un VBO. Se usa comunmente con
         * VAOs para describir la disposicion de los datos de los vertices. El parametro `index` indica el indice del atributo del
         * vertice, correspondiendo al indice en el shader de vertices. El parametro `size` especifica el numero de componentes
         * por atributo (por ejemplo, 3 para coordenadas XYZ). El parametro `type` indica el tipo de datos de cada componente (por
         * ejemplo, GL_FLOAT para valores de punto flotante). El parametro `normalized` determina si los datos deben normalizarse
         * antes de almacenarse en el VBO. El parametro `stride` establece el espacio entre conjuntos consecutivos de datos (en
         * bytes); si es 0, los datos son adyacentes. Finalmente, el parametro `pointer` o `buffer_offset` indica un
         * desplazamiento (offset) dentro del VBO donde comienzan los datos del atributo.  */
        GL20.glVertexAttribPointer(index, size, GL11.GL_FLOAT, false, 0, 0);
        // Desvincula el VBO actual
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    /**
     * Almacena los datos en un bufer.
     *
     * @param data datos.
     * @return bufer de datos.
     */
    private FloatBuffer storeDataInBuffer(float[] data) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
        buffer.put(data);
        buffer.flip();
        return buffer;
    }

    /**
     * Almacena los indices en un bufer.
     *
     * @param indices indices.
     * @return bufer de indices.
     */
    private IntBuffer storeIndicesInBuffer(int[] indices) {
        IntBuffer buffer = BufferUtils.createIntBuffer(indices.length);
        buffer.put(indices);
        buffer.flip();
        return buffer;
    }

    /**
     * Desvincula el VAO.
     */
    private void unbindVAO() {
        GL30.glBindVertexArray(0);
    }

    /**
     * Elimina todos los VAO, VBO y texturas.
     */
    public void clean() {
        /* Elimina los VAOs previamente creados. Al hacerlo, se liberan los recursos asociados al objeto, liberando memoria y
         * recursos de la GPU. Es crucial desvincular el VAO antes de eliminarlo utilizando glBindVertexArray(0) para evitar
         * posibles problemas. Este mismo enfoque se aplica a los metodos glDeleteBuffers() y glDeleteTextures(). */
        for (int vao : vaos) GL30.glDeleteVertexArrays(vao);
        for (int vbo : vbos) GL15.glDeleteBuffers(vbo);
        for (int texture : textures) GL11.glDeleteTextures(texture);
    }

    public int loadTexture(String fileName) {
        Texture texture = null;
        try {
            texture = TextureLoader.getTexture("PNG", new FileInputStream("res/" + fileName + ".png"));
            GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_NEAREST);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, -0.4f);
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, "No se pudo encontrar la imagen", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error de I/O", "Error", JOptionPane.ERROR_MESSAGE);
        }
        int textureID = texture.getTextureID();
        textures.add(textureID);
        return textureID;
    }

}
