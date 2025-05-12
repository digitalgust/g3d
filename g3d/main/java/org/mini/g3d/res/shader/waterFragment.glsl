#version 330

// 输入变量
in vec4 clipSpace;        // 裁剪空间坐标
in vec2 textureCoords;    // 纹理坐标
in vec3 toCameraVector;   // 到相机的向量
in vec2 pass_pos;         // 世界空间位置
in vec3 pass_wcolor;      // 水的颜色
in float pass_waterHeight;

out vec4 out_Color;       // 输出颜色

// 统一变量
uniform vec3 lightDirection;           // 光照方向
uniform sampler2D reflectionTexture;   // 反射纹理
uniform sampler2D refractionTexture;   // 折射纹理
uniform sampler2D dudvMap;            // 水面扰动纹理
uniform sampler2D normalMap;          // 法线贴图
uniform sampler2D depthMap;           // 深度图

uniform float moveFactor;              // 水面移动因子

// 水面效果参数
const float waveStrength = 0.01;       // 波浪强度
const float shineDamper = 30.0;        // 光泽衰减
const float reflectivity = 0.9;        // 反射率

const vec2 center = vec2(48, 48);      // 中心点

//// 模糊效果参数
//const int blurSize = 2;                // 减小模糊大小以减少循环次数
//const float blurSampleCount = 5.0;     // 固定采样数量，避免计算
//const float textureHeight = 256.0;     // 纹理高度
//const float texelSize = 1.0 / textureHeight;  // 纹素大小

// 平滑步进函数（替代glsl的smoothstep）
float smoothlyStep(float edge0, float edge1, float x){
    float t = clamp((x - edge0) / (edge1 - edge0), 0.0, 1.0);
    return t * t * (3.0 - 2.0 * t);
}

void main(void) {
    // 计算标准化设备坐标(NDC)
    vec2 ndc = (clipSpace.xy/clipSpace.w)/2.0 + 0.5;
    
    // 确保ndc不超出边界（Android上很重要）
    ndc = clamp(ndc, 0.001, 0.999);
    
    vec2 refractTexCoords = ndc;      // 折射纹理坐标
    
    // 简化计算，减少复杂度，以适应OpenGL ES
    vec3 viewDir = normalize(toCameraVector);
    
    // 计算视线与y轴的夹角（俯仰角）
    float viewAngle = acos(dot(viewDir, vec3(0.0, 1.0, 0.0)));
    
    // 调整反射系数，考虑水面高度
    float waterHeightFactor = pass_waterHeight / (pass_waterHeight*2.0); // 归一化水面高度影响
    float viewFactor = min(1.0, 2.0 / (viewAngle + 0.5)); // 减少viewAngle对反射的影响
    float reflectionScale = mix(0.4, 1.0, viewFactor); // 确保即使视角较大时也有反射
    
    // 应用调整后的反射坐标
    vec2 reflectTexCoords = vec2(ndc.x, -ndc.y * reflectionScale);
    reflectTexCoords = clamp(reflectTexCoords, vec2(-0.999, -0.999), vec2(0.999, -0.001));

    // 近平面和远平面距离
    float near = 0.1;
    float far = 1000.0;

    // 计算水面深度 - 简化以避免复杂计算
    float depth = texture(depthMap, refractTexCoords).r;
    float floorDistance = 2.0 * near * far / (far + near - (2.0 * depth - 1.0) * (far - near));

    depth = gl_FragCoord.z;
    float waterDistance = 2.0 * near * far / (far + near - (2.0 * depth - 1.0) * (far - near));
    float waterDepth = max(floorDistance - waterDistance, 0.0); // 确保水深非负
    
    // 简化水面扰动计算
    vec2 distortedTexCoords = texture(dudvMap, vec2(textureCoords.x + moveFactor, textureCoords.y)).rg*0.1;
    distortedTexCoords = textureCoords + vec2(distortedTexCoords.x, distortedTexCoords.y+moveFactor);
    vec2 totalDistortion = (texture(dudvMap, distortedTexCoords).rg * 2.0 - 1.0) * waveStrength;

    // 应用扰动到纹理坐标
    refractTexCoords += totalDistortion;
    refractTexCoords = clamp(refractTexCoords, 0.001, 0.999);

    reflectTexCoords.x += totalDistortion.x;
    reflectTexCoords.y += totalDistortion.y;
    reflectTexCoords.x = clamp(reflectTexCoords.x, 0.001, 0.999);
    reflectTexCoords.y = clamp(reflectTexCoords.y, -0.999, -0.001);

//    // 计算反射颜色（带模糊效果）
//    vec4 reflectColour = vec4(0.0);
//    float offset;
//    for (int i=-blurSize;i<=blurSize;i++){
//        offset = float(i) * texelSize;
//        reflectColour += texture(reflectionTexture, reflectTexCoords + vec2(0.0, offset));
//    }
//    reflectColour /= blurSampleCount;
    // 单次采样反射和折射纹理 - 避免循环
    vec4 reflectColour = texture(reflectionTexture, reflectTexCoords);
    vec4 refractColour = texture(refractionTexture, refractTexCoords);
    
    // 混合折射颜色与水颜色
    refractColour = mix(refractColour, vec4(pass_wcolor, 1.0), 0.2);

    // 计算简化的法线
    vec4 normalMapColour = texture(normalMap, distortedTexCoords);
    vec3 normal = vec3(normalMapColour.r * 2.0 - 1.0, normalMapColour.b*3.0, normalMapColour.g * 2.0 - 1.0);
    normal = normalize(normal);

    // 计算视角向量和折射因子
    vec3 viewVector = normalize(toCameraVector);
    float refractiveFactor = dot(viewVector, normal);
    refractiveFactor = clamp(refractiveFactor * 0.6, 0.2, 0.8);

    // 计算简化的镜面反射
    vec3 reflectedLight = reflect(normalize(lightDirection), normal);
    float specular = max(dot(reflectedLight, viewVector), 0.0);
    specular = pow(specular, shineDamper);
    vec3 specularHighlights = vec3(specular * reflectivity);

    // 混合最终颜色
    out_Color = mix(reflectColour, refractColour + vec4(specularHighlights, 0.0), refractiveFactor);
    out_Color = mix(out_Color, vec4(pass_wcolor, 1.0), 0.2);
    out_Color.a = clamp(waterDepth/5.0, 0.5, 1.0); // 确保透明度适中

    //	float disFactor = smoothlyStep(15.0, 16.0, distance(center, pass_pos));
    //	out_Color.rgb = mix(out_Color.rgb, vec3(1.0), disFactor);
}