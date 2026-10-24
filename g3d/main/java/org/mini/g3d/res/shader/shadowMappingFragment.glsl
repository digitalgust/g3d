#version 330

layout(location = 0) out float fragmentdepth;

in vec2 pass_textureCoordinates;
flat in float pass_opacity;

uniform sampler2D modelTexture;

void main()
{
    // Emissive translucent effects should not cast an opaque silhouette shadow.
    if (pass_opacity < 1.0) discard;
    // OpenGL will output gl_FragCoord.z;

    vec4 textureColour = texture(modelTexture, pass_textureCoordinates);
    if (textureColour.a<0.5){
        discard;
    }
}
