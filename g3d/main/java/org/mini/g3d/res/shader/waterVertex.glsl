#version 330

in vec2 position;

out vec4 clipSpace;
out vec2 textureCoords;
out vec3 toCameraVector;
out vec2 pass_pos;
out vec3 pass_wcolor;
out float pass_waterHeight;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;
uniform vec3 cameraPosition;
uniform vec3 waterColor;
uniform float waterHeight;

const float tiling = 4.0;

void main(void) {

    vec4 worldPosition = modelMatrix * vec4(position.x, 0.0, position.y, 1.0);
    pass_pos = worldPosition.xz;
    vec4 positionRelativeToCam = viewMatrix * worldPosition;
    clipSpace = projectionMatrix * positionRelativeToCam;
    gl_Position = clipSpace;
    textureCoords = vec2(position.x/2.0 + 0.5, position.y/2.0 + 0.5) * tiling;
    toCameraVector = cameraPosition - worldPosition.xyz;
    pass_wcolor = waterColor;
    pass_waterHeight = waterHeight;
}