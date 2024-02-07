package shaders;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

/**
 * GLSL, que significa "OpenGL Shading Language", es un lenguaje de programacion de alto nivel utilizado para escribir shaders
 * (programas especializados para procesamiento grafico) en aplicaciones que utilizan OpenGL, como videojuegos y aplicaciones
 * graficas interactivas. OpenGL es una interfaz de programacion de graficos tridimensionales (3D) ampliamente utilizada.
 * <p>
 * Aqui hay algunos puntos clave sobre GLSL:
 * <ol>
 * <li><b>Shaders:</b>
 * <p>
 * GLSL se utiliza principalmente para escribir shaders, que son programas pequeños que se ejecutan en la tarjeta grafica (GPU) y
 * estan diseñados para realizar operaciones especificas relacionadas con el renderizado de graficos.
 * </li>
 * <li><b>Tipos de Shaders:</b>
 * <ul>
 * <li><i>Vertex Shaders:</i> Procesan vertices de objetos 3D.
 * <li><i>Fragment Shaders:</i> Calculan el color final de un pixel en la pantalla.
 * <li><i>Geometry Shaders</i>: Procesan geometria entre vertices.
 * <li><i>Compute Shaders:</i> Realizan calculos de proposito general en la GPU.
 * </ul>
 * </li>
 * <li><b>Caracteristicas:</b>
 * <p>
 * GLSL proporciona caracteristicas como tipos de datos, operadores, funciones matematicas y constructores de flujo de control que
 * permiten a los programadores controlar la apariencia visual de los objetos renderizados en un entorno OpenGL.
 * </li>
 * <li><b>Desarrollo Grafico:</b>
 * <p>
 * Es esencial para el desarrollo grafico avanzado en tiempo real, como efectos visuales, sombras, iluminacion y otros aspectos
 * del renderizado tridimensional.
 * </li>
 * <li><b>Sintaxis Similar a C:</b>
 * <p>
 * La sintaxis de GLSL esta inspirada en el lenguaje de programacion C, lo que facilita su comprension para aquellos familiarizados
 * con C, C++, u otros lenguajes de estilo similar.
 * </li>
 * </ol>
 * Un ejemplo simple de un fragment shader en GLSL podria ser:
 * <pre>{@code
 * #version 400 core
 *
 * out vec4 FragColor;
 *
 * void main() {
 *     FragColor = vec4(1.0, 0.5, 0.2, 1.0);
 * }
 * }</pre>
 * Este shader establece que cada pixel en la pantalla tendra un color naranja constante.
 */

public abstract class ShaderProgram {

    private final int programID;
    private final int vertexShaderID;
    private final int fragmentShaderID;

    public ShaderProgram(String vertexFile, String fragmentFile) {
        vertexShaderID = loadShader(vertexFile, GL20.GL_VERTEX_SHADER);
        fragmentShaderID = loadShader(fragmentFile, GL20.GL_FRAGMENT_SHADER);
        // Une los dos shaders
        programID = GL20.glCreateProgram();
        GL20.glAttachShader(programID, vertexShaderID);
        GL20.glAttachShader(programID, fragmentShaderID);
        GL20.glLinkProgram(programID);
        GL20.glValidateProgram(programID);
        bindAttributes();
    }

    public void start() {
        GL20.glUseProgram(programID);
    }

    public void stop() {
        GL20.glUseProgram(0);
    }

    public void clean() {
        stop();
        GL20.glDetachShader(programID, vertexShaderID);
        GL20.glDetachShader(programID, fragmentShaderID);
        GL20.glDeleteShader(vertexShaderID);
        GL20.glDeleteShader(fragmentShaderID);
        GL20.glDeleteProgram(programID);
    }

    /**
     * Vincula el atributo.
     *
     * @param attribute    el numero de la lista de atributos en el VAO.
     * @param variableName nombre de la variable en el codigo shader.
     */
    protected void bindAttribute(int attribute, String variableName) {
        GL20.glBindAttribLocation(programID, attribute, variableName);
    }

    protected abstract void bindAttributes();

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

        int shaderID = GL20.glCreateShader(type);
        GL20.glShaderSource(shaderID, shaderSource);
        GL20.glCompileShader(shaderID);
        if (GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            System.out.println(GL20.glGetShaderInfoLog(shaderID, 500));
            System.err.println("Could not compile shader.");
            System.exit(-1);
        }

        return shaderID;
    }

}
