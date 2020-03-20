#version 330

const int MAX_LIGHT = 1;// Max amount light

// Indicates the balance between ambient and diffuse lighting
const vec2 lightBias = vec2(0.7, 0.6);

in vec2 pass_textureCoords;
in vec3 surfaceNormal;
in vec3 toLightVector[MAX_LIGHT];
in vec3 toCameraVector;
in float visibility;

out vec4 out_Color;

uniform sampler2D textureSampler;
uniform vec3 lightColour[MAX_LIGHT];
uniform vec3 attenuation[MAX_LIGHT];
uniform float shineDamper;
uniform float reflectivity;
uniform vec3 skyColour;

void main(void){

	vec3 unitNormal = normalize(surfaceNormal); // normalize makes the vector into 1
	vec3 unitVectorToCamera = normalize(toCameraVector);
	
	vec3 totalDiffuse = vec3(0.0);
	vec3 totalSpecular = vec3(0.0);
	
	for(int i = 0; i < MAX_LIGHT; i++){
		float distance = length(toLightVector[i]);
		float attFactor = attenuation[i].x + (attenuation[i].y * distance) + (attenuation[i].z * distance * distance);
		vec3 unitLightVector = normalize(toLightVector[i]);
		
		// Dot product calculation
		float nDotl = dot(unitNormal, unitLightVector);
		float brightness = max(nDotl, 0.0); // inside max so it cant be a negative value
		vec3 lightDirection = - unitLightVector;
		vec3 reflectedLightDirection = reflect(lightDirection, unitNormal); // Makes so the light reflects
		
		// Dot product calculation, calculates how much the reflecting light is going to the camera
		float specularFactor = dot(reflectedLightDirection, unitVectorToCamera);
		specularFactor = max(specularFactor, 0.0);
		float dampedFactor = pow(specularFactor, shineDamper);
		totalDiffuse = totalDiffuse + (brightness * lightColour[i]) / attFactor;
		totalSpecular = totalSpecular + (dampedFactor * reflectivity * lightColour[i]) / attFactor;
	}
	
	totalDiffuse = max(totalDiffuse, 0.2);
	
	// Takes in the texture we want to sample and the coordinates
	vec4 textureColour = texture(textureSampler, pass_textureCoords);
	if(textureColour.a < 0.5){
		discard;
	}
	
	// Returns the color of the pixel of the texture on that coordinate
	out_Color = vec4(totalDiffuse, 1.0) *  textureColour + vec4(totalSpecular, 1.0);
	out_Color = mix(vec4(skyColour, 1.0), out_Color, visibility);
	
}