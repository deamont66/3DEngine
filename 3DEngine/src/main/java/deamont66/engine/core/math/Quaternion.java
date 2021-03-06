/*
 * Copyright (c) 2012 - 2014, Jiří Šimeček
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 * 
 */
package deamont66.engine.core.math;

public class Quaternion extends javax.vecmath.Quat4f {

        public Quaternion(float x, float y, float z, float w) {
                super(x, y, z, w);
        }

        public Quaternion(float[] floats) {
                super(floats);
        }

        public Quaternion(javax.vecmath.Quat4f quat4f) {
                super(quat4f);
        }

        public Quaternion(javax.vecmath.Quat4d quat4d) {
                super(quat4d);
        }

        public Quaternion(javax.vecmath.Tuple4f tplf) {
                super(tplf);
        }

        public Quaternion(javax.vecmath.Tuple4d tpld) {
                super(tpld);
        }

        public Quaternion() {
                this(0, 0, 0, 1);
        }

        public Quaternion(Vector3f axis, float angle) {
                float sinHalfAngle = (float) Math.sin(angle / 2);
                float cosHalfAngle = (float) Math.cos(angle / 2);

                this.x = axis.getX() * sinHalfAngle;
                this.y = axis.getY() * sinHalfAngle;
                this.z = axis.getZ() * sinHalfAngle;
                this.w = cosHalfAngle;
        }

        /**
         *
         * <code>fromAxes</code> creates a <code>Quaternion</code> that
         * represents the coordinate system defined by three axes. These axes
         * are assumed to be orthogonal and no error checking is applied. Thus,
         * the user must insure that the three axes being provided indeed
         * represents a proper right handed coordinate system.
         *
         * @param axis the array containing the three vectors representing the
         * coordinate system.
         *
         */
        public Quaternion fromAxes(Vector3f[] axis) {
                if (axis.length != 3) {
                        throw new IllegalArgumentException(
                                "Axis array must have three elements");
                }
                return fromAxes(axis[0], axis[1], axis[2]);
        }

        /**
         *
         * <code>fromAxes</code> creates a <code>Quaternion</code> that
         * represents the coordinate system defined by three axes. These axes
         * are assumed to be orthogonal and no error checking is applied. Thus,
         * the user must insure that the three axes being provided indeed
         * represents a proper right handed coordinate system.
         *
         * @param xAxis vector representing the x-axis of the coordinate system.
         * @param yAxis vector representing the y-axis of the coordinate system.
         * @param zAxis vector representing the z-axis of the coordinate system.
         */
        public Quaternion fromAxes(Vector3f xAxis, Vector3f yAxis, Vector3f zAxis) {
                return fromRotationMatrix(xAxis.x, yAxis.x, zAxis.x, xAxis.y, yAxis.y,
                        zAxis.y, xAxis.z, yAxis.z, zAxis.z);
        }

        public Quaternion fromRotationMatrix(float m00, float m01, float m02,
                float m10, float m11, float m12,
                float m20, float m21, float m22) {
                // Use the Graphics Gems code, from 
                // ftp://ftp.cis.upenn.edu/pub/graphics/shoemake/quatut.ps.Z
                // *NOT* the "Matrix and Quaternions FAQ", which has errors!

                // the trace is the sum of the diagonal elements; see
                // http://mathworld.wolfram.com/MatrixTrace.html
                float t = m00 + m11 + m22;

                // we protect the division by s by ensuring that s>=1
                if (t >= 0) { // |w| >= .5
                        float s = (float) Math.sqrt(t + 1); // |s|>=1 ...
                        w = 0.5f * s;
                        s = 0.5f / s;                 // so this division isn't bad
                        x = (m21 - m12) * s;
                        y = (m02 - m20) * s;
                        z = (m10 - m01) * s;
                } else if ((m00 > m11) && (m00 > m22)) {
                        float s = (float) Math.sqrt(1.0f + m00 - m11 - m22); // |s|>=1
                        x = s * 0.5f; // |x| >= .5
                        s = 0.5f / s;
                        y = (m10 + m01) * s;
                        z = (m02 + m20) * s;
                        w = (m21 - m12) * s;
                } else if (m11 > m22) {
                        float s = (float) Math.sqrt(1.0f + m11 - m00 - m22); // |s|>=1
                        y = s * 0.5f; // |y| >= .5
                        s = 0.5f / s;
                        x = (m10 + m01) * s;
                        z = (m21 + m12) * s;
                        w = (m02 - m20) * s;
                } else {
                        float s = (float) Math.sqrt(1.0f + m22 - m00 - m11); // |s|>=1
                        z = s * 0.5f; // |z| >= .5
                        s = 0.5f / s;
                        x = (m02 + m20) * s;
                        y = (m21 + m12) * s;
                        w = (m10 - m01) * s;
                }

                return this;
        }

