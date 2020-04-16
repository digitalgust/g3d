package org.mini.g3d.core.toolbox;

import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.core.vector.Vector2f;
import org.mini.g3d.core.vector.Vector3f;

public class G3dMath {

    public static Matrix4f createTransformationMatrix(Vector2f translation, Vector2f scale) {
        Matrix4f matrix = new Matrix4f();
        matrix.identity();
        Matrix4f.translate(translation, matrix, matrix);
        Matrix4f.scale(new Vector3f(scale.x, scale.y, 1f), matrix, matrix);
        return matrix;
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
        Matrix4f.rotate((float) Math.toRadians(rx), new Vector3f(1, 0, 0), dest, dest);
        Matrix4f.rotate((float) Math.toRadians(ry), new Vector3f(0, 1, 0), dest, dest);
        Matrix4f.rotate((float) Math.toRadians(rz), new Vector3f(0, 0, 1), dest, dest);
        Matrix4f.scale(new Vector3f(scale, scale, scale), dest, dest);
        return dest;
    }


}
