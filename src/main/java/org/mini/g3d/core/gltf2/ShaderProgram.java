/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.core.gltf2;


import org.mini.g3d.core.gltf2.render.light.UniformLight;
import org.mini.g3d.core.vector.*;
import org.mini.nanovg.Gutil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.mini.gl.GL.*;
import static org.mini.nanovg.Gutil.toUtf8;

//Inspiration from shader.js and LWJGL3 book
public class ShaderProgram {


    private int programId;
    private int hash;
    private Map<String, UniformData> uniforms = new HashMap<>();
    private Map<String, Integer> attributes = new HashMap<>();

    private List<String> unknownAttributes = new ArrayList<>();
    private List<String> unknownUniforms = new ArrayList<>();

    public ShaderProgram(int programId, int hash) {
        this.programId = programId;
        this.hash = hash;

        int[] sizeB = {0};
        int[] typeB = {0};

        int[] uniformCount = {0};
        glGetProgramiv(programId, GL_ACTIVE_UNIFORMS, uniformCount, 0);
        int[] strLen = {0};
        glGetProgramiv(programId, GL_ACTIVE_UNIFORM_MAX_LENGTH, strLen, 0);
        byte[] namebuf = new byte[strLen[0]];
        int[] actuallyLen = {0};
        for (int i = 0; i < uniformCount[0]; ++i) {
            sizeB[0] = 0;
            typeB[0] = 0;
            glGetActiveUniform(programId, i, strLen[0], actuallyLen, 0, sizeB, 0, typeB, 0, namebuf);
            String info = Gutil.fromUtf8(namebuf);
            int loc = glGetUniformLocation(programId, namebuf);
            uniforms.put(info, new UniformData(loc, typeB[0]));
            //System.out.println("ShaderProgram = " + programId + ", uniform = " + info + ", " + loc);
        }

        int[] attribCount = {0};
        glGetProgramiv(programId, GL_ACTIVE_ATTRIBUTES, attribCount, 0);
        for (int i = 0; i < attribCount[0]; ++i) {
            sizeB[0] = 0;
            typeB[0] = 0;
            glGetActiveAttrib(programId, i, strLen[0], actuallyLen, 0, sizeB, 0, typeB, 0, namebuf);
            String info = Gutil.fromUtf8(namebuf);
            int loc = glGetAttribLocation(programId, namebuf);
            attributes.put(info, loc);
            //System.out.println("ShaderProgram = " + programId + ", attribute = " + info + ", " + loc);
        }
    }

    public int getProgramId() {
        return programId;
    }

    public void setUniform(String uniformName, Matrix4f value) {
        int loc = getUniformLocation(uniformName);
        if (loc > -1) {
            glUniformMatrix4fv(loc, 1, GL_FALSE, value.mat, 0);
        }
    }

    public void setUniform(String uniformName, Matrix3f value) {
        int loc = getUniformLocation(uniformName);
        if (loc > -1) {
            glUniformMatrix3fv(loc, 1, GL_FALSE, value.mat, 0);
        }
    }

    public void setUniform(String uniformName, Vector4f value) {
        int loc = getUniformLocation(uniformName);
        if (loc > -1) {
            glUniform4f(loc, value.x, value.y, value.z, value.w);
        }
    }

    public void setUniform(String uniformName, Vector3f value) {
        int loc = getUniformLocation(uniformName);
        if (loc > -1) {
            glUniform3f(loc, value.x, value.y, value.z);
        }
    }

    public void setUniform(String uniformName, Vector2f value) {
        int loc = getUniformLocation(uniformName);
        if (loc > -1) {
            glUniform2f(loc, value.x, value.y);
        }
    }

    public void setUniform(String uniformName, float value) {
        int loc = getUniformLocation(uniformName);
        if (loc > -1) {
            glUniform1f(loc, value);
        }
    }

    public void setUniform(String uniformName, int value) {
        int loc = getUniformLocation(uniformName);
        if (loc > -1) {
            glUniform1i(loc, value);
        }
    }

    public void setUniform(String uniformName, float[] value) {
        int loc = getUniformLocation(
                uniformName + "[0]"); //Only the [0] of an array is stored in uniform list
        if (loc > -1) {
            glUniform1fv(loc, 1, value, 0);
        }
    }

    public void setUniform(String uniformName, Matrix4f[] value) {
        for (int i = 0; i < value.length; i++) {
            String arrayName = String.format("%s[%d]", uniformName, i);
            setUniform(arrayName, value[i]);
        }
    }

    public void setUniform(String uniformName, Object value) {
        if (value instanceof Float) {
            setUniform(uniformName, (float) value);
            return;
        }
        if (value instanceof Vector4f) {
            setUniform(uniformName, (Vector4f) value);
            return;
        }
        if (value instanceof Vector3f) {
            setUniform(uniformName, (Vector3f) value);
            return;
        }
        if (value instanceof Vector2f) {
            setUniform(uniformName, (Vector2f) value);
            return;
        }
        if (value instanceof Integer) {
            setUniform(uniformName, (int) value);
            return;
        }
        if (value instanceof Object[]) {//We most likely don't want this to actually be called
            System.out.println("WARNING: setUniform is using an Object array, you probably don't want this");
            Object[] arr = (Object[]) value;
            int i = 0;
            for (Object obj : arr) {
                setUniform(uniformName + "[" + i + "]", obj);
                i++;
            }
            return;
        }
        System.out.println("Unhandled type in setUniform: " + value.getClass());
        assert false;
    }

    public void setUniform(String structName, List<UniformLight> lightList) {
        Field[] fields = UniformLight.class.getDeclaredFields();
        int i = 0;
        for (UniformLight uniformLight : lightList) {
            for (Field field : fields) {
                String uniformName = structName + "[" + i + "]." + field.getName();
                try {
                    setUniform(uniformName, field.get(uniformLight));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            i++;
        }
    }

    static String patternStr = "\\[[0-9]+\\]";
    private static Pattern arrayPattern = Pattern.compile(patternStr);

    public int getUniformLocation(String uniformName) {

        UniformData data = uniforms.get(uniformName);
        if (data == null) {
            int loc = glGetUniformLocation(programId, toUtf8(uniformName));
            if (loc == -1) {
                unknownUniforms.add(uniformName);
                System.out.println("Uniform " + uniformName + " does not exist");
            } else {
                data = new UniformData(loc, 0);
                uniforms.put(uniformName, data);
            }
        }
        return data.loc;
    }

    public int getAttributeLocation(String name) {
        //TODO attribute array location like above for uniforms
        Integer loc = attributes.get(name);
        if (loc == null) {
            if (!unknownAttributes.contains(name)) {
                unknownAttributes.add(name);
                System.out.println("Attribute " + name + " does not exist ");
            }
            return -1;
        }
        return loc;
    }

    private class UniformData {

        int loc;
        int type;

        UniformData(int loc, int type) {
            this.loc = loc;
            this.type = type;
        }
    }
}
