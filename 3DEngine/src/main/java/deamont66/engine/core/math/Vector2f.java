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

public class Vector2f extends javax.vecmath.Vector2f
{

    public Vector2f(float f, float f1) {
        super(f, f1);
    }

    public Vector2f(float[] floats) {
        super(floats);
    }

    public Vector2f(javax.vecmath.Vector2f vctrf) {
        super(vctrf);
    }

    public Vector2f(javax.vecmath.Vector2d vctrd) {
        super(vctrd);
    }

    public Vector2f(javax.vecmath.Tuple2f tplf) {
        super(tplf);
    }

    public Vector2f(javax.vecmath.Tuple2d tpld) {
        super(tpld);
    }

    public Vector2f() {
        this(0,0);
    }

	public float max()
	{
		return Math.max(x, y);
	}

	public float cross(Vector2f r)
	{
		return x * r.getY() - y * r.getX();
	}

	public Vector2f lerp(Vector2f dest, float lerpFactor)
	{
                Vector2f ret = new Vector2f(dest);
                ret.sub(this);
                ret.mul(lerpFactor);
                ret.add(this);
		return ret;
	}

	public Vector2f rotate(float angle)
	{
		double rad = Math.toRadians(angle);
		double cos = Math.cos(rad);
		double sin = Math.sin(rad);
		
		return new Vector2f((float)(x * cos - y * sin),(float)(x * sin + y * cos));
	}
	
	public Vector2f add(float r)
	{
                x += r;
                y += r;
                return this;
	}
	
	public Vector2f sub(float r)
	{
		x -= r;
                y -= r;
                return this;
	}
	
	public Vector2f mul(javax.vecmath.Tuple2d r)
	{
		x *= r.x;
                y *= r.y;
                return this;
	}
	
	public Vector2f mul(float r)
	{
                x *= r;
                y *= r;
		return this;
	}
	
	public Vector2f div(javax.vecmath.Tuple2d r)
	{
		x /= r.x;
                y /= r.y;
                return this;
	}
	
	public Vector2f div(float r)
	{
		x /= r;
                y /= r;
                return this;
	}
        
        public float getX() {
            return x;
        }
        
        public float getY() {
            return y;
        }
        
        public void setX(float x) {
            this.x = x;
        }
        
        public void setY(float y) {
            this.y = y;
        }
}
