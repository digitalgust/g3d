#version 330

const int MAX_JOINTS = 50;// Max amount of joints allowed in a skeleton
const int MAX_WEIGHTS = 3;// Max amount of joints that the vertex can be affected by
const int MAX_LIGHT = 1;// Max amount light

in vec3 position;
in vec2 textureCoords;
in vec3 normal;
in ivec3 jointIndices; // ivec3 holds integers and not floats, this vertex has the id of the joints that effects it
in vec3 weights; // Holds how much the vertex is affected. Vec3 because the max amount of weights effected is 3

out vec2 pass_textureCoords;
out vec3 surfaceNormal;
out vec3 toLightVector[MAX_LIGHT]; // The vector pointing towards the light source
out vec3 toCameraVector; // The vector that goes to the camera
out float visibility;

uniform mat4 jointTransforms[MAX_JOINTS]; // This is where the joints get loaded up

uniform mat4 transformationMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform vec3 lightPosition[MAX_LIGHT]; // Holds the position of the light

uniform float numberOfRows;
uniform vec2 offset;

const float density = 0.00035;
const float gradient = 5.00;

void main(void){
	
	vec4 worldPosition = transformationMatrix * vec4(position, 1.0);
	vec4 totalLocalPos = vec4(0.0); // Holds the model space position of the vertex in the current pose
	vec4 totalNormal = vec4(0.0);
	
	// Calculates the vertex so it goes to the current position the model is supposed to be in in the current pose
	for(int i=0;i<MAX_WEIGHTS;i++){
		mat4 jointTransform = jointTransforms[jointIndices[i]]; // Get the index of the joint, and then transform it
		vec4 posePosition = jointTransform * vec4(position, 1.0); // Calculates so it gets the originaly position of the joint, to the correct pose the animation is in
		totalLocalPos += posePosition * weights[i]; // We multiply the pose position with the related weight and append it to the totalLocalPos
		
		// Does the same as what is done to calculate the totalLocalPos
		vec4 worldNormal = jointTransform * vec4(normal, 0.0);
		totalNormal += worldNormal * weights[i];
	}
	
	
	vec4 positionRelativeToCam = viewMatrix * worldPosition * totalLocalPos;
	gl_Position = projectionMatrix * viewMatrix * transformationMatrix * totalLocalPos;
	pass_textureCoords = (textureCoords / numberOfRows) + offset;
	surfaceNormal = (transformationMatrix * vec4(normal,0.0)).xyz;
	
	for(int i = 0; i < MAX_LIGHT; i++){
		toLightVector[i] = lightPosition[i] - worldPosition.xyz;
	}
	
	toCameraVector = (inverse (viewMatrix) * vec4(0.0, 0.0, 0.0, 1.0)).xyz - worldPosition.xyz; // Calculates the specular lighting
	float distance = length(positionRelativeToCam.xyz);
	visibility = exp(-pow((distance*density),gradient));
	visibility = clamp(visibility,0.5,1.0);
}