package textures;

import java.nio.ByteBuffer;

/**
 * Datos de textura.
 */

public class TextureData {

    private final int width, height;
    private final ByteBuffer buffer; // Bytes decodificados de una imagen

    public TextureData(int width, int height, ByteBuffer buffer) {
        this.width = width;
        this.height = height;
        this.buffer = buffer;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

}