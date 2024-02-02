package tester;

import org.lwjgl.opengl.Display;
import render.DisplayManager;

public class GameLoop {

    public static void main(String[] args) {
        DisplayManager.create();
        while (!Display.isCloseRequested()) {
            DisplayManager.update();
        }
        DisplayManager.close();
    }

}
