#version 330 

//${MAX_LIGHT_DEFINE_IN_PROGRAM}
#ifndef MAX_LIGHT
#define MAX_LIGHT  4
#endif


in vec3 position;
in vec2 textureCoordinates;
in vec3 normal;

// 实例化属性 - 每个实例一组数据
in vec3 instancePosition;       // 实例位置
in vec3 instanceRotation;       // 实例旋转(x,y,z)
in float instanceScale;         // 实例缩放
in vec2 instanceTextureOffset;  // 实例纹理偏移
in float instanceTransparency;  // 实例透明度

out vec2 pass_textureCoordinates;
out vec3 surfaceNormal;
out vec3 toLightVector[MAX_LIGHT];
out vec3 toCameraVector;
out float visibility;
out float distanceToCam;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform vec3 lightPosition[MAX_LIGHT];

uniform float useFakeLighting;

uniform float numberOfRows;
uniform vec2 offset;
uniform float transparency;

const float density = 0.015;
const float gradient = 2.0;

// 辅助函数：旋转矩阵
mat4 createRotationMatrix(vec3 rotation) {
    float cosX = cos(rotation.x);
    float sinX = sin(rotation.x);
    float cosY = cos(rotation.y);
    float sinY = sin(rotation.y);
    float cosZ = cos(rotation.z);
    float sinZ = sin(rotation.z);
    
    mat4 rotX = mat4(
        1.0, 0.0, 0.0, 0.0,
        0.0, cosX, -sinX, 0.0,
        0.0, sinX, cosX, 0.0,
        0.0, 0.0, 0.0, 1.0
    );
    
    mat4 rotY = mat4(
        cosY, 0.0, sinY, 0.0,
        0.0, 1.0, 0.0, 0.0,
        -sinY, 0.0, cosY, 0.0,
        0.0, 0.0, 0.0, 1.0
    );
    
    mat4 rotZ = mat4(
        cosZ, -sinZ, 0.0, 0.0,
        sinZ, cosZ, 0.0, 0.0,
        0.0, 0.0, 1.0, 0.0,
        0.0, 0.0, 0.0, 1.0
    );
    
    return rotZ * rotY * rotX;
}

// 辅助函数：变换矩阵
mat4 createTransformationMatrix(vec3 translation, vec3 rotation, float scale) {
    mat4 rotMatrix = createRotationMatrix(rotation);
    
    mat4 scaleMatrix = mat4(
        scale, 0.0, 0.0, 0.0,
        0.0, scale, 0.0, 0.0,
        0.0, 0.0, scale, 0.0,
        0.0, 0.0, 0.0, 1.0
    );
    
    mat4 transMatrix = mat4(
        1.0, 0.0, 0.0, 0.0,
        0.0, 1.0, 0.0, 0.0,
        0.0, 0.0, 1.0, 0.0,
        translation.x, translation.y, translation.z, 1.0
    );
    
    return transMatrix * rotMatrix * scaleMatrix;
}

void main(void) {
    // 使用实例化数据创建变换矩阵
    mat4 transformationMatrix = createTransformationMatrix(
        instancePosition,
        instanceRotation,
        instanceScale
    );

    vec4 worldPosition = transformationMatrix * vec4(position, 1.0);
    vec4 positionRelativeToCam = viewMatrix * worldPosition;
    gl_Position = projectionMatrix * positionRelativeToCam;
    
    // 使用实例的纹理偏移而不是uniform变量
    pass_textureCoordinates = (textureCoordinates / numberOfRows) + instanceTextureOffset;

    vec3 actualNormal = normal;
    if (useFakeLighting > 0.5) {
        actualNormal = vec3(0.0, 1.0, 0.0);
    }

    surfaceNormal = (transformationMatrix * vec4(actualNormal, 0.0)).xyz;
    for (int i = 0;i < MAX_LIGHT; i++) {
        toLightVector[i] = lightPosition[i] - worldPosition.xyz;
    }
    toCameraVector = (inverse(viewMatrix) * vec4(0.0, 0.0, 0.0, 1.0)).xyz - worldPosition.xyz;

    float distance = length(positionRelativeToCam.xyz);
    distanceToCam = distance;
    if (instanceTransparency <= 0.5) {
        distanceToCam = 0.0;
    }
    visibility = exp(-pow((distance * density), gradient));
    visibility = clamp(visibility, 0.5, 1.0);
}