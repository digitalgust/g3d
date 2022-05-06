package org.mini.g3d.core;

import org.mini.g3d.core.vector.*;
import org.mini.gl.GL;
import org.mini.gui.GCmd;
import org.mini.gui.GForm;
import org.mini.gui.GToolkit;

import static org.mini.gl.GL.*;
import static org.mini.glwrap.GLUtil.toUtf8;

public abstract class ShaderProgram {

    private int programID;
    private int vertexShaderID;
    private int fragmentShaderID;

    static byte[] gl_version = {'v', 'e', 'r', 's', 'i', 'o', 'n', ' ', '3', '3', '0'};
    static byte[] gles_version = {'v', 'e', 'r', 's', 'i', 'o', 'n', ' ', '3', '0', '0'};

    float[] mbuf = new float[16];

    public ShaderProgram(String vertexFile, String fragmentFile) {
        vertexShaderID = loadShader(vertexFile, GL_VERTEX_SHADER);
        fragmentShaderID = loadShader(fragmentFile, GL_FRAGMENT_SHADER);
        init(vertexShaderID, fragmentShaderID);
        glDeleteShader(vertexShaderID);
        glDeleteShader(fragmentShaderID);
    }

    public ShaderProgram(int vertexShaderID, int fragmentShaderID) {
        init(vertexShaderID, fragmentShaderID);
    }

    public void init(int vertexShaderID, int fragmentShaderID) {

        programID = glCreateProgram();
        glAttachShader(programID, vertexShaderID);
        glAttachShader(programID, fragmentShaderID);
        bindAttributes();
        glLinkProgram(programID);

        glDetachShader(programID, vertexShaderID);
        glDetachShader(programID, fragmentShaderID);


        glValidateProgram(programID);
        getAllUniformLocations();
        //System.out.println("init shader "+vertexFile+" , "+fragmentFile+" :"+programID);
    }

    protected abstract void getAllUniformLocations();

    protected int getUniformLocation(String uniformName) {
        return glGetUniformLocation(programID, toUtf8(uniformName));
    }

    public void start() {
        glUseProgram(programID);
    }

    public void stop() {
        glUseProgram(0);
    }

    public void cleanUp() {
        stop();
        glDeleteProgram(programID);
    }

    public int getProgramId() {
        return programID;
    }
    /**
     * must be static,because finalize would be destroy ext class in minijvm
     */
    static class Cleaner implements Runnable {
        int programID;


        @Override
        public void run() {
            glUseProgram(0);
            glDeleteProgram(programID);
            System.out.println("g3d shader program clean success");
        }
    }

    public void finalize() {
        //Don't reference to this instance
        ShaderProgram.Cleaner cleaner = new ShaderProgram.Cleaner();
        cleaner.programID = programID;
        GForm.addCmd(new GCmd(GCmd.GCMD_RUN_CODE, cleaner));
    }

    protected abstract void bindAttributes();

    protected void bindAttribute(int attribute, String variableName) {
        glBindAttribLocation(programID, attribute, toUtf8(variableName));
    }

    public void loadFloatArr(int location, float[] value) {
        glUniform1fv(location, 1, value, 0);
    }

    protected void loadFloat(int location, float value) {
        glUniform1f(location, value);
    }

    protected void loadInt(int location, int value) {
        glUniform1i(location, value);
    }

    protected void loadVector(int location, Vector3f vector) {
        glUniform3f(location, vector.x, vector.y, vector.z);
    }

    protected void loadVector4f(int location, Vector4f vector) {
        glUniform4f(location, vector.x, vector.y, vector.z, vector.w);
    }

    protected void loadVector2D(int location, Vector2f vector) {
        glUniform2f(location, vector.x, vector.y);
    }

    protected void loadBoolean(int location, boolean value) {
        float toLoad = 0;
        if (value) {
            toLoad = 1;
        }
        glUniform1f(location, toLoad);
    }

    protected void loadMatrix(int location, Matrix4f matrix) {
        //matrix.store(mbuf);
        glUniformMatrix4fv(location, 1, GL_FALSE, matrix.mat, 0);//gust
    }


    public void loadUniform(int location, Object value) {
        if (value instanceof Float) {
            loadFloat(location, (float) value);
            return;
        }
        if (value instanceof Vector4f) {
            loadVector4f(location, (Vector4f) value);
            return;
        }
        if (value instanceof Vector3f) {
            loadVector(location, (Vector3f) value);
            return;
        }
        if (value instanceof Vector2f) {
            loadVector2D(location, (Vector2f) value);
            return;
        }
        if (value instanceof Integer) {
            loadInt(location, (int) value);
            return;
        }
        if (value instanceof Matrix4f[]) {//We most likely don't want this to actually be called
            loadMatrix(location, (Matrix4f) value);
            return;
        }
        System.out.println("Unhandled type in setUniform: " + value.getClass());
        assert false;
    }


    private static int loadShader(String file, int type) {

        byte[] sb = GToolkit.readFileFromJar(file);
        if (EngineManager.getGlVersion().toLowerCase().contains("opengl es")) {
            String s = new String(sb);
            s = s.replace("#version 330", "#version 300 es\r\nprecision highp float;\r\nprecision highp sampler2DShadow;\r\n");
            sb = s.getBytes();
        }
        int shaderID = glCreateShader(type);
        glShaderSource(shaderID, 1, new byte[][]{sb}, new int[]{sb.length}, 0);
        glCompileShader(shaderID);
        int[] return_val = {0};
        glGetShaderiv(shaderID, GL_COMPILE_STATUS, return_val, 0);
        if (return_val[0] == GL_FALSE) {
            GL.glGetShaderiv(shaderID, GL.GL_INFO_LOG_LENGTH, return_val, 0);
            byte[] szLog = new byte[return_val[0] + 1];
            GL.glGetShaderInfoLog(shaderID, szLog.length, return_val, 0, szLog);
            System.out.println("Compile Shader fail error :" + new String(szLog, 0, return_val[0]) + ":" + file);
            //System.exit(-1);
        }
        return shaderID;
    }

}
