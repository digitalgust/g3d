#version 330

in vec2 pass_textureCoordinates;

out vec4 out_Color;

uniform sampler2D depthTexture;
uniform sampler2D sceneTexture;
uniform sampler3D perlinNoise;  // 柏林噪声纹理
uniform vec3 fogColor;
uniform float fogDensity;
uniform float fogGradient;
uniform float nearPlane;
uniform float farPlane;
uniform float time;  // 用于动画效果
uniform vec3 cameraPosition;  // 相机位置
uniform float noiseTextureSize;  // 噪声纹理尺寸
uniform mat4 projectionMatrix;  // 投影矩阵
uniform mat4 viewMatrix;  // 视图矩阵

float getLinearDepth(vec2 texCoord) {
    float depth = texture(depthTexture, texCoord).r;
    float z = depth * 2.0 - 1.0;
    return (2.0 * nearPlane * farPlane) / (farPlane + nearPlane - z * (farPlane - nearPlane));
}

// 计算世界空间位置
vec3 getWorldPos(vec2 texCoord, float depth) {
    float z = depth * 2.0 - 1.0;
    vec4 clipSpacePosition = vec4(texCoord * 2.0 - 1.0, z, 1.0);
    vec4 viewSpacePosition = inverse(projectionMatrix) * clipSpacePosition;
    viewSpacePosition /= viewSpacePosition.w;
    vec4 worldSpacePosition = inverse(viewMatrix) * viewSpacePosition;
    return worldSpacePosition.xyz;
}

void main(void) {
    // 移除所有调试用return语句
    vec4 sceneColor = texture(sceneTexture, pass_textureCoordinates);
    float depth = getLinearDepth(pass_textureCoordinates);
    float rawDepth = texture(depthTexture, pass_textureCoordinates).r;
    vec3 worldPos = getWorldPos(pass_textureCoordinates, rawDepth);


    // 恢复噪声坐标（让噪声更密集，增强团雾细节）
    // 调整噪声坐标（控制团雾密度）
    vec3 noiseCoord = worldPos * 0.001 + vec3(0.0, time * 0.05, 0.0);  // 原0.005→0.003（更密集）
    float noise = texture(perlinNoise, noiseCoord).r;
    noise = pow(noise, 1.5);  // 增强高噪声区域的对比度（可选）
    if(noise<0.1){
        out_Color=vec4(1.0,0.0,0.0,1.0);
    }else if(noise<0.4){
        out_Color=vec4(0.0,1.0,0.0,1.0);
    }else if(noise<0.7){
        out_Color=vec4(0.0,0.0,1.0,1.0);
    }else {
        out_Color=vec4(1.0,0.0,1.0,1.0);
    }
    //return;
    // 计算到相机的距离（保留）
    float distance = length(worldPos - cameraPosition);
    if(distance<100){
        out_Color=vec4(1.0,0.0,0.0,1.0);
    }else if(distance<200){
        out_Color=vec4(0.0,1.0,0.0,1.0);
    }else if(distance<300){
        out_Color=vec4(0.0,0.0,1.0,1.0);
    }else {
        out_Color=vec4(1.0,0.0,1.0,1.0);
    }
    //return;
    // 归一化距离（基于远近面，范围0~1）
    float normalizedDistance = distance / farPlane;  // farPlane=500时，最大为1

    // 计算Y方向的衰减因子（y绝对值越大，浓度越低）
    float yAbs = abs(worldPos.y);
    //float yAttenuation = max(0.0, 1.0 - yAbs / farPlane);  // 关键新增：Y方向衰减

    // 调整雾因子计算（增强噪声影响 + Y方向衰减）
    float fogFactor = normalizedDistance * fogDensity;
    //fogFactor = pow(fogFactor, 2.0);
    fogFactor = fogFactor * ( noise ) ;//* yAttenuation;  // 加入Y方向衰减
    fogFactor *= 100.0;
    
    // 可见性计算（距离越远雾越浓）
    float visibility = exp(-fogFactor);
    visibility = clamp(visibility, 0.0, 1.0);

    
    // 混合雾颜色与场景颜色（保留）
    vec3 result = mix(fogColor, sceneColor.rgb, visibility);
    out_Color = vec4(result, 1.0);

}