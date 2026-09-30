#version 330

in vec2 pass_textureCoordinates;

out vec4 out_Color;

uniform sampler2D depthTexture;
uniform sampler2D sceneTexture;
uniform sampler3D perlinNoise;  // 柏林噪声纹理
uniform vec3 fogColor;
uniform float fogDensity;   // 云海最浓处的浓度 0~1
uniform vec4 fogArea;       // 地图矩形 minX,minZ,maxX,maxZ (世界坐标), 矩形外是云海
uniform float fogEdgeFade;  // 出界后云海增浓的过渡宽度
uniform float fogHeight;    // 云海平面高度, 天空视线与该平面的交点用于判断
uniform float fogDepthFade; // 场景像素低于云面时, 溶入云中的垂直过渡深度(云的厚度感)
uniform float time;         // 用于云的流动和翻涌
uniform vec3 cameraPosition;
uniform mat4 projectionMatrix;  // 投影矩阵
uniform mat4 viewMatrix;  // 视图矩阵

// 计算世界空间位置
vec3 getWorldPos(vec2 texCoord, float depth) {
    float z = depth * 2.0 - 1.0;
    vec4 clipSpacePosition = vec4(texCoord * 2.0 - 1.0, z, 1.0);
    vec4 viewSpacePosition = inverse(projectionMatrix) * clipSpacePosition;
    viewSpacePosition /= viewSpacePosition.w;
    vec4 worldSpacePosition = inverse(viewMatrix) * viewSpacePosition;
    return worldSpacePosition.xyz;
}

// 点到XZ平面矩形的距离, 矩形内为0
float distOutOfRect(vec2 p, vec2 bmin, vec2 bmax) {
    vec2 d = max(bmin - p, p - bmax);
    return length(max(d, vec2(0.0)));
}

void main(void) {
    vec4 sceneColor = texture(sceneTexture, pass_textureCoordinates);
    float rawDepth = texture(depthTexture, pass_textureCoordinates).r;

    vec3 worldPos;
    float hitDist; // 像素代表的采样点到相机的距离
    bool isSky = rawDepth >= 0.9999;
    if (isSky) {
        // 天空像素: 用视线与云海平面的交点判断, 交点在地图矩形外则被云海覆盖
        vec3 dir = normalize(getWorldPos(pass_textureCoordinates, 1.0) - cameraPosition);
        if (dir.y > -0.001) {
            // 视线朝上: 晴空, 但贴近地平线的天空逐渐染上雾色, 与云海远端溶合
            // 地平线处取0.55, 必须与云海远端的浓度一致(soften下限1-0.45=0.55),
            // 否则会在地平线上留下一个浓度台阶, 被看成一条横线
            float horizonBlend = clamp(1.0 - dir.y / 0.25, 0.0, 1.0); // 地平线处1, 仰角14度以上0
            float fogFactor = horizonBlend * horizonBlend * 0.55 * clamp(fogDensity, 0.0, 1.0);
            out_Color = vec4(mix(sceneColor.rgb, fogColor, clamp(fogFactor, 0.0, 1.0)), sceneColor.a);
            return;
        }
        // 限制最大距离, 保证贴着地平线的像素交点连续, 避免噪声闪烁
        hitDist = min((fogHeight - cameraPosition.y) / dir.y, 400.0);
        worldPos = cameraPosition + dir * hitDist;
    } else {
        // 场景像素: 直接用反解出的世界坐标
        worldPos = getWorldPos(pass_textureCoordinates, rawDepth);
        hitDist = length(worldPos - cameraPosition);
    }

    float dOut = distOutOfRect(worldPos.xz, fogArea.xy, fogArea.zw);

    // 云海厚度: 场景像素(如地图裙边)低于云面时, 越深越溶入云中,
    // 视觉上云有很深的下表面, 远在裙边之下, 裙边接缝被云吞没
    float depthFog = 0.0;
    if (!isSky) {
        depthFog = clamp((fogHeight - worldPos.y) / fogDepthFade, 0.0, 1.0);
    }
    if (dOut <= 0.0 && depthFog <= 0.0) {
        // 地图矩形之内且云面之上保持清澈
        out_Color = sceneColor;
        return;
    }

    // 出界水平方向的增浓: 云海顶面用0.4次幂让边缘快速变浓,
    // 场景像素保持线性, 主要靠depthFog覆盖裙边
    float fade;
    if (isSky) {
        fade = pow(clamp(dOut / fogEdgeFade, 0.0, 1.0), 0.4);
    } else {
        fade = clamp(dOut / fogEdgeFade, 0.0, 1.0);
    }

    // 噪声锚定在世界XZ平面上, 第三维用时间: 云随时间翻涌, 并整体向x方向漂移
    vec3 nc = vec3(worldPos.x * 0.025 + time * 0.014, worldPos.z * 0.025, time * 0.04);
    float noise = texture(perlinNoise, nc).r;
    // 拉开噪声对比: 柏林纹理的值集中在中段, 云的浓薄明暗翻涌不明显,
    // smoothstep两端截止, 中点0.5映射不变, 不影响远处云色的均匀化
    noise = smoothstep(0.15, 0.85, noise);

    // 远处(贴近地平线)把噪声淡出到中间值: 距离在400处截断后, 截断区内的采样点
    // 只随水平角变化而几乎不随俯仰角变化, 噪声会退化成竖纹并随时间扰动,
    // 远处云改为均匀雾带, 与天空平滑溶合, 也消除截断区边界的纹理突变
    noise = mix(0.5, noise, 1.0 - smoothstep(150.0, 380.0, hitDist));

    // 云海顶面在远处渐渐半透, 露出后面被雾染的天空, 让云海与天空溶合(场景像素不半透)
    // 用smoothstep让坡度两端零斜率, 线性clamp的两端有斜率突变, 会在地平线附近被看出一条横带
    float soften = 1.0 - 0.45 * smoothstep(120.0, 400.0, hitDist);
    if (!isSky) {
        soften = 1.0;
    }

    // 云色随噪声明暗: 噪声低处云色变暗成团影, 高处提亮, 翻涌的光影对比主要来自这里;
    // 系数中点为1.0, 远处噪声淡回0.5时云色恰为fogColor, 与天空溶合及地平线衔接不变
    vec3 cloudColor = fogColor * (0.85 + 0.3 * noise);

    // 噪声调制云的浓薄, 让云海边缘呈团絮状; 场景像素取水平/垂直两者中较浓的
    float fogFactor = max(fade, depthFog) * (0.55 + 0.9 * noise) * soften;
    fogFactor = clamp(fogFactor, 0.0, 1.0) * clamp(fogDensity, 0.0, 1.0);

    out_Color = vec4(mix(sceneColor.rgb, cloudColor, fogFactor), sceneColor.a);
}
