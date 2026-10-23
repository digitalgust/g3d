#version 330 

//${MAX_LIGHT_DEFINE_IN_PROGRAM}
#ifndef MAX_LIGHT
#define MAX_LIGHT  4
#endif


in vec2 pass_textureCoordinates;
in vec3 surfaceNormal;
in vec3 toLightVector[MAX_LIGHT];
in vec3 toCameraVector;
in float visibility;
in float distanceToCam;

out vec4 out_Color;

uniform sampler2D modelTexture;
uniform vec3 lightColour[MAX_LIGHT];
uniform vec3 attenuation[MAX_LIGHT];
uniform float shineDamper;
uniform float reflectivity;
uniform vec3 skyColour;
uniform float transparencyDistance;//离摄像机多远的物体进行透明处理

void main(void){

    vec3 unitNormal = normalize(surfaceNormal);
    vec3 unitVectorToCamera = normalize(toCameraVector);

    // One sky/ground fill, independent of the number of local lights.
    float hemisphere = unitNormal.y * 0.5 + 0.5;
    vec3 ambientTint = mix(vec3(0.86, 0.84, 0.80), vec3(0.90, 0.96, 1.06), hemisphere);
    vec3 totalDiffuse = ambientTint * (0.26 + 0.10 * hemisphere)
        * max(lightColour[0], vec3(0.18));
    vec3 totalSpecular = vec3(0.0);

    for (int i=0;i<MAX_LIGHT;i++){
        float distance = length(toLightVector[i]);
        float attFactor = max(attenuation[i].x + (attenuation[i].y * distance) + (attenuation[i].z * distance * distance), 0.001);

        vec3 unitLightVector = normalize(toLightVector[i]);
        float nDotl = dot(unitNormal, unitLightVector);
        float brightness = max(nDotl, 0.0) * 0.72;
        vec3 lightDirection = -unitLightVector;
        vec3 reflectedLightDirection = reflect(lightDirection, unitNormal);
        float specularFactor = dot(reflectedLightDirection, unitVectorToCamera);
        specularFactor = max(specularFactor, 0.0);
        float dampedFactor = pow(specularFactor, max(shineDamper, 1.0)) * smoothstep(0.0, 0.15, nDotl);

        totalDiffuse = totalDiffuse + (brightness * lightColour[i]) / attFactor;
        totalSpecular = totalSpecular + (dampedFactor * reflectivity * lightColour[i]) / attFactor;
    }
    totalDiffuse = min(totalDiffuse, vec3(1.25));

    vec4 textureColour = texture(modelTexture, pass_textureCoordinates);
    if (textureColour.a < 0.5){
        discard;
    }

    out_Color = vec4(totalDiffuse * textureColour.rgb + totalSpecular, textureColour.a);
    out_Color = mix(vec4(skyColour, 1.0), out_Color, visibility);

    if (distanceToCam < transparencyDistance){
        int mx = int(mod(gl_FragCoord.x, 2.0));
        int my = int(mod(gl_FragCoord.y, 2.0));
        if (mx+my!=1){//网格状透明
            discard;
        }
    }

}
