#version 330 

//${MAX_LIGHT_DEFINE_IN_PROGRAM}
#ifndef MAX_LIGHT
#define MAX_LIGHT  4
#endif

#define PI 3.1415926535897932384626433832795

//2x2 Bayer阈值(棋盘), 与4x4级联可得到16级有序抖动
float bayer2(vec2 a) {
    a = floor(a);
    return fract(a.x / 2.0 + a.y * a.y * 0.75);
}

in vec2 pass_textureCoordinates;
in vec3 surfaceNormal;
in vec3 toLightVector[MAX_LIGHT];
in vec3 toCameraVector;
in float visibility;
in vec4 shadowMapCoord;
in float distanceToCam;
in float angleToXZ;
in float pass_alpha;
in vec3 pass_pos;
in float pass_viewDepth;

out vec4 out_Color;

uniform sampler2D backgroundTexture;
uniform sampler2D rTexture;
uniform sampler2D gTexture;
uniform sampler2D bTexture;
uniform sampler2D blendMap;
uniform sampler2DShadow shadowMap;

uniform vec3 lightColour[MAX_LIGHT];
uniform vec3 attenuation[MAX_LIGHT];
uniform float shineDamper;
uniform float reflectivity;
uniform vec3 skyColour;
uniform float transparencyDistance;//离摄像机多远的物体进行透明处理
uniform vec4 cutawayTarget;   // xyz: followed character's feet; w: foreground depth limit
uniform vec4 cutawaySettings; // enabled, ground protection height, full fade height, opacity

////https://blog.csdn.net/weixin_44176696/article/details/113090350
//in vec3 pass_pos;
//uniform sampler2D noisetex;// 噪声纹理
//uniform vec3 lightPos;// 光源位置
//uniform vec3 cameraPos;// 相机位置
//uniform float itime;
//
//#define bottom 15.0// 云层底部
//#define top 19.0// 云层顶部
//#define width 180.0// 云层 xz 坐标范围 [-width, width]
//
//#define baseBright  vec3(1.26, 1.25, 1.29)// 基础颜色 -- 亮部
//#define baseDark    vec3(0.31, 0.31, 0.32)// 基础颜色 -- 暗部
//#define lightBright vec3(1.29, 1.17, 1.05)// 光照颜色 -- 亮部
//#define lightDark   vec3(0.7, 0.75, 0.8)// 光照颜色 -- 暗部
//
//// 计算 pos 点的云密度
//float getDensity(sampler2D noisetex, vec3 pos) {
//    // 高度衰减
//    float mid = (bottom + top) / 2.0;
//    float h = top - bottom;
//    float weight = 1.0 - 2.0 * abs(mid - pos.y) / h;
//    weight = pow(weight, 0.5);
//
//    // 采样噪声图
//    vec2 coord = pos.xz * 0.0025;
//    coord.x += itime * 0.001;
//    float noise = texture(noisetex, coord).x;
//    noise += texture(noisetex, coord*3.5).x / 3.5;
//    noise += texture(noisetex, coord*12.25).x / 12.25;
//    noise += texture(noisetex, coord*42.87).x / 42.87;
//    noise /= 1.4472;
//    noise *= weight;
//
//    // 截断
//    if (noise<0.4) {
//        noise = 0;
//    }
//
//
//    return noise;
//}
//
//// 获取体积云颜色
//vec4 getCloud(sampler2D noisetex, vec3 worldPos, vec3 cameraPos, vec3 lightPos) {
//    vec3 direction = normalize(worldPos - cameraPos);// 视线射线方向
//    vec3 step = direction * 0.1;// 步长
//    vec4 colorSum = vec4(0);// 积累的颜色
//    vec3 point = cameraPos;// 从相机出发开始测试
//
//    // 如果相机在云层下，将测试起始点移动到云层底部 bottom
//    if (point.y<bottom) {
//        point += direction * (abs(bottom - cameraPos.y) / abs(direction.y));
//    }
//    // 如果相机在云层上，将测试起始点移动到云层顶部 top
//    if (top<point.y) {
//        point += direction * (abs(cameraPos.y - top) / abs(direction.y));
//    }
//
//    // 如果目标像素遮挡了云层则放弃测试
//    float len1 = length(point - cameraPos);// 云层到眼距离
//    float len2 = length(worldPos - cameraPos);// 目标像素到眼距离
//    if (len2<len1) {
//        return vec4(0);
//    }
//
//    // ray marching
//    for (int i=0; i<30; i++) {
//        point += step / abs(direction.y);
//        if (bottom>point.y || point.y>top || -width>point.x || point.x>width || -width>point.z || point.z>width) {
//            break;
//        }
//
//        // 采样
//        float density = getDensity(noisetex, point);// 当前点云密度
//        vec3 L = normalize(lightPos - point);// 光源方向
//        float lightDensity = getDensity(noisetex, point + L);// 向光源方向采样一次 获取密度
//        float delta = clamp(density - lightDensity, 0.0, 1.0);// 两次采样密度差
//
//        // 控制透明度
//        density *= 0.5;
//
//        // 颜色计算
//        vec3 base = mix(baseBright, baseDark, density) * density;// 基础颜色
//        vec3 light = mix(lightDark, lightBright, delta);// 光照对颜色影响
//
//        // 混合
//        vec4 color = vec4(base*light, density);// 当前点的最终颜色
//        colorSum = color * (1.0 - colorSum.a) + colorSum;// 与累积的颜色混合
//    }
//
//    return colorSum;
//}



