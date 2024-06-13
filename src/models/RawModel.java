package models;

/**
 * En el ambito del diseño tridimensional, un "modelo en bruto" o RawModel se refiere a una estructura de datos que alberga
 * informacion cruda acerca de un modelo 3D. Este termino sugiere la presencia de datos fundamentales sin procesar que describen
 * la geometria y caracteristicas del modelo, antes de cualquier transformacion o manipulacion. En este contexto, el "modelo en
 * bruto" sirve como la base inicial desde la cual se puede construir y elaborar un objeto tridimensional mas complejo.
 * <p>
 * Un modelo 3D tipicamente consiste en informacion sobre vertices, normales, coordenadas de textura y, en algunos casos, colores.
 * Cuando trabajas con OpenGL, generalmente envias estos datos a la GPU para su procesamiento y renderizado eficiente.
 * <p>
 * Un rectangulo en OpenGL, es el modelo 3D mas simple compuesto por dos triangulos cuyas posiciones de vertices se almacenan en
 * un Vertex Buffer Object (VBO). Luego, se crea un Vertex Array Object (VAO) que organiza estos datos y se utiliza su ID para
 * indicar a OpenGL como representar el rectangulo cuando sea necesario.
 */

public class RawModel {

    private final int id, vertexCount;

    public RawModel(int id, int vertexCount) {
        this.id = id; // id del vao
        this.vertexCount = vertexCount; // Cantidad de vertices que hay en el modelo
    }

    public int getID() {
        return id;
    }

    public int getVertexCount() {
        return vertexCount;
    }

}
