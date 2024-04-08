package water;

public class WaterTile {

    public static final float TILE_SIZE = 60;

    private final float x, z, height;

    public WaterTile(float x, float z, float height) {
        this.x = x;
        this.z = z;
        this.height = height;
    }

    public float getHeight() {
        return height;
    }

    public float getX() {
        return x;
    }

    public float getZ() {
        return z;
    }

}
