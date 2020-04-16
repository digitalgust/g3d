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
 * Quaternions for LWJGL!
 *
 * @author fbi
 * @version $Revision$ $Id$
 */
public class Quaternionf extends Vector3f implements Serializable, ReadableVector4f, WritableVector4f {

    private static final long serialVersionUID = 1L;

    public float  w;

    /**
     * C'tor. The quaternion will be initialized to the identity.
     */
    public Quaternionf() {
        super();
        identity();
    }

    /**
     * C'tor
     *
     * @param src
     */
    public Quaternionf(ReadableVector4f src) {
        set(src);
    }

    /**
     * C'tor
     */
    public Quaternionf(float x, float y, float z, float w) {
        set(x, y, z, w);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.mini.g3d.vector.WritableVector2f#set(float, float)
     */
    public Quaternionf set(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.mini.g3d.vector.WritableVector3f#set(float, float, float)
     */
    public Quaternionf set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.mini.g3d.vector.WritableVector4f#set(float, float, float,
     *      float)
     */
    public Quaternionf set(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        return this;
    }

    /**
     * Load from another Vector4f
     *
     * @param src The source vector
     * @return this
     */
    public Quaternionf set(ReadableVector4f src) {
        x = src.getX();
        y = src.getY();
        z = src.getZ();
        w = src.getW();
        return this;
    }

    /**
     * Set this quaternion to the multiplication identity.
     *
     * @return this
     */
    public Quaternionf identity() {
        return identity(this);
    }

    /**
     * Set the given quaternion to the multiplication identity.
     *
     * @param q The quaternion
     * @return q
     */
    public static Quaternionf identity(Quaternionf q) {
        q.x = 0;
        q.y = 0;
        q.z = 0;
        q.w = 1;
        return q;
    }

    /**
     * @return the length squared of the quaternion
     */
    public float lengthSquared() {
        return x * x + y * y + z * z + w * w;
    }

    /**
     * Normalise the source quaternion and place the result in another
     * quaternion.
     *
     * @param src  The source quaternion
     * @param dest The destination quaternion, or null if a new quaternion is to
     *             be created
     * @return The normalised quaternion
     */
    public static Quaternionf normalise(Quaternionf src, Quaternionf dest) {
        float inv_l = 1f / src.length();

        if (dest == null) {
            dest = new Quaternionf();
        }

        dest.set(src.x * inv_l, src.y * inv_l, src.z * inv_l, src.w * inv_l);

        return dest;
    }

    /**
     * Normalise this quaternion and place the result in another quaternion.
     *
     * @param dest The destination quaternion, or null if a new quaternion is to
     *             be created
     * @return the normalised quaternion
     */
    public Quaternionf normalise(Quaternionf dest) {
        return normalise(this, dest);
    }

    /**
     * The dot product of two quaternions
     *
     * @param left  The LHS quat
     * @param right The RHS quat
     * @return left dot right
     */
    public static float dot(Quaternionf left, Quaternionf right) {
        return left.x * right.x + left.y * right.y + left.z * right.z + left.w
                * right.w;
    }

    /**
     * Calculate the conjugate of this quaternion and put it into the given one
     *
     * @param dest The quaternion which should be set to the conjugate of this
     *             quaternion
     */
    public Quaternionf negate(Quaternionf dest) {
        return negate(this, dest);
    }

    /**
     * Calculate the conjugate of this quaternion and put it into the given one
     *
     * @param src  The source quaternion
     * @param dest The quaternion which should be set to the conjugate of this
     *             quaternion
     */
    public static Quaternionf negate(Quaternionf src, Quaternionf dest) {
        if (dest == null) {
            dest = new Quaternionf();
        }

        dest.x = -src.x;
        dest.y = -src.y;
        dest.z = -src.z;
        dest.w = src.w;

        return dest;
    }

    /**
     * Calculate the conjugate of this quaternion
     */
    public Vector negate() {
        return negate(this, this);
    }

    /* (non-Javadoc)
     * @see org.mini.g3d.vector.Vector#load(java.nio.FloatBuffer)
     */
    public Quaternionf load(float[] buf) {
        x = buf[0];
        y = buf[1];
        z = buf[2];
        w = buf[3];
        return this;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.lwjgl.vector.Vector#scale(float)
     */
    public Quaternionf scale(float scale) {
        return scale(scale, this, this);
    }

    /**
     * Scale the source quaternion by scale and put the result in the
     * destination
     *
     * @param scale The amount to scale by
     * @param src   The source quaternion
     * @param dest  The destination quaternion, or null if a new quaternion is to
     *              be created
     * @return The scaled quaternion
     */
    public static Quaternionf scale(float scale, Quaternionf src, Quaternionf dest) {
        if (dest == null) {
            dest = new Quaternionf();
        }
        dest.x = src.x * scale;
        dest.y = src.y * scale;
        dest.z = src.z * scale;
        dest.w = src.w * scale;
        return dest;
    }

    /* (non-Javadoc)
     * @see org.mini.g3d.vector.ReadableVector#store(java.nio.FloatBuffer)
     */
    public Quaternionf store(float[] buf) {
        buf[0] = (x);
        buf[1] = (y);
        buf[2] = (z);
        buf[3] = (w);

        return this;
    }

    /**
     * @return left
     */
    public float getX() {
        return x;
    }

    /**
     * @return top
     */
    public float getY() {
        return y;
    }

    /**
     * Set X
     *
     * @param x
     */
    public Quaternionf setX(float x) {
        this.x = x;
        return this;
    }

    /**
     * Set Y
     *
     * @param y
     */
    public Quaternionf setY(float y) {
        this.y = y;
        return this;
    }

    /**
     * Set Z
     *
     * @param z
     */
    public Quaternionf setZ(float z) {
        this.z = z;
        return this;
    }

    /*
     * (Overrides)
     *
     * @see org.lwjgl.vector.ReadableVector3f#getZ()
     */
    public float getZ() {
        return z;
    }

    /**
     * Set W
     *
     * @param w
     */
    public Quaternionf setW(float w) {
        this.w = w;
        return this;
    }

    /*
     * (Overrides)
     *
     * @see org.lwjgl.vector.ReadableVector3f#getW()
     */
    public float getW() {
        return w;
    }

    public String toString() {
        return "Quaternionf: " + x + " " + y + " " + z + " " + w;
    }

    /**
     * Sets the value of this quaternion to the quaternion product of
     * quaternions left and right (this = left * right). Note that this is safe
     * for aliasing (e.g. this can be left or right).
     *
     * @param left  the first quaternion
     * @param right the second quaternion
     */
    public static Quaternionf mul(Quaternionf left, Quaternionf right,
                                  Quaternionf dest) {
        if (dest == null) {
            dest = new Quaternionf();
        }
        dest.set(left.x * right.w + left.w * right.x + left.y * right.z
                        - left.z * right.y, left.y * right.w + left.w * right.y
                        + left.z * right.x - left.x * right.z, left.z * right.w
                        + left.w * right.z + left.x * right.y - left.y * right.x,
                left.w * right.w - left.x * right.x - left.y * right.y
                        - left.z * right.z);
        return dest;
    }

    /**
     * Multiplies quaternion left by the inverse of quaternion right and places
     * the value into this quaternion. The value of both argument quaternions is
     * preservered (this = left * right^-1).
     *
     * @param left  the left quaternion
     * @param right the right quaternion
     */
    public static Quaternionf mulInverse(Quaternionf left, Quaternionf right,
                                         Quaternionf dest) {
        float n = right.lengthSquared();
        // zero-div may occur.
        n = (n == 0.0 ? n : 1 / n);
        // store on stack once for aliasing-safty
        if (dest == null) {
            dest = new Quaternionf();
        }
        dest.set((left.x * right.w - left.w * right.x - left.y
                * right.z + left.z * right.y)
                * n, (left.y * right.w - left.w * right.y - left.z
                * right.x + left.x * right.z)
                * n, (left.z * right.w - left.w * right.z - left.x
                * right.y + left.y * right.x)
                * n, (left.w * right.w + left.x * right.x + left.y
                * right.y + left.z * right.z)
                * n);

        return dest;
    }

    /**
     * Sets the value of this quaternion to the equivalent rotation of the
     * Axis-Angle argument.
     *
     * @param a1 the axis-angle: (left,top,z) is the axis and w is the angle
     */
    public final void setFromAxisAngle(Vector4f a1) {
        x = a1.x;
        y = a1.y;
        z = a1.z;
        float n = (float) Math.sqrt(x * x + y * y + z * z);
        // zero-div may occur.
        float s = (float) (Math.sin(0.5 * a1.w) / n);
        x *= s;
        y *= s;
        z *= s;
        w = (float) Math.cos(0.5 * a1.w);
    }

    /**
     * Sets the value of this quaternion using the rotational component of the
     * passed matrix.
     *
     * @param m The matrix
     * @return this
     */
    public final Quaternionf setFromMatrix(Matrix4f m) {
        return setFromMatrix(m, this);
    }

    /**
     * Sets the value of the source quaternion using the rotational component of
     * the passed matrix.
     *
     * @param m The source matrix
     * @param q The destination quaternion, or null if a new quaternion is to be
     *          created
     * @return q
     */
    public static Quaternionf setFromMatrix(Matrix4f m, Quaternionf q) {
        return q.setFromMat(m.mat[Matrix4f.M00], m.mat[Matrix4f.M01], m.mat[Matrix4f.M02], m.mat[Matrix4f.M10], m.mat[Matrix4f.M11], m.mat[Matrix4f.M12], m.mat[Matrix4f.M20],
                m.mat[Matrix4f.M21], m.mat[Matrix4f.M22]);
    }

    /**
     * Sets the value of this quaternion using the rotational component of the
     * passed matrix.
     *
     * @param m The source matrix
     */
    public final Quaternionf setFromMatrix(Matrix3f m) {
        return setFromMatrix(m, this);
    }

    /**
     * Sets the value of the source quaternion using the rotational component of
     * the passed matrix.
     *
     * @param m The source matrix
     * @param q The destination quaternion, or null if a new quaternion is to be
     *          created
     * @return q
     */
    public static Quaternionf setFromMatrix(Matrix3f m, Quaternionf q) {
        return q.setFromMat(m.mat[Matrix3f.M00], m.mat[Matrix3f.M01], m.mat[Matrix3f.M02], m.mat[Matrix3f.M10], m.mat[Matrix3f.M11], m.mat[Matrix3f.M12], m.mat[Matrix3f.M20],
                m.mat[Matrix3f.M21], m.mat[Matrix3f.M22]);
    }

    /**
     * Private method to perform the matrix-to-quaternion conversion
     */
    private Quaternionf setFromMat(float m00, float m01, float m02, float m10,
                                   float m11, float m12, float m20, float m21, float m22) {

        float s;
        float tr = m00 + m11 + m22;
        if (tr >= 0.0) {
            s = (float) Math.sqrt(tr + 1.0);
            w = s * 0.5f;
            s = 0.5f / s;
            x = (m21 - m12) * s;
            y = (m02 - m20) * s;
            z = (m10 - m01) * s;
        } else {
            float max = Math.max(Math.max(m00, m11), m22);
            if (max == m00) {
                s = (float) Math.sqrt(m00 - (m11 + m22) + 1.0);
                x = s * 0.5f;
                s = 0.5f / s;
                y = (m01 + m10) * s;
                z = (m20 + m02) * s;
                w = (m21 - m12) * s;
            } else if (max == m11) {
                s = (float) Math.sqrt(m11 - (m22 + m00) + 1.0);
                y = s * 0.5f;
                s = 0.5f / s;
                z = (m12 + m21) * s;
                x = (m01 + m10) * s;
                w = (m02 - m20) * s;
            } else {
                s = (float) Math.sqrt(m22 - (m00 + m11) + 1.0);
                z = s * 0.5f;
                s = 0.5f / s;
                x = (m20 + m02) * s;
                y = (m12 + m21) * s;
                w = (m10 - m01) * s;
            }
        }
        return this;
    }


    /**
     * Converts the quaternion to a 4x4 matrix representing the exact same
     * rotation as this quaternion. (The rotation is only contained in the
     * top-left 3x3 part, but a 4x4 matrix is returned here for convenience
     * seeing as it will be multiplied with other 4x4 matrices).
     * <p>
     * More detailed explanation here:
     * http://www.euclideanspace.com/maths/geometry/rotations/conversions/quaternionToMatrix/
     */
    public Matrix4f toRotationMatrix() {
        Matrix4f matrix = new Matrix4f();
        final float xy = x * y;
        final float xz = x * z;
        final float xw = x * w;
        final float yz = y * z;
        final float yw = y * w;
        final float zw = z * w;
        final float xSquared = x * x;
        final float ySquared = y * y;
        final float zSquared = z * z;
        matrix.mat[Matrix4f.M00] = 1 - 2 * (ySquared + zSquared);
        matrix.mat[Matrix4f.M01] = 2 * (xy - zw);
        matrix.mat[Matrix4f.M02] = 2 * (xz + yw);
        matrix.mat[Matrix4f.M03] = 0;
        matrix.mat[Matrix4f.M10] = 2 * (xy + zw);
        matrix.mat[Matrix4f.M11] = 1 - 2 * (xSquared + zSquared);
        matrix.mat[Matrix4f.M12] = 2 * (yz - xw);
        matrix.mat[Matrix4f.M13] = 0;
        matrix.mat[Matrix4f.M20] = 2 * (xz - yw);
        matrix.mat[Matrix4f.M21] = 2 * (yz + xw);
        matrix.mat[Matrix4f.M22] = 1 - 2 * (xSquared + ySquared);
        matrix.mat[Matrix4f.M23] = 0;
        matrix.mat[Matrix4f.M30] = 0;
        matrix.mat[Matrix4f.M31] = 0;
        matrix.mat[Matrix4f.M32] = 0;
        matrix.mat[Matrix4f.M33] = 1;
        return matrix;
    }
//
//	/**
//	 * Extracts the rotation part of a transformation matrix and converts it to
//	 * a quaternion using the magic of maths.
//	 * 
//	 * More detailed explanation here:
//	 * http://www.euclideanspace.com/maths/geometry/rotations/conversions/matrixToQuaternion/index.htm
//	 */
//	public static Quaternionf fromMatrix(Matrix4f matrix) {
//		float w, left, top, z;
//		float diagonal = matrix.mat[Matrix4f.M00] + matrix.mat[Matrix4f.M11] + matrix.mat[Matrix4f.M22];
//		if (diagonal > 0) {
//			float w4 = (float) (Math.sqrt(diagonal + 1f) * 2f);
//			w = w4 / 4f;
//			left = (matrix.mat[Matrix4f.M21] - matrix.mat[Matrix4f.M12]) / w4;
//			top = (matrix.mat[Matrix4f.M02] - matrix.mat[Matrix4f.M20]) / w4;
//			z = (matrix.mat[Matrix4f.M10] - matrix.mat[Matrix4f.M01]) / w4;
//		} else if ((matrix.mat[Matrix4f.M00] > matrix.mat[Matrix4f.M11]) && (matrix.mat[Matrix4f.M00] > matrix.mat[Matrix4f.M22])) {
//			float x4 = (float) (Math.sqrt(1f + matrix.mat[Matrix4f.M00] - matrix.mat[Matrix4f.M11] - matrix.mat[Matrix4f.M22]) * 2f);
//			w = (matrix.mat[Matrix4f.M21] - matrix.mat[Matrix4f.M12]) / x4;
//			left = x4 / 4f;
//			top = (matrix.mat[Matrix4f.M01] + matrix.mat[Matrix4f.M10]) / x4;
//			z = (matrix.mat[Matrix4f.M02] + matrix.mat[Matrix4f.M20]) / x4;
//		} else if (matrix.mat[Matrix4f.M11] > matrix.mat[Matrix4f.M22]) {
//			float y4 = (float) (Math.sqrt(1f + matrix.mat[Matrix4f.M11] - matrix.mat[Matrix4f.M00] - matrix.mat[Matrix4f.M22]) * 2f);
//			w = (matrix.mat[Matrix4f.M02] - matrix.mat[Matrix4f.M20]) / y4;
//			left = (matrix.mat[Matrix4f.M01] + matrix.mat[Matrix4f.M10]) / y4;
//			top = y4 / 4f;
//			z = (matrix.mat[Matrix4f.M12] + matrix.mat[Matrix4f.M21]) / y4;
//		} else {
//			float z4 = (float) (Math.sqrt(1f + matrix.mat[Matrix4f.M22] - matrix.mat[Matrix4f.M00] - matrix.mat[Matrix4f.M11]) * 2f);
//			w = (matrix.mat[Matrix4f.M10] - matrix.mat[Matrix4f.M01]) / z4;
//			left = (matrix.mat[Matrix4f.M02] + matrix.mat[Matrix4f.M20]) / z4;
//			top = (matrix.mat[Matrix4f.M12] + matrix.mat[Matrix4f.M21]) / z4;
//			z = z4 / 4f;
//		}
//		return new Quaternionf(left, top, z, w);
//	}

    /**
     * Interpolates between two quaternion rotations and returns the resulting
     * quaternion rotation. The interpolation method here is "nlerp", or
     * "normalized-lerp". Another mnethod that could be used is "slerp", and you
     * can see a comparison of the methods here:
     * https://keithmaggio.wordpress.com/2011/02/15/math-magician-lerp-slerp-and-nlerp/
     * <p>
     * and here:
     * http://number-none.com/product/Understanding%20Slerp,%20Then%20Not%20Using%20It/
     *
     * @param a
     * @param b
     * @param blend - a value between 0 and 1 indicating how far to interpolate
     *              between the two quaternions.
     * @return The resulting interpolated rotation in quaternion format.
     */
    public static Quaternionf interpolate(Quaternionf a, Quaternionf b, float blend) {
        Quaternionf result = new Quaternionf(0, 0, 0, 1);
        float dot = a.w * b.w + a.x * b.x + a.y * b.y + a.z * b.z;
        float blendI = 1f - blend;
        if (dot < 0) {
            result.w = blendI * a.w + blend * -b.w;
            result.x = blendI * a.x + blend * -b.x;
            result.y = blendI * a.y + blend * -b.y;
            result.z = blendI * a.z + blend * -b.z;
        } else {
            result.w = blendI * a.w + blend * b.w;
            result.x = blendI * a.x + blend * b.x;
            result.y = blendI * a.y + blend * b.y;
            result.z = blendI * a.z + blend * b.z;
        }
        //result.normalize();
        normalise(result, result);
        return result;
    }


    /**
     * Interpolate between <code>this</code> {@link #normalise() unit} quaternion and the specified
     * <code>target</code> {@link #normalise() unit} quaternion using spherical linear interpolation using the specified interpolation factor <code>alpha</code>.
     * <p>
     * This method resorts to non-spherical linear interpolation when the absolute dot product of <code>this</code> and <code>target</code> is
     * below <code>1E-6f</code>.
     *
     * @param target the target of the interpolation, which should be reached with <code>alpha = 1.0</code>
     * @param alpha  the interpolation factor, within <code>[0..1]</code>
     * @return this
     */
    public Quaternionf slerp(Quaternionf target, float alpha) {
        return slerp(target, alpha, this);
    }

    /* (non-Javadoc)
     * @see org.joml.Quaternionfc#slerp(org.joml.Quaternionfc, float, org.joml.Quaternionf)
     */
    public Quaternionf slerp(Quaternionf target, float alpha, Quaternionf dest) {
        float cosom = x * target.x + y * target.y + z * target.z + w * target.w;
        float absCosom = Math.abs(cosom);
        float scale0, scale1;
        if (1.0f - absCosom > 1E-6f) {
            float sinSqr = 1.0f - absCosom * absCosom;
            float sinom = (float) (1.0 / Math.sqrt(sinSqr));
            float omega = (float) Math.atan2(sinSqr * sinom, absCosom);
            scale0 = (float) (Math.sin((1.0 - alpha) * omega) * sinom);
            scale1 = (float) (Math.sin(alpha * omega) * sinom);
        } else {
            scale0 = 1.0f - alpha;
            scale1 = alpha;
        }
        scale1 = cosom >= 0.0f ? scale1 : -scale1;
        dest.x = scale0 * x + scale1 * target.x;
        dest.y = scale0 * y + scale1 * target.y;
        dest.z = scale0 * z + scale1 * target.z;
        dest.w = scale0 * w + scale1 * target.w;
        return dest;
    }


    private void setFromUnnormalized(float m00, float m01, float m02, float m10, float m11, float m12, float m20, float m21, float m22) {
        float nm00 = m00, nm01 = m01, nm02 = m02;
        float nm10 = m10, nm11 = m11, nm12 = m12;
        float nm20 = m20, nm21 = m21, nm22 = m22;
        float lenX = 1.0f / (float) Math.sqrt(m00 * m00 + m01 * m01 + m02 * m02);
        float lenY = 1.0f / (float) Math.sqrt(m10 * m10 + m11 * m11 + m12 * m12);
        float lenZ = 1.0f / (float) Math.sqrt(m20 * m20 + m21 * m21 + m22 * m22);
        nm00 *= lenX;
        nm01 *= lenX;
        nm02 *= lenX;
        nm10 *= lenY;
        nm11 *= lenY;
        nm12 *= lenY;
        nm20 *= lenZ;
        nm21 *= lenZ;
        nm22 *= lenZ;
        setFromNormalized(nm00, nm01, nm02, nm10, nm11, nm12, nm20, nm21, nm22);
    }

    private void setFromNormalized(float m00, float m01, float m02, float m10, float m11, float m12, float m20, float m21, float m22) {
        float t;
        float tr = m00 + m11 + m22;
        if (tr >= 0.0) {
            t = (float) Math.sqrt(tr + 1.0);
            w = t * 0.5f;
            t = 0.5f / t;
            x = (m12 - m21) * t;
            y = (m20 - m02) * t;
            z = (m01 - m10) * t;
        } else {
            if (m00 >= m11 && m00 >= m22) {
                t = (float) Math.sqrt(m00 - (m11 + m22) + 1.0);
                x = t * 0.5f;
                t = 0.5f / t;
                y = (m10 + m01) * t;
                z = (m02 + m20) * t;
                w = (m12 - m21) * t;
            } else if (m11 > m22) {
                t = (float) Math.sqrt(m11 - (m22 + m00) + 1.0f);
                y = t * 0.5f;
                t = 0.5f / t;
                z = (m21 + m12) * t;
                x = (m10 + m01) * t;
                w = (m20 - m02) * t;
            } else {
                t = (float) Math.sqrt(m22 - (m00 + m11) + 1.0);
                z = t * 0.5f;
                t = 0.5f / t;
                x = (m02 + m20) * t;
                y = (m21 + m12) * t;
                w = (m01 - m10) * t;
            }
        }
    }

    public Quaternionf setFromUnnormalized(Matrix4f mat) {
        setFromUnnormalized(mat.mat[Matrix4f.M00], mat.mat[Matrix4f.M01], mat.mat[Matrix4f.M02], mat.mat[Matrix4f.M10], mat.mat[Matrix4f.M11], mat.mat[Matrix4f.M12], mat.mat[Matrix4f.M20], mat.mat[Matrix4f.M21], mat.mat[Matrix4f.M22]);
        return this;
    }

    public Quaternionf setFromNormalized(Matrix4f mat) {
        setFromNormalized(mat.mat[Matrix4f.M00], mat.mat[Matrix4f.M01], mat.mat[Matrix4f.M02], mat.mat[Matrix4f.M10], mat.mat[Matrix4f.M11], mat.mat[Matrix4f.M12], mat.mat[Matrix4f.M20], mat.mat[Matrix4f.M21], mat.mat[Matrix4f.M22]);
        return this;
    }
}
