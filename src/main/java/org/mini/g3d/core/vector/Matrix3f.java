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

/**
 *
 * Holds a 3x3 matrix.
 *
 * @author cix_foo <cix_foo@users.sourceforge.net>
 * @version $Revision$ $Id$
 */
public class Matrix3f extends Matrix implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int M00 = 0,
            M01 = 1,
            M02 = 2,
            M10 = 3,
            M11 = 4,
            M12 = 5,
            M20 = 6,
            M21 = 7,
            M22 = 8;
    public float[] mat = new float[3 * 3];

    /**
     * Constructor for Matrix3f. Matrix is initialised to the identity.
     */
    public Matrix3f() {
        super();
        setIdentity();
    }

    /**
     * Load from another matrix
     *
     * @param src The source matrix
     * @return this
     */
    public Matrix3f load(Matrix3f src) {
        return load(src, this);
    }

    /**
     * Copy source matrix to destination matrix
     *
     * @param src The source matrix
     * @param dest The destination matrix, or null of a new matrix is to be
     * created
     * @return The copied matrix
     */
    public static Matrix3f load(Matrix3f src, Matrix3f dest) {
        if (dest == null) {
            dest = new Matrix3f();
        }

        dest.mat[M00] = src.mat[M00];
        dest.mat[M10] = src.mat[M10];
        dest.mat[M20] = src.mat[M20];
        dest.mat[M01] = src.mat[M01];
        dest.mat[M11] = src.mat[M11];
        dest.mat[M21] = src.mat[M21];
        dest.mat[M02] = src.mat[M02];
        dest.mat[M12] = src.mat[M12];
        dest.mat[M22] = src.mat[M22];

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

        mat[M00] = buf[0];
        mat[M01] = buf[1];
        mat[M02] = buf[2];
        mat[M10] = buf[3];
        mat[M11] = buf[4];
        mat[M12] = buf[5];
        mat[M20] = buf[6];
        mat[M21] = buf[7];
        mat[M22] = buf[8];

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

        mat[M00] = buf[0];
        mat[M10] = buf[1];
        mat[M20] = buf[2];
        mat[M01] = buf[3];
        mat[M11] = buf[4];
        mat[M21] = buf[5];
        mat[M02] = buf[6];
        mat[M12] = buf[7];
        mat[M22] = buf[8];

        return this;
    }

    /**
     * Store this matrix in a float buffer. The matrix is stored in column major
     * (openGL) order.
     *
     * @param buf The buffer to store this matrix in
     */
    public Matrix store(float[] buf) {
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
     * Store this matrix in a float buffer. The matrix is stored in row major
     * (maths) order.
     *
     * @param buf The buffer to store this matrix in
     */
    public Matrix storeTranspose(float[] buf) {
        buf[0] = (mat[M00]);
        buf[1] = (mat[M10]);
        buf[2] = (mat[M20]);
        buf[3] = (mat[M01]);
        buf[4] = (mat[M11]);
        buf[5] = (mat[M21]);
        buf[6] = (mat[M02]);
        buf[7] = (mat[M12]);
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
    public static Matrix3f add(Matrix3f left, Matrix3f right, Matrix3f dest) {
        if (dest == null) {
            dest = new Matrix3f();
        }

        dest.mat[M00] = left.mat[M00] + right.mat[M00];
        dest.mat[M01] = left.mat[M01] + right.mat[M01];
        dest.mat[M02] = left.mat[M02] + right.mat[M02];
        dest.mat[M10] = left.mat[M10] + right.mat[M10];
        dest.mat[M11] = left.mat[M11] + right.mat[M11];
        dest.mat[M12] = left.mat[M12] + right.mat[M12];
        dest.mat[M20] = left.mat[M20] + right.mat[M20];
        dest.mat[M21] = left.mat[M21] + right.mat[M21];
        dest.mat[M22] = left.mat[M22] + right.mat[M22];

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
    public static Matrix3f sub(Matrix3f left, Matrix3f right, Matrix3f dest) {
        if (dest == null) {
            dest = new Matrix3f();
        }

        dest.mat[M00] = left.mat[M00] - right.mat[M00];
        dest.mat[M01] = left.mat[M01] - right.mat[M01];
        dest.mat[M02] = left.mat[M02] - right.mat[M02];
        dest.mat[M10] = left.mat[M10] - right.mat[M10];
        dest.mat[M11] = left.mat[M11] - right.mat[M11];
        dest.mat[M12] = left.mat[M12] - right.mat[M12];
        dest.mat[M20] = left.mat[M20] - right.mat[M20];
        dest.mat[M21] = left.mat[M21] - right.mat[M21];
        dest.mat[M22] = left.mat[M22] - right.mat[M22];

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
    public static Matrix3f mul(Matrix3f left, Matrix3f right, Matrix3f dest) {
        if (dest == null) {
            dest = new Matrix3f();
        }

        dest.mat[M00]
                = left.mat[M00] * right.mat[M00] + left.mat[M10] * right.mat[M01] + left.mat[M20] * right.mat[M02];
        dest.mat[M01]
                = left.mat[M01] * right.mat[M00] + left.mat[M11] * right.mat[M01] + left.mat[M21] * right.mat[M02];
        dest.mat[M02]
                = left.mat[M02] * right.mat[M00] + left.mat[M12] * right.mat[M01] + left.mat[M22] * right.mat[M02];
        dest.mat[M10]
                = left.mat[M00] * right.mat[M10] + left.mat[M10] * right.mat[M11] + left.mat[M20] * right.mat[M12];
        dest.mat[M11]
                = left.mat[M01] * right.mat[M10] + left.mat[M11] * right.mat[M11] + left.mat[M21] * right.mat[M12];
        dest.mat[M12]
                = left.mat[M02] * right.mat[M10] + left.mat[M12] * right.mat[M11] + left.mat[M22] * right.mat[M12];
        dest.mat[M20]
                = left.mat[M00] * right.mat[M20] + left.mat[M10] * right.mat[M21] + left.mat[M20] * right.mat[M22];
        dest.mat[M21]
                = left.mat[M01] * right.mat[M20] + left.mat[M11] * right.mat[M21] + left.mat[M21] * right.mat[M22];
        dest.mat[M22]
                = left.mat[M02] * right.mat[M20] + left.mat[M12] * right.mat[M21] + left.mat[M22] * right.mat[M22];

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
    public static Vector3f transform(Matrix3f left, Vector3f right, Vector3f dest) {
        if (dest == null) {
            dest = new Vector3f();
        }

        float x = left.mat[M00] * right.x + left.mat[M10] * right.y + left.mat[M20] * right.z;
        float y = left.mat[M01] * right.x + left.mat[M11] * right.y + left.mat[M21] * right.z;
        float z = left.mat[M02] * right.x + left.mat[M12] * right.y + left.mat[M22] * right.z;

        dest.x = x;
        dest.y = y;
        dest.z = z;

        return dest;
    }

    /**
     * Transpose this matrix
     *
     * @return this
     */
    public Matrix transpose() {
        return transpose(this, this);
    }

    /**
     * Transpose this matrix and place the result in another matrix
     *
     * @param dest The destination matrix or null if a new matrix is to be
     * created
     * @return the transposed matrix
     */
    public Matrix3f transpose(Matrix3f dest) {
        return transpose(this, dest);
    }

    /**
     * Transpose the source matrix and place the result into the destination
     * matrix
     *
     * @param src The source matrix to be transposed
     * @param dest The destination matrix or null if a new matrix is to be
     * created
     * @return the transposed matrix
     */
    public static Matrix3f transpose(Matrix3f src, Matrix3f dest) {
        if (dest == null) {
            dest = new Matrix3f();
        }

        dest.mat[M00] = src.mat[M00];
        dest.mat[M01] = src.mat[M10];
        dest.mat[M02] = src.mat[M20];
        dest.mat[M10] = src.mat[M01];
        dest.mat[M11] = src.mat[M11];
        dest.mat[M12] = src.mat[M21];
        dest.mat[M20] = src.mat[M02];
        dest.mat[M21] = src.mat[M12];
        dest.mat[M22] = src.mat[M22];
        return dest;
    }

    /**
     * @return the determinant of the matrix
     */
    public float determinant() {
        float f
                = mat[M00] * (mat[M11] * mat[M22] - mat[M12] * mat[M21])
                + mat[M01] * (mat[M12] * mat[M20] - mat[M10] * mat[M22])
                + mat[M02] * (mat[M10] * mat[M21] - mat[M11] * mat[M20]);
        return f;
    }

    /**
     * Returns a string representation of this matrix
     */
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(mat[M00]).append(' ').append(mat[M10]).append(' ').append(mat[M20]).append(' ').append('\n');
        buf.append(mat[M01]).append(' ').append(mat[M11]).append(' ').append(mat[M21]).append(' ').append('\n');
        buf.append(mat[M02]).append(' ').append(mat[M12]).append(' ').append(mat[M22]).append(' ').append('\n');
        return buf.toString();
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
     * Invert the source matrix and put the result into the destination matrix
     *
     * @param src The source matrix to be inverted
     * @param dest The destination matrix, or null if a new one is to be created
     * @return The inverted matrix if successful, null otherwise
     */
    public static Matrix3f invert(Matrix3f src, Matrix3f dest) {
        float determinant = src.determinant();

        if (determinant != 0) {
            if (dest == null) {
                dest = new Matrix3f();
            }
            /* do it the ordinary way
			  *
			  * inv(A) = 1/det(A) * adj(T), where adj(T) = transpose(Conjugate Matrix)
			  *
			  * mat[M00] mat[M01] mat[M02]
			  * mat[M10] mat[M11] mat[M12]
			  * mat[M20] mat[M21] mat[M22]
             */
            float determinant_inv = 1f / determinant;

            // get the conjugate matrix
            float t00 = src.mat[M11] * src.mat[M22] - src.mat[M12] * src.mat[M21];
            float t01 = -src.mat[M10] * src.mat[M22] + src.mat[M12] * src.mat[M20];
            float t02 = src.mat[M10] * src.mat[M21] - src.mat[M11] * src.mat[M20];
            float t10 = -src.mat[M01] * src.mat[M22] + src.mat[M02] * src.mat[M21];
            float t11 = src.mat[M00] * src.mat[M22] - src.mat[M02] * src.mat[M20];
            float t12 = -src.mat[M00] * src.mat[M21] + src.mat[M01] * src.mat[M20];
            float t20 = src.mat[M01] * src.mat[M12] - src.mat[M02] * src.mat[M11];
            float t21 = -src.mat[M00] * src.mat[M12] + src.mat[M02] * src.mat[M10];
            float t22 = src.mat[M00] * src.mat[M11] - src.mat[M01] * src.mat[M10];

            dest.mat[M00] = t00 * determinant_inv;
            dest.mat[M11] = t11 * determinant_inv;
            dest.mat[M22] = t22 * determinant_inv;
            dest.mat[M01] = t10 * determinant_inv;
            dest.mat[M10] = t01 * determinant_inv;
            dest.mat[M20] = t02 * determinant_inv;
            dest.mat[M02] = t20 * determinant_inv;
            dest.mat[M12] = t21 * determinant_inv;
            dest.mat[M21] = t12 * determinant_inv;
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
    public Matrix3f negate(Matrix3f dest) {
        return negate(this, dest);
    }

    /**
     * Negate the source matrix and place the result in the destination matrix.
     *
     * @param src The source matrix
     * @param dest The destination matrix, or null if a new matrix is to be
     * created
     * @return the negated matrix
     */
    public static Matrix3f negate(Matrix3f src, Matrix3f dest) {
        if (dest == null) {
            dest = new Matrix3f();
        }

        dest.mat[M00] = -src.mat[M00];
        dest.mat[M01] = -src.mat[M02];
        dest.mat[M02] = -src.mat[M01];
        dest.mat[M10] = -src.mat[M10];
        dest.mat[M11] = -src.mat[M12];
        dest.mat[M12] = -src.mat[M11];
        dest.mat[M20] = -src.mat[M20];
        dest.mat[M21] = -src.mat[M22];
        dest.mat[M22] = -src.mat[M21];
        return dest;
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
     * Set the matrix to be the identity matrix.
     *
     * @param m The matrix to be set to the identity
     * @return m
     */
    public static Matrix3f setIdentity(Matrix3f m) {
        m.mat[M00] = 1.0f;
        m.mat[M01] = 0.0f;
        m.mat[M02] = 0.0f;
        m.mat[M10] = 0.0f;
        m.mat[M11] = 1.0f;
        m.mat[M12] = 0.0f;
        m.mat[M20] = 0.0f;
        m.mat[M21] = 0.0f;
        m.mat[M22] = 1.0f;
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
     * Set the matrix matrix to 0.
     *
     * @param m The matrix to be set to 0
     * @return m
     */
    public static Matrix3f setZero(Matrix3f m) {
        m.mat[M00] = 0.0f;
        m.mat[M01] = 0.0f;
        m.mat[M02] = 0.0f;
        m.mat[M10] = 0.0f;
        m.mat[M11] = 0.0f;
        m.mat[M12] = 0.0f;
        m.mat[M20] = 0.0f;
        m.mat[M21] = 0.0f;
        m.mat[M22] = 0.0f;
        return m;
    }
}
