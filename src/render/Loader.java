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

public class Loader {

    private final List<Integer> vaos = new ArrayList<>(), vbos = new ArrayList<>(), textures = new ArrayList<>();

    /**
     * Carga el modelo en el vao.
     *
     * @param vertices      array de vertices.
     * @param textureCoords array de coordenadas de texturas.
     * @param normals       array de normales.
     * @param indices       array de indices.
     * @return el modelo sin procesar.
     */
    public RawModel loadToVAO(float[] vertices, float[] textureCoords, float[] normals, int[] indices) {
        int vaoID = init();
        bindIndicesBuffer(indices);
        loadDataIntoVAO(0, 3, vertices);
        loadDataIntoVAO(1, 2, textureCoords);
        loadDataIntoVAO(2, 3, normals);
        unbindVao();
        return new RawModel(vaoID, indices.length);
    }

    /**
     * Carga los objetos que estan compuestos solo por vertices, como guis, skybox y water.
     *
     * @param vertices   array de vertices.
     * @param dimensions dimension.
     * @return el modelo sin procesar.
     */
    public RawModel loadToVAO(float[] vertices, int dimensions) {
        int id = init();
        loadDataIntoVAO(0, dimensions, vertices);
        unbindVao();
        return new RawModel(id, vertices.length / dimensions);
    }

    /**
     * Inicializa un objeto vao y lo vincula al contexto actual de OpenGL.
     *
     * @return el identificador del vao inicializado.
     */
    private int init() {
        int id = glGenVertexArrays();
        vaos.add(id);
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
     * Carga los datos del vbo en el vao.
     *
     * @param index indice del atributo del vertice.
     * @param size  numero de componentes por atributo.
     * @param data  array de datos.
     */
    private void loadDataIntoVAO(int index, int size, float[] data) {
        int id = glGenBuffers();
        vbos.add(id);
        glBindBuffer(GL_ARRAY_BUFFER, id);
        FloatBuffer buffer = Utils.storeDataInBuffer(data);
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        glVertexAttribPointer(index, size, GL_FLOAT, false, 0, 0);
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
     * @return el id de la textura del mapa de cubo para que podamos vincularlo cuando rendericemos el skybox.
     */
    public int loadCubeMap(String[] textureFiles) {
        int textureID = glGenTextures();
        /* La muestra en su sombreador de fragmentos esta vinculada a la unidad de textura 0. Las unidades de textura son
         * referencias a objetos de textura que se pueden muestrear en un sombreador. Las texturas estan vinculadas a unidades de
         * textura usando la funcion glBindTexture que has usado antes. Como no especificaste explicitamente que unidad de textura
         * usar, la textura estaba vinculada a GL_TEXTURE0. Esta funcion especifica a que unidad de textura esta vinculado un
         * objeto de textura cuando se llama a glBindTexture. La cantidad de unidades de textura admitidas varia segun la tarjeta
         * grafica, pero sera al menos 48. Es seguro decir que nunca alcanzara este limite ni siquiera en las aplicaciones de
         * graficos mas extremas. */
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_CUBE_MAP, textureID);
        for (int i = 0; i < textureFiles.length; i++) {
            TextureData data = decodeTextureFile("res/" + textureFiles[i] + ".png");
            glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGBA, data.getWidth(), data.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, data.getBuffer());
        }
        // Hace que la textura lusza suave
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        textures.add(textureID);
        return textureID;
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
