package org.mini.g3d.core.util;

import org.mini.g3d.core.models.RawModel;
import org.mini.g3d.core.models.TexturedModel;
import org.mini.g3d.core.objmodel.ModelData;
import org.mini.g3d.core.objmodel.OBJFileLoader;
import org.mini.g3d.core.textures.Texture;
import org.mini.g3d.core.textures.TextureData;
import org.mini.gl.GL;
import org.mini.glwrap.GLUtil;
import org.mini.gui.GForm;
import org.mini.gui.callback.GCmd;
import org.mini.util.IntList;
import org.mini.util.SysLog;

import java.util.HashMap;
import java.util.Map;

import static org.mini.g3d.core.util.G3dUtil.loadFileFromJar;
import static org.mini.gl.GL.*;


/**
 * Loader load data and hold it's loaded
 * if loader released, all resource would be GC
 */
public class Loader {
    public static final int FLOAT_SIZE = 4, INT_SIZE = 4;

    private final IntList vaos = new IntList();
    private final IntList vbos = new IntList();
    private final IntList textures = new IntList();
    private final Map<String, Integer> path2texid = new HashMap();
    private final Map<String, Texture> path2tex = new HashMap();
    private final Map<String, RawModel> path2model = new HashMap();
    private final Map<String, TexturedModel> path2texmodel = new HashMap();

    int tmp[] = {0};

    //    public TexturedModel loadTexturedModel(String modelFileName, String textureFileName, int numberOfRows) {
//        final ModelData data = OBJFileLoader.loadOBJ(modelFileName);
//        final RawModel rawModel = loadToVAO(data.getVertices(), data.getTextureCoords(), data.getNormals(), data.getIndices());
//
//        Texture temp = new Texture(loadTexture(textureFileName));
//        if (numberOfRows > 1) {
//            temp.setNumberOfRows(numberOfRows);
//        }
//        return new TexturedModel(rawModel, temp);
//    }

    public TexturedModel loadTexturedModel(String modelFileName, String textureFileName, int numberOfRows) {

        String key = modelFileName + textureFileName + numberOfRows;
        TexturedModel tm = path2texmodel.get(key);
        if (tm == null) {
            RawModel rawModel = path2model.get(modelFileName);
            if (rawModel == null) {
                ModelData data = OBJFileLoader.loadOBJ(modelFileName);
                rawModel = loadToVAO(data.getVertices(), data.getTextureCoords(), data.getNormals(), data.getIndices());
                path2model.put(modelFileName, rawModel);
            } else {
                SysLog.info("G3D|found cached model:" + modelFileName);
            }


            Texture tex = path2tex.get(textureFileName);
            if (tex == null) {
                tex = new Texture(loadTexture(textureFileName));
                if (numberOfRows > 1) {
                    tex.setNumberOfRows(numberOfRows);
                }
                path2tex.put(textureFileName, tex);
            }
            tm = new TexturedModel(rawModel, tex);
            path2texmodel.put(key, tm);
        } else {
            SysLog.info("G3D|found cached TexturedModel:" + key);
        }
        return tm;
    }

    public RawModel loadToVAO(float[] positions, float[] textureCoords, float[] normals, int[] indices) {
        //SysLog.info("G3D|pos,texture,normals,indices: "+positions.length+",\t"+textureCoords.length+",\t"+normals.length+",\t"+indices.length+",\t");
        int vaoID = createVAO();
        bindIndicesBuffer(indices);
        storeDataInAttributeList(0, 3, positions);
        storeDataInAttributeList(1, 2, textureCoords);
        storeDataInAttributeList(2, 3, normals);
        unbindVAO();
        return new RawModel(vaoID, indices.length);
    }

    public RawModel loadToVAO(int[] indices, float[] positions, int... lengths) {
        //SysLog.info("G3D|pos,texture,normals,indices: "+positions.length+",\t"+textureCoords.length+",\t"+normals.length+",\t"+indices.length+",\t");
        int vaoID = createVAO();
        storeInterleavedData(positions, lengths);
        bindIndicesBuffer(indices);
        unbindVAO();
        return new RawModel(vaoID, indices.length);
    }

    public RawModel loadToVAO(float[] positions, int dimensions) {
        int vaoID = createVAO();
        this.storeDataInAttributeList(0, dimensions, positions);
        unbindVAO();
        return new RawModel(vaoID, positions.length / dimensions);
    }

