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

import deamont66.engine.components.BaseLight;
import deamont66.engine.components.Camera;
import deamont66.engine.core.Scene;
import deamont66.engine.core.Transform;
import deamont66.engine.core.math.Matrix4f;
import deamont66.engine.rendering.resourceManagement.MappedValues;

public abstract class Renderer extends MappedValues {

    public abstract void updateUniformStruct(Transform transform, Material material, Shader shader, String uniformName, String uniformType);

    public abstract void render(Scene object);

    public abstract String getRendererVersion();

    public abstract Camera getMainCamera();

    public abstract void setMainCamera(Camera mainCamera);

    public void addLight(BaseLight light) {
    }
    
    public void clearLights() {
    }

    public int getSamplerSlot(String samplerName) {
        return 0;
    }

    public BaseLight getActiveLight() {
        return null;
    }

    public Matrix4f getLightMatrix() {
        return new Matrix4f();
    }

    public void to2D(int width, int height) {
    }

    public void backTo3D() {
    }
}
