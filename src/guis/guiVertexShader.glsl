#version 400 core

in vec2 position;

out vec2 textureCoords;

uniform mat4 transformationMatrix;

void main(void) {

/*  gl_Position es una variable especial que representa la posicion 3D de un vertice despues de todas las
    transformaciones necesarias para convertirlo de su posicion local en el espacio del objeto a su posicion en la
    pantalla o en la ventana de visualizacion. Esta variable es crucial para el proceso de renderizado en OpenGL, ya
    que define donde aparecera un vertice en la pantalla final despues de todas las manipulaciones de transformacion.
    La funcion vec4 convierte el vec2 (position) a un vec4 agregando 0.0 para la coordenada z y 1.0 para la coordenada
    w (homogenea).

    En OpenGL, las coordenadas de textura se definen en el rango [0.0, 1.0] para ambos ejes, donde (0.0, 0.0) la esquina
    inferior izquierda de la textura y (1.0, 1.0) representa la esquina superior derecha.

    Sin embargo, las coordenadas de posicion de los vertices suelen estar en el rango [-1.0, 1.0], donde (0.0, 0.0) es
    el centro de la pantalla, (-1.0, -1.0) es la esquina inferior izquierda y (1.0, 1.0) es la esquina superior derecha.

    Para mapear correctamente las coordenadas de posicion al rango de coordenadas de textura, se realiza la siguiente
    operacion:
    1. (position.x + 1.0) / 2.0: Esta parte de la expresion se encarga de mapear la coordenada x de posicion al rango
    [0.0, 1.0]. Se suma 1.0 a position.x para mover el rango [-1.0, 1.0] al rango [0.0, 2.0], y luego se divide por
    2.0 para escalar el rango a [0.0, 1.0].
    2. 1 - (position.y + 1.0) / 2.0: Esta parte de la expresion se encarga de mapear la coordenada y de posicion al
    rango [0.0, 1.0]. Sin embargo, aqui se realiza una operacion adicional: 1 - (...). Esto se debe a que en OpenGL,
    el eje y está invertido en comparacion con el sistema de coordenadas de la textura. En OpenGL, (0.0, 0.0) es la
    esquina inferior izquierda, mientras que en el sistema de coordenadas de textura, (0.0, 0.0) es la esquina
    superior izquierda. Por lo tanto, se resta el resultado de (position.y + 1.0) / 2.0 de 1.0 para invertir el eje y
    y obtener las coordenadas de textura correctas. */
    gl_Position = transformationMatrix * vec4(position, 0.0, 1.0);
    textureCoords = vec2((position.x + 1.0) / 2.0, 1 - (position.y + 1.0) / 2.0);

}