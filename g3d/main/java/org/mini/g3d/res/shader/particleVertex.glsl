#version 330

in vec2 position;
in vec3 particlePosition;
in vec3 particleRotation;// x, y, z旋转角度(度)
in float particleScale;
in float particleOrientCamera;// 是否朝向摄像机
in vec4 texOffsets;
in float blendFactor;
in vec4 blendColor;

out vec2 textureCoords1;
out vec2 textureCoords2;
out float blend;
out vec4 fblendColor;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;// 新增：视图矩阵
uniform float numberOfRows;

mat4 createRotationMatrix(vec3 rotationDegrees) {
    vec3 rotation = radians(rotationDegrees);
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

mat4 createModelViewMatrix() {
    mat4 modelMatrix = mat4(1.0);
    modelMatrix[3].xyz = particlePosition;

    if (particleOrientCamera > 0.5) {
        // 正确实现面朝摄像机的矩阵转换，与Java代码保持一致
        // 注意：这里是将视图矩阵的旋转部分（3x3）应用到模型矩阵上
        // Java中的矩阵是行主序，GLSL中是列主序，需要进行适当转换
        modelMatrix[0][0] = viewMatrix[0][0];
        modelMatrix[0][1] = viewMatrix[1][0];
        modelMatrix[0][2] = viewMatrix[2][0];
        modelMatrix[1][0] = viewMatrix[0][1];
        modelMatrix[1][1] = viewMatrix[1][1];
        modelMatrix[1][2] = viewMatrix[2][1];
        modelMatrix[2][0] = viewMatrix[0][2];
        modelMatrix[2][1] = viewMatrix[1][2];
        modelMatrix[2][2] = viewMatrix[2][2];
    }

    mat4 modelViewMatrix = viewMatrix * modelMatrix;

    mat4 rotationMatrix = createRotationMatrix(particleRotation);
    modelViewMatrix = modelViewMatrix * rotationMatrix;

    modelViewMatrix[0] *= particleScale;
    modelViewMatrix[1] *= particleScale;
    modelViewMatrix[2] *= particleScale;

    return modelViewMatrix;
}

void main(void){
    vec2 textureCoords = position + vec2(0.5, 0.5);
    textureCoords.y = 1.0 - textureCoords.y;
    textureCoords /= numberOfRows;
    textureCoords1 = textureCoords + texOffsets.xy;
    textureCoords2 = textureCoords + texOffsets.zw;
    blend = blendFactor;
    fblendColor = blendColor;

    mat4 modelViewMatrix = createModelViewMatrix();

    gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 0.0, 1.0);
}
