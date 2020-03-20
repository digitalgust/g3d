#version 330

in vec2 textureCoords;

out vec4 out_Color;

uniform sampler2D guiTexture;

void main(void){

	vec4 color = texture(guiTexture,textureCoords);

	out_Color = color;

}