#version 400 core

in vec2 position;

out vec2 textureCoords;
out vec3 toCameraVector, fromLightVector;
out vec4 clipSpace;

uniform mat4 projectionMatrix, viewMatrix, modelMatrix;
uniform vec3 cameraPosition, lightPosition;

// Valor para colocar en mosaicos las coordendas de textura
const float tiling = 6.0;

/*  El efecto Fresnel describe como la reflectividad del agua cambia segun el angulo de vision. El agua parece mas
    transparente cuando se mira desde arriba y mas reflectante desde angulos bajos. Si la normal del agua y el vector
    hacia la camara apuntan en la misma direccion, el agua debe ser mas transparente. Cuando apuntan en direcciones
    diferentes, el agua debe ser mas reflectante. El producto escalar entre la normal del agua y el vector hacia la
    camara determina que tan transparente debe ser el agua. */

void main(void) {

    vec4 worldPosition = modelMatrix * vec4(position.x, 0.0, position.y, 1.0);
    // Genera las coordenadas del espacio de recorte de este vertice
    clipSpace = projectionMatrix * viewMatrix * worldPosition;
    gl_Position = clipSpace;
    textureCoords = vec2(position.x / 2.0 + 0.5, position.y / 2.0 + 0.5) * tiling;
    // Calcula el vector que apunta hacia la camara restando la posicion de esta y la posicion del modelo
    toCameraVector = cameraPosition - worldPosition.xyz;
    // Calcula el vector que apunta desde la luz al agua
    fromLightVector = worldPosition.xyz - lightPosition;

}