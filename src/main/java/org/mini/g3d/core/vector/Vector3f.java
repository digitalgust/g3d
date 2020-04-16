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
 * Holds a 3-tuple vector.
 *
 * @author cix_foo <cix_foo@users.sourceforge.net>
 * @version $Revision$ $Id$
 */
public class Vector3f extends Vector2f implements Serializable, ReadableVector3f, WritableVector3f {

    private static final long serialVersionUID = 1L;

    public float  z;

    /**
     * Constructor for Vector3f.
     */
    public Vector3f() {
        super();
    }

    /**
     * Constructor
     */
    public Vector3f(ReadableVector3f src) {
        set(src);
    }

    /**
     * Constructor
     */
    public Vector3f(float x, float y, float z) {
        set(x, y, z);
    }

    /* (non-Javadoc)
     * @see org.mini.g3d.vector.WritableVector2f#set(float, float)
     */
    public Vector3f set(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    /* (non-Javadoc)
     * @see org.mini.g3d.vector.WritableVector3f#set(float, float, float)
     */
    public Vector3f set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    /**
     * Set all components to zero.
     *
     * @return a vector holding the result
     */
    public Vector3f zero() {
        set(0, 0, 0);
        return this;
    }

    /**
     * Load from another Vector3f
     *
     * @param src The source vector
     * @return this
     */
    public Vector3f set(ReadableVector3f src) {
        x = src.getX();
        y = src.getY();
        z = src.getZ();
        return this;
    }

    /**
     * @return the length squared of the vector
     */
    public float lengthSquared() {
        return x * x + y * y + z * z;
    }

    /**
     * Translate a vector
     *
     * @param x The translation in left
     * @param y the translation in top
     * @return this
     */
    public Vector3f translate(float x, float y, float z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public Vector3f translate(Vector3f pos) {
        this.x += pos.x;
        this.y += pos.y;
        this.z += pos.z;
        return this;
    }

    /**
     * Add a vector to another vector and place the result in a destination
     * vector.
     *
     * @param left  The LHS vector
     * @param right The RHS vector
     * @param dest  The destination vector, or null if a new vector is to be
     *              created
     * @return the sum of left and right in dest
     */
    public static Vector3f add(Vector3f left, Vector3f right, Vector3f dest) {
        if (dest == null) {
            return new Vector3f(left.x + right.x, left.y + right.y, left.z + right.z);
        } else {
            dest.set(left.x + right.x, left.y + right.y, left.z + right.z);
            return dest;
        }
    }

    /**
     * Subtract a vector from another vector and place the result in a
     * destination vector.
     *
     * @param left  The LHS vector
     * @param right The RHS vector
     * @param dest  The destination vector, or null if a new vector is to be
     *              created
     * @return left minus right in dest
     */
    public static Vector3f sub(Vector3f left, Vector3f right, Vector3f dest) {
        if (dest == null) {
            return new Vector3f(left.x - right.x, left.y - right.y, left.z - right.z);
        } else {
            dest.set(left.x - right.x, left.y - right.y, left.z - right.z);
            return dest;
        }
    }

    /**
     * The cross product of two vectors.
     *
     * @param left  The LHS vector
     * @param right The RHS vector
     * @param dest  The destination result, or null if a new vector is to be
     *              created
     * @return left cross right
     */
    public static Vector3f cross(
            Vector3f left,
            Vector3f right,
            Vector3f dest) {

        if (dest == null) {
            dest = new Vector3f();
        }

        dest.set(
                left.y * right.z - left.z * right.y,
                right.x * left.z - right.z * left.x,
                left.x * right.y - left.y * right.x
        );

        return dest;
    }

    /**
     * Negate a vector
     *
     * @return this
     */
    public Vector negate() {
        x = -x;
        y = -y;
        z = -z;
        return this;
    }

    /**
     * Negate a vector and place the result in a destination vector.
     *
     * @param dest The destination vector or null if a new vector is to be
     *             created
     * @return the negated vector
     */
    public Vector3f negate(Vector3f dest) {
        if (dest == null) {
            dest = new Vector3f();
        }
        dest.x = -x;
        dest.y = -y;
        dest.z = -z;
        return dest;
    }

    /**
     * Normalise this vector and place the result in another vector.
     *
     * @param dest The destination vector, or null if a new vector is to be
     *             created
     * @return the normalised vector
     */
    public Vector3f normalise(Vector3f dest) {
        float l = length();

        if (dest == null) {
            dest = new Vector3f(x / l, y / l, z / l);
        } else {
            dest.set(x / l, y / l, z / l);
        }

        return dest;
    }

    /**
     * The dot product of two vectors is calculated as v1.left * v2.left + v1.top * v2.top
     * + v1.z * v2.z
     *
     * @param left  The LHS vector
     * @param right The RHS vector
     * @return left dot right
     */
    public static float dot(Vector3f left, Vector3f right) {
        return left.x * right.x + left.y * right.y + left.z * right.z;
    }

    /**
     * Calculate the angle between two vectors, in radians
     *
     * @param a A vector
     * @param b The other vector
     * @return the angle between the two vectors, in radians
     */
    public static float angle(Vector3f a, Vector3f b) {
        float dls = dot(a, b) / (a.length() * b.length());
        if (dls < -1f) {
            dls = -1f;
        } else if (dls > 1.0f) {
            dls = 1.0f;
        }
        return (float) Math.acos(dls);
    }


    static final double PIHalf = Math.PI * 0.5;
    static final double PI2 = Math.PI * 2.0;
    static boolean FASTMATH = true;

    public static double cosFromSin(double sin, double angle) {
        if (FASTMATH)
            return Math.sin(angle + PIHalf);
        // sin(x)^2 + cos(x)^2 = 1
        double cos = Math.sqrt(1.0 - sin * sin);
        double a = angle + PIHalf;
        double b = a - (int) (a / PI2) * PI2;
        if (b < 0.0)
            b = PI2 + b;
        if (b >= Math.PI)
            return -cos;
        return cos;
    }

    /**
     * Rotate this vector the specified radians around the X axis.
     *
     * @param angle the angle in radians
     * @return a vector holding the result
     */
    public Vector3f rotateX(float angle) {
        return rotateX(angle, this);
    }

    /* (non-Javadoc)
     * @see org.joml.Vector3fc#rotateX(float, org.joml.Vector3f)
     */
    public Vector3f rotateX(float angle, Vector3f dest) {
        float sin = (float) Math.sin(angle), cos = (float) cosFromSin(sin, angle);
        float y = this.y * cos - this.z * sin;
        float z = this.y * sin + this.z * cos;
        dest.x = this.x;
        dest.y = y;
        dest.z = z;
        return dest;
    }

    /**
     * Rotate this vector the specified radians around the Y axis.
     *
     * @param angle the angle in radians
     * @return a vector holding the result
     */
    public Vector3f rotateY(float angle) {
        return rotateY(angle, this);
    }

    /* (non-Javadoc)
     * @see org.joml.Vector3fc#rotateY(float, org.joml.Vector3f)
     */
    public Vector3f rotateY(float angle, Vector3f dest) {
        float sin = (float) Math.sin(angle), cos = (float) cosFromSin(sin, angle);
        float x = this.x * cos + this.z * sin;
        float z = -this.x * sin + this.z * cos;
        dest.x = x;
        dest.y = this.y;
        dest.z = z;
        return dest;
    }

    /**
     * Rotate this vector the specified radians around the Z axis.
     *
     * @param angle the angle in radians
     * @return a vector holding the result
     */
    public Vector3f rotateZ(float angle) {
        return rotateZ(angle, this);
    }

    /* (non-Javadoc)
     * @see org.joml.Vector3fc#rotateZ(float, org.joml.Vector3f)
     */
    public Vector3f rotateZ(float angle, Vector3f dest) {
        float sin = (float) Math.sin(angle), cos = (float) cosFromSin(sin, angle);
        float x = this.x * cos - this.y * sin;
        float y = this.x * sin + this.y * cos;
        dest.x = x;
        dest.y = y;
        dest.z = this.z;
        return dest;
    }

    /* (non-Javadoc)
     * @see org.lwjgl.vector.Vector#load(FloatBuffer)
     */
    public Vector3f load(float[] buf) {
        x = buf[0];
        y = buf[1];
        z = buf[2];
        return this;
    }

    /* (non-Javadoc)
     * @see org.lwjgl.vector.Vector#scale(float)
     */
    public Vector3f scale(float scale) {

        x *= scale;
        y *= scale;
        z *= scale;

        return this;

    }

    /* (non-Javadoc)
     * @see org.lwjgl.vector.Vector#store(FloatBuffer)
     */
    public Vector3f store(float[] buf) {

        buf[0] = (x);
        buf[1] = (y);
        buf[2] = (z);

        return this;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuilder sb = new StringBuilder(64);

        sb.append("Vector3f[");
        sb.append(x);
        sb.append(", ");
        sb.append(y);
        sb.append(", ");
        sb.append(z);
        sb.append(']');
        return sb.toString();
    }

    /**
     * @return left
     */
    public  float getX() {
        return x;
    }

    /**
     * @return top
     */
    public  float getY() {
        return y;
    }

    /**
     * Set X
     *
     * @param x
     */
    public  Vector3f setX(float x) {
        this.x = x;
        return this;
    }

    /**
     * Set Y
     *
     * @param y
     */
    public  Vector3f setY(float y) {
        this.y = y;
        return this;
    }

    /**
     * Set Z
     *
     * @param z
     */
    public Vector3f setZ(float z) {
        this.z = z;
        return this;
    }

    /* (Overrides)
     * @see org.lwjgl.vector.ReadableVector3f#getZ()
     */
    public float getZ() {
        return z;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Vector3f other = (Vector3f) obj;

        if (x == other.x && y == other.y && z == other.z) {
            return true;
        }

        return false;
    }


    /**
     * Linearly interpolate <code>this</code> and <code>other</code> using the given interpolation factor <code>t</code>
     * and store the result in <code>this</code>.
     * <p>
     * If <code>t</code> is <code>0.0</code> then the result is <code>this</code>. If the interpolation factor is <code>1.0</code>
     * then the result is <code>other</code>.
     *
     * @param other the other vector
     * @param t     the interpolation factor between 0.0 and 1.0
     * @return a vector holding the result
     */
    public Vector3f lerp(Vector3f other, float t) {
        return lerp(other, t, this);
    }

    /* (non-Javadoc)
     * @see org.joml.Vector3fc#lerp(org.joml.Vector3fc, float, org.joml.Vector3f)
     */
    public Vector3f lerp(Vector3f other, float t, Vector3f dest) {
        dest.x = x + (other.x - x) * t;
        dest.y = y + (other.y - y) * t;
        dest.z = z + (other.z - z) * t;
        return dest;
    }

}
