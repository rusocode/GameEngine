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
 * <h2>Pipeline Grafico</h2>
 * El termino "pipeline grafico" se refiere al proceso que sigue la informacion grafica a medida que se transforma desde datos de
 * entrada hasta la imagen final que se muestra en la pantalla. Este proceso se divide en varias etapas o pasos, cada uno con una
 * funcion especifica. En el contexto de graficos por computadora, especialmente en API como OpenGL o DirectX, el pipeline grafico
 * tipicamente incluye las siguientes etapas:
 * <ol>
 * <li><b>Entrada de Datos (Input Assembly):</b> Se ingresan los datos de vertices, que contienen informacion sobre la geometria
 * tridimensional de los objetos en la escena.
 * <li><b>Vertex Shader:</b> Este shader procesa cada vertice individual, realizando transformaciones como traslacion, rotacion y
 * escala. Tambien puede realizar calculos adicionales, como iluminacion o deformaciones.
 * <li><b>Tessellation:</b> Opcionalmente, esta etapa divide poligonos grandes en fragmentos mas pequeños para un mayor detalle 
 * geometrico.
 * <li><b>Geometry Shader:</b> Tambien es opcional, este shader manipula la geometria, generando nuevos vertices o primitivas, lo
 * que permite efectos como la creacion de instancias multiples o la generacion de sombras.
 * <li><b>Rasterizacion:</b> Convierte las primitivas geometricas en fragmentos (pixeles) que se deben renderizar.
 * <li><b>Fragment Shader:</b> Procesa cada fragmento individualmente, determinando el color final y otros atributos. Aqui se
 * realizan operaciones como texturizacion, sombreado y efectos visuales.
 * <li><b>Salida a Pantalla:</b> Combina los fragmentos procesados y los envia al bufer de trama para su visualizacion en la
 * pantalla.
 * </ol>
 * Cada una de estas etapas contribuye al proceso global de renderizacion en graficos por computadora. El pipeline grafico es
 * esencial para convertir datos de modelos 3D en imagenes visuales en una pantalla.
 * <br><br>
 * <h2>GLSL</h2>
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
 * <br><br>
 * <h3>¿Como interactuan OpenGL y GLSL?</h3>
 * Supongamos que tenemos la siguiente linea en GLSL, {@code in vec3 position;}, que define una variable de entrada (in) llamada
 * position y espera un vector de tres componentes (vec3). Esta variable generalmente se utiliza para representar la posicion
 * tridimensional de un vertice en el espacio.
 * <p>
 * En el contexto de OpenGL y GLSL, estos datos de entrada se proporcionan desde la aplicacion que utiliza la API. Cuando
 * renderizas objetos en OpenGL, la aplicacion proporciona datos como las coordenadas de vertices, colores, texturas, etc., y
 * estos datos se envian a traves de buffers a la GPU para su procesamiento.
 * <p>
 * El codigo de la aplicacion podria incluir algo como esto (en pseudocodigo) para enviar datos a la GPU:
 * <pre>{@code
 * // Crear y configurar un bufer de vertices
 * GLuint vbo;
 * glGenBuffers(1, &vbo);
 * glBindBuffer(GL_ARRAY_BUFFER, vbo);
 * glBufferData(GL_ARRAY_BUFFER, sizeof(vertices), vertices, GL_STATIC_DRAW);
 *
 * // Especificar el diseño de los datos de vertices
 * glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 3 * sizeof(GLfloat), (GLvoid*)0);
 * glEnableVertexAttribArray(0);
 * }</pre>
 * Aqui, vertices es un array que contiene las coordenadas de los vertices del objeto. Luego, el codigo de configuracion de
 * atributos de vertices en la aplicacion le dice a OpenGL como interpretar esos datos y como enviarlos al shader. En este caso,
 * glVertexAttribPointer se utiliza para especificar el diseño del atributo de posicion (vec3) en el bufer de vertices.
 * <p>
 * En el shader GLSL, la declaracion {@code in vec3 position;} se correlaciona con este diseño y espera que la informacion de
 * posicion de cada vertice se envie a traves de esta variable. La conexion entre la aplicacion y el shader se establece a traves
 * de los indices de atributos (en este caso, 0) y la especificacion de como se organizan los datos en el bufer de vertices.
 */

public abstract class ShaderProgram {

    private final int programID;
    private final int vertexShaderID;
    private final int fragmentShaderID;

    private static final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

