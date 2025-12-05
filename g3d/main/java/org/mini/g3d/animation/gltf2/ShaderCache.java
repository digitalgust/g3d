/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.animation.gltf2;


import org.mini.g3d.core.DisplayManager;
import org.mini.glwrap.GLUtil;
import org.mini.gui.GToolkit;

import java.util.*;
import java.util.Map.Entry;

//Inspiration from shader_cache.js

/**
 * Shader programs vary depending on attributes in a GLTFMeshPrimitive The shader files that
 * glTF-Sample-Viewer uses take configurations as #define [name] before the shader code.
 * <p>
 * This class looks at a GLTFMeshPrimitive and returns a correct AnimatedShader to render it.
 */
public class ShaderCache {

    private static class ShaderVars {

        public Integer glRef = -1;
        public List<String> varNames;

        ShaderVars(int glRef, List<String> varNames) {
            this.glRef = glRef;
            this.varNames = varNames;
        }
    }

    /**
     * Shader name -> source code
     */
    private static Map<String, String> sources;
    /**
     * name & permutations hashed -> compiled shader
     */
    private static Map<Integer, ShaderVars> vertShaders = new HashMap<>();
    private static Map<Integer, ShaderVars> fragShaders = new HashMap<>();
    /**
     * (vertex shader, fragment shader) -> program
     */
    private static Map<Integer, AnimatedShader> programs = new HashMap<>();

    static {
        sources = new HashMap<>();
        //Set up shader sources
        sources.put("gltfVertex.glsl", GToolkit.readFileFromJarAsString("/org/mini/g3d/res/shader/gltfVertex.glsl", "utf-8"));
        sources.put("gltfFragment.glsl", GToolkit.readFileFromJarAsString("/org/mini/g3d/res/shader/gltfFragment.glsl", "utf-8"));

        //key, source
        for (Entry<String, String> entry : sources.entrySet()) {
            boolean changed = false;
            String src = entry.getValue();

            //includeName, includeSource
            for (Entry<String, String> includeEntry : sources.entrySet()) {
                String pattern = "#include <" + includeEntry.getKey() + ">";

                if (src.contains(pattern)) {
                    src = src.replace(pattern, includeEntry.getValue());
                    src = src.replaceAll(pattern, "");
                    changed = true;
                }
            }
            if (changed) {
                sources.replace(entry.getKey(), src);
            }
        }
    }


    /**
     * eg "pbr.vert" ["NORMALS", "TANGENTS"]
     *
     * @param shaderIdentifier
     * @param permutationDefines
     * @return
     */
    public static int selectShader(String shaderIdentifier, List<String> permutationDefines) {
        String src = sources.get(shaderIdentifier);
        if (src == null) {
            System.err.println("[G3D][ERROR]Shader source for " + shaderIdentifier + " not found!");
            return -1;
        }

        boolean isVert = shaderIdentifier.contains("Vertex");
        int hash = shaderIdentifier.hashCode();

        //calc hash, 采用更快的方法计算相同配置的 shader
        Collections.sort(permutationDefines);
        for (int i = 0; i < permutationDefines.size(); i++) {
            String def = permutationDefines.get(i);
            hash *= def.hashCode();
        }

        StringBuilder sb = new StringBuilder();
        if (DisplayManager.getGlVersion().toLowerCase().contains("opengl es")) {
            sb.append("#version 300 es\n");
        } else {
            sb.append("#version 330\n"); //Put this in so it doesn't give a warning
        }
        for (int i = 0; i < permutationDefines.size(); i++) {
            String define = permutationDefines.get(i);
            sb.append("#define ").append(define).append('\n');
        }
        //hash *= sb.toString().hashCode();

        String finalShaderCode = sb.append(src).toString();

        Map<Integer, ShaderVars> shaders = isVert ? vertShaders : fragShaders;
        if (!shaders.containsKey(hash)) {
            int shader = GLDriver.compileShader(shaderIdentifier, isVert, finalShaderCode);
            ShaderVars vars = new ShaderVars(shader, permutationDefines);
            shaders.put(hash, vars);
        }
        return hash;
    }

    public static AnimatedShader getShaderProgram(int vertexShaderHash, int fragmentShaderHash) {
        int hash = vertexShaderHash * fragmentShaderHash;

        if (programs.containsKey(hash)) {
            return programs.get(hash);
        }

        //SysLog.info("G3D|compile a new shader.");
        ShaderVars vertVars = vertShaders.get(vertexShaderHash);
        ShaderVars fragVars = fragShaders.get(fragmentShaderHash);
        int programID = GLDriver.linkProgram(vertVars.glRef.intValue(), fragVars.glRef.intValue());
        AnimatedShader program = new AnimatedShader(programID);

        //glBindFragDataLocation(programId, 0, "fragColor"); //Personally defined always used
        programs.put(hash, program);

        return program;
    }

    public static AnimatedShader getShaderProgram(String vertIdentifier, List<String> vertDefines, String fragIdentifier, List<String> fragDefines) {
        int vertexHash = ShaderCache.selectShader(vertIdentifier, vertDefines);
        GLUtil.checkGlError("drawRenderObject 0.1");
        int fragmentHash = ShaderCache.selectShader(fragIdentifier, fragDefines);
        GLUtil.checkGlError("drawRenderObject 0.2");
        AnimatedShader program = getShaderProgram(vertexHash, fragmentHash);
        GLUtil.checkGlError("drawRenderObject 0.3");
        return program;
    }

    public static AnimatedShader getDebugShaderProgram() {
        List<String> vertDefines = new ArrayList<>();
        List<String> fragDefines = new ArrayList<>();
        fragDefines.add("DEBUG 1");
        fragDefines.add("DEBUG_NORMALS 1");
        int vertHash = selectShader("gltfVertex.glsl", vertDefines);
        int fragHash = selectShader("gltfFragment.glsl", fragDefines);
        return getShaderProgram(vertHash, fragHash);
    }
}
