/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.animation.gltf2;


import org.mini.g3d.animation.gltf2.loader.data.GLTFAccessor;
import org.mini.g3d.animation.gltf2.loader.data.GLTFSampler;
import org.mini.g3d.animation.gltf2.loader.data.GLTFTextureInfo;
import org.mini.g3d.animation.gltf2.render.GLTFRenderer;
import org.mini.g3d.animation.gltf2.render.RenderTexture;
import org.mini.gl.GL;
import org.mini.glwrap.GLUtil;
import org.mini.util.SysLog;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import org.mini.g3d.animation.gltf2.render.RenderMeshPrimitive;
import org.mini.g3d.core.vector.Vector4f;

import static org.mini.gl.GL.*;

public class GLDriver {


    //todo gust what time to clear the cache
    private static final Map<Object, int[]> texInfo2GlTextureMap = new HashMap<>();
    private static final Map<GLTFAccessor, int[]> accessor2GlBufferMap = new HashMap<>();
    static int[] max = {0};

    //  private static int max =
    static {
        glGetIntegerv(GL_MAX_TEXTURE_SIZE, max, 0);
    }

    public static int compileShader(String shaderIdentifier, boolean isVert, String shaderSource) {
//        SysLog.info("G3D|gltf shader compile begin: " + shaderIdentifier);
//    SysLog.info("G3D|" + shaderSource);
        int shader = glCreateShader(isVert ? GL_VERTEX_SHADER : GL_FRAGMENT_SHADER);
//    glShaderSource(shader, shaderSource);
        glShaderSource(shader, 1, new byte[][]{GLUtil.toCstyleBytes(shaderSource)}, null, 0);
        glCompileShader(shader);

        int[] return_val = {0};
        glGetShaderiv(shader, GL_COMPILE_STATUS, return_val, 0);

        if (return_val[0] == GL_FALSE) {
            GL.glGetShaderiv(shader, GL.GL_INFO_LOG_LENGTH, return_val, 0);
            byte[] szLog = new byte[return_val[0] + 1];
            GL.glGetShaderInfoLog(shader, szLog.length, return_val, 0, szLog);
            SysLog.error("G3D|gltf shader compile fail :" + new String(szLog, 0, return_val[0]) + "\n" + shaderSource + "\n");
            return 0;
        }
//        SysLog.info("G3D|gltf shader compile finish " + shader + " : " + shaderIdentifier);
        return shader;
    }

    public static int linkProgram(int vertex, int fragment) {
        long startAt = System.currentTimeMillis();
//        SysLog.info("G3D|gltf shader link begin vertex = " + vertex + ", fragment = " + fragment);
        int program = glCreateProgram();
        glAttachShader(program, vertex);
        glAttachShader(program, fragment);

        glLinkProgram(program);
        int[] return_val = {0};
        GL.glGetProgramiv(program, GL.GL_LINK_STATUS, return_val, 0);
        if (return_val[0] == GL_FALSE) {
            GL.glGetProgramiv(program, GL.GL_INFO_LOG_LENGTH, return_val, 0);
            byte[] szLog = new byte[return_val[0] + 1];
            GL.glGetProgramInfoLog(program, szLog.length, return_val, 0, szLog);
            SysLog.error("G3D|gltf shader link error :" + new String(szLog, 0, return_val[0]) + "\n vertex shader:" + vertex + "\nfragment shader:" + fragment + "\n");
            return 0;
        }

        // ShaderCache owns compiled stages and can link them into multiple programs.
        // Deleting a cached stage here causes GL_INVALID_OPERATION on subsequent links.
        glDetachShader(program, vertex);
        glDetachShader(program, fragment);
//        SysLog.info("G3D|gltf shader link finish, vertex = " + vertex + ", fragment = " + fragment + ", program = "
//                + program + " time = " + (System.currentTimeMillis() - startAt));
        return program;
    }

    private static int instanceGlowVbo;
    private static final float[] instanceGlowData = new float[AnimatedShader.MAX_INSTANCED_SIZE * 4];

