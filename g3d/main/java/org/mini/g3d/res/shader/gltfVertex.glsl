//animation.glsl/////////

const int MAX_INSTANCED_SIZE = 100;

#ifdef HAS_TARGET_POSITION0
in vec3 a_Target_Position0;
#endif

#ifdef HAS_TARGET_POSITION1
in vec3 a_Target_Position1;
#endif

#ifdef HAS_TARGET_POSITION2
in vec3 a_Target_Position2;
#endif

#ifdef HAS_TARGET_POSITION3
in vec3 a_Target_Position3;
#endif

#ifdef HAS_TARGET_POSITION4
in vec3 a_Target_Position4;
#endif

#ifdef HAS_TARGET_POSITION5
in vec3 a_Target_Position5;
#endif

#ifdef HAS_TARGET_POSITION6
in vec3 a_Target_Position6;
#endif

#ifdef HAS_TARGET_POSITION7
in vec3 a_Target_Position7;
#endif

#ifdef HAS_TARGET_NORMAL0
in vec3 a_Target_Normal0;
#endif

#ifdef HAS_TARGET_NORMAL1
in vec3 a_Target_Normal1;
#endif

#ifdef HAS_TARGET_NORMAL2
in vec3 a_Target_Normal2;
#endif

#ifdef HAS_TARGET_NORMAL3
in vec3 a_Target_Normal3;
#endif

#ifdef HAS_TARGET_TANGENT0
in vec3 a_Target_Tangent0;
#endif

#ifdef HAS_TARGET_TANGENT1
in vec3 a_Target_Tangent1;
#endif

#ifdef HAS_TARGET_TANGENT2
in vec3 a_Target_Tangent2;
#endif

#ifdef HAS_TARGET_TANGENT3
in vec3 a_Target_Tangent3;
#endif

#ifdef USE_MORPHING
uniform float u_morphWeights[WEIGHT_COUNT];
#endif

#ifdef HAS_JOINT_SET1
in vec4 a_Joint1;
#endif

#ifdef HAS_JOINT_SET2
in vec4 a_Joint2;
#endif

#ifdef HAS_WEIGHT_SET1
in vec4 a_Weight1;
#endif

#ifdef HAS_WEIGHT_SET2
in vec4 a_Weight2;
#endif

#ifdef USE_SKINNING
uniform mat4 u_jointMatrix[JOINT_COUNT];
uniform mat4 u_jointNormalMatrix[JOINT_COUNT];
const int TEX_MAT_SIZE = 4, MAT_OFFSET = 1;
uniform sampler2D u_jointMatrixTex;
uniform int u_jointMatrixTexWidth;
uniform int u_frameIndex[MAX_INSTANCED_SIZE];

#endif


#ifdef USE_SKINNING

mat4x4 getJointMatFromTex(int boneIndex) {

    int texW = u_jointMatrixTexWidth;
    //
    int offset = u_frameIndex[gl_InstanceID] * JOINT_COUNT * TEX_MAT_SIZE + boneIndex * TEX_MAT_SIZE;
    vec4 v1 = texelFetch(u_jointMatrixTex, ivec2(int((offset+0)%texW), int((offset+0)/texW)), 0).rgba;
    vec4 v2 = texelFetch(u_jointMatrixTex, ivec2(int((offset+1)%texW), int((offset+1)/texW)), 0).rgba;
    vec4 v3 = texelFetch(u_jointMatrixTex, ivec2(int((offset+2)%texW), int((offset+2)/texW)), 0).rgba;
    vec4 v4 = texelFetch(u_jointMatrixTex, ivec2(int((offset+3)%texW), int((offset+3)/texW)), 0).rgba;

    mat4x4 jointMat = mat4x4(v1, v2, v3, v4);
    jointMat -= 0.5;
    jointMat *= 5.0;

    return jointMat;
}

