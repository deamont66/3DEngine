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

import deamont66.engine.core.CoreEngine;
import deamont66.engine.core.math.Quaternion;
import deamont66.engine.core.math.Vector3f;
import deamont66.engine.rendering.Shader;
import deamont66.engine.rendering.ShadowCameraTransform;
import deamont66.engine.rendering.ShadowInfo;

public class BaseLight extends EntityComponent {

	private Vector3f    m_color;
	private float       m_intensity;
	private Shader      m_shader;
	private boolean     m_active;
	private ShadowInfo  m_shadowInfo;

	public BaseLight(Vector3f color, float intensity, Shader shader) {
		this.m_color = color;
		this.m_intensity = intensity;
                this.m_shader = shader;
		this.m_active = true;
		this.m_shadowInfo = new ShadowInfo();
	}

        public ShadowCameraTransform calcShadowCameraTransform(Vector3f mainCameraPos, Quaternion mainCameraRot) {
            return new ShadowCameraTransform(getTransform().getTransformedPos(), getTransform().getTransformedRot());
        }
        
	@Override
	public void addToEngine(CoreEngine engine) {
		engine.getRenderingEngine().addLight(this);
	}

	public Shader getShader() {
		return m_shader;
	}

	public Vector3f getColor() {
		return m_color;
	}

	public float getIntensity() {
		return m_intensity;
	}

	public boolean isActive() {
		return m_active;
	}

	public void setActive(boolean active) {
		this.m_active = active;
	}

	public ShadowInfo getShadowInfo() {
		return m_shadowInfo;
	}

	protected void setShadowInfo(ShadowInfo shadowInfo) {
		this.m_shadowInfo = shadowInfo;
	}
}
