#version 330

in vec2 pass_textureCoordinates;

out vec4 out_Color;

uniform sampler2D srcTexture;

void main(void) {
    out_Color = texture(srcTexture, pass_textureCoordinates);
}
