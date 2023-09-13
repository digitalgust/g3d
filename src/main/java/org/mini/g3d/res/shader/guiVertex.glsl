#version 330

in vec2 position;

out vec2 textureCoords;

uniform mat4 transformationMatrix;
uniform float numberOfRows;
uniform vec2 texOffsets;

void main(void){

    textureCoords = (position  + vec2(1.0, 1.0)) / 2.0;
    //textureCoords.y = 1.0 - textureCoords.y;
    textureCoords /= numberOfRows;
    textureCoords = textureCoords + texOffsets;


    gl_Position = transformationMatrix * vec4(position, 0.0, 1.0);
    //    textureCoords = vec2((position.x+1.0)/2.0, 1.0 - (position.y+1.0)/2.0);
}