#version 400 core

in vec2 textureCoords;
in vec3 toCameraVector, fromLightVector;
in vec4 clipSpace;

out vec4 out_Color;

uniform sampler2D reflectionTexture, refractionTexture;
/*  Para distorsionar la textura, se agregan desplazamientos a las coordenadas de textura, pero esto solo crea una
    distorsion constante en el quad del agua. Para que parezca realista, la distorsion debe variar en diferentes puntos
    de la superficie. Esto se logra con el DuDv Map, una textura con oscilaciones rojas y verdes que representan
    vectores 2D de desplazamiento. Como los valores rojo y verde son positivos en el DuDv Map, se convierten al rango
    -1 a 1 multiplicando por 2 y restando 1, permitiendo distorsiones tanto positivas como negativas para un efecto mas
    realista. */
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

const float waveStrength = 0.02; // Indica la instensidad de la distorsion
const float shineDamper = 20.0;
const float reflectivity = 0.6;

void main(void) {
/*  Se realiza la division de perspectiva para normalizar el espacio del dispositivo. Para muestrear una textura,
    necesitamos las coordenadas de pantalla de los puntos del cuadrilatero de agua. Esto se logra dividiendo las
    coordenadas entre 2 y sumando 0.5, dando las coordenadas de pantalla para muestrear las texturas. */
    vec2 ndc = (clipSpace.xy / clipSpace.w) / 2.0 + 0.5;
    // Invierte la coordenada [y] debido a que es una reflexion
    vec2 reflectTexCoords = vec2(ndc.x, -ndc.y);
    // Las coordenadas de la texturan de refraccion son iguales a las coordenadas normalizadas del dispositivo (ndc)
    vec2 refractTexCoords = vec2(ndc.x, ndc.y);

    // Muestrea el mapa dudv para obtener la distorsion obteniendo solo los valores verde y rojo
    vec2 distortedTexCoords = texture(dudvMap, vec2(textureCoords.x + moveFactor, textureCoords.y)).rg * 0.1;
    // Mueva la distorsion en una direccion diferente
    distortedTexCoords = textureCoords + vec2(distortedTexCoords.x, distortedTexCoords.y + moveFactor);
    vec2 totalDistortion = (texture(dudvMap, distortedTexCoords).rg * 2.0 - 1.0) * waveStrength;

    // Ahora podemos usar este valor para distorsionar las coordenadas de textura de reflexion y refraccion
    refractTexCoords += totalDistortion;
/*  Debido a la textura proyectiva y la distorsion, las coordenadas de textura de reflexion y refraccion a veces estan
    fuera del rango 0-1 en el eje y, causando que las texturas se salgan y regresen de forma incorrecta. Para
    solucionarlo, se fijan las coordenadas de textura entre 0.001 y 0.999 utilizando el metodo clamp, evitando que suban
    o bajen demasiado. */
    refractTexCoords = clamp(refractTexCoords, 0.001, 0.999);

    reflectTexCoords += totalDistortion;
/*  En el caso de la reflexion, tenemos que fijar los componentes de forma separada. Para la coordenada [y], los valores
    se invierten entre -0.999 y -0.001. */
    reflectTexCoords.x = clamp(reflectTexCoords.x, 0.001, 0.999);
    reflectTexCoords.y = clamp(reflectTexCoords.y, -0.999, -0.001);

    vec4 reflectColour = texture(reflectionTexture, reflectTexCoords);
    vec4 refractColour = texture(refractionTexture, refractTexCoords);

    // Normaliza el vector que apunta a la camara ya que el producto escalar necesita que los vectores sean vectores unitarios
    vec3 viewVector = normalize(toCameraVector);
    // Calcula el producto escalar de el vector que apunta a la camara (ya normalizado) y de la normal que apunta hacia arriba
    float refractiveFactor = dot(viewVector, vec3(0.0, 1.0, 0.0));
    // Indica que tan reflectante es la superficie del agua calculando la potencia del producto escalar
    refractiveFactor = pow(refractiveFactor, 1.0);

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