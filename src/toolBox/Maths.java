package toolBox;

import entities.Camera;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

/**
 * En LWJGL (Lightweight Java Game Library), la clase Matrix4f es parte de la biblioteca de algebra lineal que proporciona
 * funciones para trabajar con matrices 4x4. LWJGL es una biblioteca de enlace nativo de Java para OpenGL, OpenAL, y otras
 * bibliotecas graficas y de audio.
 * <p>
 * La clase Matrix4f se utiliza para representar y manipular matrices de transformacion en graficos 3D. Estas matrices se utilizan
 * comunmente para aplicar operaciones de transformacion, como traslacion, rotacion y escalado, a objetos en un espacio tridimensional.
 */

public class Maths {

    public static Matrix4f createTransformationMatrix(Vector3f translation, float rx, float ry, float rz, Vector3f scale) {
        // Crear una matriz de identidad
        Matrix4f matrix = new Matrix4f();
        matrix.setIdentity();
        // Aplica una traslacion a la matriz
        Matrix4f.translate(translation, matrix, matrix);
        // Aplica una rotacion a la matriz para cada eje
        Matrix4f.rotate((float) Math.toRadians(rx), new Vector3f(1, 0, 0), matrix, matrix); // El metodo toRadians convierte un angulo medido en grados en un angulo aproximadamente equivalente medido en radianes
        Matrix4f.rotate((float) Math.toRadians(ry), new Vector3f(0, 1, 0), matrix, matrix);
        Matrix4f.rotate((float) Math.toRadians(rz), new Vector3f(0, 0, 1), matrix, matrix);
        // Aplica una escala a la matriz
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
