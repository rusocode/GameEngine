#version 400 core

in vec2 pass_textureCoords;
in vec3 surfaceNormal;
in vec3 toLightVector[4];
in vec3 toCameraVector;
in float visibility;

// La salida es el color del pixel que esta procesando actualmente y que sera un vector de 4 colores con su respectivo brillo y luz especular
out vec4 out_Color;

uniform sampler2D modelTexture;
uniform vec3 lightColour[4], attenuation[4];
uniform float shineDamper, reflectivity;
uniform vec3 skyColor;

const float levels = 3.0;

void main(void) {

/*  Normaliza los dos vectores para asegurarse de que el tamanio de los dos vectores sea uno. Asi la direccion del
    vector permanece exactamente igual, de modo que solo importa la direccion y la magnitud del vector es irrelevante. */
    vec3 unitNormal = normalize(surfaceNormal);
    // Normaliza el vector de la camara para asegurarnos que el tamanio de este sea solo 1
    vec3 unitVectorToCamera = normalize(toCameraVector);

    vec3 totalDiffuse = vec3(0.0);
    vec3 totalSpecular = vec3(0.0);

    // Calcula la iluminacion del pixel para cada una de las fuentes de luz
    for (int i = 0; i < 4; i++) {
        float distance = length(toLightVector[i]);
        // Calcula el factor de atenuacion
        float attFactor = attenuation[i].x + (attenuation[i].y * distance) + (attenuation[i].z * distance * distance);
        vec3 unitLightVector = normalize(toLightVector[i]);
    /*  Calcula el producto escalar de los dos vectores normales. El resultado representa que tan correcto debe ser el pixel.
        Lo que es importante tener en cuenta es que el producto escalar de dos vectores unitarios que apuntan exactamente en
        la misma direccion es uno y el producto escalar de dos vectores perpendiculares que apuntan en direcciones totalmente
        diferentes es 0 y todo lo demas esta en algun punto intermedio (0.6, 0.3, etc.). Esto nos da una representacion
        perfecta de que tan similares son dos vectores y, por lo tanto, una representacion perfecta de que tan brillante debe
        ser un cierto punto en el objeto. */
        float nDotl = dot(unitNormal, unitLightVector);
        // Se asegura de que el resultado se encuentre entre 0 y 1, porque a veces el producto escalar devolvera valores menores a 0
        float brightness = max(nDotl, 0.0);
        // float level = floor(brightness * levels); // Averigua en que nivel de sombreado se encuentra este valor de brillo
        // brightness = level / levels; // Establece el brillo en el limite inferior de ese nivel
        // CALCULA LA LUZ ESPECULAR DEL PIXEL
        // Crea el vector con los puntos en la direccion de donde proviene la luz, esto es justo lo opuesto al vector que apunta hacia la luz, por lo tanto lo invierte
        vec3 lightDirection = -unitLightVector;
        // Esta funcion toma el vector de la luz entrante y la normal de la superficie con la que desea reflejar la luz, y devuelve la direccion de la luz reflejada
        vec3 reflectedLightDirection = reflect(lightDirection, unitNormal);
        // Indica que tan brillante debe ser la luz especular sin ninguna amortiguacion (shineDamper)
        float specularFactor = dot(reflectedLightDirection, unitVectorToCamera);
        // Se asegura de que el factor especular sea mayor que cero porque obviamente no podemos tener un brillo negativo
        specularFactor = max(specularFactor, 0.0);
    /*  Aplica la amortiguacion elevando el factor especular a la potencia del valor de amortiguacion, lo que hace que
        los factores especulares bajos sean aun mas bajos, pero no afecta tanto a los valores mas altos. */
        float dampedFactor = pow(specularFactor, shineDamper);
        // level = floor(dampedFactor * levels);
        // dampedFactor = level / levels;
        // Multiplica el brillo por el color de la luz actual (i) para obtener la iluminacion final y acumularlo en totalDiffuse
        totalDiffuse = totalDiffuse + (brightness * lightColour[i]) / attFactor; // TODO No es mejor usar +=?
        // Multiplica el factor amortiguado por la reflectividad por el color de la luz actual (i) y lo acumula en totalSpecular
        totalSpecular = totalSpecular + (dampedFactor * reflectivity * lightColour[i]) / attFactor;
    }

    // Se asegura que ninguna parte del modelo se oscurece por completo
    totalDiffuse = max(totalDiffuse, 0.2);

/*  El metodo texture() devuelve el color del pixel en la coordenada de textura obtenida, de modo que muestreara la
    textura del modelTexture y la probara en las coordenadas de texturas del pass_textureCoords. Asi obtiene el
    color del pixel que encuentre en esas coordenadas de textura y lo envia al pixel que se esta procesando
    actualmente. */
    vec4 textureColor = texture(modelTexture, pass_textureCoords);
    // Verifica si el color alpha (transparente) de la textura es menor a 0.5
    if (textureColor.a < 0.5) {
        // Le dice a OpenGL (o a la GPU?) que no renderice las secciones negras transparentes de las texturas
        discard;
    }

    // PIXEL CON LA ILUMINACION FINAL
    // El color del pixel se multiplica por la iluminacion y se suma la luz especular para obtener el resultado final
    out_Color = vec4(totalDiffuse, 1.0) * textureColor + vec4(totalSpecular, 1.0);

    // Mezcla el color final con el color del cielo
    // out_Color = mix(vec4(skyColor, 1.0), out_Color, visibility);

}