#version 400 core

in vec2 textureCoords;
in vec3 toCameraVector, fromLightVector;
in vec4 clipSpace;

out vec4 out_Color;

uniform sampler2D reflectionTexture, refractionTexture;
/*  Un DuDv map es una textura que contiene vectores de desplazamiento en lugar de colores. Estos vectores de
    desplazamiento se utilizan para crear la ilusion de detalles geometricos adicionales en una superficie, sin tener
    que aumentar la complejidad de la malla del modelo 3D. Mas especificamente, cada texel (textura + pixel) del DuDv
    map contiene un vector 2D codificado en los canales rojo y verde de la textura. Estos vectores se utilizan para
    desplazar ligeramente la posicion de la superficie en la direccion de la normal de la superficie, creando relieves,
    hendiduras y otros detalles aparentes. Para distorsionar la superficie del agua, se agregan desplazamientos a las 
    coordenadas de textura de esta, pero esto solo crea una distorsion constante en el quad del agua. Para que parezca 
    realista, la distorsion debe variar en diferentes puntos de la superficie. Esto se logra con el DuDv Map, una 
    textura con oscilaciones rojas y verdes que representan vectores 2D de desplazamiento. Como los valores rojo y verde 
    son positivos en el DuDv Map, se convierten al rango -1 a 1 multiplicando por 2 y restando 1, permitiendo 
    distorsiones tanto positivas como negativas para un efecto mas realista. */
uniform sampler2D dudvMap;
/*  El normal map es principalmente de color azul porque el valor azul representa el eje de altura [y] del vector
    normal. Utilizaremos el componente azul para el componente [y]. Los componentes rojo y verde se usan como [x] y [z],
    pero no pueden ser negativos, lo cual es un problema porque las normales deben poder apuntar en direcciones [x] y
    [z] negativas para ser realistas. Por lo tanto, convertiremos los componentes [x] y [z] del rango 0 a 1, al rango -1
    a 1. Asi extraeremos los vectores normales completos del normal map. */
uniform sampler2D normalMap;

uniform vec3 lightColour;
// Offset para donde tomamos muestras del mapa dudv que cambia con el tiempo, lo que hara que el agua parezca que se esta moviendo
uniform float moveFactor;

const float waveStrength = 0.04; // Indica la instensidad de la distorsion
const float shineDamper = 20.0;
const float reflectivity = 0.5;