mat4 getSkinningMatrix()
{
    mat4 skin = mat4(0);

    #if defined(HAS_WEIGHT_SET1) && defined(HAS_JOINT_SET1)
    skin +=
    a_Weight1.x * getJointMatFromTex(int(a_Joint1.x)) +
    a_Weight1.y * getJointMatFromTex(int(a_Joint1.y)) +
    a_Weight1.z * getJointMatFromTex(int(a_Joint1.z)) +
    a_Weight1.w * getJointMatFromTex(int(a_Joint1.w));
    #endif

    #if defined(HAS_WEIGHT_SET2) && defined(HAS_JOINT_SET2)
    skin +=
    a_Weight2.x * getJointMatFromTex(int(a_Joint2.x)) +
    a_Weight2.y * getJointMatFromTex(int(a_Joint2.y)) +
    a_Weight2.z * getJointMatFromTex(int(a_Joint2.z)) +
    a_Weight2.w * getJointMatFromTex(int(a_Joint2.w));
    #endif

    return skin;
}

mat4 getSkinningNormalMatrix()
{
    mat4 skin = mat4(0);

    #if defined(HAS_WEIGHT_SET1) && defined(HAS_JOINT_SET1)
    skin +=
    a_Weight1.x * u_jointNormalMatrix[int(a_Joint1.x)] +
    a_Weight1.y * u_jointNormalMatrix[int(a_Joint1.y)] +
    a_Weight1.z * u_jointNormalMatrix[int(a_Joint1.z)] +
    a_Weight1.w * u_jointNormalMatrix[int(a_Joint1.w)];
    #endif

    #if defined(HAS_WEIGHT_SET2) && defined(HAS_JOINT_SET2)
    skin +=
    a_Weight2.x * u_jointNormalMatrix[int(a_Joint2.x)] +
    a_Weight2.y * u_jointNormalMatrix[int(a_Joint2.y)] +
    a_Weight2.z * u_jointNormalMatrix[int(a_Joint2.z)] +
    a_Weight2.w * u_jointNormalMatrix[int(a_Joint2.w)];
    #endif

    return skin;
}
    #endif// !USE_SKINNING

    #ifdef USE_MORPHING
vec4 getTargetPosition()
{
    vec4 pos = vec4(0);

    #ifdef HAS_TARGET_POSITION0
    pos.xyz += u_morphWeights[0] * a_Target_Position0;
    #endif

    #ifdef HAS_TARGET_POSITION1
    pos.xyz += u_morphWeights[1] * a_Target_Position1;
    #endif

    #ifdef HAS_TARGET_POSITION2
    pos.xyz += u_morphWeights[2] * a_Target_Position2;
    #endif

    #ifdef HAS_TARGET_POSITION3
    pos.xyz += u_morphWeights[3] * a_Target_Position3;
    #endif

    #ifdef HAS_TARGET_POSITION4
    pos.xyz += u_morphWeights[4] * a_Target_Position4;
    #endif

    return pos;
}

vec4 getTargetNormal()
{
    vec4 normal = vec4(0);

    #ifdef HAS_TARGET_NORMAL0
    normal.xyz += u_morphWeights[0] * a_Target_Normal0;
    #endif

    #ifdef HAS_TARGET_NORMAL1
    normal.xyz += u_morphWeights[1] * a_Target_Normal1;
    #endif

    #ifdef HAS_TARGET_NORMAL2
    normal.xyz += u_morphWeights[2] * a_Target_Normal2;
    #endif

    #ifdef HAS_TARGET_NORMAL3
    normal.xyz += u_morphWeights[3] * a_Target_Normal3;
    #endif

    #ifdef HAS_TARGET_NORMAL4
    normal.xyz += u_morphWeights[4] * a_Target_Normal4;
    #endif

    return normal;
}

vec4 getTargetTangent()
{
    vec4 tangent = vec4(0);

    #ifdef HAS_TARGET_TANGENT0
    tangent.xyz += u_morphWeights[0] * a_Target_Tangent0;
    #endif

    #ifdef HAS_TARGET_TANGENT1
    tangent.xyz += u_morphWeights[1] * a_Target_Tangent1;
    #endif

    #ifdef HAS_TARGET_TANGENT2
    tangent.xyz += u_morphWeights[2] * a_Target_Tangent2;
    #endif

    #ifdef HAS_TARGET_TANGENT3
    tangent.xyz += u_morphWeights[3] * a_Target_Tangent3;
    #endif

    #ifdef HAS_TARGET_TANGENT4
    tangent.xyz += u_morphWeights[4] * a_Target_Tangent4;
    #endif

    return tangent;
}

    #endif// !USE_MORPHING

