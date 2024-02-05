package render;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Carga modelos 3D en memoria, almacenando posiciones y datos sobre el modelo en VBO.
 */

public class Loader {

    // Crea una lista de seguimientos de los VAO y VBO
    private List<Integer> vaos = new ArrayList<>();
    private List<Integer> vbos = new ArrayList<>();

    /**
     * Obtiene las posiciones de los vertices y los carga en un VAO.
     *
     * @param positions posiciones de los vertices.
     * @return la informacion sobre el VAO como modelo sin procesar.
     */
    public RawModel loadToVAO(float[] positions) {
        int vaoID = createVAO();
        storeDataInAttributeList(0, positions);
        unbindVAO();
        // Divide el numero de vertices del modelo por tres ya que cada vertice tiene tres floats (x, y, z)
        return new RawModel(vaoID, positions.length / 3);
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
     * Almacena los datos en un VAO.
     *
     * @param attributeNumber numero de atributo.
     * @param data            datos.
     */
    private void storeDataInAttributeList(int attributeNumber, float[] data) {
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
        GL20.glVertexAttribPointer(attributeNumber, 3, GL11.GL_FLOAT, false, 0, 0);
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
     * Elimina todos los VAO y VBO.
     */
    public void clean() {
        for (int vao : vaos) GL30.glDeleteVertexArrays(vao);
        for (int vbo : vbos) GL15.glDeleteBuffers(vbo);
    }

}
