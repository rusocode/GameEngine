#version 400 core

in vec2 textureCoords;

out vec4 out_Color;

uniform sampler2D guiTexture;

void main(void) {

/*  El metodo texture se utiliza para muestrear una textura en una coordenada especifica. Toma como argumento la textura
    y las coordenadas de textura, y devuelve el color de la textura en esas coordenadas. */
    out_Color = texture(guiTexture, textureCoords);

}