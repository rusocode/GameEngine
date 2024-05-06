package water;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

/**
 * Cuando se renderiza una escena, renderizamos nuestros objetos y terrenos uno por uno, pero no aparecen en pantalla hasta que
 * actualizamos la visualizacion. Cuando renderizamos un objeto, primero se representa en el framebuffer. Un framebuffer en OpenGL
 * es un objeto que contiene todos los datos necesarios para realizar el renderizado de una escena, incluidos los buffers de color
 * (matriz 2D de colores) y profundidad. El buffer de profundidad almacena la informacion de profundidad de cada pixel que esta en
 * el bufer de color.
 * <p>
 * Sin embargo, no solo tenemos que renderizar en este framebuffer, tambien podemos crear nuestros propios <b>objetos de bufer de
 * fotogramas</b> (FBO). Una vez que hayamos creado un FBO, podemos darles archivos adjuntos como un bufer de color y un bufer de
 * profundidad, y luego podemos renderizar objetos en este framebuffer en lugar del framebuffer predeterminado. Por lo que antes
 * de renderizar cualquier cosa, ahora tenemos la opcion de donde queremos que se represente el objeto.
 * <p>
 * Con el fin de renderizar agua vamos a crear dos FBOs, ambos con bufer de profundidad y archivos adjuntos de bufer de color. En
 * uno de estos, representamos la textura de reflexion y en el otro vamos a representar la textura de refraccion. Los archivos
 * adjuntos de buffer aqui seran texturas, por supuesto, para que podamos usarlos para texturizar la superficie del agua, pero
 * el bufer de profundidad de la textura de refraccion tambien sera una textura porque basicamente almacenera la profundidad del
 * agua y nosotros queremos probar eso en el Fragment Shader en el futuro cuando rendericemos el agua para que podamos hacer
 * algunos efectos de profundidad agradables.
 */

public class WaterFrameBuffers {

    // Resoluciones, mientras menor sea la resolucion, menos costoso sera renderizar
    protected static final int REFLECTION_WIDTH = 320, REFLECTION_HEIGHT = 180;
    protected static final int REFRACTION_WIDTH = 1280, REFRACTION_HEIGHT = 720;

    private int reflectionFrameBuffer, reflectionTexture, reflectionDepthBuffer;
    private int refractionFrameBuffer, refractionTexture, refractionDepthTexture;

    public WaterFrameBuffers() {
        /* Crea dos FBOs, uno con un archivo adjunto de textura de buffer de color y de profundidad, y el otro con un archivo
         * adjunto de textura de bufer de color y un archivo adjunto de bufer de renderizado de bufer de profundidad. */
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
    public void unbindCurrentFrameBuffer() { // Se llama para cambiar al bufer de cuadros predeterminado
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
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
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0); // Se asegura de que la textura no este unida
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frameBuffer);
        // Cambia la resolucion de nuestra ventana grafica a la resolucion de nuestro FBO
        GL11.glViewport(0, 0, width, height);
    }

    private int createFrameBuffer() {
        // Genera un id para el framebuffer
        int frameBuffer = GL30.glGenFramebuffers();
        // Vincula el framebuffer
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frameBuffer);
        // Indica que siempre renderizaremos al color adjunto 0
        GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
        return frameBuffer;
    }

    private int createTextureAttachment(int width, int height) {
        // Genera un id para la textura
        int texture = GL11.glGenTextures();
        // Enlaza la textura especificando el tipo de textura (GL_TEXTURE_2D) y el id
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        /* Define una imagen bidimensional como contenido de una textura. Esta funcion especifica los datos de la imagen, como su
         * formato de pixeles, tamaño, y el contenido de los pixeles en si. */
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, width, height, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        /* Adjunta una textura a un framebuffer. Esto significa que la textura se utilizara como un destino de renderizado en
         * lugar de un framebuffer tradicional. La constante GL_COLOR_ATTACHMENT0 especifica el tipo de buffer de framebuffer al
         * que se adjuntara la textura. */
        GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, texture, 0);
        return texture;
    }

    private int createDepthTextureAttachment(int width, int height) {
        int texture = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT32, width, height, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, texture, 0);
        return texture;
    }

    private int createDepthBufferAttachment(int width, int height) {
        // Genera un id para el buffer de profundidad
        int depthBuffer = GL30.glGenRenderbuffers();
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, depthBuffer);
        /* Especifica el formato y el tamaño de almacenamiento del renderbuffer. Es decir, se utiliza para reservar memoria y
         * configurar el renderbuffer con ciertas propiedades. La constante GL_DEPTH_COMPONENT epecifica el formato interno de
         * almacenamiento de datos del renderbuffer. */
        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL11.GL_DEPTH_COMPONENT, width, height);
        GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, depthBuffer);
        return depthBuffer;
    }

    public void clean() {
        GL30.glDeleteFramebuffers(reflectionFrameBuffer);
        GL11.glDeleteTextures(reflectionTexture);
        GL30.glDeleteRenderbuffers(reflectionDepthBuffer);
        GL30.glDeleteFramebuffers(refractionFrameBuffer);
        GL11.glDeleteTextures(refractionTexture);
        GL11.glDeleteTextures(refractionDepthTexture);
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