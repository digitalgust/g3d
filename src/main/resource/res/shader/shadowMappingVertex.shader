#version 330

layout(location = 0) in vec3 position; ///< the vertex co-odinate from VBO
layout(location = 1) in vec2 uv; ///< the UV co-odinate from VBO
layout(location = 2) in vec3 normal; ///< the normal from VBO

out vec2 pass_textureCoordinates;

uniform mat4 depthMVP;

void main()
{
  gl_Position =  depthMVP * vec4(position,1.0);
  pass_textureCoordinates = uv;
}
