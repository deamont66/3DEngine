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
package deamont66.engine.rendering;

import deamont66.engine.core.Debug;
import deamont66.engine.core.math.Matrix4f;

/**
 *
 * @author JiriSimecek
 */
public class ShadowInfo {

    private Matrix4f    m_projection;
    private boolean     m_flipFaces;
    private int         m_shadowMapSizeAsPowerOf2;
    private float       m_shadowSoftness;
    private float       m_lightBleedReductionAmount;
    private float       m_minVariance;

    public ShadowInfo() {
        this(new Matrix4f().initIdentity());
    }
    
    public ShadowInfo(Matrix4f projection) {
        this(projection, false);
    }
    
    public ShadowInfo(Matrix4f projection, boolean flipFaces) {
        this(projection, flipFaces, 0);
    }
    
    public ShadowInfo(Matrix4f projection, boolean flipFaces, int shadowMapSizeAsPowerOf2) {
        this(projection, flipFaces, shadowMapSizeAsPowerOf2, 1.0f);
    }

    public ShadowInfo(Matrix4f projection, boolean flipFaces, int shadowMapSizeAsPowerOf2, float shadowSoftness) {
        this(projection, flipFaces, shadowMapSizeAsPowerOf2, shadowSoftness, 0.2f);
    }
    
    public ShadowInfo(Matrix4f projection, boolean flipFaces, int shadowMapSizeAsPowerOf2, float shadowSoftness, float lightBleedReductionAmount) {
        this(projection, flipFaces, shadowMapSizeAsPowerOf2, shadowSoftness, lightBleedReductionAmount, 0.00002f);
    }

    public ShadowInfo(Matrix4f projection, boolean flipFaces, int shadowMapSizeAsPowerOf2, float shadowSoftness, float lightBleedReductionAmount, float minVariance) {
        this.m_projection = projection;
        this.m_flipFaces = flipFaces;
        this.m_shadowMapSizeAsPowerOf2 = ((Debug.ENABLE_SHADOWS) ? shadowMapSizeAsPowerOf2 : 0);
        this.m_shadowSoftness = shadowSoftness;
        this.m_lightBleedReductionAmount = lightBleedReductionAmount;
        this.m_minVariance = minVariance;
    }

    public Matrix4f getProjection() {
        return m_projection;
    }

    public boolean getFlipfaces() {
        return m_flipFaces;
    }

    public int getShadowMapSizeAsPowerOf2() {
        return m_shadowMapSizeAsPowerOf2;
    }
    
    public float getShadowSoftness() {
        return m_shadowSoftness;
    }

    public float getMinVariance() {
        return m_minVariance;
    }

    public float getLightBleedReductionAmount() {
        return m_lightBleedReductionAmount;
    }
}
