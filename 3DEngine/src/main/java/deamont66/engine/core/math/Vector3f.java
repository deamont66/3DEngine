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

public class Vector3f extends javax.vecmath.Vector3f {

        public Vector3f() {
                super();
        }

        public Vector3f(float[] v) {
                super(v);
        }

        public Vector3f(float x, float y, float z) {
                super(x, y, z);
        }

        public Vector3f(javax.vecmath.Vector3f vctrf) {
                super(vctrf);
        }

        public Vector3f(javax.vecmath.Vector3d vctrd) {
                super(vctrd);
        }

        public Vector3f(javax.vecmath.Tuple3f tplf) {
                super(tplf);
        }

        public Vector3f(javax.vecmath.Tuple3d tpld) {
                super(tpld);
        }

        public float max() {
                return Math.max(x, Math.max(y, z));
        }

        public float dot(Vector3f r) {
                return x * r.getX() + y * r.getY() + z * r.getZ();
        }

        public Vector3f cross(Vector3f r) {
                Vector3f ret = new Vector3f();
                ret.cross(this, r);
                return ret;
        }

        public Vector3f normalized() {
                Vector3f ret = new Vector3f(this);
                ret.normalize();
                return ret;
        }

        public Vector3f rotate(Vector3f axis, float angle) {
                float sinAngle = (float) Math.sin(-angle);
                float cosAngle = (float) Math.cos(-angle);

                return this.cross(axis.mul(sinAngle)).add( //Rotation on local X
                        (this.mul(cosAngle)).add( //Rotation on local Z
                                axis.mul(this.dot(axis.mul(1 - cosAngle))))); //Rotation on local Y
        }

        public Vector3f rotate(Quaternion rotation) {
                Quaternion conjugate = rotation.conjugated();
                Quaternion w = new Quaternion(rotation);
                w.mul(this);
                w.mul(conjugate);
                return new Vector3f(w.getX(), w.getY(), w.getZ());
        }

        public Vector3f lerp(Vector3f dest, float lerpFactor) {
                return dest.sub(this).mul(lerpFactor).add(this);
        }

        public Vector3f add(Vector3f r) {
                return new Vector3f(x + r.getX(), y + r.getY(), z + r.getZ());
        }

        public Vector3f add(float r) {
                return new Vector3f(x + r, y + r, z + r);
        }

        public Vector3f sub(Vector3f r) {
                return new Vector3f(x - r.getX(), y - r.getY(), z - r.getZ());
        }

        public Vector3f sub(float r) {
                return new Vector3f(x - r, y - r, z - r);
        }

        public Vector3f mul(Vector3f r) {
                return new Vector3f(x * r.getX(), y * r.getY(), z * r.getZ());
        }

        public Vector3f mul(float r) {
                return new Vector3f(x * r, y * r, z * r);
        }

        public Vector3f div(Vector3f r) {
                return new Vector3f(x / r.getX(), y / r.getY(), z / r.getZ());
        }

        public Vector3f div(float r) {
                return new Vector3f(x / r, y / r, z / r);
        }

        public Vector3f abs() {
                return new Vector3f(Math.abs(x), Math.abs(y), Math.abs(z));
        }

        public Vector2f getXY() {
                return new Vector2f(x, y);
        }

        public Vector2f getYZ() {
                return new Vector2f(y, z);
        }

        public Vector2f getZX() {
                return new Vector2f(z, x);
        }

        public Vector2f getYX() {
                return new Vector2f(y, x);
        }

        public Vector2f getZY() {
                return new Vector2f(z, y);
        }

        public Vector2f getXZ() {
                return new Vector2f(x, z);
        }

        public Vector3f set(Vector3f r) {
                set(r.getX(), r.getY(), r.getZ());
                return this;
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

        public boolean equals(Vector3f r) {
                if (r == null) {
                        return false;
                }
                return x == r.getX() && y == r.getY() && z == r.getZ();
        }

        public static Vector3f one() {
                return new Vector3f(1f, 1f, 1f);
        }

        public static Vector3f zero() {
                return new Vector3f(0f, 0f, 0f);
        }

        public static Vector3f left() {
                return new Vector3f(-1, 0, 0);
        }
        
        public static Vector3f right() {
                return new Vector3f(1, 0, 0);
        }
        
        public static Vector3f back() {
                return new Vector3f(0, 0, -1);
        }

        public static Vector3f up() {
                return new Vector3f(0, 1, 0);
        }
        
        public static Vector3f down() {
                return new Vector3f(0, -1, 0);
        }

        public static Vector3f forward() {
                return new Vector3f(0, 0, 1);
        }
}
