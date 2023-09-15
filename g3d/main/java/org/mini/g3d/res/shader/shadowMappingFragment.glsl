#version 330

layout(location = 0) out float fragmentdepth;

in vec2 pass_textureCoordinates;

uniform sampler2D modelTexture;

void main()
{
    // OpenGL will output gl_FragCoord.z;

    vec4 textureColour = texture(modelTexture, pass_textureCoordinates);
    if (textureColour.a<0.5){
        discard;
    }
}
