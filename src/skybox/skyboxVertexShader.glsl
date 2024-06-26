#version 400

in vec3 position;

out vec3 textureCoords;

uniform mat4 viewMatrix, projectionMatrix;

void main(void) {
/* 	Como la posicion es un vector 3D entonces necesita agregarle un componente mas (1.0 como el componente w) para
	que se pueda multiplicar por la matriz de proyeccion y la matriz de vista. */
    gl_Position = projectionMatrix * viewMatrix * vec4(position, 1.0);
    textureCoords = position;
}

/* 	Tutoriales:
	Introduction to shaders: Learn the basics! >> https://www.youtube.com/watch?v=3mfvZ-mdtZQ
	An introduction to Shader Art Coding >> https://www.youtube.com/watch?v=f4s1h2YETNY */