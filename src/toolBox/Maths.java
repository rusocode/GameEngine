package toolBox;

import entities.Camera;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector2f;

/**
 * En LWJGL (Lightweight Java Game Library), la clase Matrix4f es parte de la biblioteca de algebra lineal que proporciona
 * funciones para trabajar con matrices 4x4. LWJGL es una biblioteca de enlace nativo de Java para OpenGL, OpenAL, y otras
 * bibliotecas graficas y de audio.
 * <p>
 * La clase Matrix4f se utiliza para representar y manipular matrices de transformacion en graficos 3D. Estas matrices se utilizan
 * comunmente para aplicar operaciones de transformacion, como traslacion, rotacion y escalado, a objetos en un espacio tridimensional.
 */

public class Maths {

    /**
     * Este metodo toma tres vectores 3D que son los tres puntos del triangulo, y tambien un vector 2D que es la coordenada (x,z)
     * del player.
     *
     * @return la altura del triangulo en la posicion del player.
     */
    public static float barryCentric(Vector3f p1, Vector3f p2, Vector3f p3, Vector2f pos) {
        float det = (p2.z - p3.z) * (p1.x - p3.x) + (p3.x - p2.x) * (p1.z - p3.z);
        float l1 = ((p2.z - p3.z) * (pos.x - p3.x) + (p3.x - p2.x) * (pos.y - p3.z)) / det;
        float l2 = ((p3.z - p1.z) * (pos.x - p3.x) + (p1.x - p3.x) * (pos.y - p3.z)) / det;
        float l3 = 1.0f - l1 - l2;
        return l1 * p1.y + l2 * p2.y + l3 * p3.y;
    }

    public static Matrix4f createTransformationMatrix(Vector2f translation, Vector2f scale) {
        Matrix4f matrix = new Matrix4f();
        matrix.setIdentity();
        Matrix4f.translate(translation, matrix, matrix);
        Matrix4f.scale(new Vector3f(scale.x, scale.y, 1f), matrix, matrix);
        return matrix;
    }

    public static Matrix4f createTransformationMatrix(Vector3f translation, Vector3f angle, Vector3f scale) {
        // Crear una matriz de identidad
        Matrix4f matrix = new Matrix4f();
        matrix.setIdentity();
        // Aplica la traslacion a la matriz
        Matrix4f.translate(translation, matrix, matrix);
        // Aplica una rotacion a la matriz para cada eje
        Matrix4f.rotate((float) Math.toRadians(angle.x), new Vector3f(1, 0, 0), matrix, matrix); // El metodo toRadians convierte un angulo medido en grados en un angulo aproximadamente equivalente medido en radianes
        Matrix4f.rotate((float) Math.toRadians(angle.y), new Vector3f(0, 1, 0), matrix, matrix);
        Matrix4f.rotate((float) Math.toRadians(angle.z), new Vector3f(0, 0, 1), matrix, matrix);
        // Aplica la escala a la matriz
        Matrix4f.scale(scale, matrix, matrix);
        return matrix;
    }

    public static Matrix4f createViewMatrix(Camera camera) {
        Matrix4f matrix = new Matrix4f();
        matrix.setIdentity();
        Matrix4f.rotate((float) Math.toRadians(camera.getPitch()), new Vector3f(1, 0, 0), matrix, matrix);
        Matrix4f.rotate((float) Math.toRadians(camera.getYaw()), new Vector3f(0, 1, 0), matrix, matrix);
        Vector3f cameraPos = camera.getPosition();
        Vector3f negativeCameraPos = new Vector3f(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        Matrix4f.translate(negativeCameraPos, matrix, matrix);
        return matrix;
    }

}
