package org.mini.g3d.water;

import org.mini.g3d.core.models.RawModel;
import org.mini.g3d.core.util.G3dUtil;
import org.mini.g3d.core.util.Loader;
import org.mini.glwrap.GLUtil;

public class QuadGenerator {

    private static final int VERTEX_COUNT = 4;
    private static final float[] VERTICES = {0, 0, 1, 0, 1, 1, 0, 1};
    private static final int[] INDICES = {0, 3, 1, 1, 3, 2};

    public static RawModel generateQuad(Loader loader) {
        GLUtil.checkGlError("generateQuad 0");
        int[] lengths = getAttributeLengths(VERTEX_COUNT, VERTICES);
        float[] interleavedData = interleaveFloatData(VERTEX_COUNT, VERTICES);

        RawModel model = loader.loadToVAO(INDICES, interleavedData, lengths);
        GLUtil.checkGlError("generateQuad 1");

//        Loader loader=EngineManager.getLoader();
//        int vbo = loader.createEmptyFloatVbo(interleavedData.length);
//        RawModel quad = loader.loadToVAO(this.VERTICES, 2);
//        loader.addInstancedAttribute(quad.getVaoID(), vbo, 1, 4, INSTANCE_DATA_LENGTH, 0);
//        loader.addInstancedAttribute(quad.getVaoID(), vbo, 2, 4, INSTANCE_DATA_LENGTH, 4);
//        loader.addInstancedAttribute(quad.getVaoID(), vbo, 3, 4, INSTANCE_DATA_LENGTH, 8);
//        loader.addInstancedAttribute(quad.getVaoID(), vbo, 4, 4, INSTANCE_DATA_LENGTH, 12);
//        loader.addInstancedAttribute(quad.getVaoID(), vbo, 5, 4, INSTANCE_DATA_LENGTH, 16);
//        loader.addInstancedAttribute(quad.getVaoID(), vbo, 6, 1, INSTANCE_DATA_LENGTH, 20);
//        loader.addInstancedAttribute(quad.getVaoID(), vbo, 7, 4, INSTANCE_DATA_LENGTH, 21);


        return model;
    }

    static private int[] getAttributeLengths(int vertexCount, float[]... data) {
        int[] lengths = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            lengths[i] = data[i].length / vertexCount;
        }
        return lengths;
    }

    static private float[] interleaveFloatData(int count, float[]... data) {
        int totalSize = 0;
        int[] lengths = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            lengths[i] = data[i].length / count;
            totalSize += data[i].length;
        }
        float[] interleavedBuffer = new float[totalSize];
        int pointer = 0;
        for (int i = 0; i < count; i++) {
            for (int j = 0; j < data.length; j++) {
                int elementLength = lengths[j];
                for (int k = 0; k < elementLength; k++) {
                    interleavedBuffer[pointer++] = data[j][i * elementLength + k];
                }
            }
        }
        return interleavedBuffer;
    }
}
