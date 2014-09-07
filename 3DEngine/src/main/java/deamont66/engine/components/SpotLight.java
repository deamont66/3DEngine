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
import deamont66.engine.core.math.Vector3f;
import deamont66.engine.rendering.Attenuation;
import deamont66.engine.rendering.Shader;
import deamont66.engine.rendering.ShadowInfo;

public class SpotLight extends PointLight {

    private final float cutoff;
    
    public SpotLight() {
        this(new Vector3f(0,0,0));
    }
    
    public SpotLight(Vector3f color) {
        this(color, 0);
    }
    
    public SpotLight(Vector3f color, float intensity) {
        this(color, intensity, new Attenuation());
    }
    
    public SpotLight(Vector3f color, float intensity, Attenuation attenuation) {
        this(color, intensity, attenuation, (float) Math.toRadians(170.0f));
    }
    
    public SpotLight(Vector3f color, float intensity, Attenuation attenuation, float viewAngle) {
        this(color, intensity, attenuation, viewAngle, 0);
    }
    
    public SpotLight(Vector3f color, float intensity, Attenuation attenuation, float viewAngle, int shadowMapSizeAsPowerOf2) {
        this(color, intensity, attenuation, viewAngle, shadowMapSizeAsPowerOf2, 1);
    }
    
    public SpotLight(Vector3f color, float intensity, Attenuation attenuation, float viewAngle, int shadowMapSizeAsPowerOf2, float shadowSoftness) {
        this(color, intensity, attenuation, viewAngle, shadowMapSizeAsPowerOf2, shadowSoftness, 0.02f);
    }
    
    public SpotLight(Vector3f color, float intensity, Attenuation attenuation, float viewAngle, int shadowMapSizeAsPowerOf2, float shadowSoftness, float lightBleedReductionAmount) {
        this(color, intensity, attenuation, viewAngle, shadowMapSizeAsPowerOf2, shadowSoftness, lightBleedReductionAmount, 0.00002f);
    }
    
    public SpotLight(Vector3f color, float intensity, Attenuation attenuation, float viewAngle, int shadowMapSizeAsPowerOf2, float shadowSoftness, float lightBleedReductionAmount, float minVariance) {
        super(color, intensity, attenuation, new Shader("forward-spot"));
        cutoff = (float) Math.cos(viewAngle / 2);

        if (shadowMapSizeAsPowerOf2 != 0) {
            setShadowInfo(new ShadowInfo(new Matrix4f().initPerspective(viewAngle, 1.0f, 0.1f, getRange()), false, shadowMapSizeAsPowerOf2, shadowSoftness, lightBleedReductionAmount, minVariance));
        }
    }

    public float getCutoff() {
        return cutoff;
    }
}
