/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.core.gltf2;


import org.mini.g3d.core.gltf2.loader.data.GLTFAccessor;
import org.mini.g3d.core.gltf2.loader.data.GLTFSampler;
import org.mini.g3d.core.gltf2.render.RenderEnvironmentMap;
import org.mini.g3d.core.gltf2.render.RenderTexture;
import org.mini.g3d.core.gltf2.render.Renderer;
import org.mini.gl.GL;
import org.mini.nanovg.Gutil;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mini.gl.GL.*;

public class GlUtil {


    private static final Map<GLTFAccessor, Integer> accessorGlBufferMap = new HashMap<>();
    static int[] max = {0};

    //  private static int max =
    static {
        glGetIntegerv(GL_MAX_TEXTURE_SIZE, max, 0);
    }

    public static int compileShader(String shaderIdentifier, boolean isVert, String shaderSource) {
        System.out.println("Begin compileShader: " + shaderIdentifier);
//    System.out.println(shaderSource);
        int shader = glCreateShader(isVert ? GL_VERTEX_SHADER : GL_FRAGMENT_SHADER);
//    glShaderSource(shader, shaderSource);
        glShaderSource(shader, 1, new byte[][]{Gutil.toUtf8(shaderSource)}, null, 0);
        glCompileShader(shader);

        int[] return_val = {0};
        glGetShaderiv(shader, GL_COMPILE_STATUS, return_val, 0);

        if (return_val[0] == GL_FALSE) {
            GL.glGetShaderiv(shader, GL.GL_INFO_LOG_LENGTH, return_val, 0);
            byte[] szLog = new byte[return_val[0] + 1];
            GL.glGetShaderInfoLog(shader, szLog.length, return_val, 0, szLog);
            System.out.println("Compile Shader fail error :" + new String(szLog, 0, return_val[0]) + "\n" + shaderSource + "\n");
            return 0;
        }
        System.out.println("End comileShader: " + shaderIdentifier);
        return shader;
    }

    public static int linkProgram(int vertex, int fragment) {
        System.out.println("Begin linkProgram: vertex = " + vertex + ", fragment = " + fragment);
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
            System.out.println("Link Shader fail error :" + new String(szLog, 0, return_val[0]) + "\n vertex shader:" + vertex + "\nfragment shader:" + fragment + "\n");
            return 0;
        }

