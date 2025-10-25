package org.mini.g3d.core;

import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.core.vector.Vector2f;
import org.mini.g3d.core.vector.Vector3f;
import org.mini.g3d.core.vector.Vector4f;
import org.mini.gl.GL;
import org.mini.glwrap.GLUtil;
import org.mini.gui.GForm;
import org.mini.gui.GToolkit;
import org.mini.gui.callback.GCmd;
import org.mini.util.SysLog;

import static org.mini.gl.GL.*;

public abstract class ShaderProgram {

    private int programID;

    static byte[] gl_version = {'v', 'e', 'r', 's', 'i', 'o', 'n', ' ', '3', '3', '0'};
    static byte[] gles_version = {'v', 'e', 'r', 's', 'i', 'o', 'n', ' ', '3', '0', '0'};

    float[] mbuf = new float[16];

    public static final int LOAD_FROM_JAR = 0;
    public static final int LOAD_FROM_FILE = 1;
    public static final int LOAD_FROM_CONTENT = 2;


    /**
     * 从文件加载shader
     *
     * @param vertexFile
     * @param fragmentFile
     * @param loadFromFile
     */
    public ShaderProgram(String vertexFile, String fragmentFile, int loadFromFile) {
        long start = System.currentTimeMillis();
        if (loadFromFile == LOAD_FROM_FILE) {
            String vcontent = GToolkit.readFileFromFileAsString(vertexFile, "utf-8");
            int vertexShaderID = loadShaderContents(vcontent, vertexFile, GL_VERTEX_SHADER);
            String fcontent = GToolkit.readFileFromFileAsString(fragmentFile, "utf-8");
            int fragmentShaderID = loadShaderContents(fcontent, fragmentFile, GL_FRAGMENT_SHADER);
            link(vertexShaderID, fragmentShaderID);
        } else if (loadFromFile == LOAD_FROM_JAR) {
            String vcontent = GToolkit.readFileFromJarAsString(vertexFile, "utf-8");
            int vertexShaderID = loadShaderContents(vcontent, vertexFile, GL_VERTEX_SHADER);
            String fcontent = GToolkit.readFileFromJarAsString(fragmentFile, "utf-8");
            int fragmentShaderID = loadShaderContents(fcontent, fragmentFile, GL_FRAGMENT_SHADER);
            link(vertexShaderID, fragmentShaderID);
        } else if (loadFromFile == LOAD_FROM_CONTENT) {
            int vertexShaderID = loadShaderContents(vertexFile, "vertfile", GL_VERTEX_SHADER);
            int fragmentShaderID = loadShaderContents(fragmentFile, "fragmentfile", GL_FRAGMENT_SHADER);
            link(vertexShaderID, fragmentShaderID);
        }
        SysLog.info("G3D|init shader " + programID + " time:" + (System.currentTimeMillis() - start));
    }

    /**
     * 从jar包加载shader
     *
     * @param vertexFile
     * @param fragmentFile
     */
    public ShaderProgram(String vertexFile, String fragmentFile) {
        this(vertexFile, fragmentFile, LOAD_FROM_JAR);
    }

    private void link(int vertexShaderID, int fragmentShaderID) {

        programID = glCreateProgram();
        // GLUtil.checkGlError("init 0");
        glAttachShader(programID, vertexShaderID);
        // GLUtil.checkGlError("init 1");
        glAttachShader(programID, fragmentShaderID);
        // GLUtil.checkGlError("init 2");

        bindAttributes();

        // GLUtil.checkGlError("init 3");
        glLinkProgram(programID);

        // GLUtil.checkGlError("init 4");

        glValidateProgram(programID);
        // GLUtil.checkGlError("init 5");
        getAllUniformLocations();
        // SysLog.info("G3D|init shader link " + vertexShaderID + " + " +
        // fragmentShaderID + " = " + programID);
        // GLUtil.checkGlError("init 8");


        // //把链接成功的programID生成的shader数据从显卡中取出，并保存为文件
        // // 获取并保存着色器二进制数据
        // int[] length = new int[1];
        // int[] binaryFormat = new int[1];
        // glGetProgramiv(programID, GL.GL_PROGRAM_BINARY_LENGTH, length, 0);
        // byte[] binary = new byte[length[0]];
        // GL.glGetProgramBinary(programID, binary.length, length, 0, binaryFormat, 0, binary);
        //
        // // 这里可以添加代码将binary保存到文件
        // // GToolkit.writeFile("shader.bin", binary);

        // release vert and frag
        glDetachShader(programID, vertexShaderID);
        glDetachShader(programID, fragmentShaderID);
        glDeleteShader(vertexShaderID);
        glDeleteShader(fragmentShaderID);
    }

    public ShaderProgram(int vertexShaderID, int fragmentShaderID) {
        link(vertexShaderID, fragmentShaderID);
    }

    public ShaderProgram(int programID) {
        this.programID = programID;
    }

    protected abstract void getAllUniformLocations();

    protected int getUniformLocation(String uniformName) {
        return glGetUniformLocation(programID, GLUtil.toCstyleBytes(uniformName));
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

    protected void finalize() {
        GForm.addCmd(new GCmd(() -> {
            cleanUp();
            SysLog.info("G3D|" + this.getClass() + "shader program clean success");
        }));
    }

    protected abstract void bindAttributes();

    protected void bindAttribute(int attribute, String variableName) {
        glBindAttribLocation(programID, attribute, GLUtil.toCstyleBytes(variableName));
    }

    public void loadFloatArr(int location, float[] value) {
        glUniform1fv(location, value.length, value, 0);
    }

    protected void loadFloat(int location, float value) {
        glUniform1f(location, value);
    }

    public void loadIntArr(int location, int[] value) {
        glUniform1iv(location, value.length, value, 0);
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
        // matrix.store(mbuf);
        glUniformMatrix4fv(location, 1, GL_FALSE, matrix.mat, 0);// gust
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
        if (value instanceof Matrix4f[]) {// We most likely don't want this to actually be called
            loadMatrix(location, (Matrix4f) value);
            return;
        }
        SysLog.info("G3D|Unhandled type in setUniform: " + value.getClass());
        assert false;
    }

    protected String preProcessShader(String shader, int type) {
        return shader;
    }

    private int loadShaderContents(String sstr, String fileName, int type) {

        sstr = preProcessShader(sstr, type);
        sstr = adapteOpenGLorES(sstr);
        byte[] sb = null;
        try {
            sb = sstr.getBytes("utf-8");
        } catch (Exception e) {
            e.printStackTrace();
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
            SysLog.error("G3D|Compile Shader fail.file:" + fileName + ", error :" + new String(szLog, 0, return_val[0])
                    + ":" + fileName);
            // System.exit(-1);
        }
        return shaderID;
    }

    public static String adapteOpenGLorES(String shaderBytes) {
        if (DisplayManager.getGlVersion().toLowerCase().contains("opengl es")) {
            shaderBytes = shaderBytes.replace("#version 330",
                    "#version 300 es\r\nprecision highp float;\r\nprecision highp sampler2DShadow;\r\nprecision highp sampler2D;\r\nprecision highp samplerCube;\r\n");

        }
        return shaderBytes;
    }

}
