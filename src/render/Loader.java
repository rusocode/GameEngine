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
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

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
     * Carga las propiedades del modelo en el VAO.
     *
     * @param positions     posicion de los vertices. TODO Aunque se podria llamar vertex o vertexPosition
     * @param textureCoords coordenadas de texturas.
     * @param indices       indices.
     * @return modelo sin procesar.
     */
    public RawModel loadToVAO(float[] positions, float[] textureCoords, int[] indices) {
        int vaoID = createVAO();
        bindIndicesBuffer(indices);
        storeDataInAttributeList(0, 3, positions);
        storeDataInAttributeList(1, 2, textureCoords);
        unbindVAO();
        return new RawModel(vaoID, indices.length);
    }

    /**
     * Crea y vincula el VAO.
     *
     * @return el identificador del VAO.
     */
    private int createVAO() {
        // La funcion glGenVertexArrays() en OpenGL se utiliza para generar identificadores (IDs) para Vertex Array Objects (VAOs)
        int vaoID = GL30.glGenVertexArrays();
        vaos.add(vaoID);
        /* La funcion glBindVertexArray() toma como argumento el identificador (ID) del VAO que se desea enlazar. Cuando un VAO
         * esta enlazado, cualquier operacion subsiguiente que afecte a los estados del VAO se aplicara a ese VAO en particular. */
        GL30.glBindVertexArray(vaoID);
        return vaoID;
    }

    /**
     * Vincula el bufer de indices.
     *
     * @param indices indices.
     */
    private void bindIndicesBuffer(int[] indices) {
        // La funcion glGenBuffers() en OpenGL se utiliza para generar identificadores (IDs) para Vertex Buffer Objects (VBOs)
        int vboID = GL15.glGenBuffers();
        vbos.add(vboID);
        /* La funcion glBindBuffer() en OpenGL se utiliza para enlazar (bind) un VBO especifico. El primer parametro especifica el
         * tipo de destino al cual se enlazara el VBO. Puede ser GL_ARRAY_BUFFER (para datos de atributos de vertices) o
         * GL_ELEMENT_ARRAY_BUFFER (para indices de elementos en un VAO, a menudo asociado con glDrawElements). El segundo
         * parametro indica el ID del VBO que se va a enlazar. */
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboID);
        IntBuffer buffer = storeIndicesInBuffer(indices);
        /* Carga el bufer de indices en el VBO que esta actualmente enlazado. El tercer parametro especifica como se utilizaran
         * los datos en el VBO. Puede ser GL_STATIC_DRAW (datos que rara vez cambian), GL_DYNAMIC_DRAW (datos que cambian
         * ocasionalmente) o GL_STREAM_DRAW (datos que cambian frecuentemente). */
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
    }

    /**
     * Almacena los datos del VBO en la lista de atributos del VAO.
     *
     * @param index indice del atributo del vertice.
     * @param size  numero de componentes por atributo.
     * @param data  atributos de vertices.
     */
    private void storeDataInAttributeList(int index, int size, float[] data) {
        int vboID = GL15.glGenBuffers();
        vbos.add(vboID);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
        FloatBuffer buffer = storeDataInBuffer(data);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        /* La funcion glVertexAttribPointer() en OpenGL se utiliza para especificar como los datos de los atributos de los
         * vertices estan organizados y almacenados en un VBO. Esta funcion se utiliza tipicamente en conjunto con VAOs para
         * describir la disposicion de los datos de los vertices. El parametro index especifica el indice del atributo del vertice.
         * Corresponde al indice del atributo en el shader de vertices. El parametro size especifica el numero de componentes por
         * atributo (por ejemplo, 3 para coordenadas XYZ). El parametro type especifica el tipo de datos de cada componente (por
         * ejemplo, GL_FLOAT para valores de punto flotante). El parametro normalized especifica si los datos deben ser
         * normalizados antes de ser almacenados en el VBO. El parametro stride especifica el espacio entre los conjuntos de datos
         * consecutivos (en bytes). Si es 0, los datos son adyacentes. El parametro pointer o buffer_buffer_offset especifica un
         * desplazamiento (offset) dentro del VBO donde comienzan los datos del atributo. */
        GL20.glVertexAttribPointer(index, size, GL11.GL_FLOAT, false, 0, 0);
        // Una vez terminado se desvincula el VBO actual
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
        /* La funcion glDeleteVertexArrays() se utiliza para eliminar VAOs previamente generados. Es importante destacar que al
         * eliminar un VAO, se liberan los recursos asociados a este objeto, liberando la memoria y recursos de la GPU. Tambien es
         * recomendable desenlazar el VAO antes de eliminarlo mediante glBindVertexArray(0) para evitar posibles problemas. Esto
         * mismo se aplica para el metodo glDeleteBuffers() y glDeleteTextures(). */
        for (int vao : vaos) GL30.glDeleteVertexArrays(vao);
        for (int vbo : vbos) GL15.glDeleteBuffers(vbo);
        for (int texture : textures) GL11.glDeleteTextures(texture);
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

}
