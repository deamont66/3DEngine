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
package deamont66.engine.components;

import deamont66.engine.core.math.Matrix4f;
import deamont66.engine.core.math.Quaternion;
import deamont66.engine.core.math.Vector3f;
import deamont66.engine.rendering.Shader;
import deamont66.engine.rendering.ShadowCameraTransform;
import deamont66.engine.rendering.ShadowInfo;

public class DirectionalLight extends BaseLight {

    private float m_halfShadowArea;

    public DirectionalLight() {
        this(new Vector3f(0,0,0));
    }
    
    public DirectionalLight(Vector3f color) {
        this(color, 0);
    }
    
    public DirectionalLight(Vector3f color, float intensity) {
        this(color, intensity, 0);
    }
    
    public DirectionalLight(Vector3f color, float intensity, int shadowMapSizeAsPowerOf2) {
        this(color, intensity, shadowMapSizeAsPowerOf2, 80.0f);
    }
    
    public DirectionalLight(Vector3f color, float intensity, int shadowMapSizeAsPowerOf2, float shadowArea) {
        this(color, intensity, shadowMapSizeAsPowerOf2, shadowArea, 1.0f);
    }
    
    public DirectionalLight(Vector3f color, float intensity, int shadowMapSizeAsPowerOf2, float shadowArea, float shadowSoftness) {
        this(color, intensity, shadowMapSizeAsPowerOf2, shadowArea, shadowSoftness, 0.2f);
    }
    
    public DirectionalLight(Vector3f color, float intensity, int shadowMapSizeAsPowerOf2, float shadowArea, float shadowSoftness, float lightBleedReductionAmount) {
        this(color, intensity, shadowMapSizeAsPowerOf2, shadowArea, shadowSoftness, lightBleedReductionAmount, 0.00002f);
    }
    
    public DirectionalLight(Vector3f color, float intensity, int shadowMapSizeAsPowerOf2, float shadowArea, float shadowSoftness, float lightBleedReductionAmount, float minVariance) {
        super(color, intensity, new Shader("forward-directional"));
        m_halfShadowArea = shadowArea / 2.0f;
        
        if (shadowMapSizeAsPowerOf2 != 0) {
            setShadowInfo(new ShadowInfo(new Matrix4f().initOrthographic(-m_halfShadowArea, m_halfShadowArea, -m_halfShadowArea,
                    m_halfShadowArea, -m_halfShadowArea, m_halfShadowArea),
                    true, shadowMapSizeAsPowerOf2, shadowSoftness, lightBleedReductionAmount, minVariance));
        }
    }

    @Override
    public ShadowCameraTransform calcShadowCameraTransform(Vector3f mainCameraPos, Quaternion mainCameraRot) {
        Vector3f resultPos = mainCameraPos.add(mainCameraRot.getForward().mul(getHalfShadowArea()));
	Quaternion resultRot = getTransform().getTransformedRot();
	
	float worldTexelSize = (getHalfShadowArea()*2)/((float)(1 << getShadowInfo().getShadowMapSizeAsPowerOf2()));
	
	Vector3f lightSpaceCameraPos = resultPos.rotate(resultRot.conjugated());
	
	lightSpaceCameraPos.setX(worldTexelSize * (float)Math.floor(lightSpaceCameraPos.getX() / worldTexelSize));
	lightSpaceCameraPos.setY(worldTexelSize * (float)Math.floor(lightSpaceCameraPos.getY() / worldTexelSize));
	
	resultPos = lightSpaceCameraPos.rotate(resultRot);
	
	return new ShadowCameraTransform(resultPos, resultRot);
    }

    public float getHalfShadowArea() {
        return m_halfShadowArea;
    }
}
