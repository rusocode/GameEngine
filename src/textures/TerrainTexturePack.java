package textures;

/**
 * Pack de texturas para combinar en el terreno.
 */

public class TerrainTexturePack {

    // Estas texturas estan relacionadas con el blendMap
    private final TerrainTexture background, r, g, b;

    public TerrainTexturePack(TerrainTexture background, TerrainTexture r, TerrainTexture g, TerrainTexture b) {
        this.background = background;
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public TerrainTexture getBackground() {
        return background;
    }

    public TerrainTexture getR() {
        return r;
    }

    public TerrainTexture getG() {
        return g;
    }

    public TerrainTexture getB() {
        return b;
    }

}