        //Kinda useless to have here
        glValidateProgram(program);
        GL.glGetProgramiv(program, GL.GL_LINK_STATUS, return_val, 0);
        if (return_val[0] == GL_FALSE) {
            GL.glGetProgramiv(program, GL.GL_INFO_LOG_LENGTH, return_val, 0);
            byte[] szLog = new byte[return_val[0] + 1];
            GL.glGetProgramInfoLog(program, szLog.length, return_val, 0, szLog);
            System.out.println("Validate Shader fail error :" + new String(szLog, 0, return_val[0]) + "\n vertex shader:" + vertex + "\nfragment shader:" + fragment + "\n");
            return 0;
        }
        System.out.println("End linkProgram: vertex = " + vertex + ", fragment = " + fragment + ", program = "
                + program);
        return program;
    }

    public static void setIndices(GLTFAccessor accessor) {
        if (!accessorGlBufferMap.containsKey(accessor)) {
            int[] glBuffer = {0};
            glGenBuffers(1, glBuffer, 0);
            accessorGlBufferMap.put(accessor, glBuffer[0]);

            ByteBuffer bbuf = accessor.getData();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, glBuffer[0]);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER,
                    accessor.getSizeInBytes(),
//          MemoryUtil.memAddress(accessor.getData()),
                    bbuf.array(),
                    bbuf.arrayOffset(),
                    GL_STATIC_DRAW);
        } else {
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, accessorGlBufferMap.get(accessor));
        }
    }

    public static void enableAttribute(int attributeLocation, GLTFAccessor accessor) {
//    System.out.println("Begin enableAttribute: location = " + attributeLocation);
        if (!accessorGlBufferMap.containsKey(accessor)) {
            //System.out.println("Generating buffer: " + accessor.toString());
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
            accessorGlBufferMap.put(accessor, glBuffer[0]);
        } else {
            glBindBuffer(GL_ARRAY_BUFFER, accessorGlBufferMap.get(accessor));
        }

        glVertexAttribPointer(attributeLocation, accessor.getType().getPrimitiveCount(),
                accessor.getGLType(), accessor.isNormalized() ? GL_TRUE : GL_FALSE, accessor.getByteStride(), null, 0);
        glEnableVertexAttribArray(attributeLocation);
//    System.out.println("End enableAttribute: location = " + attributeLocation);
    }

    public static boolean setTexture(int location, RenderTexture renderTexture, int texSlot) {
        return setTexture(location, renderTexture, texSlot, true);
    }

    private static boolean setTexture(int location, RenderTexture renderTexture, int texSlot,
                                      boolean useSampler) {
        if (renderTexture == null) {
            System.out.println("Texture is undefined: " + renderTexture.toString());
            return false;
        }

        //Activate a texture slot for new texture
        glActiveTexture(GL_TEXTURE0 + texSlot);
        //Bind renderTexture to slot
        glBindTexture(renderTexture.getType(), renderTexture.getGlTexture());

        //Load texture location into shader uniform
        glUniform1i(location, texSlot);

        if (!renderTexture.isInitialized()) {
            System.out.println("Begin init texture");

            GLTFSampler sampler = renderTexture.getSampler();
            if (useSampler) {
                if (sampler == null) {
                    System.out.println("Sampler is undefined for texture: " + renderTexture.toString());
                    return false;
                }
            }

            loadImageToTexture(renderTexture);

            if (useSampler) {
                int wrapS = sampler.getWrapS().getValue();
                int wrapT = sampler.getWrapT().getValue();
                int minFilter = sampler.getMinFilter().getValue();
                int maxFilter = sampler.getMagFilter().getValue();
                setSampler(wrapS, wrapT, minFilter, maxFilter, renderTexture.getType(),
                        renderTexture.shouldGenerateMips() && Renderer.generateMipmaps);
                renderTexture.setInitialized(true);
            }

            System.out.println("End init texture");
        }
        return true;
    }

    private static void loadImageToTexture(RenderTexture renderTexture) {
        ByteBuffer buffer = renderTexture.loadData(); //Must load before width/height are available
        int type = renderTexture.getType();
        int width = renderTexture.getTextureWidth();
        int height = renderTexture.getTextureHeight();
        int pixBytes = renderTexture.getPixBytes();
        int glType = pixBytes < 4 ? GL_RGB : GL_RGBA;
        glTexImage2D(type, renderTexture.getMipLevel(), glType, width, height, 0, glType, GL_UNSIGNED_BYTE, buffer.array(), 0);
    }

    public static void setCubeMap(ShaderProgram shader, RenderEnvironmentMap envData, int texSlot) {
        List<RenderTexture> diffuseMap = envData.getDiffuseEnvMap();
        int wrapS = GL_CLAMP_TO_EDGE;
        int wrapT = GL_CLAMP_TO_EDGE;
        int minFilter = GL_LINEAR;
        int maxFilter = GL_LINEAR;

        applyCubeMapType(shader,
                "u_DiffuseEnvSampler",
                diffuseMap,
                texSlot++,
                wrapS,
                wrapT,
                minFilter,
                maxFilter,
                Renderer.generateMipmaps
        );

        List<RenderTexture> specularMap = envData.getSpecularEnvMap();
        wrapS = GL_CLAMP_TO_EDGE;
        wrapT = GL_CLAMP_TO_EDGE;
        minFilter = GL_LINEAR_MIPMAP_LINEAR;
        maxFilter = GL_LINEAR;

        applyCubeMapType(shader,
                "u_SpecularEnvSampler",
                specularMap,
                texSlot++,
                wrapS,
                wrapT,
                minFilter,
                maxFilter,
                false); //Do not generate mips for specular

        //Lut
        RenderTexture lut = envData.getLut();
        GlUtil.setTexture(shader.getUniformLocation("u_brdfLUT"), lut, texSlot++, false);
        setSampler(GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE, GL_LINEAR, GL_LINEAR, lut.getType(),
                false); //Do not generate mips for lut
        lut.setInitialized(true);
    }

    private static void applyCubeMapType(ShaderProgram shader,
                                         String uniform,
                                         List<RenderTexture> textures,
                                         int texSlot,
                                         int wrapS,
                                         int wrapT,
                                         int minFilter,
                                         int maxFilter,
                                         boolean generateMipmaps) {
        int location = shader.getUniformLocation(uniform);
        for (RenderTexture renderTexture : textures) {
            //The cube map texture must be first
            if (renderTexture.getType() == GL_TEXTURE_CUBE_MAP) {
                //Activate a texture slot for new texture
                glActiveTexture(GL_TEXTURE0 + texSlot);

                //Bind renderTexture to slot
                glBindTexture(renderTexture.getType(), renderTexture.getGlTexture());

                //Load texture location into shader uniform
                glUniform1i(location, texSlot);

                renderTexture.setInitialized(true); //Set initialized so it doesn't try to load the texture
            }

            if (!renderTexture.isInitialized()) {
                System.out.println("Loading cubemap texture");
                loadImageToTexture(renderTexture);
                renderTexture.setInitialized(true);
            }
        }
        setSampler(wrapS, wrapT, minFilter, maxFilter, GL_TEXTURE_CUBE_MAP, generateMipmaps);

    }

    private static void setSampler(int wrapS, int wrapT, int minFilter, int maxFilter, int target,
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
}
