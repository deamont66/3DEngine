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

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4d;
import javax.vecmath.Quat4f;

public class Matrix4f extends javax.vecmath.Matrix4f {

    public Matrix4f(float f, float f1, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10, float f11, float f12, float f13, float f14, float f15) {
        super(f, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15);
    }

    public Matrix4f(float[] floats) {
        super(floats);
    }

    public Matrix4f(Quat4f quat4f, javax.vecmath.Vector3f vctrf, float f) {
        super(quat4f, vctrf, f);
    }

    public Matrix4f(Matrix4d mtrxd) {
        super(mtrxd);
    }

    public Matrix4f(javax.vecmath.Matrix4f mtrxf) {
        super(mtrxf);
    }

    public Matrix4f(Matrix3f mtrxf, javax.vecmath.Vector3f vctrf, float f) {
        super(mtrxf, vctrf, f);
    }

    public Matrix4f() {
    }

    public Matrix4f initIdentity() {
        m00 = 1;    m01 = 0;    m02 = 0;    m03 = 0;
        m10 = 0;    m11 = 1;    m12 = 0;    m13 = 0;
        m20 = 0;    m21 = 0;    m22 = 1;    m23 = 0;
        m30 = 0;    m31 = 0;    m32 = 0;    m33 = 1;
        return this;
    }

    public Matrix4f initTranslation(float x, float y, float z) {
        m00 = 1;    m01 = 0;    m02 = 0;    m03 = x;
        m10 = 0;    m11 = 1;    m12 = 0;    m13 = y;
        m20 = 0;    m21 = 0;    m22 = 1;    m23 = z;
        m30 = 0;    m31 = 0;    m32 = 0;    m33 = 1;

        return this;
    }

    public Matrix4f initRotation(float x, float y, float z) {
        Matrix4f rx = new Matrix4f();
        Matrix4f ry = new Matrix4f();
        Matrix4f rz = new Matrix4f();

        x = (float) Math.toRadians(x);
        y = (float) Math.toRadians(y);
        z = (float) Math.toRadians(z);

        rz.m00 = (float) Math.cos(z);   rz.m01 = -(float) Math.sin(z);      rz.m02 = 0;                     rz.m03 = 0;
        rz.m10 = (float) Math.sin(z);   rz.m11 = (float) Math.cos(z);       rz.m12 = 0;                     rz.m13 = 0;
        rz.m20 = 0;                     rz.m21 = 0;                         rz.m22 = 1;                     rz.m23 = 0;
        rz.m30 = 0;                     rz.m31 = 0;                         rz.m32 = 0;                     rz.m33 = 1;

        rx.m00 = 1;                     rx.m01 = 0;                         rx.m02 = 0;                     rx.m03 = 0;
        rx.m10 = 0;                     rx.m11 = (float) Math.cos(x);       rx.m12 = -(float) Math.sin(x);  rx.m13 = 0;
        rx.m20 = 0;                     rx.m21 = (float) Math.sin(x);       rx.m22 = (float) Math.cos(x);   rx.m23 = 0;
        rx.m30 = 0;                     rx.m31 = 0;                         rx.m32 = 0;                     rx.m33 = 1;

        ry.m00 = (float) Math.cos(y);   ry.m01 = 0;                         ry.m02 = -(float) Math.sin(y);  ry.m03 = 0;
        ry.m10 = 0;                     ry.m11 = 1;                         ry.m12 = 0;                     ry.m13 = 0;
        ry.m20 = (float) Math.sin(y);   ry.m21 = 0;                         ry.m22 = (float) Math.cos(y);   ry.m23 = 0;
        ry.m30 = 0;                     ry.m31 = 0;                         ry.m32 = 0;                     ry.m33 = 1;

        this.set(rz.mul(ry.mul(rx)));

        return this;
    }

    public Matrix4f initScale(float x, float y, float z) {
        m00 = x;        m01 = 0;        m02 = 0;        m03 = 0;
        m10 = 0;        m11 = y;        m12 = 0;        m13 = 0;
        m20 = 0;        m21 = 0;        m22 = z;        m23 = 0;
        m30 = 0;        m31 = 0;        m32 = 0;        m33 = 1;
        return this;
    }

    public Matrix4f initPerspective(float fov, float aspectRatio, float zNear, float zFar) {
        float tanHalfFOV = (float) Math.tan(fov / 2);
        float zRange = zNear - zFar;

        m00 = 1.0f / (tanHalfFOV * aspectRatio);    m01 = 0;                            m02 = 0;                                    m03 = 0;
        m10 = 0;                                    m11 = 1.0f / tanHalfFOV;            m12 = 0;                                    m13 = 0;
        m20 = 0;                                    m21 = 0;                            m22 = (-zNear - zFar) / zRange;             m23 = 2 * zFar * zNear / zRange;
        m30 = 0;                                    m31 = 0;                            m32 = 1;                                    m33 = 0;
        return this;
    }

    public Matrix4f initOrthographic(float left, float right, float bottom, float top, float near, float far) {
        float width = right - left;
        float height = top - bottom;
        float depth = far - near;

        m00 = 2 / width;        m01 = 0;            m02 = 0;                m03 = -(right + left) / width;
        m10 = 0;                m11 = 2 / height;   m12 = 0;                m13 = -(top + bottom) / height;
        m20 = 0;                m21 = 0;            m22 = -2 / depth;       m23 = -(far + near) / depth;
        m30 = 0;                m31 = 0;            m32 = 0;                m33 = 1;

        return this;
    }

    public Matrix4f initRotation(Vector3f forward, Vector3f up) {
        Vector3f f = forward.normalized();

        Vector3f r = up.normalized();
        r = r.cross(f);

        Vector3f u = f.cross(r);

        return initRotation(f, u, r);
    }

    public Matrix4f initRotation(Vector3f forward, Vector3f up, Vector3f right) {
        Vector3f f = forward;
        Vector3f r = right;
        Vector3f u = up;
        
        m00 = r.getX();        m01 = r.getY();        m02 = r.getZ();        m03 = 0;
        m10 = u.getX();        m11 = u.getY();        m12 = u.getZ();        m13 = 0;
        m20 = f.getX();        m21 = f.getY();        m22 = f.getZ();        m23 = 0;
        m30 = 0;               m31 = 0;               m32 = 0;               m33 = 1;

        return this;
    }

    public Vector3f transform(Vector3f r) {
        return new Vector3f(m00 * r.getX() + m01 * r.getY() + m02 * r.getZ() + m03,
                m10 * r.getX() + m11 * r.getY() + m12 * r.getZ() + m13,
                m20 * r.getX() + m21 * r.getY() + m22 * r.getZ() + m23);
    }

    public Matrix4f mul(Matrix4f r) {
        Matrix4f res = new Matrix4f(this);
        res.mul(this, r);
        return res;
    }
}