        public float length() {
                return (float) Math.sqrt(x * x + y * y + z * z + w * w);
        }

        public Quaternion conjugated() {
                Quaternion ret = new Quaternion(this);
                ret.conjugate();
                return ret;
        }

        public void mul(float r) {
                set(x * r, y * r, z * r, w * r);
        }

        public void mul(Vector3f r) {
                float w_ = -x * r.getX() - y * r.getY() - z * r.getZ();
                float x_ = w * r.getX() + y * r.getZ() - z * r.getY();
                float y_ = w * r.getY() + z * r.getX() - x * r.getZ();
                float z_ = w * r.getZ() + x * r.getY() - y * r.getX();
                set(x_, y_, z_, w_);
        }

        public Matrix4f toRotationMatrix() {
                Vector3f forward = new Vector3f(2.0f * (x * z - w * y), 2.0f * (y * z + w * x), 1.0f - 2.0f * (x * x + y * y));
                Vector3f up = new Vector3f(2.0f * (x * y + w * z), 1.0f - 2.0f * (x * x + z * z), 2.0f * (y * z - w * x));
                Vector3f right = new Vector3f(1.0f - 2.0f * (y * y + z * z), 2.0f * (x * y - w * z), 2.0f * (x * z + w * y));

                return new Matrix4f().initRotation(forward, up, right);
        }

        public float dot(Quaternion r) {
                return x * r.getX() + y * r.getY() + z * r.getZ() + w * r.getW();
        }

        public Quaternion nlerp(Quaternion dest, float lerpFactor, boolean shortest) {
                Quaternion correctedDest = dest;

                if (shortest && this.dot(dest) < 0) {
                        correctedDest = new Quaternion(-dest.getX(), -dest.getY(), -dest.getZ(), -dest.getW());
                }

                correctedDest.sub(this);
                correctedDest.mul(lerpFactor);
                correctedDest.add(this);
                correctedDest.normalize();
                return correctedDest;
        }

        public Quaternion slerp(Quaternion dest, float lerpFactor, boolean shortest) {
                final float EPSILON = 1e3f;

                float cos = this.dot(dest);
                Quaternion correctedDest = dest;

                if (shortest && cos < 0) {
                        cos = -cos;
                        correctedDest = new Quaternion(-dest.getX(), -dest.getY(), -dest.getZ(), -dest.getW());
                }

                if (Math.abs(cos) >= 1 - EPSILON) {
                        return nlerp(correctedDest, lerpFactor, false);
                }

                float sin = (float) Math.sqrt(1.0f - cos * cos);
                float angle = (float) Math.atan2(sin, cos);
                float invSin = 1.0f / sin;

                float srcFactor = (float) Math.sin((1.0f - lerpFactor) * angle) * invSin;
                float destFactor = (float) Math.sin((lerpFactor) * angle) * invSin;

                Quaternion ret = new Quaternion(this);
                ret.mul(srcFactor);
                correctedDest.mul(destFactor);
                ret.add(correctedDest);
                return ret;
        }