//END animation.glsl//////////////////

in vec4 a_Position;
out vec3 v_Position;

#ifdef HAS_NORMALS
in vec4 a_Normal;
#endif

#ifdef HAS_TANGENTS
in vec4 a_Tangent;
#endif

#ifdef HAS_NORMALS
#ifdef HAS_TANGENTS
out mat3 v_TBN;
#else
out vec3 v_Normal;
#endif
#endif

#ifdef HAS_UV_SET1
in vec2 a_UV1;
#endif

#ifdef HAS_UV_SET2
in vec2 a_UV2;
#endif

out vec2 v_UVCoord1;
out vec2 v_UVCoord2;

#ifdef HAS_VERTEX_COLOR_VEC3
in vec3 a_Color;
out vec3 v_Color;
#endif

#ifdef HAS_VERTEX_COLOR_VEC4
in vec4 a_Color;
out vec4 v_Color;
#endif

uniform mat4 u_ViewProjectionMatrix;
uniform mat4 u_ModelMatrix[MAX_INSTANCED_SIZE];
uniform mat4 u_NormalMatrix;

flat out int out_gl_InstanceID;

vec4 getPosition()
{
    vec4 pos = a_Position;

    #ifdef USE_MORPHING
    pos += getTargetPosition();
    #endif

    #ifdef USE_SKINNING
    pos = getSkinningMatrix() * pos;
    #endif

    return pos;
}

    #ifdef HAS_NORMALS
vec4 getNormal()
{
    vec4 normal = a_Normal;

    #ifdef USE_MORPHING
    normal += getTargetNormal();
    #endif

    #ifdef USE_SKINNING
    normal = getSkinningNormalMatrix() * normal;
    #endif

    return normalize(normal);
}
    #endif

    #ifdef HAS_TANGENTS
vec4 getTangent()
{
    vec4 tangent = a_Tangent;

    #ifdef USE_MORPHING
    tangent += getTargetTangent();
    #endif

    #ifdef USE_SKINNING
    tangent = getSkinningMatrix() * tangent;
    #endif

    return normalize(tangent);
}
    #endif

void main()
{
    out_gl_InstanceID = gl_InstanceID;
    vec4 pos = u_ModelMatrix[gl_InstanceID] * getPosition();
    v_Position = vec3(pos.xyz) / pos.w;

    #ifdef HAS_NORMALS
    #ifdef HAS_TANGENTS
    vec4 tangent = getTangent();
    vec3 normalW = normalize(vec3(u_NormalMatrix * vec4(getNormal().xyz, 0.0)));
    vec3 tangentW = normalize(vec3(u_ModelMatrix[gl_InstanceID] * vec4(tangent.xyz, 0.0)));
    vec3 bitangentW = cross(normalW, tangentW) * tangent.w;
    v_TBN = mat3(tangentW, bitangentW, normalW);
    #else// !HAS_TANGENTS
    v_Normal = normalize(vec3(u_NormalMatrix * vec4(getNormal().xyz, 0.0)));
    #endif
    #endif// !HAS_NORMALS

    v_UVCoord1 = vec2(0.0, 0.0);
    v_UVCoord2 = vec2(0.0, 0.0);

    #ifdef HAS_UV_SET1
    v_UVCoord1 = a_UV1;
    #endif

    #ifdef HAS_UV_SET2
    v_UVCoord2 = a_UV2;
    #endif

    #if defined(HAS_VERTEX_COLOR_VEC3) || defined(HAS_VERTEX_COLOR_VEC4)
    v_Color = a_Color;
    #endif

    gl_Position = u_ViewProjectionMatrix * pos;
}
