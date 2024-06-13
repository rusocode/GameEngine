package render;

import textures.TextureData;
import models.RawModel;
import utils.Utils;

import java.io.*;
import java.nio.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

import de.matthiasmann.twl.utils.PNGDecoder;
import org.lwjgl.opengl.*;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * Esta clase carga modelos 3D en memoria.
 * <h3>VBO</h3>
 * Un buffer en OpenGL es un area de memoria reservada en la GPU que se utiliza para almacenar datos como vertices, colores,
 * normales, coordenadas de textura, elementos de indices, etc. Cada buffer tiene un identificador unico que permite referenciarlo
 * y manipularlo.
 * <p>
 * La funcion {@code glBindBuffer()} toma dos parametros:
 * <ol>
 * <li>{@code target}: Especifica el tipo de buffer que se desea vincular. Los valores comunes son:
 * <ul>
 * <li><b>GL_ARRAY_BUFFER:</b> Para vincular un buffer de vertices (Vertex Buffer Object, VBO)
 * <li><b>GL_ELEMENT_ARRAY_BUFFER:</b> Para vincular un buffer de elementos de indices (Element Buffer Object, EBO)
 * </ul>
 * <li>{@code buffer}: Es el identificador del objeto de buffer que se desea vincular. Si el valor es 0, se desvincularan todos los buffers
 * actualmente vinculados para el target especificado.
 * </ol>
 * Una vez que un buffer esta vinculado, todas las operaciones posteriores relacionadas con ese tipo de buffer afectaran al buffer
 * vinculado. Por ejemplo, si se vincula un VBO (GL_ARRAY_BUFFER) y luego se llama a glBufferData(), los datos se cargaran en ese
 * VBO vinculado.
 * <p>
 * Es importante desvincular el buffer cuando se haya terminado de trabajar con el, vinculando un buffer con un identificador 0.
 * Esto evita que se modifiquen accidentalmente los datos del buffer en operaciones posteriores.
 * <h3>Index Buffer</h3>
 * El renderizado con Index Buffer (buffer de indices) es una tecnica utilizada en graficos por computadora para optimizar la
 * representacion de geometria 3D. Esta tecnica se aplica especialmente al utilizar primitivas graficas como triangulos.
 * <p>
 * En el contexto de OpenGL o Direct3D, el Index Buffer es un tipo de buffer que almacena indices que apuntan a los vertices en un
 * Vertex Buffer Object (VBO). En lugar de enviar cada vertice de forma independiente, se utilizan indices para referenciar a los
 * vertices en el VBO, permitiendo la reutilizacion de vertices comunes entre multiples triangulos.
 * <p>
 * El proceso general del renderizado con Index Buffer implica los siguientes pasos:
 * <ol>
 * <li><b>Creacion de Vertex Buffer Object (VBO):</b>
 * Se almacenan los datos de los vertices, como posiciones, normales y coordenadas de textura, en un buffer de vertices.
 * <li><b>Creacion de Index Buffer:</b>
 * Se crea un buffer de indices que contiene informacion sobre como se conectan los vertices para formar triangulos.
 * <li><b>Envio de Datos a la GPU:</b>
 * Ambos buffers (VBO e Index Buffer) se envian a la GPU.
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

    // Listas para poder administrar la memoria
    private final List<Integer> vaos = new ArrayList<>(), vbos = new ArrayList<>(), textures = new ArrayList<>();

    /**
     * Carga el modelo en el vao.
     *
     * @param position      array de posiciones.
     * @param textureCoords array de coordenadas de texturas.
     * @param normals       array de normales.
     * @param indices       array de indices.
     * @return el modelo sin procesar.
     */
    public RawModel loadToVAO(float[] position, float[] textureCoords, float[] normals, int[] indices) {
        int id = init();
        bindIndicesBuffer(indices);
        storeDataInAttributeList(0, 3, position);
        storeDataInAttributeList(1, 2, textureCoords);
        storeDataInAttributeList(2, 3, normals);
        unbindVao();
        return new RawModel(id, indices.length);
    }

    /**
     * Carga los objetos que estan compuestos solo por vertices, como guis, skybox y water.
     *
     * @param vertices  array de vertices.
     * @param dimension cantidad de componentes.
     * @return el modelo sin procesar.
     */
    public RawModel loadToVAO(float[] vertices, int dimension) {
        int id = init();
        storeDataInAttributeList(0, dimension, vertices);
        unbindVao();
        return new RawModel(id, vertices.length / dimension);
    }

    /**
     * Inicializa un objeto vao y lo vincula al contexto actual de OpenGL.
     *
     * @return el identificador del vao inicializado.
     */
    private int init() {
        // Genera un id para el vao (el primer id que genera es el entero 1)
        int id = glGenVertexArrays();
        vaos.add(id);
        /* Vincula el objeto vao identificado por el id al contexto actual de OpenGL, lo que permite configurar y utilizar los
         * atributos de vertice y los buffers de vertices asociados a ese vao. */
        glBindVertexArray(id);
        return id;
    }

    /**
     * Vincula el buffer de indices.
     *
     * @param indices array de indices.
     */
    private void bindIndicesBuffer(int[] indices) {
        int id = glGenBuffers();
        vbos.add(id);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, id);
        IntBuffer buffer = Utils.storeIndicesInBuffer(indices);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
    }

    /**
     * Almacena los datos del vbo en la lista de atributos del vao.
     *
     * @param attributeNumber numero en la lista de atributos.
     * @param size            numero de componentes por atributo.
     * @param data            array de datos.
     */
    private void storeDataInAttributeList(int attributeNumber, int size, float[] data) {
        // Genera un id para el vbo
        int id = glGenBuffers();
        vbos.add(id);
        // Vincula el vbo de tipo GL_ARRAY_BUFFER
        glBindBuffer(GL_ARRAY_BUFFER, id);
        // Almacena los datos en un buffer
        FloatBuffer buffer = Utils.storeDataInBuffer(data);
        /* Carga el buffer de datos en el vbo. Los datos se cargan desde el objeto buffer y se indican como datos estaticos para
         * que OpenGL sepa que nunca vamos a editar los datos una vez que esten almacenados en el vbo. */
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        /* Coloca el vbo en el vao en una de las listas de atributos. Tomando como ejemplo el atributo 0 (position), lo configura
         * para que use 3 componentes de punto flotante (x, y, z) por vertice, sin normalizacion, con los datos empaquetados uno
         * despues del otro en el buffer, comenzando desde el inicio del buffer. Esta configuracion se aplica si el vbo esta
         * habilitado. */
        glVertexAttribPointer(attributeNumber, size, GL_FLOAT, false, 0, 0);
        // Desvincula el vbo
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    /**
     * Desvincula el vao.
     */
    private void unbindVao() {
        glBindVertexArray(0);
    }

    /**
     * Elimina todos los vaos, vbos y texturas.
     */
    public void clean() {
        /* Elimina los vaos previamente creados. Al hacerlo, se liberan los recursos asociados al objeto, liberando memoria y
         * recursos de la GPU. Es crucial desvincular el vao antes de eliminarlo utilizando glBindVertexArray(0) para evitar
         * posibles problemas. Este mismo enfoque se aplica a los metodos glDeleteBuffers() y glDeleteTextures(). */
        for (int vao : vaos) glDeleteVertexArrays(vao);
        for (int vbo : vbos) glDeleteBuffers(vbo);
        for (int texture : textures) glDeleteTextures(texture);
    }

    public int loadTexture(String fileName) {
        Texture texture = null;
        try {
            texture = TextureLoader.getTexture("PNG", new FileInputStream("res/" + fileName + ".png"));
            glGenerateMipmap(GL_TEXTURE_2D);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_NEAREST);
            glTexParameterf(GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, -0.4f);
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, "No se pudo encontrar la imagen", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error de I/O", "Error", JOptionPane.ERROR_MESSAGE);
        }
        int textureID = texture.getTextureID();
        textures.add(textureID);
        return textureID;
    }

    /**
     * Carga un mapa de cubo.
     * <p>
     * <a href="https://open.gl/textures">Textures objects and parameters</a>
     *
     * @param textureFiles texturas del mapa de cubo.
     * @return el id de la textura del mapa de cubo para vincularlo cuando renderize el skybox.
     */
    public int loadCubeMap(String[] textureFiles) {
        int id = glGenTextures();
        /* La muestra en su sombreador de fragmentos esta vinculada a la unidad de textura 0. Las unidades de textura son
         * referencias a objetos de textura que se pueden muestrear en un sombreador. Las texturas estan vinculadas a unidades de
         * textura usando la funcion glBindTexture que has usado antes. Como no especificaste explicitamente que unidad de textura
         * usar, la textura estaba vinculada a GL_TEXTURE0. Esta funcion especifica a que unidad de textura esta vinculado un
         * objeto de textura cuando se llama a glBindTexture. La cantidad de unidades de textura admitidas varia segun la tarjeta
         * grafica, pero sera al menos 48. Es seguro decir que nunca alcanzara este limite ni siquiera en las aplicaciones de
         * graficos mas extremas. */
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_CUBE_MAP, id);
        for (int i = 0; i < textureFiles.length; i++) {
            TextureData data = decodeTextureFile("res/" + textureFiles[i] + ".png");
            glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGBA, data.getWidth(), data.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, data.getBuffer());
        }
        // Hace que la textura lusza suave
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        textures.add(id);
        return id;
    }

    /**
     * Decodifica un archivo de textura.
     *
     * @param fileName nombre del archivo.
     * @return los datos de la textura.
     */
    private TextureData decodeTextureFile(String fileName) {
        int width = 0, height = 0;
        ByteBuffer buffer = null;
        try {
            FileInputStream in = new FileInputStream(fileName);
            PNGDecoder decoder = new PNGDecoder(in);
            width = decoder.getWidth();
            height = decoder.getHeight();
            buffer = ByteBuffer.allocateDirect(4 * width * height);
            decoder.decode(buffer, width * 4, PNGDecoder.Format.RGBA);
            buffer.flip();
            in.close();
        } catch (Exception e) {
            System.err.println("Tried to load texture " + fileName + ", didn't work");
            System.exit(-1);
        }
        return new TextureData(width, height, buffer);
    }

}