    public ShaderProgram(String vertexFile, String fragmentFile) {
        vertexShaderID = loadShader(vertexFile, GL20.GL_VERTEX_SHADER);
        fragmentShaderID = loadShader(fragmentFile, GL20.GL_FRAGMENT_SHADER);
        // Crea un programa de shader
        programID = GL20.glCreateProgram();
        // Adjunta un shader a un programa de shader
        GL20.glAttachShader(programID, vertexShaderID);
        GL20.glAttachShader(programID, fragmentShaderID);
        // Vincula los atributos antes de vincular el programa
        bindAttributes();
        /* Enlaza los shaders adjuntos a un programa de shader. Despues de adjuntar shaders individuales al programa, glLinkProgram()
         * realiza la vinculacion final para crear un programa de shader completo y listo para su uso en el pipeline grafico. */
        GL20.glLinkProgram(programID);
        /* Valida un programa de shader despues de ser enlazado. La validacion proporciona informacion sobre la compatibilidad
         * entre los shaders en el programa y asegura que esten configurados de manera coherente para ejecutarse correctamente en
         * el hardware de la tarjeta grafica. */
        GL20.glValidateProgram(programID);
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
        GL20.glBindAttribLocation(programID, index, name);
    }

    /**
     * Obtiene la ubicacion de la variable uniforme declarada en el Vertex Shader.
     * <p>
     * Estas variables permanecen constantes durante la ejecucion del shader y se emplean para enviar datos desde la aplicacion
     * de OpenGL al shader. La funcion toma como parametros el identificador del programa de shader y el nombre de la variable
     * uniforme deseada, devolviendo un entero que representa la posicion de la variable en el programa de shader. Esta
     * posicion se utiliza despues para asignar valores a la variable uniforme mediante funciones como {@code  glUniform1f()}, {@code glUniformMatrix4fv()}, etc.
     *
     * @param name nombre de la variable uniforme.
     * @return un entero que representa la ubicacion de la variable uniforme.
     */
    protected int getUniformLocation(String name) {
        return GL20.glGetUniformLocation(programID, name);
    }

    /**
     * Carga un valor de tipo float a una variable uniforme en un programa de shader. Las variables uniformes mantienen su
     * valor constante durante la ejecucion de conjuntos de vertices o fragmentos. El "1f" en el nombre de la funcion señala que
     * se asigna un solo valor de tipo float.
     *
     * @param location Especifica la ubicacion de la variable uniforme en el programa de shader actual.
     * @param value    Especifica el valor float que se va a asignar a la variable uniforme.
     */
    protected void loadFloat(int location, float value) {
        GL20.glUniform1f(location, value);
    }

    protected void loadVector(int location, Vector3f vector) {
        GL20.glUniform3f(location, vector.x, vector.y, vector.z);
    }

    protected void loadBoolean(int location, boolean value) {
        float toLoad = 0;
        if (value) toLoad = 1;
        GL20.glUniform1f(location, toLoad);
    }

    public void loadMatrix(int location, Matrix4f matrix) {
        matrix.store(matrixBuffer);
        matrixBuffer.flip();
        GL20.glUniformMatrix4(location, false, matrixBuffer);
    }

    /**
     * Activa un programa de shader especifico en el pipeline grafico. Este programa consta de shaders individuales (como Vertex
     * Shader y Fragment Shader) que definen el procesamiento de vertices y fragmentos en la GPU. Durante la activacion, los
     * shaders adjuntos se utilizan segun la configuracion para procesar vertices o fragmentos. Esta funcion es esencial para
     * cambiar entre programas de shader y aplicar efectos visuales durante la renderizacion.
     */
    public void start() {
        GL20.glUseProgram(programID);
    }

    public void stop() {
        GL20.glUseProgram(0);
    }

    public void clean() {
        stop();
        // Desvincula un shader de un programa de shader eliminando la conexion entre un shader especifico y el programa sin destruir el shader en si
        GL20.glDetachShader(programID, vertexShaderID);
        GL20.glDetachShader(programID, fragmentShaderID);
        /* Elimina un objeto de shader. Estos objetos contienen y representan el codigo fuente de shaders, como Vertex Shaders o
         * Fragment Shaders. La funcion toma el identificador del shader que se desea eliminar como parametro y libera los recursos
         * asociados con ese shader. Es crucial destacar que esta funcion no elimina el programa de shader completo; unicamente
         * elimina el objeto de shader individual. */
        GL20.glDeleteShader(vertexShaderID);
        GL20.glDeleteShader(fragmentShaderID);
        /* Elimina un programa de shader compuesto por shaders individuales adjuntos, compilados y enlazados, como Vertex Shaders
         * o Fragment Shaders. Es crucial entender que glDeleteProgram() borra el programa de shader, pero no afecta a los shaders
         * individuales adjuntos. */
        GL20.glDeleteProgram(programID);
    }

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
        // Carga el codigo fuente
        GL20.glShaderSource(shaderID, shaderSource);
        // Compila el codigo fuente
        GL20.glCompileShader(shaderID);
        /* Despues de compilar el shader, es recomendable verificar si hubo errores de compilacion utilizando glGetShaderiv() y
         * glGetShaderInfoLog(). La informacion de registro (log) proporciona detalles sobre cualquier problema que haya surgido
         * durante la compilacion del shader. */
        if (GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            System.out.println(GL20.glGetShaderInfoLog(shaderID, 500));
            System.err.println("Could not compile shader.");
            System.exit(-1);
        }

        return shaderID;
    }

}
