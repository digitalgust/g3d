package org.mini.g3d.core;

import org.mini.g3d.core.models.RawModel;
import org.mini.g3d.core.textures.TextureData;
import org.mini.gui.GCmd;
import org.mini.gui.GForm;
import org.mini.glwrap.GLUtil;

import java.util.ArrayList;
import java.util.List;

import static org.mini.g3d.core.EngineManager.loadFileFromJar;
import static org.mini.gl.GL.*;

public class Loader {
    public static final int FLOAT_SIZE = 4, INT_SIZE = 4;

    private final List<Integer> vaos = new ArrayList<>();
    private final List<Integer> vbos = new ArrayList<>();
    private final List<Integer> textures = new ArrayList<>();

    public RawModel loadToVAO(float[] positions, float[] textureCoords, float[] normals, int[] indices) {
        //System.out.println("pos,texture,normals,indices: "+positions.length+",\t"+textureCoords.length+",\t"+normals.length+",\t"+indices.length+",\t");
        int vaoID = createVAO();
        bindIndicesBuffer(indices);
        storeDataInAttributeList(0, 3, positions);
        storeDataInAttributeList(1, 2, textureCoords);
        storeDataInAttributeList(2, 3, normals);
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

    public int loadTexture(String fileName) {
        byte[] filecont = loadFileFromJar(EngineManager.RES_LOC + fileName + ".png");
        int textureID = GLUtil.gl_image_load(filecont, w_h_d);
        textures.add(textureID);
        return textureID;
    }

    int tmp[] = {0};

    public int loadCubeMap(String[] textureFiles) {
        glGenTextures(1, tmp, 0);
        int texID = tmp[0];
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_CUBE_MAP, texID);

        for (int i = 0; i < textureFiles.length; i++) {
            TextureData data = decodeTextureFile(EngineManager.RES_LOC + textureFiles[i] + ".png");
            glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, data.getChannels() < 4 ? GL_RGB : GL_RGBA, data.getWidth(), data.getHeight(), 0, data.getChannels() < 4 ? GL_RGB : GL_RGBA, GL_UNSIGNED_BYTE, data.getBuffer(), 0);
        }

        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        textures.add(texID);

        return texID;
    }

    int[] w_h_d = {0, 0, 0};

    private TextureData decodeTextureFile(String fileName) {
        byte[] filecont = EngineManager.loadFileFromJar(fileName);
        byte[] b = GLUtil.image_parse_from_file_content(filecont, w_h_d);
        //System.out.println("load " + fileName + " whn:" + w_h_d[0] + "," + w_h_d[1] + "," + w_h_d[2]);
        return new TextureData(b, w_h_d[0], w_h_d[1], w_h_d[2]);
    }

    public void cleanUp() {
        int[] tmp = {0};
        for (int vaoID : vaos) {
            tmp[0] = vaoID;
            glDeleteVertexArrays(1, tmp, 0);
        }
        for (int vboID : vbos) {
            tmp[0] = vboID;
            glDeleteBuffers(1, tmp, 0);
        }
        for (int textureID : textures) {
            tmp[0] = textureID;
            glDeleteTextures(1, tmp, 0);
        }
    }

    /**
     * must be static,because finalize would be destroy ext class in minijvm
     */
    static class Cleaner implements Runnable {
        int index;
        int[] vaos;
        int[] vbos;
        int[] textures;

        @Override
        public void run() {
            glDeleteVertexArrays(vaos.length, vaos, 0);
            glDeleteBuffers(vbos.length, vbos, 0);
            glDeleteTextures(textures.length, textures, 0);
            System.out.println("g3d loader clean success");
        }
    }

    public void finalize() {
        //Don't reference to this instance
        Loader.Cleaner cleaner = new Loader.Cleaner();
        cleaner.vaos = new int[vaos.size()];
        cleaner.index = 0;
        vaos.forEach(v -> {
            cleaner.vaos[cleaner.index] = v;
            cleaner.index++;
        });

        cleaner.vbos = new int[vbos.size()];
        cleaner.index = 0;
        vbos.forEach(v -> {
            cleaner.vbos[cleaner.index] = v;
            cleaner.index++;
        });


        cleaner.textures = new int[textures.size()];
        cleaner.index = 0;
        textures.forEach(v -> {
            cleaner.textures[cleaner.index] = v;
            cleaner.index++;
        });
        GForm.addCmd(new GCmd(GCmd.GCMD_RUN_CODE, cleaner));
    }

    private int createVAO() {
        glGenVertexArrays(1, tmp, 0);
        int vaoID = tmp[0];
        vaos.add(vaoID);
        glBindVertexArray(vaoID);
        return vaoID;
    }

    public int createEmptyFloatVbo(int floatCount) {
        glGenBuffers(1, tmp, 0);
        int vboID = tmp[0];
        vbos.add(vboID);
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
        vbos.add(vboID);
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
        vbos.add(vboID);
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, data.length * INT_SIZE, data, 0, GL_STATIC_DRAW);
        glVertexAttribIPointer(attributeNumber, coordinateSize, GL_INT, coordinateSize * INT_SIZE, null, 0); // This line did the fix, We multiply it by 4, because floats and Integers are 4 bytes big
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    private void unbindVAO() {
        glBindVertexArray(0);
    }

    private void bindIndicesBuffer(int[] indices) {
        glGenBuffers(1, tmp, 0);
        int vboID = tmp[0];
        vbos.add(vboID);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboID);
//		IntBuffer buffer = storeDataInIntBuffer(indices);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices.length * INT_SIZE, indices, 0, GL_STATIC_DRAW);
    }

}