// Low-frequency world-space variation, stable while the camera moves.
float terrainNoise(vec2 p) {
    vec2 cell = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    vec4 seeds = vec4(dot(cell, vec2(127.1, 311.7)),
                      dot(cell + vec2(1.0, 0.0), vec2(127.1, 311.7)),
                      dot(cell + vec2(0.0, 1.0), vec2(127.1, 311.7)),
                      dot(cell + vec2(1.0, 1.0), vec2(127.1, 311.7)));
    vec4 values = fract(sin(seeds) * 43758.5453);
    return mix(mix(values.x, values.y, f.x), mix(values.z, values.w, f.x), f.y);
}

float terrainShadow() {
    vec3 coord = shadowMapCoord.xyz / shadowMapCoord.w;
    vec2 texel = 1.0 / vec2(textureSize(shadowMap, 0));
    if (coord.z <= 0.0 || coord.z >= 1.0 ||
        any(lessThan(coord.xy, texel)) || any(greaterThan(coord.xy, vec2(1.0) - texel))) return 1.0;
    vec2 offset = texel * 0.75;
    float shade = texture(shadowMap, vec3(coord.xy + vec2(-offset.x, -offset.y), coord.z));
    shade += texture(shadowMap, vec3(coord.xy + vec2(offset.x, -offset.y), coord.z));
    shade += texture(shadowMap, vec3(coord.xy + vec2(-offset.x, offset.y), coord.z));
    shade += texture(shadowMap, vec3(coord.xy + vec2(offset.x, offset.y), coord.z));
    return mix(0.42, 1.0, shade * 0.25);
}

