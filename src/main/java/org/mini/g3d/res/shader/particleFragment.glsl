#version 330

out vec4 out_colour;

in vec2 textureCoords1;
in vec2 textureCoords2;
in float blend;
in vec4 fblendColor;

uniform sampler2D particleTexture;

void main(void){

    vec4 colour1 = texture(particleTexture, textureCoords1);
    vec4 colour2 = texture(particleTexture, textureCoords2);

    out_colour = mix(colour1, colour2, blend);
    //    out_colour = vec4(out_colour.rgb, out_colour.a * alpha_pass);
    //    out_colour = mix(out_colour, fblendColor, 0.5);
    out_colour = out_colour * fblendColor;
}
