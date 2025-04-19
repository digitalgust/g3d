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
//const vec3 waterColour = vec3(0.0, 0.3, 0.5);
//const vec3 waterColour = vec3(0.8, 0.1, 0.1);
vec3 waterColour = pass_wcolor;        // 水的颜色

const vec2 center = vec2(48, 48);      // 中心点

// 模糊效果参数
const int blurSize = 4;                // 模糊大小
const float blurSampleCount = (float(blurSize) * 2.0) + 1.0;  // 模糊采样数量
const float textureHeight = 360.0;     // 纹理高度
const float texelSize = 1.0 / textureHeight;  // 纹素大小

// 平滑步进函数（替代glsl的smoothstep）
float smoothlyStep(float edge0, float edge1, float x){
    float t = clamp((x - edge0) / (edge1 - edge0), 0.0, 1.0);
    return t * t * (3.0 - 2.0 * t);
}

void main(void) {
    // 计算标准化设备坐标(NDC)
    vec2 ndc = (clipSpace.xy/clipSpace.w)/2.0 + 0.5;
    vec2 refractTexCoords = vec2(ndc.x, ndc.y);      // 折射纹理坐标
    
    // 修改反射纹理坐标计算方式，考虑水面高度和俯仰角影响
    vec3 viewDir = normalize(toCameraVector);
    
    // 计算视线与y轴的夹角（俯仰角）
    float viewAngle = acos(dot(viewDir, vec3(0.0, 1.0, 0.0)));
    
    // 调整反射系数，考虑水面高度
    float waterHeightFactor = pass_waterHeight / 50.0; // 归一化水面高度影响
    float viewFactor = min(1.0, 2.0 / (viewAngle + 0.5)); // 减少viewAngle对反射的影响
    float reflectionScale = mix(0.4, 1.0, viewFactor) + waterHeightFactor; // 确保即使视角较大时也有反射
    
    // 应用调整后的反射坐标
    vec2 reflectTexCoords = vec2(ndc.x, -ndc.y * reflectionScale);

    // 近平面和远平面距离
    float near = 0.1;
    float far = 1000.0;

    // 计算水面深度
    float depth = texture(depthMap, refractTexCoords).r;
    float floorDistance = 2.0 * near * far / (far + near - (2.0 * depth - 1.0) * (far - near));

    depth = gl_FragCoord.z;
    float waterDistance = 2.0 * near * far / (far + near - (2.0 * depth - 1.0) * (far - near));
    float waterDepth = floorDistance - waterDistance;

    // 计算水面扰动
    vec2 distortedTexCoords = texture(dudvMap, vec2(textureCoords.x + moveFactor, textureCoords.y)).rg*0.1;
    distortedTexCoords = textureCoords + vec2(distortedTexCoords.x, distortedTexCoords.y+moveFactor);
    vec2 totalDistortion = (texture(dudvMap, distortedTexCoords).rg * 2.0 - 1.0) * waveStrength * clamp(waterDepth/1.0, 0.0, 1.0);

    // 应用扰动到纹理坐标
    refractTexCoords += totalDistortion;
    refractTexCoords = clamp(refractTexCoords, 0.001, 0.999);

    reflectTexCoords += totalDistortion;
    reflectTexCoords.x = clamp(reflectTexCoords.x, 0.001, 0.999);
    reflectTexCoords.y = clamp(reflectTexCoords.y, -0.999, -0.001);

    // 计算反射颜色（带模糊效果）
    vec4 reflectColour = vec4(0.0);
    float offset;
    for (int i=-blurSize;i<=blurSize;i++){
        offset = float(i) * texelSize;
        reflectColour += texture(reflectionTexture, reflectTexCoords + vec2(0.0, offset));
    }
    reflectColour /= blurSampleCount;

    // 计算折射颜色
    vec4 refractColour = texture(refractionTexture, refractTexCoords);
    refractColour = mix(refractColour, vec4(waterColour, 1.0), clamp(waterDepth/70.0, 0.0, 1.0));

    // 计算法线
    vec4 normalMapColour = texture(normalMap, distortedTexCoords);
    vec3 normal = vec3(normalMapColour.r * 2.0 - 1.0, normalMapColour.b*3.0, normalMapColour.g * 2.0 - 1.0);
    normal = normalize(normal);

    // 计算视角向量和折射因子
    vec3 viewVector = normalize(toCameraVector);
    float refractiveFactor = dot(viewVector, normal);
    refractiveFactor = pow(refractiveFactor, 0.6);
    // 降低折射因子，提高反射比例
    refractiveFactor = clamp(refractiveFactor * 0.6, 0.0, 0.7);

    // 计算镜面反射高光
    vec3 reflectedLight = reflect(normalize(lightDirection), normal);
    float specular = max(dot(reflectedLight, viewVector), 0.0);
    specular = pow(specular, shineDamper);
    vec3 specularHighlights = vec3(1.0)*specular * reflectivity * clamp(waterDepth/0.5, 0.0, 1.0);

    // 混合最终颜色
    out_Color = mix(reflectColour, refractColour + vec4(specularHighlights, 0.0), refractiveFactor);
    // 增加水的反射强度
    out_Color = mix(out_Color, vec4(waterColour, 1.0), 0.15);
    out_Color.a = clamp(waterDepth/0.5, 0.0, 1.0);

    //	float disFactor = smoothlyStep(15.0, 16.0, distance(center, pass_pos));
    //	out_Color.rgb = mix(out_Color.rgb, vec3(1.0), disFactor);

}