void main(void) {
/*  Despues de la etapa de recorte, las coordenadas de vertice se transforman al espacio de vista normalizado (NDC),
    donde el cubo de recorte se mapea a un cubo unitario con coordenadas (x, y, z) que van de -1 a +1. En esta linea se
    realiza la division de perspectiva para normalizar el espacio del dispositivo. Para muestrear una textura,
    necesitamos las coordenadas de pantalla de los puntos del cuadrilatero de agua. Esto se logra dividiendo las
    coordenadas entre 2 y sumando 0.5, dando las coordenadas de pantalla para muestrear las texturas. */
    vec2 ndc = (clipSpace.xy / clipSpace.w) / 2.0 + 0.5;
/*  Finalmente, las coordenadas NDC se transforman al espacio de ventana o window space mediante la transformacion de
    viewport, que las mapea a las coordenadas de pixeles 2D en la pantalla. En resumen, el espacio de recorte es un paso
    clave en el pipeline de renderizado de OpenGL que permite realizar operaciones de recorte de geometria de manera
    eficiente antes de la proyeccion final a la pantalla 2D. */
    vec2 reflectTexCoords = vec2(ndc.x, -ndc.y); // Invierte la coordenada [y] debido a que es una reflexion
    vec2 refractTexCoords = vec2(ndc.x, ndc.y); // Las coordenadas de la texturan de refraccion son iguales a las coordenadas normalizadas del dispositivo

    // Muestrea el DuDv Map para obtener la distorsion movida en [x] obteniendo solo los valores rojo y verde (rg = red, green)
    vec2 distortedTexCoords = texture(dudvMap, vec2(textureCoords.x + moveFactor, textureCoords.y)).rg * 0.1;
    // Ahora mueve la distorsion en la direccion [y] para darle un efecto realista a la distorsion
    distortedTexCoords = textureCoords + vec2(distortedTexCoords.x, distortedTexCoords.y + moveFactor);
    // Calcula la distorsion total obteniendo valores entre -1 y 1 de cada color en el DuDv Map para un efecto mas realista y se multiplica por la instensidad de la onda
    vec2 totalDistortion = (texture(dudvMap, distortedTexCoords).rg * 2.0 - 1.0) * waveStrength;

    // Ahora podemos usar este valor para distorsionar las coordenadas de textura de reflexion y refraccion
    refractTexCoords += totalDistortion;
/*  Debido a la textura proyectiva y la distorsion, las coordenadas de textura de reflexion y refraccion a veces estan
    fuera del rango 0-1 en el eje [y], causando que las texturas se salgan y regresen de forma incorrecta. Para
    solucionarlo, se fijan las coordenadas de textura entre 0.001 y 0.999 utilizando el metodo clamp, evitando que suban
    o bajen demasiado. */
    refractTexCoords = clamp(refractTexCoords, 0.001, 0.999);

    reflectTexCoords += totalDistortion;
/*  En el caso de la reflexion, tenemos que fijar los componentes de forma separada. Para la coordenada [y], los valores
    se invierten entre -0.999 y -0.001. */
    reflectTexCoords.x = clamp(reflectTexCoords.x, 0.001, 0.999);
    reflectTexCoords.y = clamp(reflectTexCoords.y, -0.999, -0.001);

/*  "Muestrear" (sampling en ingles) se refiere al proceso de obtener un valor de una fuente de datos, como una textura, 
    en un punto especifico. Mas concretamente, cuando se habla de "muestrear una textura", significa recuperar el valor
    de color (o cualquier otro dato almacenado) de un pixel especifico dentro de la textura, utilizando sus coordenadas
    de textura (texcoords). Por ejemplo, si tienes una textura 2D que representa una imagen, y quieres conocer el color
    de un pixel en particular, debes "muestrear" la textura en las coordenadas de textura correspondientes a ese pixel.
    En este caso se muestrea el color de la textura reflectionTexture en las coordenadas de textura especificadas por
    reflectTexCoords, y almacena ese color muestreado en la variable reflectColour. reflectionTexture es un sampler2D
    que representa la textura 2D de la que se muestreara el color. reflectTexCoords es un vec2 que contiene las
    coordenadas de textura en las que se muestreara el color. La funcion texture devuelve un vec4 que contiene los
    cuatro componentes (rojo, verde, azul y alfa) del color muestreado de la textura en esas coordenadas. */
    vec4 reflectColour = texture(reflectionTexture, reflectTexCoords);
    vec4 refractColour = texture(refractionTexture, refractTexCoords);

    // Normaliza el vector que apunta a la camara ya que el producto escalar necesita que los vectores sean vectores unitarios
    vec3 viewVector = normalize(toCameraVector);
    // Calcula el producto escalar de el vector que apunta a la camara (ya normalizado) y de la normal que apunta hacia arriba
    float refractiveFactor = dot(viewVector, vec3(0.0, 1.0, 0.0));
    // Indica que tan reflectante es la superficie del agua calculando la potencia del producto escalar
    refractiveFactor = pow(refractiveFactor, 1.0); // Valor calculado del efecto Fresnel

    // Muestrea el valor de color del Normal Map en las coordenadas de textura distorsionadas
    vec4 normalMapColour = texture(normalMap, distortedTexCoords);
    // Extrae la normal de ese color del Normal Map
    vec3 normal = vec3(normalMapColour.r * 2.0 - 1.0, normalMapColour.b, normalMapColour.g * 2.0 - 1.0);
    // Normaliza la normal para asegurarnos de que sea un vector unitario
    normal = normalize(normal);

    vec3 reflectedLight = reflect(normalize(fromLightVector), normal);
    float specular = max(dot(reflectedLight, viewVector), 0.0);
    specular = pow(specular, shineDamper);
    vec3 specularHighlights = lightColour * specular * reflectivity;

    // Mezcla el color de reflexion con el color de refraccion dependiendo del angulo de la camara (en %)
    out_Color = mix(reflectColour, refractColour, refractiveFactor);
    // Representa un color azul verdoso semitransparente en el espacio de color RGBA
    vec4 color = vec4(0.0, 0.3, 0.5, 1.0);
    float mezcla = 0.2; // 20% mezcla
    // Mezcla el color final con el color azul verdoso en un 20% y le agrega la luz especular
    out_Color = mix(out_Color, color, mezcla) + vec4(specularHighlights, 0.0);

}