#version 330

layout(location = 0) in vec3 position;///< the vertex co-odinate from VBO
layout(location = 1) in vec2 uv;///< the UV co-odinate from VBO
layout(location = 2) in vec3 normal;///< the normal from VBO

// 实例化属性
layout(location = 3) in vec3 instancePosition;       // 实例位置
layout(location = 4) in vec3 instanceRotation;       // 实例旋转(x,y,z) - 注意：这里需要传入角度值，内部会转换为弧度
layout(location = 5) in float instanceScale;         // 实例缩放
layout(location = 6) in vec2 instanceTextureOffset;  // 实例纹理偏移
layout(location = 7) in float instanceTransparency;  // 实例透明度

out vec2 pass_textureCoordinates;

uniform mat4 depthMVP;

// 辅助函数：旋转矩阵
mat4 createRotationMatrix(vec3 rotationDegrees) {
    vec3 rotation = radians(-rotationDegrees);
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
mat4 createTransformationMatrix(vec3 translation, vec3 rotationDegrees, float scale) {
    mat4 rotMatrix = createRotationMatrix(rotationDegrees);
    
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

void main()
{
    // 使用实例化数据创建模型变换矩阵
    mat4 modelMatrix = createTransformationMatrix(
        instancePosition,
        instanceRotation,
        instanceScale
    );
    
    // 应用深度MVP矩阵
    gl_Position = depthMVP * modelMatrix * vec4(position, 1.0);
    pass_textureCoordinates = uv;
}
