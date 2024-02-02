package render;

/**
 * Carga modelos 3D en memoria, almacenando posiciones y datos sobre el modelo en VBO.
 */

public class Loader {

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

    }

    private void storeDataInAttributeList(int attributeNumber, float[] data) {

    }

    private void unbindVAO() {

    }

}
