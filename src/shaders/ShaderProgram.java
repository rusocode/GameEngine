package shaders;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

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

    private static final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

    public ShaderProgram(String vertexFile, String fragmentFile) {
        vertexShaderID = loadShader(vertexFile, GL20.GL_VERTEX_SHADER);
        fragmentShaderID = loadShader(fragmentFile, GL20.GL_FRAGMENT_SHADER);
        /* La funcion glCreateProgram() se utiliza para crear un nuevo objeto de programa de shader. En OpenGL, los programas de
         * shader son programas escritos en lenguajes como GLSL (OpenGL Shading Language) que se ejecutan en la unidad de
         * procesamiento de shaders de la tarjeta grafica.
         * Un programa de shader en OpenGL esta compuesto por al menos un fragment shader y un vertex shader. El fragment shader
         * se encarga de calcular el color de cada pixel en una pantalla, mientras que el vertex shader se encarga de calcular la
         * posicion de los vertices en el espacio 3D.
         * La funcion glCreateProgram() devuelve un identificador unico para el programa de shader recien creado. Posteriormente,
         * los shaders (tanto el fragment shader como el vertex shader) se adjuntan al programa utilizando funciones como
         * glAttachShader(). Despues de adjuntar los shaders, se debe enlazar el programa utilizando glLinkProgram().
         * En resumen, glCreateProgram() se utiliza para inicializar y obtener un identificador para un nuevo programa de shader
         * en OpenGL, que luego se completa adjuntando shaders y enlazando el programa antes de su uso en el pipeline de graficos. */
        programID = GL20.glCreateProgram();

        /* La funcion glAttachShader() en OpenGL se utiliza para adjuntar un objeto de shader a un programa de shader. Un programa
         * de shader en OpenGL esta compuesto por al menos un shader de vertices y un shader de fragmentos, y posiblemente tambien
         * por otros tipos de shaders como shaders de geometria o de teselacion.
         * La funcion glAttachShader() toma como parametros el identificador del programa al que se desea adjuntar el shader y el
         * identificador del shader que se desea adjuntar. Especificamente, se utiliza de la siguiente manera: */
        GL20.glAttachShader(programID, vertexShaderID);
        GL20.glAttachShader(programID, fragmentShaderID);
        // Vincula los atributos antes de vincular el programa
        bindAttributes();
        /* La funcion glLinkProgram() en OpenGL se utiliza para enlazar los shaders adjuntos a un programa de shader. Despues de
         * adjuntar shaders individuales al programa con glAttachShader(), glLinkProgram() realiza la vinculacion final para crear
         * un programa de shader completo y listo para su uso en el pipeline de graficos. */
        GL20.glLinkProgram(programID);
        /* La funcion glValidateProgram() en OpenGL se utiliza para validar un programa de shader despues de que haya sido
         * enlazado con glLinkProgram(). La validacion proporciona informacion sobre la compatibilidad entre los shaders en el
         * programa y si estan configurados de manera coherente para ejecutarse correctamente en el hardware de la tarjeta grafica. */
        GL20.glValidateProgram(programID);
        getAllUniformLocations();
    }

    protected abstract void getAllUniformLocations();

    protected int getUniformLocation(String uniformName) {
        /* La funcion glGetUniformLocation() se utiliza para obtener la ubicacion de una variable uniforme en un programa de
         * shader. Las variables uniformes son variables que permanecen constantes durante la ejecucion de un shader y se utilizan
         * para pasar datos desde la aplicacion de OpenGL al shader.
         * La funcion glGetUniformLocation() toma como parametros el identificador del programa de shader y el nombre de la
         * variable uniforme cuya ubicacion deseas obtener. La funcion devuelve un entero que representa la ubicacion de la
         * variable uniforme en el programa de shader.
         * La ubicacion devuelta por glGetUniformLocation() se utiliza luego para establecer el valor de la variable uniforme
         * utilizando funciones como glUniform1f(), glUniformMatrix4fv(), etc. */
        return GL20.glGetUniformLocation(programID, uniformName);
    }

    protected void loadVector(int location, Vector3f vector) {
        GL20.glUniform3f(location, vector.x, vector.y, vector.z);
    }

    protected void loadBoolean(int location, boolean value) {
        float toLoad = 0;
        if (value) toLoad = 1;
        GL20.glUniform1f(location, toLoad);
    }

    protected void loadFloat(int location, float value) {
        GL20.glUniform1f(location, value);
    }

    /**
     * Carga la matriz en el codigo del shader.
     */
    public void loadMatrix(int location, Matrix4f matrix) {
        matrix.store(matrixBuffer);
        matrixBuffer.flip();
        // Establece el valor de una variable uniforme de tipo matriz 4x4 en un programa de shader
        GL20.glUniformMatrix4(location, false, matrixBuffer);
    }

    public void start() {
        /* La funcion glUseProgram() en OpenGL se utiliza para activar un programa de shader especifico en el pipeline de graficos.
         * Un programa de shader en OpenGL esta compuesto por shaders individuales, como el vertex shader y el fragment shader, que
         * definen como se procesan los vertices y fragmentos en la GPU.
         * Durante la activacion del programa de shader, los shaders adjuntos se utilizaran para procesar los vertices, fragmentos
         * u otras etapas del pipeline de graficos, segun la configuracion de los shaders. Despues de la renderizacion, puedes
         * optar por desactivar el programa llamando a glUseProgram(0) para volver al estado predeterminado sin programa de shader
         * activo. La funcion glUseProgram() es esencial para cambiar entre diferentes programas de shader durante la
         * renderizacion y aplicar efectos visuales especificos. */
        GL20.glUseProgram(programID);
    }

    public void stop() {
        GL20.glUseProgram(0);
    }

    public void clean() {
        stop();
        /* La funcion glDetachShader() se utiliza para desvincular un shader de un programa de shader. Un programa de shader en
         * OpenGL esta compuesto por uno o mas shaders (vertex shader, fragment shader, etc.), y glDetachShader() permite
         * eliminar la conexion entre un shader especifico y el programa, sin destruir el shader en si. */
        GL20.glDetachShader(programID, vertexShaderID);
        GL20.glDetachShader(programID, fragmentShaderID);
        /* La funcion glDeleteShader() se utiliza para eliminar un objeto de shader previamente creado con glCreateShader(). Los
         * objetos de shader son creados para contener y representar el codigo fuente de shaders, como vertex shaders o fragment
         * shaders.
         * La funcion glDeleteShader() toma como parametro el identificador del shader que se desea eliminar y libera los recursos
         * asociados con ese shader. Es importante tener en cuenta que esta funcion no elimina el programa de shader completo;
         * simplemente elimina el objeto de shader individual. */
        GL20.glDeleteShader(vertexShaderID);
        GL20.glDeleteShader(fragmentShaderID);
        /* La funcion glDeleteProgram() se utiliza para eliminar un programa de shader. Un programa de shader en OpenGL esta
         * compuesto por shaders individuales (vertex shader, fragment shader, etc.) que se han adjuntado, compilado y enlazado
         * juntos.
         * Es importante tener en cuenta que glDeleteProgram() elimina el programa de shader, pero no afecta a los shaders
         * individuales que se adjuntaron al programa. Para eliminar un shader individual, se utiliza glDeleteShader(). Ademas, es
         * recomendable liberar los programas de shader cuando ya no se necesiten para evitar posibles perdidas de memoria. */
        GL20.glDeleteProgram(programID);
    }

    /**
     * Vincula el atributo.
     *
     * @param index indice de ubicacion del atributo que se esta asociando (el numero de la lista de atributos en el VAO).
     * @param name  nombre del atributo que se asocia al indice de ubicacion en el programa (nombre de la variable en el codigo shader).
     */
    protected void bindAttribute(int index, String name) {
        /* La funcion glBindAttribLocation() en OpenGL se utiliza para asociar un indice de ubicacion de atributo a un nombre de
         * atributo especifico en un programa de shader. Los atributos son variables en los shaders, generalmente en el vertex
         * shader, que reciben datos de los vertices durante la renderizacion.
         * Esta funcion es util cuando deseas establecer manualmente las ubicaciones de atributos en lugar de dejar que OpenGL las
         * elija automaticamente. La vinculacion de atributos es importante para establecer la correspondencia entre los atributos
         * en el codigo del shader y los datos de los vertices proporcionados por tu aplicacion.
         * Despues de llamar a glBindAttribLocation(), deberias enlazar el programa con glLinkProgram() para que la asociacion
         * tenga efecto. */
        GL20.glBindAttribLocation(programID, index, name); // Asocia el nombre especificado al indice de ubicacion especificado
    }

    protected abstract void bindAttributes();

    /**
     * Carga el shader.
     *
     * @param file archivo del shader.
     * @param type tipo de shader.
     * @return identificador del shader
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

        // Devuelve un identificador unico para el objeto de shader creado
        int shaderID = GL20.glCreateShader(type);
        /* La funcion glShaderSource() se utiliza para cargar el codigo fuente de un shader en un objeto de shader creado
         * previamente con glCreateShader(). Esta funcion establece el codigo fuente del shader, permitiendo que OpenGL compile
         * ese codigo en el objeto de shader correspondiente. */
        GL20.glShaderSource(shaderID, shaderSource);
        /* La funcion glCompileShader() se utiliza para compilar el codigo fuente cargado previamente en un objeto de shader
         * creado con glCreateShader(). Esta funcion toma el identificador del shader como parametro y compila el codigo fuente
         * asociado a ese shader.
         * Despues de compilar el shader, es recomendable verificar si hubo errores de compilacion utilizando glGetShaderiv() y
         * glGetShaderInfoLog(). La informacion de registro (log) proporciona detalles sobre cualquier problema que haya surgido
         * durante la compilacion del shader. */
        GL20.glCompileShader(shaderID);
        if (GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            System.out.println(GL20.glGetShaderInfoLog(shaderID, 500));
            System.err.println("Could not compile shader.");
            System.exit(-1);
        }

        return shaderID;
    }

}
