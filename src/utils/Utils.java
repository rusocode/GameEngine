package utils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;

public final class Utils {

    private Utils() {
    }

    /**
     * Almacena los datos en un buffer de floats.
     *
     * @param data array de datos.
     * @return un buffer de datos.
     */
    public static FloatBuffer storeDataInBuffer(float[] data) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
        buffer.put(data);
        buffer.flip();
        return buffer;
    }

    /**
     * Almacena los indices en un buffer de enteros.
     *
     * @param indices array de indices.
     * @return un buffer de indices.
     */
    public static IntBuffer storeIndicesInBuffer(int[] indices) {
        IntBuffer buffer = BufferUtils.createIntBuffer(indices.length);
        buffer.put(indices);
        buffer.flip();
        return buffer;
    }

}
