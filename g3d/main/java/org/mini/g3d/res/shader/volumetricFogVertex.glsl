#version 330

in vec3 position;
in vec2 textureCoordinates;

out vec2 pass_textureCoordinates;

void main(void) {
    // 直接使用顶点位置，不进行任何变换
    gl_Position = vec4(position, 1.0);
    pass_textureCoordinates = textureCoordinates;
} 