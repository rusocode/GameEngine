package water;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.Display;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.*;

/**
 * Cuando se renderiza una escena, renderizamos nuestros objetos y terrenos uno por uno, pero no aparecen en pantalla hasta que
 * actualizamos la visualizacion. Cuando renderizamos un objeto, primero se representa en el framebuffer. Un framebuffer en OpenGL
 * es un objeto que contiene todos los datos necesarios para realizar el renderizado de una escena, incluidos los buffers de color
 * (matriz 2D de colores) y profundidad. El buffer de profundidad almacena la informacion de profundidad de cada pixel que esta en
 * el buffer de color.
 * <p>
 * Sin embargo, no solo tenemos que renderizar en este framebuffer, tambien podemos crear nuestros propios <b>objetos de buffer de
 * fotogramas</b> (FBO). Una vez que hayamos creado un FBO, podemos darles archivos adjuntos como un buffer de color y un buffer de
 * profundidad, y luego podemos renderizar objetos en este framebuffer en lugar del framebuffer predeterminado. Por lo que antes
 * de renderizar cualquier cosa, ahora tenemos la opcion de donde queremos que se represente el objeto.
 * <p>
 * Con el fin de renderizar agua vamos a crear dos FBOs, ambos con buffer de profundidad y archivos adjuntos de buffer de color. En
 * uno de estos, representamos la textura de reflexion y en el otro vamos a representar la textura de refraccion. Los archivos
 * adjuntos de buffer aqui seran texturas, por supuesto, para que podamos usarlos para texturizar la superficie del agua, pero
 * el buffer de profundidad de la textura de refraccion tambien sera una textura porque basicamente almacenera la profundidad del
 * agua y nosotros queremos probar eso en el Fragment Shader en el futuro cuando rendericemos el agua para que podamos hacer
 * algunos efectos de profundidad agradables.
 */

public class WaterFrameBuffers {

    // Resoluciones, mientras menor sea la resolucion, menos costoso sera renderizar
    protected static final int REFLECTION_WIDTH = 320, REFLECTION_HEIGHT = 180;
    protected static final int REFRACTION_WIDTH = 800, REFRACTION_HEIGHT = 600;

    private int reflectionFrameBuffer, reflectionTexture, reflectionDepthBuffer;
    private int refractionFrameBuffer, refractionTexture, refractionDepthTexture;

    public WaterFrameBuffers() {
        /* Crea dos FBOs, uno con un archivo adjunto de textura de buffer de color y de profundidad, y el otro con un archivo
         * adjunto de textura de buffer de color y un archivo adjunto de buffer de renderizado de buffer de profundidad. */
        initialiseReflectionFrameBuffer();
        initialiseRefractionFrameBuffer();
    }

    private void initialiseReflectionFrameBuffer() {
        reflectionFrameBuffer = createFrameBuffer();
        reflectionTexture = createTextureAttachment(REFLECTION_WIDTH, REFLECTION_HEIGHT);
        reflectionDepthBuffer = createDepthBufferAttachment(REFLECTION_WIDTH, REFLECTION_HEIGHT);
        unbindCurrentFrameBuffer();
    }

    private void initialiseRefractionFrameBuffer() {
        refractionFrameBuffer = createFrameBuffer();
        refractionTexture = createTextureAttachment(REFRACTION_WIDTH, REFRACTION_HEIGHT);
        refractionDepthTexture = createDepthTextureAttachment(REFRACTION_WIDTH, REFRACTION_HEIGHT);
        unbindCurrentFrameBuffer();
    }

    /**
     * Si queremos volver a renderizar al framebuffer predeterminado, tenemos que llamar al metodo glBindFramebuffer y pasarle 0
     * como ID.
     */
    public void unbindCurrentFrameBuffer() { // Se llama para cambiar al buffer de cuadros predeterminado
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, Display.getWidth(), Display.getHeight());
    }

    public void bindReflectionFrameBuffer() { // Se llama antes de renderizar a este FBO
        bindFrameBuffer(reflectionFrameBuffer, REFLECTION_WIDTH, REFLECTION_HEIGHT);
    }

    public void bindRefractionFrameBuffer() { // Se llama antes de renderizar a este FBO
        bindFrameBuffer(refractionFrameBuffer, REFRACTION_WIDTH, REFRACTION_HEIGHT);
    }

    /**
     * Para decirle a OpenGL que queremos renderizar en uno de nuestros FBOs tenemos que vincular el FBO relevante y luego todo
     * lo que rendericemos despues de eso se renderizara a ese FBO.
     */
    private void bindFrameBuffer(int frameBuffer, int width, int height) {
        glBindTexture(GL_TEXTURE_2D, 0); // Se asegura de que la textura no este unida
        glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);
        // Cambia la resolucion de nuestra ventana grafica a la resolucion de nuestro FBO
        glViewport(0, 0, width, height);
    }

    private int createFrameBuffer() {
        // Genera un id para el framebuffer
        int id = glGenFramebuffers();
        // Vincula el framebuffer
        glBindFramebuffer(GL_FRAMEBUFFER, id);
        // Indica que siempre renderizaremos al color adjunto 0
        glDrawBuffer(GL_COLOR_ATTACHMENT0);
        return id;
    }

    private int createTextureAttachment(int width, int height) {
        // Genera un id para la textura
        int id = glGenTextures();
        // Enlaza la textura especificando el tipo de textura (GL_TEXTURE_2D) y el id
        glBindTexture(GL_TEXTURE_2D, id);
        /* Define una imagen bidimensional como contenido de una textura. Esta funcion especifica los datos de la imagen, como su
         * formato de pixeles, tamaño, y el contenido de los pixeles en si. */
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        /* Adjunta una textura a un framebuffer. Esto significa que la textura se utilizara como un destino de renderizado en
         * lugar de un framebuffer tradicional. La constante GL_COLOR_ATTACHMENT0 especifica el tipo de buffer de framebuffer al
         * que se adjuntara la textura. */
        glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, id, 0);
        return id;
    }

    private int createDepthTextureAttachment(int width, int height) {
        int id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, id);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32, width, height, 0, GL_DEPTH_COMPONENT, GL_FLOAT, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, id, 0);
        return id;
    }

    private int createDepthBufferAttachment(int width, int height) {
        // Genera un id para el buffer de profundidad
        int id = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, id);
        /* Especifica el formato y el tamaño de almacenamiento del renderbuffer. Es decir, se utiliza para reservar memoria y
         * configurar el renderbuffer con ciertas propiedades. La constante GL_DEPTH_COMPONENT epecifica el formato interno de
         * almacenamiento de datos del renderbuffer. */
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, width, height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, id);
        return id;
    }

    public void clean() {
        glDeleteFramebuffers(reflectionFrameBuffer);
        glDeleteTextures(reflectionTexture);
        glDeleteRenderbuffers(reflectionDepthBuffer);
        glDeleteFramebuffers(refractionFrameBuffer);
        glDeleteTextures(refractionTexture);
        glDeleteTextures(refractionDepthTexture);
    }

    public int getReflectionTexture() {
        return reflectionTexture;
    }

    public int getRefractionTexture() {
        return refractionTexture;
    }

    public int getRefractionDepthTexture() {
        return refractionDepthTexture;
    }

}