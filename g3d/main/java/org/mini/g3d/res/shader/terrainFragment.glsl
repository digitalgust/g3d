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
in vec4 shadowMapCoord;
in float distanceToCam;

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



void main(void){

    vec4 blendMapColour = texture(blendMap, pass_textureCoordinates);

    float backTextureAmount = 1.0 - (blendMapColour.r + blendMapColour.g + blendMapColour.b);
    vec2 tiledCoords = pass_textureCoordinates * 16.0;//tile size
    vec4 backgroundTextureColour = texture(backgroundTexture, tiledCoords) * backTextureAmount;
    vec4 rTextureColour = texture(rTexture, tiledCoords) * blendMapColour.r;
    vec4 gTextureColour = texture(gTexture, tiledCoords) * blendMapColour.g;
    vec4 bTextureColour = texture(bTexture, tiledCoords) * blendMapColour.b;

    vec4 totalColour = backgroundTextureColour + rTextureColour + gTextureColour + bTextureColour;

    //gust
    // in shadow or not
    float shadow_visibility = 1.0;
    // calulate shadow
    shadow_visibility = textureProj(shadowMap, shadowMapCoord);
    shadow_visibility = shadow_visibility * 0.5 + 0.5;//effect by shadow

    vec3 unitNormal = normalize(surfaceNormal);
    vec3 unitVectorToCamera = normalize(toCameraVector);

    vec3 totalDiffuse = vec3(0.0);
    vec3 totalSpecular = vec3(0.0);

    for (int i=0;i<MAX_LIGHT;i++){
        float distance = length(toLightVector[i]);
        float attFactor = attenuation[i].x + (attenuation[i].y * distance) + (attenuation[i].z * distance * distance);


        vec3 unitLightVector = normalize(toLightVector[i]);
        float nDotl = dot(unitNormal, unitLightVector);
        float brightness = max(nDotl, 0.0);
        brightness = max(brightness, 0.4);//gust
        brightness = shadow_visibility * brightness;
        vec3 lightDirection = -unitLightVector;
        vec3 reflectedLightDirection = reflect(lightDirection, unitNormal);
        float specularFactor = dot(reflectedLightDirection, unitVectorToCamera);
        specularFactor = max(specularFactor, 0.0);
        float dampedFactor = pow(specularFactor, shineDamper);
        totalDiffuse = totalDiffuse + (brightness * lightColour[i]) / attFactor;
        totalSpecular = totalSpecular + (dampedFactor * reflectivity * lightColour[i]) / attFactor;
    }
    totalDiffuse = max(totalDiffuse, 0.3);
    //totalColour = shadow_visibility * totalColour;

    out_Color =  vec4(totalDiffuse, 1.0) * totalColour + vec4(totalSpecular, 1.0);
    out_Color = mix(vec4(skyColour, 1.0), out_Color, visibility);

    //    vec3 worldPos = pass_pos;// 想办法弄到当前片元的世界坐标，可以是深度重建或者读坐标纹理
    //    vec4 cloud = getCloud(noisetex, worldPos, cameraPos, lightPos);// 云颜色
    //    out_Color.rgb = out_Color.rgb*(1.0 - cloud.a) + cloud.rgb;// 混色

    if (distanceToCam < 7.0){
        float mx = mod(gl_FragCoord.x, 2.0);
        if (mod(floor(gl_FragCoord.y + mx), 2.0)!=0.0){
            discard;
        }
    }
}