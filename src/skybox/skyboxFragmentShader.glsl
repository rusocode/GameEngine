#version 400

in vec3 textureCoords;
out vec4 out_Color;

uniform samplerCube cubeMap, cubeMap2;
uniform float blendFactor;
uniform vec3 fogColour;

/*  La forma en que se simula la niebla es desvaneciendo los objetos en el color de esta. Pero al usar un skybox, los
    objetos distantes aun son muy visibles porque el color de la niebla constrasta con el colorido skybox. Para arreglar
    esto, vamos a desvancer la seccion inferior del skybox en el color de la niebla para que los objetos distantes una
    vez mas tengan un fondo de color liso para fusionarse. Vamos a definir dos alturas en el skybox, lowerLimit y
    upperLimit. Por encima del limite superior, la altura del skybox usa el color de la textura del skybox y por debajo
    de la altura del limite inferior el skybox sera completamente del color de la niebla. Entre los dos limites, el color
    se desvanecera linealmente entre el color de la niebla y el color del skybox. */

const float lowerLimit = 0.0;
const float upperLimit = 30.0;

// const float levels = 10.0;

void main(void) {
    vec4 texture1 = texture(cubeMap, textureCoords);
    vec4 texture2 = texture(cubeMap2, textureCoords);
    vec4 finalColour = mix(texture1, texture2, blendFactor); // Mezcla las dos texturas usando el factor de mezcla

    // Cel Shading
    // float amount = (finalColour.r - finalColour.g + finalColour.b) / 3.0;
    // amount = floor(amount * levels) / levels;
    // finalColour.rgb = amount * fogColour;

    // Representa la visibilidad del skybox donde un factor 0 significa que esta por debajo del limite inferior (color de la niebla) y un factor 1 significa que este fragmento esta por encima de los limites superiores (color de la textura del skybox)
    float factor = (textureCoords.y - lowerLimit) / (upperLimit - lowerLimit);
    factor = clamp(factor, 0.0, 1.0); // Fija el valor del factor entre 0 y 1
    // Mezcla el color de la niebla y el color de la textura del skybox usando el valor del factor y decidir cuanto de cada color se debe representar
    // out_Color = mix(vec4(fogColour, 1.0), finalColour, factor);

    // Solo utiliza el color de la textura del skybox
    out_Color = finalColour;

}