        //From Ken Shoemake's "Quaternion Calculus and Fast Animation" article
        public Quaternion(Matrix4f rot) {
                float trace = rot.getElement(0, 0) + rot.getElement(1, 1) + rot.getElement(2, 2);

                if (trace > 0) {
                        float s = 0.5f / (float) Math.sqrt(trace + 1.0f);
                        w = 0.25f / s;
                        x = (rot.getElement(1, 2) - rot.getElement(2, 1)) * s;
                        y = (rot.getElement(2, 0) - rot.getElement(0, 2)) * s;
                        z = (rot.getElement(0, 1) - rot.getElement(1, 0)) * s;
                } else {
                        if (rot.getElement(0, 0) > rot.getElement(1, 1) && rot.getElement(0, 0) > rot.getElement(2, 2)) {
                                float s = 2.0f * (float) Math.sqrt(1.0f + rot.getElement(0, 0) - rot.getElement(1, 1) - rot.getElement(2, 2));
                                w = (rot.getElement(1, 2) - rot.getElement(2, 1)) / s;
                                x = 0.25f * s;
                                y = (rot.getElement(1, 0) + rot.getElement(0, 1)) / s;
                                z = (rot.getElement(2, 0) + rot.getElement(0, 2)) / s;
                        } else if (rot.getElement(1, 1) > rot.getElement(2, 2)) {
                                float s = 2.0f * (float) Math.sqrt(1.0f + rot.getElement(1, 1) - rot.getElement(0, 0) - rot.getElement(2, 2));
                                w = (rot.getElement(2, 0) - rot.getElement(0, 2)) / s;
                                x = (rot.getElement(1, 0) + rot.getElement(0, 1)) / s;
                                y = 0.25f * s;
                                z = (rot.getElement(2, 1) + rot.getElement(1, 2)) / s;
                        } else {
                                float s = 2.0f * (float) Math.sqrt(1.0f + rot.getElement(2, 2) - rot.getElement(0, 0) - rot.getElement(1, 1));
                                w = (rot.getElement(0, 1) - rot.getElement(1, 0)) / s;
                                x = (rot.getElement(2, 0) + rot.getElement(0, 2)) / s;
                                y = (rot.getElement(1, 2) + rot.getElement(2, 1)) / s;
                                z = 0.25f * s;
                        }
                }

                float length = (float) Math.sqrt(x * x + y * y + z * z + w * w);
                x /= length;
                y /= length;
                z /= length;
                w /= length;
        }

        public Vector3f getForward() {
                return new Vector3f(0, 0, 1).rotate(this);
        }

        public Vector3f getBack() {
                return new Vector3f(0, 0, -1).rotate(this);
        }

        public Vector3f getUp() {
                return new Vector3f(0, 1, 0).rotate(this);
        }

        public Vector3f getDown() {
                return new Vector3f(0, -1, 0).rotate(this);
        }

        public Vector3f getRight() {
                return new Vector3f(1, 0, 0).rotate(this);
        }

        public Vector3f getLeft() {
                return new Vector3f(-1, 0, 0).rotate(this);
        }

        public float getX() {
                return x;
        }

        public void setX(float x) {
                this.x = x;
        }

        public float getY() {
                return y;
        }

        public void setY(float y) {
                this.y = y;
        }

        public float getZ() {
                return z;
        }

        public void setZ(float z) {
                this.z = z;
        }

        public float getW() {
                return w;
        }

        public void setW(float w) {
                this.w = w;
        }

        public boolean equals(Quaternion r) {
                return x == r.getX() && y == r.getY() && z == r.getZ() && w == r.getW();
        }

        public static Quaternion identity() {
                return new Quaternion();
        }

        /**
         * <code>lookAt</code> is a convienence method for auto-setting the
         * quaternion based on a direction and an up vector. It computes the
         * rotation to transform the z-axis to point into 'direction' and the
         * y-axis to 'up'.
         *
         * @param direction where to look at in terms of local coordinates
         * @param up a vector indicating the local up direction. (typically {0,
         * 1, 0} in jME.)
         */
        public void lookAt(Vector3f direction, Vector3f up) {
                Vector3f vect3 = direction.normalized();
                Vector3f vect1 = up.cross(direction).normalized();
                Vector3f vect2 = direction.cross(vect1).normalized();
                fromAxes(vect1, vect2, vect3);
        }
        
        public void lookAt(Vector3f direction) {
                lookAt(direction, Vector3f.up());
        }
}