void main(void) {

    vec4 blendMapColour = texture(blendMap, pass_textureCoordinates);

    // Painted RGB can sum above one at transitions; keep all weights positive.
    vec3 weights = clamp(blendMapColour.rgb, 0.0, 1.0);
    float backTextureAmount = max(1.0 - (weights.r + weights.g + weights.b), 0.0);
    float weightSum = max(backTextureAmount + weights.r + weights.g + weights.b, 0.001);
    weights /= weightSum;
    backTextureAmount /= weightSum;
    vec2 tiledCoords = pass_textureCoordinates * 16.0;//tile size
    vec4 backgroundTextureColour = texture(backgroundTexture, tiledCoords) * backTextureAmount;
    vec4 rTextureColour = texture(rTexture, tiledCoords) * weights.r;
    vec4 gTextureColour = texture(gTexture, tiledCoords) * weights.g;
    vec4 bTextureColour = texture(bTexture, tiledCoords) * weights.b;

    vec4 totalColour = backgroundTextureColour + rTextureColour + gTextureColour + bTextureColour;
    float variation = terrainNoise(pass_pos.xz * 0.12);
    totalColour.rgb *= mix(vec3(0.94, 0.95, 0.93), vec3(1.04, 1.025, 0.99), variation);

    //gust
    // in shadow or not
    float shadow_visibility = terrainShadow();

    vec3 unitNormal = normalize(surfaceNormal);
    vec3 unitVectorToCamera = normalize(toCameraVector);

    float hemisphere = unitNormal.y * 0.5 + 0.5;
    vec3 ambientTint = mix(vec3(0.86, 0.84, 0.80), vec3(0.90, 0.96, 1.06), hemisphere);
    vec3 totalDiffuse = ambientTint * (0.26 + 0.10 * hemisphere)
        * max(lightColour[0], vec3(0.18));
    vec3 totalSpecular = vec3(0.0);

    for (int i = 0;i < MAX_LIGHT; i++) {
        float distance = length(toLightVector[i]);
        float attFactor = max(attenuation[i].x + (attenuation[i].y * distance) + (attenuation[i].z * distance * distance), 0.001);


        vec3 unitLightVector = normalize(toLightVector[i]);
        float nDotl = dot(unitNormal, unitLightVector);
        float lightShadow = i == 0 ? shadow_visibility : 1.0;
        float brightness = max(nDotl, 0.0) * 0.72 * lightShadow;
        vec3 lightDirection = -unitLightVector;
        vec3 reflectedLightDirection = reflect(lightDirection, unitNormal);
        float specularFactor = dot(reflectedLightDirection, unitVectorToCamera);
        specularFactor = max(specularFactor, 0.0);
        float dampedFactor = pow(specularFactor, max(shineDamper, 1.0))
            * smoothstep(0.0, 0.15, nDotl) * lightShadow;
        totalDiffuse = totalDiffuse + (brightness * lightColour[i]) / attFactor;
        totalSpecular = totalSpecular + (dampedFactor * reflectivity * lightColour[i]) / attFactor;
    }
    totalDiffuse = min(totalDiffuse, vec3(1.25));
    //totalColour = shadow_visibility * totalColour;

    out_Color = vec4(totalDiffuse * totalColour.rgb + totalSpecular, 1.0);
    out_Color = mix(vec4(skyColour, 1.0), out_Color, visibility);
    float terrainOpacity = pass_alpha;
    if (cutawaySettings.x > 0.5) {
        // No slope test: raised horizontal platforms can hide an entire combat area too.
        // Keep terrain at foot level solid, fading all elevated foreground across the view.
        float aboveGround = smoothstep(cutawaySettings.y, cutawaySettings.z, pass_pos.y - cutawayTarget.y);
        float foreground = 1.0 - smoothstep(cutawayTarget.w - 2.0, cutawayTarget.w, pass_viewDepth);
        float amount = aboveGround * foreground;
        terrainOpacity *= mix(1.0, cutawaySettings.w, amount);
    }
    //裙边渐隐带和前景遮挡统一使用多级点透: 保留的片元完全不透明且写深度,
    //丢弃的片元不碰帧缓冲, 不产生alpha混合不写深度的深度缓冲问题,
    //FBO的alpha也保持1(避免nanovg合成透出UI背景), 坐标先mod 4防止大坐标下浮点精度破坏图案
    if (terrainOpacity < 1.0) {
        vec2 fp = mod(gl_FragCoord.xy, 4.0);
        float threshold = bayer2(0.5 * fp) * 0.25 + bayer2(fp); // 4x4 Bayer, 16级
        if (terrainOpacity <= threshold) {
            discard;
        }
    }

    //    vec3 worldPos = pass_pos;// 想办法弄到当前片元的世界坐标，可以是深度重建或者读坐标纹理
    //    vec4 cloud = getCloud(noisetex, worldPos, cameraPos, lightPos);// 云颜色
    //    out_Color.rgb = out_Color.rgb*(1.0 - cloud.a) + cloud.rgb;// 混色

    //旧坡度规则作为关闭新模式时的兼容回退；两种点透不能叠加，避免图案冲突。
    //注意：angleToXZ实际上是法线与Y轴的夹角
    //裙边(侧面)例外: 裙边法线纯水平(y恒为0), 而地表法线y分量恒为正(calculateNormal固定y=2),
    //用normal.y是否为0判别, 裙边不需要也不应该点透
    if (cutawaySettings.x < 0.5 && angleToXZ > PI * 5.0 / 12.0) {  // 75度
        if (distanceToCam < transparencyDistance) {
            if (abs(surfaceNormal.y) > 1e-5) {
                // 创建棋盘格图案
                float mx = mod(gl_FragCoord.x, 2.0);
                float my = mod(gl_FragCoord.y, 2.0);
                if ((mx < 1.0 && my < 1.0) || (mx >= 1.0 && my >= 1.0)) {
                    discard; // 丢弃片段，形成透明效果
                }
            }
        }
    }
}