    public static void bindInstanceGlow(int location, List<RenderMeshPrimitive> instances) {
        if (location < 0) return;
        if (instances.size() > AnimatedShader.MAX_INSTANCED_SIZE) {
            throw new IllegalArgumentException("Instance glow batch exceeds capacity");
        }
        for (int i = 0; i < instances.size(); i++) {
            Vector4f glow = instances.get(i).getInstanceGlow();
            int offset = i * 4;
            instanceGlowData[offset] = glow.x;
            instanceGlowData[offset + 1] = glow.y;
            instanceGlowData[offset + 2] = glow.z;
            instanceGlowData[offset + 3] = glow.w;
        }
        if (instanceGlowVbo == 0) {
            int[] buffer = new int[1];
            glGenBuffers(1, buffer, 0);
            instanceGlowVbo = buffer[0];
        }
        glBindBuffer(GL_ARRAY_BUFFER, instanceGlowVbo);
        glBufferData(GL_ARRAY_BUFFER, instances.size() * 4 * 4, instanceGlowData, 0, GL_STREAM_DRAW);
        glEnableVertexAttribArray(location);
        glVertexAttribPointer(location, 4, GL_FLOAT, GL_FALSE, 4 * 4, null, 0);
        glVertexAttribDivisor(location, 1);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public static void setIndices(GLTFAccessor accessor) {
        if (!accessor2GlBufferMap.containsKey(accessor)) {
            int[] glBuffer = {-1, -1};
            glGenVertexArrays(1, glBuffer, 1);
            glBindVertexArray(glBuffer[1]);
            glGenBuffers(1, glBuffer, 0);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, glBuffer[0]);

            accessor2GlBufferMap.put(accessor, glBuffer);
//            SysLog.info("G3D|GLDriver setIndices create new buffer, map size: " + accessor2GlBufferMap.size());

            ByteBuffer bbuf = accessor.getData();
            glBufferData(GL_ELEMENT_ARRAY_BUFFER,
                    accessor.getSizeInBytes(),
//          MemoryUtil.memAddress(accessor.getData()),
                    bbuf.array(),
                    bbuf.arrayOffset(),
                    GL_STATIC_DRAW);
            accessor.clearData();
        } else {
            int[] glBuffer = (int[]) accessor2GlBufferMap.get(accessor);
            glBindVertexArray(glBuffer[1]);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, glBuffer[0]);
        }
    }

    public static void enableAttribute(int attributeLocation, GLTFAccessor accessor) {
//    SysLog.info("G3D|Begin enableAttribute: location = " + attributeLocation);
        if (!accessor2GlBufferMap.containsKey(accessor)) {
            //SysLog.info("G3D|Generating buffer: " + accessor.toString());
            int[] glBuffer = {0};
            glGenBuffers(1, glBuffer, 0);
            glBindBuffer(GL_ARRAY_BUFFER, glBuffer[0]);
            ByteBuffer bbuf = accessor.getData();
            glBufferData(GL_ARRAY_BUFFER,
                    accessor.getSizeInBytes(),
//          MemoryUtil.memAddress(accessor.getData()),
                    bbuf.array(),
                    bbuf.arrayOffset(),
                    GL_STATIC_DRAW);
            accessor2GlBufferMap.put(accessor, glBuffer);
//            SysLog.info("G3D|GLDriver enableAttribute create new buffer, loc: " + attributeLocation + ", map size: " + accessor2GlBufferMap.size());
            accessor.clearData();
        } else {
            int[] glBuffer = accessor2GlBufferMap.get(accessor);
            glBindBuffer(GL_ARRAY_BUFFER, glBuffer[0]);
        }
        glEnableVertexAttribArray(attributeLocation);

        glVertexAttribPointer(attributeLocation, accessor.getType().getPrimitiveCount(),
                accessor.getGLType(), accessor.isNormalized() ? GL_TRUE : GL_FALSE, accessor.getByteStride(), null, 0);
//    SysLog.info("G3D|End enableAttribute: location = " + attributeLocation);
    }

    private static void loadImageToTexture(RenderTexture renderTexture) {
        ByteBuffer buffer = renderTexture.loadData(); //Must load before width/height are available
        int type = renderTexture.getType();
        int width = renderTexture.getTextureWidth();
        int height = renderTexture.getTextureHeight();
        int pixBytes = renderTexture.getPixBytes();
        int glType = pixBytes < 4 ? GL_RGB : GL_RGBA;
        glTexImage2D(type, renderTexture.getMipLevel(), glType, width, height, 0, glType, GL_UNSIGNED_BYTE, buffer.array(), buffer.arrayOffset());
        renderTexture.clearData();
    }

    public static int getTexture(RenderTexture renderTexture, Object info) {
        int[] tex = texInfo2GlTextureMap.get(info);
        if (tex == null) {
            tex = new int[]{0};
            glGenTextures(1, tex, 0);
            glBindTexture(renderTexture.getType(), tex[0]);
            texInfo2GlTextureMap.put(info, tex);
//            SysLog.info("G3D|Begin init texture " + info);

            GLTFSampler sampler = renderTexture.getSampler();
//            if (useSampler) {
//                if (sampler == null) {
//                    SysLog.info("G3D|Sampler is undefined for texture: " + renderTexture.toString());
//                    return false;
//                }
//            }

            loadImageToTexture(renderTexture);

//            if (useSampler) {
            int wrapS = sampler.getWrapS().getValue();
            int wrapT = sampler.getWrapT().getValue();
            int minFilter = sampler.getMinFilter().getValue();
            int maxFilter = sampler.getMagFilter().getValue();
            setSampler(wrapS, wrapT, minFilter, maxFilter, renderTexture.getType(),
                    renderTexture.shouldGenerateMips() && GLTFRenderer.generateMipmaps);
//            }

//            SysLog.info("G3D|End init texture");
            return tex[0];
        }
        return tex[0];
    }

    public static boolean setTexture(int location, RenderTexture renderTexture, int texSlot) {
        return setTexture(location, renderTexture, texSlot, true);
    }

    private static boolean setTexture(int location, RenderTexture renderTexture, int texSlot,
                                      boolean useSampler) {
        if (renderTexture == null) {
            SysLog.warn("G3D|Texture is undefined: " + renderTexture.toString());
            return false;
        }

        //Activate a texture slot for new texture
        glActiveTexture(GL_TEXTURE0 + texSlot);
        //Bind renderTexture to slot
        renderTexture.bindTexture();

        //Load texture location into shader uniform
        glUniform1i(location, texSlot);

        return true;
    }


    public static void setSampler(int wrapS, int wrapT, int minFilter, int maxFilter, int target,
                                  boolean generateMipmaps) {
        if (generateMipmaps) {
            glTexParameteri(target, GL_TEXTURE_WRAP_S, wrapS);
            glTexParameteri(target, GL_TEXTURE_WRAP_T, wrapT);
        } else {
            glTexParameteri(target, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(target, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        }

        //If not mip-mapped, force to non-mip-mapped sampler
        if (!generateMipmaps && minFilter != GL_NEAREST && minFilter != GL_LINEAR) {
            if (minFilter == GL_NEAREST_MIPMAP_NEAREST || minFilter == GL_NEAREST_MIPMAP_LINEAR) {
                glTexParameteri(target, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            } else {
                glTexParameteri(target, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            }
        } else {
            glTexParameteri(target, GL_TEXTURE_MIN_FILTER, minFilter);
        }
        glTexParameteri(target, GL_TEXTURE_MAG_FILTER, maxFilter);

        //TODO anisotropy

        //Generate mipmaps
        if (generateMipmaps) {
            switch (minFilter) {
                case GL_NEAREST_MIPMAP_NEAREST:
                case GL_NEAREST_MIPMAP_LINEAR:
                case GL_LINEAR_MIPMAP_NEAREST:
                case GL_LINEAR_MIPMAP_LINEAR:
                    //TODO figure out why it looks like it applies to every texture
                    //Makes things super laggy at least
                    //TODO Need to have environment map generates for mipmaps
                    //TODO look at how sample-viewer uses mipmaps
                    glGenerateMipmap(target);
                    break;
                default:
                    break;
            }
        }
    }

    public static void cleanUp() {
        if (instanceGlowVbo != 0) {
            glDeleteBuffers(1, new int[]{instanceGlowVbo}, 0);
            instanceGlowVbo = 0;
        }
        ShaderCache.cleanUp();
        GLUtil.checkGlError("error 0.00");
        for (Iterator<Object> it = texInfo2GlTextureMap.keySet().iterator(); it.hasNext(); ) {
            Object info = it.next();
            int[] tex = texInfo2GlTextureMap.get(info);
            glDeleteTextures(1, tex, 0);
            it.remove();
            GLUtil.checkGlError("error tex: " + tex[0]);
        }

        GLUtil.checkGlError("error 5.00");
        for (Iterator<GLTFAccessor> it = accessor2GlBufferMap.keySet().iterator(); it.hasNext(); ) {
            GLTFAccessor accessor = it.next();
            int[] buf = accessor2GlBufferMap.get(accessor);
            glDeleteBuffers(1, buf, 0);
            it.remove();
            GLUtil.checkGlError("error buf: " + buf[0]);
        }
        SysLog.info("G3D|" + GLDriver.class + " cleanup");
        GLUtil.checkGlError("error 10.00");
    }

    public static void cleanUpBuffers() {
        int count = accessor2GlBufferMap.size();
        for (Iterator<GLTFAccessor> it = accessor2GlBufferMap.keySet().iterator(); it.hasNext(); ) {
            GLTFAccessor accessor = it.next();
            int[] buf = accessor2GlBufferMap.get(accessor);
            glDeleteBuffers(1, buf, 0);
            it.remove();
        }
        SysLog.info("G3D|" + GLDriver.class + " cleanup buffers, count: " + count);
    }
}
