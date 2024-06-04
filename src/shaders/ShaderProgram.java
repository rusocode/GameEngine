package shaders;

import javax.swing.*;
import java.io.*;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

public abstract class ShaderProgram {

    private final int programID, vertexShaderID, fragmentShaderID;

    private static final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

    public ShaderProgram(String vertexFile, String fragmentFile) {
        vertexShaderID = loadShader(vertexFile, GL_VERTEX_SHADER);
        fragmentShaderID = loadShader(fragmentFile, GL_FRAGMENT_SHADER);
        programID = glCreateProgram();
        glAttachShader(programID, vertexShaderID);
        glAttachShader(programID, fragmentShaderID);
        bindAttributes();
        glLinkProgram(programID);
        glValidateProgram(programID);
        getAllUniformLocations();
    }

    protected abstract void bindAttributes();

    protected abstract void getAllUniformLocations();

    /**
     * Vincula el atributo.
     * <p>
     * Asigna un indice de ubicacion de atributo a un nombre especifico en un programa de shader. Los atributos, variables en
     * shaders, especialmente en el Vertex Shader, reciben datos de vertices durante la renderizacion. Util cuando se quiere
     * establecer manualmente ubicaciones de atributos en lugar de depender de la eleccion automatica de OpenGL, la vinculacion de
     * atributos es esencial para hacer coincidir atributos en el codigo del shader con datos de vertices proporcionados por la
     * aplicacion.
     *
     * @param index indice de ubicacion del atributo que se esta asociando (el numero de la lista de atributos en el VAO).
     * @param name  nombre del atributo que se asocia al indice de ubicacion en el programa (nombre de la variable en el codigo shader).
     */
    protected void bindAttribute(int index, String name) {
        glBindAttribLocation(programID, index, name);
    }

    protected int getUniformLocation(String name) {
        return glGetUniformLocation(programID, name);
    }

    /**
     * Carga el valor de tipo int a la variable uniforme del shader.
     *
     * @param location ubicacion de la variable uniforme.
     * @param value    valor que se va a asignar a la variable uniforme.
     */
    protected void loadInt(int location, int value) {
        glUniform1i(location, value);
    }

    /**
     * Carga el valor de tipo float a la variable uniforme del shader. El "1f" en el nombre de la funcion señala que se asigna un
     * solo valor de tipo float.
     *
     * @param location ubicacion de la variable uniforme.
     * @param value    valor que se va a asignar a la variable uniforme.
     */
    protected void loadFloat(int location, float value) {
        glUniform1f(location, value);
    }

    protected void loadVector(int location, Vector3f vector) {
        glUniform3f(location, vector.x, vector.y, vector.z);
    }

    protected void loadVector(int location, Vector4f vector) {
        glUniform4f(location, vector.x, vector.y, vector.z, vector.w);
    }

    // TODO Mejorar los nombres para cada metodo
    protected void load2DVector(int location, Vector2f vector) {
        glUniform2f(location, vector.x, vector.y);
    }

    protected void loadBoolean(int location, boolean value) {
        float toLoad = 0;
        if (value) toLoad = 1;
        glUniform1f(location, toLoad);
    }

    public void loadMatrix(int location, Matrix4f matrix) {
        matrix.store(matrixBuffer);
        matrixBuffer.flip();
        glUniformMatrix4(location, false, matrixBuffer);
    }

    public void start() {
        glUseProgram(programID);
    }

    public void stop() {
        glUseProgram(0);
    }

    public void clean() {
        stop();
        glDetachShader(programID, vertexShaderID);
        glDetachShader(programID, fragmentShaderID);
        glDeleteShader(vertexShaderID);
        glDeleteShader(fragmentShaderID);
        glDeleteProgram(programID);
    }

    /**
     * Carga el shader.
     *
     * @param file archivo del shader.
     * @param type tipo de shader.
     * @return el identificador del shader
     */
    private static int loadShader(String file, int type) {
        StringBuilder shaderSource = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null)
                shaderSource.append(line).append("\n");

            reader.close();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error de I/O", "Error", JOptionPane.ERROR_MESSAGE);
        }

        int shaderID = glCreateShader(type);
        glShaderSource(shaderID, shaderSource);
        glCompileShader(shaderID);
        if (glGetShaderi(shaderID, GL_COMPILE_STATUS) == GL_FALSE) {
            System.out.println(glGetShaderInfoLog(shaderID, 500));
            System.err.println("Could not compile shader!");
            System.exit(-1);
        }

        return shaderID;
    }

}
