package render;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.*;

public class DisplayManager {

    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;
    private static final int FPS = 120;

    public static void create() {

        ContextAttribs attribs = new ContextAttribs(3, 2);
        attribs.withForwardCompatible(true); // Compatible con versiones anteriores
        attribs.withProfileCore(true);

        try {
            Display.setDisplayMode(new DisplayMode(WIDTH, HEIGHT));
            Display.create(new PixelFormat(), attribs);
        } catch (LWJGLException e) {
            throw new RuntimeException(e);
        }

        GL11.glViewport(0, 0, WIDTH, HEIGHT);

    }

    public static void update() {
        Display.sync(FPS);
        Display.update();
    }

    public static void close() {
        Display.destroy();
    }
}