    public RawModel loadAnimatedModelToVAO(float[] positions, float[] textureCoords, float[] normals, int[] indices, int[] jointIds,
                                           float[] vertexWeights) {
        int vaoID = createVAO();
        bindIndicesBuffer(indices);
        storeDataInAttributeList(0, 3, positions);
        storeDataInAttributeList(1, 2, textureCoords);
        storeDataInAttributeList(2, 3, normals);
        storeIntDataInAttributeList(3, 3, jointIds);
        storeDataInAttributeList(4, 3, vertexWeights);
        return new RawModel(vaoID, positions.length / 2);
    }

    /**
     * 装载 RGB32F 数据格式的图片，
     * 每像素由3个float组成
     *
     * @param data
     * @param w
     * @param h
     * @return
     */
    public int loadTextureRGBA16F(byte[] data, int w, int h) {
        int[] texture = {0};
        GL.glGenTextures(1, texture, 0);
        GL.glBindTexture(GL.GL_TEXTURE_2D, texture[0]);
        GL.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA16F, w, h, 0, GL.GL_RGBA, GL_HALF_FLOAT, data, 0);
        GL.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
        GL.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        glBindTexture(GL_TEXTURE_2D, 0);
        textures.add(texture[0]);
        return texture[0];
    }

    public int loadTextureRGBA32F(byte[] data, int w, int h) {
        int[] texture = {0};
        GL.glGenTextures(1, texture, 0);
        GL.glBindTexture(GL.GL_TEXTURE_2D, texture[0]);
        GL.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA32F, w, h, 0, GL.GL_RGBA, GL_FLOAT, data, 0);
        GL.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
        GL.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        glBindTexture(GL_TEXTURE_2D, 0);
        textures.add(texture[0]);
        return texture[0];
    }


    public int loadTexture(String fileName) {
        return loadTexture(fileName, false, false, false);
    }

    public int loadTexture(String fileName, boolean mipmap, boolean nearest, boolean clamp2edge) {
        String key = fileName + mipmap + nearest + clamp2edge;
        Integer loadedtex = path2texid.get(key);
        if (loadedtex != null) {
            SysLog.info("G3D|found cached tex:" + fileName);
            return loadedtex.intValue();
        }
        SysLog.info("G3D|" + fileName);
        byte[] filecont = loadFileFromJar(fileName);
        if (filecont == null) {
            throw new RuntimeException("[G3D][WARN]file not found: " + fileName);
        }
        byte[] data = GLUtil.image_parse_from_file_content(filecont, w_h_d);
        if (data == null) {
            throw new RuntimeException("[G3D][WARN]parse file error: " + fileName);
        }
        int[] tex = {0};
        glGenTextures(1, tex, 0);
        glBindTexture(GL_TEXTURE_2D, tex[0]);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        int format = w_h_d[2] < 4 ? GL_RGB : GL_RGBA;
        /* ios restrict inner format MUST same as outer format */
        glTexImage2D(GL_TEXTURE_2D, 0, format, w_h_d[0], w_h_d[1], 0, format, GL_UNSIGNED_BYTE, data, 0);
        if (mipmap) {
            glGenerateMipmap(GL_TEXTURE_2D);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        } else if (nearest) {
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        } else {
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        }

        //
        if (clamp2edge) {
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        } else {
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        }

        glBindTexture(GL_TEXTURE_2D, 0);
        textures.add(Integer.valueOf(tex[0]));
        path2texid.put(key, Integer.valueOf(tex[0]));
        return tex[0];
    }

    public int loadCubeMap(String[] textureFiles) {
        glGenTextures(1, tmp, 0);
        int texID = tmp[0];
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_CUBE_MAP, texID);

        for (int i = 0; i < textureFiles.length; i++) {
            TextureData data = decodeTextureFile(textureFiles[i]);
            glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, data.getChannels() < 4 ? GL_RGB : GL_RGBA, data.getWidth(), data.getHeight(), 0, data.getChannels() < 4 ? GL_RGB : GL_RGBA, GL_UNSIGNED_BYTE, data.getBuffer(), 0);
        }

        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        //防止出现缝隙
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        textures.add(Integer.valueOf(texID));

        return texID;
    }

    int[] w_h_d = {0, 0, 0};

    private TextureData decodeTextureFile(String fileName) {
        byte[] filecont = G3dUtil.loadFileFromJar(fileName);
        byte[] b = GLUtil.image_parse_from_file_content(filecont, w_h_d);
        //SysLog.info("G3D|load " + fileName + " whn:" + w_h_d[0] + "," + w_h_d[1] + "," + w_h_d[2]);
        return new TextureData(b, w_h_d[0], w_h_d[1], w_h_d[2]);
    }

    private int createVAO() {
        glGenVertexArrays(1, tmp, 0);
        int vaoID = tmp[0];
        vaos.add(Integer.valueOf(vaoID));
        glBindVertexArray(vaoID);
        return vaoID;
    }

    public int createEmptyFloatVbo(int floatCount) {
        glGenBuffers(1, tmp, 0);
        int vboID = tmp[0];
        vbos.add(Integer.valueOf(vboID));
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, floatCount * FLOAT_SIZE, null, 0, GL_STREAM_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        return vboID;
    }

    public void addInstancedAttribute(int vao, int vbo, int attribute, int dataSize, int instancedDataLength, int offset) {
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBindVertexArray(vao);
        glVertexAttribPointer(attribute, dataSize, GL_FLOAT, GL_FALSE, instancedDataLength * FLOAT_SIZE, null, offset * FLOAT_SIZE);
        glVertexAttribDivisor(attribute, 1);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void updateVbo(int vbo, float[] data) {

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, data.length * FLOAT_SIZE, null, 0, GL_STREAM_DRAW);
        glBufferSubData(GL_ARRAY_BUFFER, 0, data.length * FLOAT_SIZE, data, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

    }


    private void storeDataInAttributeList(int attributeNumber, int coordinateSize, float[] data) {
        glGenBuffers(1, tmp, 0);
        int vboID = tmp[0];
        vbos.add(Integer.valueOf(vboID));
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
//        FloatBuffer buffer = storeDataInFloatBuffer(data);
        glBufferData(GL_ARRAY_BUFFER, data.length * FLOAT_SIZE, data, 0, GL_STATIC_DRAW);
//        glVertexAttribPointer(attributeNumber, coordinateSize, GL_FLOAT, false, 0, 0);
        glVertexAttribPointer(attributeNumber, coordinateSize, GL_FLOAT, GL_FALSE, 0, null, 0); //gust
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    private void storeIntDataInAttributeList(int attributeNumber, int coordinateSize, int[] data) {
        glGenBuffers(1, tmp, 0);
        int vboID = tmp[0];
        vbos.add(Integer.valueOf(vboID));
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, data.length * INT_SIZE, data, 0, GL_STATIC_DRAW);
        glVertexAttribIPointer(attributeNumber, coordinateSize, GL_INT, coordinateSize * INT_SIZE, null, 0); // This line did the fix, We multiply it by 4, because floats and Integers are 4 bytes big
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public void unbindVAO() {
        glBindVertexArray(0);
    }

    private void bindIndicesBuffer(int[] indices) {
        glGenBuffers(1, tmp, 0);
        int vboID = tmp[0];
        vbos.add(Integer.valueOf(vboID));
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices.length * INT_SIZE, indices, 0, GL_STATIC_DRAW);
    }


    private void storeInterleavedData(float[] data, int... lengths) {
        glGenBuffers(1, tmp, 0);
        int vboID = tmp[0];
        vbos.add(Integer.valueOf(vboID));
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, data.length * INT_SIZE, data, 0, GL_STATIC_DRAW);

        int bytesPerVertex = calculateBytesPerVertex(lengths);
        int total = 0;
        for (int i = 0; i < lengths.length; i++) {
            glVertexAttribPointer(i, lengths[i], GL_FLOAT, GL_FALSE, bytesPerVertex, null, FLOAT_SIZE * total);
            total += lengths[i];
        }
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    private int calculateBytesPerVertex(int[] lengths) {
        int total = 0;
        for (int i = 0; i < lengths.length; i++) {
            total += lengths[i];
        }
        return FLOAT_SIZE * total;
    }


    public void cleanUp() {
        int[] tmp = {0};
        for (int i = 0; i < vaos.size(); i++) {
            int vaoID = vaos.get(i);
            tmp[0] = vaoID;
            glDeleteVertexArrays(1, tmp, 0);
        }
        for (int i = 0; i < vbos.size(); i++) {
            int vboID = vbos.get(i);
            tmp[0] = vboID;
            glDeleteBuffers(1, tmp, 0);
        }
        for (int i = 0; i < textures.size(); i++) {
            int textureID = textures.get(i);
            tmp[0] = textureID;
            glDeleteTextures(1, tmp, 0);
        }
        SysLog.info("G3D|loader clean.");
    }


    @Override
    protected void finalize() {
        GForm.addCmd(new GCmd(() -> {
            cleanUp();
        }));

    }

    /**
     * 加载3D柏林噪声纹理
     *
     * @param fileName 噪声数据文件路径
     * @param width    纹理宽度
     * @param height   纹理高度
     * @param depth    纹理深度
     * @return 纹理ID
     */
    public int loadTexture3D(String fileName, int width, int height, int depth) {
        byte[] filecont = loadFileFromJar(fileName);
        if (filecont == null) {
            throw new RuntimeException("[G3D][WARN]file not found: " + fileName);
        }

        // 检查数据大小是否匹配
        int expectedSize = width * height * depth;
        if (filecont.length != expectedSize) {
            throw new RuntimeException("[G3D][WARN]Invalid data size for 3D texture. Expected " +
                    expectedSize + " bytes but got " + filecont.length + " bytes");
        }

        int[] tex = {0};
        glGenTextures(1, tex, 0);
        GLUtil.checkGlError(this.getClass().getCanonicalName());
        glBindTexture(GL_TEXTURE_3D, tex[0]);
        GLUtil.checkGlError(this.getClass().getCanonicalName());

        // 设置纹理参数
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        GLUtil.checkGlError(this.getClass().getCanonicalName());
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        GLUtil.checkGlError(this.getClass().getCanonicalName());
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        GLUtil.checkGlError(this.getClass().getCanonicalName());
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        GLUtil.checkGlError(this.getClass().getCanonicalName());
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_R, GL_REPEAT);

        GLUtil.checkGlError(this.getClass().getCanonicalName());
        // 加载3D纹理数据 (固定8位深度)
        glTexImage3D(GL_TEXTURE_3D, 0, GL_R8, width, height, depth, 0,
                GL_RED, GL_UNSIGNED_BYTE, filecont, 0);

        GLUtil.checkGlError(this.getClass().getCanonicalName());
        glBindTexture(GL_TEXTURE_3D, 0);
        textures.add(Integer.valueOf(tex[0]));
        return tex[0];
    }

    /**
     * 加载不需要法线的模型数据到VAO
     *
     * @param positions     顶点位置数组
     * @param textureCoords 纹理坐标数组
     * @param indices       索引数组
     * @return RawModel对象
     */
    public RawModel loadToVAOWithoutNormals(float[] positions, float[] textureCoords, int[] indices) {
        int vaoID = createVAO();
        bindIndicesBuffer(indices);
        storeDataInAttributeList(0, 3, positions);
        storeDataInAttributeList(1, 2, textureCoords);
        unbindVAO();
        return new RawModel(vaoID, indices.length);
    }

    /**
     * 绑定纹理到指定的纹理单元
     *
     * @param textureID   纹理ID
     * @param textureUnit 纹理单元
     */
    public void bindTexture(int textureID, int textureUnit) {
        glActiveTexture(GL_TEXTURE0 + textureUnit);
        glBindTexture(GL_TEXTURE_2D, textureID);
    }

    /**
     * 绑定3D纹理到指定的纹理单元
     *
     * @param textureID   纹理ID
     * @param textureUnit 纹理单元
     */
    public void bindTexture3D(int textureID, int textureUnit) {
        glActiveTexture(GL_TEXTURE0 + textureUnit);
        glBindTexture(GL_TEXTURE_3D, textureID);
    }

    /**
     * 绑定VAO
     *
     * @param vaoID VAO ID
     */
    public void bindVAO(int vaoID) {
        glBindVertexArray(vaoID);
    }

    /**
     * 启用顶点属性数组
     *
     * @param attributeNumber 属性编号
     */
    public void enableVertexAttribArray(int attributeNumber) {
        glEnableVertexAttribArray(attributeNumber);
    }

    /**
     * 禁用顶点属性数组
     *
     * @param attributeNumber 属性编号
     */
    public void disableVertexAttribArray(int attributeNumber) {
        glDisableVertexAttribArray(attributeNumber);
    }

    /**
     * 删除纹理
     *
     * @param textureID 纹理ID
     */
    public void deleteTexture(int textureID) {
        int[] tmp = {textureID};
        glDeleteTextures(1, tmp, 0);
    }

}
