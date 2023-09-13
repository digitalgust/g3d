/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mini.g3d.core.util;

import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.core.vector.Vector2f;
import org.mini.g3d.core.vector.Vector3f;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * @author Gust
 */
public class G3dUtil {

//    static Loader loader;

    /**
     * 在src中搜索key
     *
     * @param src
     * @param key
     * @return
     */
    static public int binSearch(byte[] src, byte[] key, int startPos) {
        if (src == null || key == null || src.length == 0 || key.length == 0 || startPos >= src.length) {
            return -1;
        }
        for (int i = startPos, iLen = src.length - key.length; i <= iLen; i++) {
            if (src[i] == key[0]) {
                boolean march = true;
                for (int j = 1; j < key.length; j++) {
                    if (src[i + j] != key[j]) {
                        march = false;
                    }
                }
                if (march) {
                    return i;
                }
            }
        }
        return -1;
    }

    static public byte[] loadFileFromJar(String fileName) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            InputStream is = Loader.class.getResourceAsStream(fileName);
            byte[] b = new byte[4096];
            if (is != null) {
                int r;
                while ((r = is.read(b)) > 0) {
                    baos.write(b, 0, r);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }

    public static void Assert(boolean isTrue) {
        if (!isTrue)
            throw new AssertionError("Assertion Failed.");
    }

    public static void Assert(boolean isTrue, String message) {
        if (!isTrue)
            throw new AssertionError("Assertion Failed : " + message);
    }

    public static Matrix4f createTransformationMatrix(Vector2f translation, Vector2f scale) {
        return createTransformationMatrix(translation, scale, new Matrix4f());
    }

    public static Matrix4f createTransformationMatrix(Vector2f translation, Vector2f scale, Matrix4f dest) {
        if (dest == null) dest = new Matrix4f();
        dest.identity();
        Matrix4f.translate(translation, dest, dest);
        Matrix4f.scale(scale.x, scale.y, 1f, dest, dest);
        return dest;
    }

    public static float barryCentric(Vector3f p1, Vector3f p2, Vector3f p3, Vector2f pos) {
        float det = (p2.z - p3.z) * (p1.x - p3.x) + (p3.x - p2.x) * (p1.z - p3.z);
        float l1 = ((p2.z - p3.z) * (pos.x - p3.x) + (p3.x - p2.x) * (pos.y - p3.z)) / det;
        float l2 = ((p3.z - p1.z) * (pos.x - p3.x) + (p1.x - p3.x) * (pos.y - p3.z)) / det;
        float l3 = 1.0f - l1 - l2;
        return l1 * p1.y + l2 * p2.y + l3 * p3.y;
    }

    public static Matrix4f createTransformationMatrix(Vector3f translation, float rx, float ry, float rz, float scale) {
        return createTransformationMatrix(translation, rx, ry, rz, scale, null);
    }

    public static Matrix4f createTransformationMatrix(Vector3f translation, float rx, float ry, float rz, float scale, Matrix4f dest) {
        if (dest == null) {
            dest = new Matrix4f();
        }
        dest.identity();
        Matrix4f.translate(translation, dest, dest);
        Matrix4f.rotate((float) Math.toRadians(rx), 1, 0, 0, dest, dest);
        Matrix4f.rotate((float) Math.toRadians(ry), 0, 1, 0, dest, dest);
        Matrix4f.rotate((float) Math.toRadians(rz), 0, 0, 1, dest, dest);
        Matrix4f.scale(scale, scale, scale, dest, dest);
        return dest;
    }


}
