#version 400 core // Version de GLSL

// Datos entrantes de la aplicacion
in vec3 position;
in vec2 textureCoords;
in vec3 normal;

out vec2 pass_textureCoords;
out vec3 surfaceNormal; // Superficie normal del vector
out vec3 toLightVector[4]; // Vector que apunta hacia la fuente de luz
out vec3 toCameraVector; // Vector que apunta hacia la camara
out float visibility;

uniform mat4 transformationMatrix, viewMatrix, projectionMatrix;
uniform vec3 lightPosition[4];

// El valor sera 0 si no debemos usar iluminacion falsa y 1 en caso contrario
uniform float useFakeLighting;

uniform float numberOfRows;
uniform vec2 offset;

const float density = 0.0025; // Determina el espesor de la niebla, y aumentar este valor disminuye la visibilidad general de la escena
const float gradient = 5.0; // Determina que tan rapido disminuye la visibilidad con la distancia, y aumentar este valor hace que la transicion de visibilidad total a visibilidad 0 sea mucho mas pequenia

// Crea un plano horizontal
uniform vec4 plane;

// Metodo principal que se ejecutara cada vez que este sombreador de vertices procese un vertice
void main(void) {

    vec4 worldPosition = transformationMatrix * vec4(position, 1.0);

    // Busca la distancia de cada vertice desde ese plano
    gl_ClipDistance[0] = dot(worldPosition, plane);

    // Distancia del vertice a la camara
    vec4 positionRelativeToCam = viewMatrix * worldPosition;
    // Le dice a la GPU donde representar este vertice en la pantalla
    gl_Position = projectionMatrix * positionRelativeToCam;
    // Pasa (pass_) las coordendas de texturas al Fragment Shader
    pass_textureCoords = (textureCoords / numberOfRows) + offset;

    vec3 actualNormal = normal;
    if (useFakeLighting > 0.5) {
        // Si la iluminacion falsa es mayor a 0.5, entonces la normal apunta hacia arriba
        actualNormal = vec3(0.0, 1.0, 0.0);
    }

/*  Podria pensar que la normal de la superficie deberia ser igual a la normal que obtenemos, pero no olvidemos que
    a veces vamos a rotar el modelo. Cuando tenemos la matriz de transformacion, va a rotar la entidad, por lo que
    la normal tambien tiene que rotarse porque cambiara su direccion si la entidad se gira. Asi que tenemos que hacer
    una matriz de transformacion multiplicada por la normal y tenemos que convertir la normal en un vector 4D para que
    pueda multiplicarse por la matriz 4x4 de transformacion. */
    surfaceNormal = (transformationMatrix * vec4(actualNormal, 0.0)).xyz;

    for (int i = 0; i < 4; i++) {
        // Calcula la diferencia entre la posicion de la luz y la posicion mundial del vertice, y como la posicion mundial es un vector 4D y solo necesitamos que sea 3D, entonces obtiene los componentes xyz
        toLightVector[i] = lightPosition[i] - worldPosition.xyz;
    }

/*  Como no tenemos la camara en ningun lado del codigo del shader, utilizamos viewMatrix que contiene la version
    negativa de la posicion de la camara. Por lo que toma la inversa de esa matriz y aplica esta transformacion a un
    vector (0,0,0) para convertirlo en un vector 4D, siendo esta la posicion de la camara. Ahora podemos obtener el
    el vector desde el vertice a la camara restando la posicion de los vertices de la posicion de la camara. */
    toCameraVector = (inverse(viewMatrix) * vec4(0.0, 0.0, 0.0, 1.0)).xyz - worldPosition.xyz;

    // Distancia desde la camara obteniendo los componentes xyz con el metodo length()
    float distance = length(positionRelativeToCam.xyz);
/*  Aplica la funcion exponencial al resultado negativo de la potencia de la distancia por la densidad elevada al
    degradado. La multiplicacion de la distancia por la densidad controla como la densidad afecta la visibilidad en
    funcion de la distancia. El resultado anterior se eleva a la potencia indicada por gradient. Esto permite ajustar
    como la visibilidad disminuira con la distancia. Luego aplica la funcion exponencial al resultado negativo de la
    potencia. La funcion exponencial se utiliza para suavizar la transicion y asegurar que la visibilidad disminuya
    de manera suave y realista con la distancia. */
    visibility = exp(-pow((distance * density), gradient));
    // Se asegura de que la visibilidad se mantenga entre cero y uno
    visibility = clamp(visibility, 0.0, 1.0);

}

//Este comentario se aplica a los primeros videos que hacen referencia un cuadrado rectangulo
/*  Para resumir, tenemos nuestro cuadrado de 6 lados con sus cuatro vertices almacenados en una matriz de atributos en
    un VAO: V1 (-0.5, 0.5, -0.5), V1 (-0.5, -0.5, -0.5), V2 (0.5, -0.5, -0.5) y V3 (0.5, 0.5, -0.5). El sombreador de
    vertices que acabamos de programar tiene acceso a estas posiciones desde: in vec4 position. Para que el sombreador de
    vertices se ejecute para cada vertice y use esta posicion de entrada primero debe decirle a la GPU en que parte de la
    pantalla se debe representar el vertice. Esto se hace estableciendo la variable de posicion gl_Position = vec4(position, 1.0).
    Luego el sombreador de vertices calcula el color para cada vertice basado en la posicion de ese vertice usando las
    coordenadas de texturas. */