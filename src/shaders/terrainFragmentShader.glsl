#version 400 core

in vec2 pass_textureCoords;
in vec3 surfaceNormal, toLightVector[4], toCameraVector;
in float visibility;

/*  En OpenGL, se utiliza un mapa de mezcla (blendMap) para indicar la ubicacion de diferentes texturas en un terreno.
    Este mapa asigna colores a texturas especificas, como hierba (negro), tierra (rojo), flores (verde) y camino (azul).
    Se crea un blendMap para cada terreno, y en los sombreadores, se hace referencia a este mapa para determinar la ubicacion
    de cada textura. En el Fragment Shader, al implementar multitexturing, se obtienen colores de cuatro texturas para
    cada pixel en el terreno. El color final del pixel es una mezcla de estos cuatro colores, y el blendMap se utiliza
    para ajustar la proporcion de cada textura en el resultado final. */
uniform sampler2D background, r, g, b, blendMap;

uniform vec3 lightColour[4], attenuation[4];
uniform float shineDamper, reflectivity;
uniform vec3 skyColor;

out vec4 out_Color;

void main(void) {

    // Obtiene el color del blendMap que nos dira que cantidad de cada textura debemos renderizar
    vec4 blendMapColor = texture(blendMap, pass_textureCoords);
    // Calcula la cantidad a renderizar restando 1 al color total del blendMap, porque queremos que esto se renderice cuando el blenMap este negro
    float backTextureAmount = 1 - (blendMapColor.r + blendMapColor.g + blendMapColor.b);
    // Multiplica las coordenadas de textura para colocarlas en forma de tiles dando una mejor definicion y evitar que se estire
    vec2 tiledCoords = pass_textureCoords * 30.0;
    // Muestrea la textura de fondo en las coordenadas de textura del tile y lo multiplica por la cantidad que se debe representar
    vec4 backgroundTextureColor = texture(background, tiledCoords) * backTextureAmount;
/*  La textura r se representa dependiendo del valor rojo en el blendMap. Se obtenie el color de esa textura y lo
    multiplica por el componente rojo del color del blendMap. */
    vec4 rTextureColor = texture(r, tiledCoords) * blendMapColor.r;
    vec4 gTextureColor = texture(g, tiledCoords) * blendMapColor.g;
    vec4 bTextureColor = texture(b, tiledCoords) * blendMapColor.b;

    // Calcula el color total del terreno, que es una mezcla de los tres colores que ya hemos calculado dependiendo del color del blendMap
    vec4 totalColor = backgroundTextureColor + rTextureColor + gTextureColor + bTextureColor;

    vec3 unitNormal = normalize(surfaceNormal);
    vec3 unitVectorToCamera = normalize(toCameraVector);

    vec3 totalDiffuse = vec3(0.0);
    vec3 totalSpecular = vec3(0.0);

    for (int i = 0; i < 4; i++) {
        float distance = length(toLightVector[i]);
        float attFactor = attenuation[i].x + (attenuation[i].y * distance) + (attenuation[i].z * distance * distance);
        vec3 unitLightVector = normalize(toLightVector[i]);
        float nDotl = dot(unitNormal, unitLightVector);
        float brightness = max(nDotl, 0.0);
        vec3 lightDirection = -unitLightVector;
        vec3 reflectedLightDirection = reflect(lightDirection, unitNormal);
        float specularFactor = dot(reflectedLightDirection, unitVectorToCamera);
        specularFactor = max(specularFactor, 0.0);
        float dampedFactor = pow(specularFactor, shineDamper);
        totalDiffuse = totalDiffuse + (brightness * lightColour[i]) / attFactor;
        totalSpecular = totalSpecular + (dampedFactor * reflectivity * lightColour[i]) / attFactor;
    }

    totalDiffuse = max(totalDiffuse, 0.2);

    out_Color = vec4(totalDiffuse, 1.0) * totalColor + vec4(totalSpecular, 1.0);

    // Se comenta esta linea en caso de mantener la visibilidad del terreno y no se oscurezca cuando se aleja la camara
    // out_Color = mix(vec4(skyColor, 1.0), out_Color, visibility);
}