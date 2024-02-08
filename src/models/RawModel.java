package models;

/**
 * Representa un modelo 3D crudo almacenado en memoria. Cada modelo es un VAO. Las propiedades e informacion de dicho modelo son
 * VBOS. El EBO (Element Array Buffer) no necesita una asignacion de VAO ya que OpenGL lo vincula automaticamente al VAO
 * actualmente vinculado. Al usar DrawElements, OpenGL sabe automaticamente que debe usar EBO.
 * <p>
 * Vamos a renderizar el modelo 3D mas simple, un rectangulo, que esta hecho por dos triangulos y vamos a tomar los datos de
 * posicion de los vertices de esos triangulos creando un VBO almacenando los datos en este. Despues se crea un VAO almacenando los
 * datos del VBO en la lista de atributos del VAO y luego usamos el ID del VAO para que le diga a OpenGL que represente el
 * rectangulo cuando sea necesario.
 */

public class RawModel {

    private final int vaoID;
    private final int vertexCount;

    public RawModel(int vaoID, int vertexCount) {
        this.vaoID = vaoID;
        this.vertexCount = vertexCount;
    }

    public int getVaoID() {
        return vaoID;
    }

    public int getVertexCount() {
        return vertexCount;
    }

}
