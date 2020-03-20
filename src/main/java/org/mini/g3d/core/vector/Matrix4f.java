/*
 * Copyright (c) 2002-2008 LWJGL Project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'LWJGL' nor the names of
 *   its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.mini.g3d.core.vector;

import java.io.Serializable;
import org.mini.nanovg.Gutil;

/**
 * Holds a 4x4 float matrix.
 *
 * @author foo
 */
public class Matrix4f extends Matrix implements Serializable {

    private static final long serialVersionUID = 1L;

    //public float M00, M01, M02, M03, M10, M11, M12, M13, M20, M21, M22, M23, M30, M31, M32, M33;
    public static final int M00 = 0, M01 = 1, M02 = 2, M03 = 3, M10 = 4, M11 = 5, M12 = 6, M13 = 7, M20 = 8, M21 = 9, M22 = 10, M23 = 11, M30 = 12, M31 = 13, M32 = 14, M33 = 15;
    public static final int MAT_LEN = 4 * 4;
    public float[] mat = new float[MAT_LEN];

    /**
     * Construct a new matrix, initialized to the identity.
     */
    public Matrix4f() {
        super();
        setIdentity();
    }

    public Matrix4f(final Matrix4f src) {
        super();
        load(src);
    }

    /**
     * Returns a string representation of this matrix
     */
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(mat[M00]).append(' ').append(mat[M10]).append(' ').append(mat[M20]).append(' ').append(mat[M30]).append('\n');
        buf.append(mat[M01]).append(' ').append(mat[M11]).append(' ').append(mat[M21]).append(' ').append(mat[M31]).append('\n');
        buf.append(mat[M02]).append(' ').append(mat[M12]).append(' ').append(mat[M22]).append(' ').append(mat[M32]).append('\n');
        buf.append(mat[M03]).append(' ').append(mat[M13]).append(' ').append(mat[M23]).append(' ').append(mat[M33]).append('\n');
        return buf.toString();
    }

    /**
     * Set this matrix to be the identity matrix.
     *
     * @return this
     */
    public Matrix setIdentity() {
        return setIdentity(this);
    }

    /**
     * Set the given matrix to be the identity matrix.
     *
     * @param m The matrix to set to the identity
     * @return m
     */
    public static Matrix4f setIdentity(Matrix4f m) {
        Gutil.mat4x4_identity(m.mat);
//        m.mat[M00] = 1.0f;
//        m.mat[M01] = 0.0f;
//        m.mat[M02] = 0.0f;
//        m.mat[M03] = 0.0f;
//        m.mat[M10] = 0.0f;
//        m.mat[M11] = 1.0f;
//        m.mat[M12] = 0.0f;
//        m.mat[M13] = 0.0f;
//        m.mat[M20] = 0.0f;
//        m.mat[M21] = 0.0f;
//        m.mat[M22] = 1.0f;
//        m.mat[M23] = 0.0f;
//        m.mat[M30] = 0.0f;
//        m.mat[M31] = 0.0f;
//        m.mat[M32] = 0.0f;
//        m.mat[M33] = 1.0f;

        return m;
    }

    /**
     * Set this matrix to 0.
     *
     * @return this
     */
    public Matrix setZero() {
        return setZero(this);
    }

    /**
     * Set the given matrix to 0.
     *
     * @param m The matrix to set to 0
     * @return m
     */
    public static Matrix4f setZero(Matrix4f m) {
        m.mat[M00] = 0.0f;
        m.mat[M01] = 0.0f;
        m.mat[M02] = 0.0f;
        m.mat[M03] = 0.0f;
        m.mat[M10] = 0.0f;
        m.mat[M11] = 0.0f;
        m.mat[M12] = 0.0f;
        m.mat[M13] = 0.0f;
        m.mat[M20] = 0.0f;
        m.mat[M21] = 0.0f;
        m.mat[M22] = 0.0f;
        m.mat[M23] = 0.0f;
        m.mat[M30] = 0.0f;
        m.mat[M31] = 0.0f;
        m.mat[M32] = 0.0f;
        m.mat[M33] = 0.0f;

        return m;
    }

    /**
     * Load from another matrix4f
     *
     * @param src The source matrix
     * @return this
     */
    public Matrix4f load(Matrix4f src) {
        return load(src, this);
    }

    /**
     * Copy the source matrix to the destination matrix
     *
     * @param src The source matrix
     * @param dest The destination matrix, or null of a new one is to be created
     * @return The copied matrix
     */
    public static Matrix4f load(Matrix4f src, Matrix4f dest) {
        if (dest == null) {
            dest = new Matrix4f();
        }
        Gutil.mat4x4_dup(dest.mat, src.mat);
//        dest.mat[M00] = src.mat[M00];
//        dest.mat[M01] = src.mat[M01];
//        dest.mat[M02] = src.mat[M02];
//        dest.mat[M03] = src.mat[M03];
//        dest.mat[M10] = src.mat[M10];
//        dest.mat[M11] = src.mat[M11];
//        dest.mat[M12] = src.mat[M12];
//        dest.mat[M13] = src.mat[M13];
//        dest.mat[M20] = src.mat[M20];
//        dest.mat[M21] = src.mat[M21];
//        dest.mat[M22] = src.mat[M22];
//        dest.mat[M23] = src.mat[M23];
//        dest.mat[M30] = src.mat[M30];
//        dest.mat[M31] = src.mat[M31];
//        dest.mat[M32] = src.mat[M32];
//        dest.mat[M33] = src.mat[M33];

        return dest;
    }

    /**
     * Load from a float buffer. The buffer stores the matrix in column major
     * (OpenGL) order.
     *
     * @param buf A float buffer to read from
     * @return this
     */
    public Matrix load(float[] buf) {

        Gutil.mat4x4_dup(mat, buf);

//        mat[M00] = buf[0];
//        mat[M01] = buf[1];
//        mat[M02] = buf[2];
//        mat[M03] = buf[3];
//        mat[M10] = buf[4];
//        mat[M11] = buf[5];
//        mat[M12] = buf[6];
//        mat[M13] = buf[7];
//        mat[M20] = buf[8];
//        mat[M21] = buf[9];
//        mat[M22] = buf[10];
//        mat[M23] = buf[11];
//        mat[M30] = buf[12];
//        mat[M31] = buf[13];
//        mat[M32] = buf[14];
//        mat[M33] = buf[15];
        return this;
    }

    /**
     * Load from a float buffer. The buffer stores the matrix in row major
     * (maths) order.
     *
     * @param buf A float buffer to read from
     * @return this
     */
    public Matrix loadTranspose(float[] buf) {

        Gutil.mat4x4_transpose(mat, buf);

//        mat[M00] = buf[0];
//        mat[M10] = buf[1];
//        mat[M20] = buf[2];
//        mat[M30] = buf[3];
//        mat[M01] = buf[4];
//        mat[M11] = buf[5];
//        mat[M21] = buf[6];
//        mat[M31] = buf[7];
//        mat[M02] = buf[8];
//        mat[M12] = buf[9];
//        mat[M22] = buf[10];
//        mat[M32] = buf[11];
//        mat[M03] = buf[12];
//        mat[M13] = buf[13];
//        mat[M23] = buf[14];
//        mat[M33] = buf[15];
        return this;
    }

    /**
     * Store this matrix in a float buffer. The matrix is stored in column major
     * (openGL) order.
     *
     * @param buf The buffer to store this matrix in
     */
    public Matrix store(float[] buf) {
        Gutil.mat4x4_dup(buf, mat);
//        buf[0] = (mat[M00]);
//        buf[1] = (mat[M01]);
//        buf[2] = (mat[M02]);
//        buf[3] = (mat[M03]);
//        buf[4] = (mat[M10]);
//        buf[5] = (mat[M11]);
//        buf[6] = (mat[M12]);
//        buf[7] = (mat[M13]);
//        buf[8] = (mat[M20]);
//        buf[9] = (mat[M21]);
//        buf[10] = (mat[M22]);
//        buf[11] = (mat[M23]);
//        buf[12] = (mat[M30]);
//        buf[13] = (mat[M31]);
//        buf[14] = (mat[M32]);
//        buf[15] = (mat[M33]);
        return this;
    }

    /**
     * Store this matrix in a float buffer. The matrix is stored in row major
     * (maths) order.
     *
     * @param buf The buffer to store this matrix in
     */
    public Matrix storeTranspose(float[] buf) {
        Gutil.mat4x4_transpose(buf, mat);
//        buf[0] = (mat[M00]);
//        buf[1] = (mat[M10]);
//        buf[2] = (mat[M20]);
//        buf[3] = (mat[M30]);
//        buf[4] = (mat[M01]);
//        buf[5] = (mat[M11]);
//        buf[6] = (mat[M21]);
//        buf[7] = (mat[M31]);
//        buf[8] = (mat[M02]);
//        buf[9] = (mat[M12]);
//        buf[10] = (mat[M22]);
//        buf[11] = (mat[M32]);
//        buf[12] = (mat[M03]);
//        buf[13] = (mat[M13]);
//        buf[14] = (mat[M23]);
//        buf[15] = (mat[M33]);
        return this;
    }

    /**
     * Store the rotation portion of this matrix in a float buffer. The matrix
     * is stored in column major (openGL) order.
     *
     * @param buf The buffer to store this matrix in
     */
    public Matrix store3f(float[] buf) {
        buf[0] = (mat[M00]);
        buf[1] = (mat[M01]);
        buf[2] = (mat[M02]);
        buf[3] = (mat[M10]);
        buf[4] = (mat[M11]);
        buf[5] = (mat[M12]);
        buf[6] = (mat[M20]);
        buf[7] = (mat[M21]);
        buf[8] = (mat[M22]);
        return this;
    }

    /**
     * Add two matrices together and place the result in a third matrix.
     *
     * @param left The left source matrix
     * @param right The right source matrix
     * @param dest The destination matrix, or null if a new one is to be created
     * @return the destination matrix
     */
    public static Matrix4f add(Matrix4f left, Matrix4f right, Matrix4f dest) {
        if (dest == null) {
            dest = new Matrix4f();
        }
        Gutil.mat4x4_add(dest.mat, left.mat, right.mat);

//        dest.mat[M00] = left.mat[M00] + right.mat[M00];
//        dest.mat[M01] = left.mat[M01] + right.mat[M01];
//        dest.mat[M02] = left.mat[M02] + right.mat[M02];
//        dest.mat[M03] = left.mat[M03] + right.mat[M03];
//        dest.mat[M10] = left.mat[M10] + right.mat[M10];
//        dest.mat[M11] = left.mat[M11] + right.mat[M11];
//        dest.mat[M12] = left.mat[M12] + right.mat[M12];
//        dest.mat[M13] = left.mat[M13] + right.mat[M13];
//        dest.mat[M20] = left.mat[M20] + right.mat[M20];
//        dest.mat[M21] = left.mat[M21] + right.mat[M21];
//        dest.mat[M22] = left.mat[M22] + right.mat[M22];
//        dest.mat[M23] = left.mat[M23] + right.mat[M23];
//        dest.mat[M30] = left.mat[M30] + right.mat[M30];
//        dest.mat[M31] = left.mat[M31] + right.mat[M31];
//        dest.mat[M32] = left.mat[M32] + right.mat[M32];
//        dest.mat[M33] = left.mat[M33] + right.mat[M33];
        return dest;
    }

    /**
     * Subtract the right matrix from the left and place the result in a third
     * matrix.
     *
     * @param left The left source matrix
     * @param right The right source matrix
     * @param dest The destination matrix, or null if a new one is to be created
     * @return the destination matrix
     */
    public static Matrix4f sub(Matrix4f left, Matrix4f right, Matrix4f dest) {
        if (dest == null) {
            dest = new Matrix4f();
        }
        Gutil.mat4x4_sub(dest.mat, left.mat, right.mat);

//        dest.mat[M00] = left.mat[M00] - right.mat[M00];
//        dest.mat[M01] = left.mat[M01] - right.mat[M01];
//        dest.mat[M02] = left.mat[M02] - right.mat[M02];
//        dest.mat[M03] = left.mat[M03] - right.mat[M03];
//        dest.mat[M10] = left.mat[M10] - right.mat[M10];
//        dest.mat[M11] = left.mat[M11] - right.mat[M11];
//        dest.mat[M12] = left.mat[M12] - right.mat[M12];
//        dest.mat[M13] = left.mat[M13] - right.mat[M13];
//        dest.mat[M20] = left.mat[M20] - right.mat[M20];
//        dest.mat[M21] = left.mat[M21] - right.mat[M21];
//        dest.mat[M22] = left.mat[M22] - right.mat[M22];
//        dest.mat[M23] = left.mat[M23] - right.mat[M23];
//        dest.mat[M30] = left.mat[M30] - right.mat[M30];
//        dest.mat[M31] = left.mat[M31] - right.mat[M31];
//        dest.mat[M32] = left.mat[M32] - right.mat[M32];
//        dest.mat[M33] = left.mat[M33] - right.mat[M33];
        return dest;
    }

    /**
     * Multiply the right matrix by the left and place the result in a third
     * matrix.
     *
     * @param left The left source matrix
     * @param right The right source matrix
     * @param dest The destination matrix, or null if a new one is to be created
     * @return the destination matrix
     */
    public static Matrix4f mul(Matrix4f left, Matrix4f right, Matrix4f dest) {
        if (dest == null) {
            dest = new Matrix4f();
        }
        Gutil.mat4x4_mul(dest.mat, left.mat, right.mat);

//        dest.mat[M00] = left.mat[M00] * right.mat[M00] + left.mat[M10] * right.mat[M01] + left.mat[M20] * right.mat[M02] + left.mat[M30] * right.mat[M03];
//        dest.mat[M01] = left.mat[M01] * right.mat[M00] + left.mat[M11] * right.mat[M01] + left.mat[M21] * right.mat[M02] + left.mat[M31] * right.mat[M03];
//        dest.mat[M02] = left.mat[M02] * right.mat[M00] + left.mat[M12] * right.mat[M01] + left.mat[M22] * right.mat[M02] + left.mat[M32] * right.mat[M03];
//        dest.mat[M03] = left.mat[M03] * right.mat[M00] + left.mat[M13] * right.mat[M01] + left.mat[M23] * right.mat[M02] + left.mat[M33] * right.mat[M03];
//        dest.mat[M10] = left.mat[M00] * right.mat[M10] + left.mat[M10] * right.mat[M11] + left.mat[M20] * right.mat[M12] + left.mat[M30] * right.mat[M13];
//        dest.mat[M11] = left.mat[M01] * right.mat[M10] + left.mat[M11] * right.mat[M11] + left.mat[M21] * right.mat[M12] + left.mat[M31] * right.mat[M13];
//        dest.mat[M12] = left.mat[M02] * right.mat[M10] + left.mat[M12] * right.mat[M11] + left.mat[M22] * right.mat[M12] + left.mat[M32] * right.mat[M13];
//        dest.mat[M13] = left.mat[M03] * right.mat[M10] + left.mat[M13] * right.mat[M11] + left.mat[M23] * right.mat[M12] + left.mat[M33] * right.mat[M13];
//        dest.mat[M20] = left.mat[M00] * right.mat[M20] + left.mat[M10] * right.mat[M21] + left.mat[M20] * right.mat[M22] + left.mat[M30] * right.mat[M23];
//        dest.mat[M21] = left.mat[M01] * right.mat[M20] + left.mat[M11] * right.mat[M21] + left.mat[M21] * right.mat[M22] + left.mat[M31] * right.mat[M23];
//        dest.mat[M22] = left.mat[M02] * right.mat[M20] + left.mat[M12] * right.mat[M21] + left.mat[M22] * right.mat[M22] + left.mat[M32] * right.mat[M23];
//        dest.mat[M23] = left.mat[M03] * right.mat[M20] + left.mat[M13] * right.mat[M21] + left.mat[M23] * right.mat[M22] + left.mat[M33] * right.mat[M23];
//        dest.mat[M30] = left.mat[M00] * right.mat[M30] + left.mat[M10] * right.mat[M31] + left.mat[M20] * right.mat[M32] + left.mat[M30] * right.mat[M33];
//        dest.mat[M31] = left.mat[M01] * right.mat[M30] + left.mat[M11] * right.mat[M31] + left.mat[M21] * right.mat[M32] + left.mat[M31] * right.mat[M33];
//        dest.mat[M32] = left.mat[M02] * right.mat[M30] + left.mat[M12] * right.mat[M31] + left.mat[M22] * right.mat[M32] + left.mat[M32] * right.mat[M33];
//        dest.mat[M33] = left.mat[M03] * right.mat[M30] + left.mat[M13] * right.mat[M31] + left.mat[M23] * right.mat[M32] + left.mat[M33] * right.mat[M33];
        return dest;
    }

    /**
     * Transform a Vector by a matrix and return the result in a destination
     * vector.
     *
     * @param left The left matrix
     * @param right The right vector
     * @param dest The destination vector, or null if a new one is to be created
     * @return the destination vector
     */
    public static Vector4f transform(Matrix4f left, Vector4f right, Vector4f dest) {
        if (dest == null) {
            dest = new Vector4f();
        }

        float x = left.mat[M00] * right.x + left.mat[M10] * right.y + left.mat[M20] * right.z + left.mat[M30] * right.w;
        float y = left.mat[M01] * right.x + left.mat[M11] * right.y + left.mat[M21] * right.z + left.mat[M31] * right.w;
        float z = left.mat[M02] * right.x + left.mat[M12] * right.y + left.mat[M22] * right.z + left.mat[M32] * right.w;
        float w = left.mat[M03] * right.x + left.mat[M13] * right.y + left.mat[M23] * right.z + left.mat[M33] * right.w;

        dest.x = x;
        dest.y = y;
        dest.z = z;
        dest.w = w;

        return dest;
    }

    /**
     * Transpose this matrix
     *
     * @return this
     */
    public Matrix transpose() {
        return transpose(this);
    }

    /**
     * Translate this matrix
     *
     * @param vec The vector to translate by
     * @return this
     */
    public Matrix4f translate(Vector2f vec) {
        return translate(vec, this);
    }

    /**
     * Translate this matrix
     *
     * @param vec The vector to translate by
     * @return this
     */
    public Matrix4f translate(Vector3f vec) {
        return translate(vec, this);
    }

    /**
     * Scales this matrix
     *
     * @param vec The vector to scale by
     * @return this
     */
    public Matrix4f scale(Vector3f vec) {
        return scale(vec, this, this);
    }

    /**
     * Scales the source matrix and put the result in the destination matrix
     *
     * @param vec The vector to scale by
     * @param src The source matrix
     * @param dest The destination matrix, or null if a new matrix is to be
     * created
     * @return The scaled matrix
     */
    public static Matrix4f scale(Vector3f vec, Matrix4f src, Matrix4f dest) {
        if (dest == null) {
            dest = new Matrix4f();
        }
        Gutil.mat4x4_scale_aniso(dest.mat, src.mat, vec.x, vec.y, vec.z);
//        dest.mat[M00] = src.mat[M00] * vec.left;
//        dest.mat[M01] = src.mat[M01] * vec.left;
//        dest.mat[M02] = src.mat[M02] * vec.left;
//        dest.mat[M03] = src.mat[M03] * vec.left;
//        dest.mat[M10] = src.mat[M10] * vec.top;
//        dest.mat[M11] = src.mat[M11] * vec.top;
//        dest.mat[M12] = src.mat[M12] * vec.top;
//        dest.mat[M13] = src.mat[M13] * vec.top;
//        dest.mat[M20] = src.mat[M20] * vec.z;
//        dest.mat[M21] = src.mat[M21] * vec.z;
//        dest.mat[M22] = src.mat[M22] * vec.z;
//        dest.mat[M23] = src.mat[M23] * vec.z;
        return dest;
    }

    /**
     * Rotates the matrix around the given axis the specified angle
     *
     * @param angle the angle, in radians.
     * @param axis The vector representing the rotation axis. Must be
     * normalized.
     * @return this
     */
    public Matrix4f rotate(float angle, Vector3f axis) {
        return rotate(angle, axis, this);
    }

    /**
     * Rotates the matrix around the given axis the specified angle
     *
     * @param angle the angle, in radians.
     * @param axis The vector representing the rotation axis. Must be
     * normalized.
     * @param dest The matrix to put the result, or null if a new matrix is to
     * be created
     * @return The rotated matrix
     */
    public Matrix4f rotate(float angle, Vector3f axis, Matrix4f dest) {
        return rotate(angle, axis, this, dest);
    }

    /**
     * Rotates the source matrix around the given axis the specified angle and
     * put the result in the destination matrix.
     *
     * @param angle the angle, in radians.
     * @param axis The vector representing the rotation axis. Must be
     * normalized.
     * @param src The matrix to rotate
     * @param dest The matrix to put the result, or null if a new matrix is to
     * be created
     * @return The rotated matrix
     */
    public static Matrix4f rotate(float angle, Vector3f axis, Matrix4f src, Matrix4f dest) {
        if (dest == null) {
            dest = new Matrix4f();
        }
        if (src == dest) {
            float[] tmp = new float[MAT_LEN];
            Gutil.mat4x4_rotate(tmp, src.mat, axis.x, axis.y, axis.z, angle);
            Gutil.mat4x4_dup(dest.mat, tmp);
        } else {
            Gutil.mat4x4_rotate(dest.mat, src.mat, axis.x, axis.y, axis.z, angle);
        }
//        float c = (float) Math.cos(angle);
//        float s = (float) Math.sin(angle);
//        float oneminusc = 1.0f - c;
//        float xy = axis.left * axis.top;
//        float yz = axis.top * axis.z;
//        float xz = axis.left * axis.z;
//        float xs = axis.left * s;
//        float ys = axis.top * s;
//        float zs = axis.z * s;
//
//        float f00 = axis.left * axis.left * oneminusc + c;
//        float f01 = xy * oneminusc + zs;
//        float f02 = xz * oneminusc - ys;
//        // n[3] not used
//        float f10 = xy * oneminusc - zs;
//        float f11 = axis.top * axis.top * oneminusc + c;
//        float f12 = yz * oneminusc + xs;
//        // n[7] not used
//        float f20 = xz * oneminusc + ys;
//        float f21 = yz * oneminusc - xs;
//        float f22 = axis.z * axis.z * oneminusc + c;
//
//        float t00 = src.mat[M00] * f00 + src.mat[M10] * f01 + src.mat[M20] * f02;
//        float t01 = src.mat[M01] * f00 + src.mat[M11] * f01 + src.mat[M21] * f02;
//        float t02 = src.mat[M02] * f00 + src.mat[M12] * f01 + src.mat[M22] * f02;
//        float t03 = src.mat[M03] * f00 + src.mat[M13] * f01 + src.mat[M23] * f02;
//        float t10 = src.mat[M00] * f10 + src.mat[M10] * f11 + src.mat[M20] * f12;
//        float t11 = src.mat[M01] * f10 + src.mat[M11] * f11 + src.mat[M21] * f12;
//        float t12 = src.mat[M02] * f10 + src.mat[M12] * f11 + src.mat[M22] * f12;
//        float t13 = src.mat[M03] * f10 + src.mat[M13] * f11 + src.mat[M23] * f12;
//        dest.mat[M20] = src.mat[M00] * f20 + src.mat[M10] * f21 + src.mat[M20] * f22;
//        dest.mat[M21] = src.mat[M01] * f20 + src.mat[M11] * f21 + src.mat[M21] * f22;
//        dest.mat[M22] = src.mat[M02] * f20 + src.mat[M12] * f21 + src.mat[M22] * f22;
//        dest.mat[M23] = src.mat[M03] * f20 + src.mat[M13] * f21 + src.mat[M23] * f22;
//        dest.mat[M00] = t00;
//        dest.mat[M01] = t01;
//        dest.mat[M02] = t02;
//        dest.mat[M03] = t03;
//        dest.mat[M10] = t10;
//        dest.mat[M11] = t11;
//        dest.mat[M12] = t12;
//        dest.mat[M13] = t13;
        return dest;
    }

    /**
     * Translate this matrix and stash the result in another matrix
     *
     * @param vec The vector to translate by
     * @param dest The destination matrix or null if a new matrix is to be
     * created
     * @return the translated matrix
     */
    public Matrix4f translate(Vector3f vec, Matrix4f dest) {
        return translate(vec, this, dest);
    }

    /**
     * Translate the source matrix and stash the result in the destination
     * matrix
     *
     * @param vec The vector to translate by
     * @param src The source matrix
     * @param dest The destination matrix or null if a new matrix is to be
     * created
     * @return The translated matrix
     */
    public static Matrix4f translate(Vector3f vec, Matrix4f src, Matrix4f dest) {
        if (dest == null) {
            dest = new Matrix4f();
        }
        Gutil.mat4x4_dup(dest.mat, src.mat);
        Gutil.mat4x4_translate_in_place(dest.mat, vec.x, vec.y, vec.z);
//        dest.mat[M30] += src.mat[M00] * vec.left + src.mat[M10] * vec.top + src.mat[M20] * vec.z;
//        dest.mat[M31] += src.mat[M01] * vec.left + src.mat[M11] * vec.top + src.mat[M21] * vec.z;
//        dest.mat[M32] += src.mat[M02] * vec.left + src.mat[M12] * vec.top + src.mat[M22] * vec.z;
//        dest.mat[M33] += src.mat[M03] * vec.left + src.mat[M13] * vec.top + src.mat[M23] * vec.z;

        return dest;
    }

    /**
     * Translate this matrix and stash the result in another matrix
     *
     * @param vec The vector to translate by
     * @param dest The destination matrix or null if a new matrix is to be
     * created
     * @return the translated matrix
     */
    public Matrix4f translate(Vector2f vec, Matrix4f dest) {
        return translate(vec, this, dest);
    }

    /**
     * Translate the source matrix and stash the result in the destination
     * matrix
     *
     * @param vec The vector to translate by
     * @param src The source matrix
     * @param dest The destination matrix or null if a new matrix is to be
     * created
     * @return The translated matrix
     */
    public static Matrix4f translate(Vector2f vec, Matrix4f src, Matrix4f dest) {
        if (dest == null) {
            dest = new Matrix4f();
        }
        Gutil.mat4x4_dup(dest.mat, src.mat);
        Gutil.mat4x4_translate_in_place(dest.mat, vec.x, vec.y, 0.f);

//        dest.mat[M30] += src.mat[M00] * vec.left + src.mat[M10] * vec.top;
//        dest.mat[M31] += src.mat[M01] * vec.left + src.mat[M11] * vec.top;
//        dest.mat[M32] += src.mat[M02] * vec.left + src.mat[M12] * vec.top;
//        dest.mat[M33] += src.mat[M03] * vec.left + src.mat[M13] * vec.top;
        return dest;
    }

    /**
     * Transpose this matrix and place the result in another matrix
     *
     * @param dest The destination matrix or null if a new matrix is to be
     * created
     * @return the transposed matrix
     */
    public Matrix4f transpose(Matrix4f dest) {
        return transpose(this, dest);
    }

    /**
     * Transpose the source matrix and place the result in the destination
     * matrix
     *
     * @param src The source matrix
     * @param dest The destination matrix or null if a new matrix is to be
     * created
     * @return the transposed matrix
     */
    public static Matrix4f transpose(Matrix4f src, Matrix4f dest) {
        if (dest == null) {
            dest = new Matrix4f();
        }
        if (dest != src) {
            Gutil.mat4x4_transpose(dest.mat, src.mat);
        } else {
            float[] tmp = new float[MAT_LEN];
            Gutil.mat4x4_transpose(tmp, src.mat);
            Gutil.mat4x4_dup(dest.mat, tmp);
        }

//            dest.mat[M00] = src.mat[M00];
//            dest.mat[M01] = src.mat[M10];
//            dest.mat[M02] = src.mat[M20];
//            dest.mat[M03] = src.mat[M30];
//            dest.mat[M10] = src.mat[M01];
//            dest.mat[M11] = src.mat[M11];
//            dest.mat[M12] = src.mat[M21];
//            dest.mat[M13] = src.mat[M31];
//            dest.mat[M20] = src.mat[M02];
//            dest.mat[M21] = src.mat[M12];
//            dest.mat[M22] = src.mat[M22];
//            dest.mat[M23] = src.mat[M32];
//            dest.mat[M30] = src.mat[M03];
//            dest.mat[M31] = src.mat[M13];
//            dest.mat[M32] = src.mat[M23];
//            dest.mat[M33] = src.mat[M33];
        return dest;
    }

    /**
     * @return the determinant of the matrix
     */
    public float determinant() {
        float f
                = mat[M00]
                * ((mat[M11] * mat[M22] * mat[M33] + mat[M12] * mat[M23] * mat[M31] + mat[M13] * mat[M21] * mat[M32])
                - mat[M13] * mat[M22] * mat[M31]
                - mat[M11] * mat[M23] * mat[M32]
                - mat[M12] * mat[M21] * mat[M33]);
        f -= mat[M01]
                * ((mat[M10] * mat[M22] * mat[M33] + mat[M12] * mat[M23] * mat[M30] + mat[M13] * mat[M20] * mat[M32])
                - mat[M13] * mat[M22] * mat[M30]
                - mat[M10] * mat[M23] * mat[M32]
                - mat[M12] * mat[M20] * mat[M33]);
        f += mat[M02]
                * ((mat[M10] * mat[M21] * mat[M33] + mat[M11] * mat[M23] * mat[M30] + mat[M13] * mat[M20] * mat[M31])
                - mat[M13] * mat[M21] * mat[M30]
                - mat[M10] * mat[M23] * mat[M31]
                - mat[M11] * mat[M20] * mat[M33]);
        f -= mat[M03]
                * ((mat[M10] * mat[M21] * mat[M32] + mat[M11] * mat[M22] * mat[M30] + mat[M12] * mat[M20] * mat[M31])
                - mat[M12] * mat[M21] * mat[M30]
                - mat[M10] * mat[M22] * mat[M31]
                - mat[M11] * mat[M20] * mat[M32]);
        return f;
    }

    /**
     * Calculate the determinant of a 3x3 matrix
     *
     * @return result
     */
    private static float determinant3x3(float t00, float t01, float t02,
            float t10, float t11, float t12,
            float t20, float t21, float t22) {
        return t00 * (t11 * t22 - t12 * t21)
                + t01 * (t12 * t20 - t10 * t22)
                + t02 * (t10 * t21 - t11 * t20);
    }

    /**
     * Invert this matrix
     *
     * @return this if successful, null otherwise
     */
    public Matrix invert() {
        return invert(this, this);
    }

    /**
     * Invert the source matrix and put the result in the destination
     *
     * @param src The source matrix
     * @param dest The destination matrix, or null if a new matrix is to be
     * created
     * @return The inverted matrix if successful, null otherwise
     */
    public static Matrix4f invert(Matrix4f src, Matrix4f dest) {
        float determinant = src.determinant();

        if (determinant != 0) {
            /*
			 * mat[M00] mat[M01] mat[M02] mat[M03]
			 * mat[M10] mat[M11] mat[M12] mat[M13]
			 * mat[M20] mat[M21] mat[M22] mat[M23]
			 * mat[M30] mat[M31] mat[M32] mat[M33]
             */
            if (dest == null) {
                dest = new Matrix4f();
            }
            if (src == dest) {
                float[] tmp = new float[MAT_LEN];
                Gutil.mat4x4_invert(tmp, src.mat);
                Gutil.mat4x4_dup(dest.mat, tmp);
            } else {
                Gutil.mat4x4_invert(dest.mat, src.mat);
            }

//            float determinant_inv = 1f / determinant;
//
//            // first row
//            float t00 = determinant3x3(src.mat[M11], src.mat[M12], src.mat[M13], src.mat[M21], src.mat[M22], src.mat[M23], src.mat[M31], src.mat[M32], src.mat[M33]);
//            float t01 = -determinant3x3(src.mat[M10], src.mat[M12], src.mat[M13], src.mat[M20], src.mat[M22], src.mat[M23], src.mat[M30], src.mat[M32], src.mat[M33]);
//            float t02 = determinant3x3(src.mat[M10], src.mat[M11], src.mat[M13], src.mat[M20], src.mat[M21], src.mat[M23], src.mat[M30], src.mat[M31], src.mat[M33]);
//            float t03 = -determinant3x3(src.mat[M10], src.mat[M11], src.mat[M12], src.mat[M20], src.mat[M21], src.mat[M22], src.mat[M30], src.mat[M31], src.mat[M32]);
//            // second row
//            float t10 = -determinant3x3(src.mat[M01], src.mat[M02], src.mat[M03], src.mat[M21], src.mat[M22], src.mat[M23], src.mat[M31], src.mat[M32], src.mat[M33]);
//            float t11 = determinant3x3(src.mat[M00], src.mat[M02], src.mat[M03], src.mat[M20], src.mat[M22], src.mat[M23], src.mat[M30], src.mat[M32], src.mat[M33]);
//            float t12 = -determinant3x3(src.mat[M00], src.mat[M01], src.mat[M03], src.mat[M20], src.mat[M21], src.mat[M23], src.mat[M30], src.mat[M31], src.mat[M33]);
//            float t13 = determinant3x3(src.mat[M00], src.mat[M01], src.mat[M02], src.mat[M20], src.mat[M21], src.mat[M22], src.mat[M30], src.mat[M31], src.mat[M32]);
//            // third row
//            float t20 = determinant3x3(src.mat[M01], src.mat[M02], src.mat[M03], src.mat[M11], src.mat[M12], src.mat[M13], src.mat[M31], src.mat[M32], src.mat[M33]);
//            float t21 = -determinant3x3(src.mat[M00], src.mat[M02], src.mat[M03], src.mat[M10], src.mat[M12], src.mat[M13], src.mat[M30], src.mat[M32], src.mat[M33]);
//            float t22 = determinant3x3(src.mat[M00], src.mat[M01], src.mat[M03], src.mat[M10], src.mat[M11], src.mat[M13], src.mat[M30], src.mat[M31], src.mat[M33]);
//            float t23 = -determinant3x3(src.mat[M00], src.mat[M01], src.mat[M02], src.mat[M10], src.mat[M11], src.mat[M12], src.mat[M30], src.mat[M31], src.mat[M32]);
//            // fourth row
//            float t30 = -determinant3x3(src.mat[M01], src.mat[M02], src.mat[M03], src.mat[M11], src.mat[M12], src.mat[M13], src.mat[M21], src.mat[M22], src.mat[M23]);
//            float t31 = determinant3x3(src.mat[M00], src.mat[M02], src.mat[M03], src.mat[M10], src.mat[M12], src.mat[M13], src.mat[M20], src.mat[M22], src.mat[M23]);
//            float t32 = -determinant3x3(src.mat[M00], src.mat[M01], src.mat[M03], src.mat[M10], src.mat[M11], src.mat[M13], src.mat[M20], src.mat[M21], src.mat[M23]);
//            float t33 = determinant3x3(src.mat[M00], src.mat[M01], src.mat[M02], src.mat[M10], src.mat[M11], src.mat[M12], src.mat[M20], src.mat[M21], src.mat[M22]);
//
//            // transpose and divide by the determinant
//            dest.mat[M00] = t00 * determinant_inv;
//            dest.mat[M11] = t11 * determinant_inv;
//            dest.mat[M22] = t22 * determinant_inv;
//            dest.mat[M33] = t33 * determinant_inv;
//            dest.mat[M01] = t10 * determinant_inv;
//            dest.mat[M10] = t01 * determinant_inv;
//            dest.mat[M20] = t02 * determinant_inv;
//            dest.mat[M02] = t20 * determinant_inv;
//            dest.mat[M12] = t21 * determinant_inv;
//            dest.mat[M21] = t12 * determinant_inv;
//            dest.mat[M03] = t30 * determinant_inv;
//            dest.mat[M30] = t03 * determinant_inv;
//            dest.mat[M13] = t31 * determinant_inv;
//            dest.mat[M31] = t13 * determinant_inv;
//            dest.mat[M32] = t23 * determinant_inv;
//            dest.mat[M23] = t32 * determinant_inv;
            return dest;
        } else {
            return null;
        }
    }

    /**
     * Negate this matrix
     *
     * @return this
     */
    public Matrix negate() {
        return negate(this);
    }

    /**
     * Negate this matrix and place the result in a destination matrix.
     *
     * @param dest The destination matrix, or null if a new matrix is to be
     * created
     * @return the negated matrix
     */
    public Matrix4f negate(Matrix4f dest) {
        return negate(this, dest);
    }

    /**
     * Negate this matrix and place the result in a destination matrix.
     *
     * @param src The source matrix
     * @param dest The destination matrix, or null if a new matrix is to be
     * created
     * @return The negated matrix
     */
    public static Matrix4f negate(Matrix4f src, Matrix4f dest) {
        if (dest == null) {
            dest = new Matrix4f();
        }

        dest.mat[M00] = -src.mat[M00];
        dest.mat[M01] = -src.mat[M01];
        dest.mat[M02] = -src.mat[M02];
        dest.mat[M03] = -src.mat[M03];
        dest.mat[M10] = -src.mat[M10];
        dest.mat[M11] = -src.mat[M11];
        dest.mat[M12] = -src.mat[M12];
        dest.mat[M13] = -src.mat[M13];
        dest.mat[M20] = -src.mat[M20];
        dest.mat[M21] = -src.mat[M21];
        dest.mat[M22] = -src.mat[M22];
        dest.mat[M23] = -src.mat[M23];
        dest.mat[M30] = -src.mat[M30];
        dest.mat[M31] = -src.mat[M31];
        dest.mat[M32] = -src.mat[M32];
        dest.mat[M33] = -src.mat[M33];

        return dest;
    }
}
