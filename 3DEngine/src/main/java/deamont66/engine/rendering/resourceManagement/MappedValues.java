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
package deamont66.engine.rendering.resourceManagement;

import deamont66.engine.core.Debug;
import deamont66.engine.core.math.Vector3f;
import deamont66.engine.rendering.Texture;

import java.util.HashMap;

public abstract class MappedValues {

    private final HashMap<String, Vector3f> vector3fHashMap;
    private final HashMap<String, Float> floatHashMap;
    private final HashMap<String, Texture> textureHashMap;

    public MappedValues() {
        vector3fHashMap = new HashMap<String, Vector3f>();
        floatHashMap = new HashMap<String, Float>();
        textureHashMap = new HashMap<>();
        textureHashMap.put("normalMap", new Texture("default_normal.jpg"));
        textureHashMap.put("dispMap", new Texture("default_disp.png"));
    }

    public void setVector3f(String name, Vector3f vector3f) {
        vector3fHashMap.put(name, vector3f);
    }

    public void setFloat(String name, float floatValue) {
        floatHashMap.put(name, floatValue);
    }

    public Vector3f getVector3f(String name) {
        Vector3f result = vector3fHashMap.get(name);
        if (result != null) {
            return result;
        }

        return new Vector3f(0, 0, 0);
    }

    public float getFloat(String name) {
        Float result = floatHashMap.get(name);
        if (result != null) {
            return result;
        }

        return 0;
    }
    
    public void setTexture(String name, Texture texture) {
        if (name.equals("normalMap") && !Debug.ENABLE_NORMAL_MAP) {
            return;
        }
        if (name.equals("dispMap") && !Debug.ENABLE_PARALLAX_MAP) {
            return;
        }
        textureHashMap.put(name, texture);
    }
    
    public Texture getTexture(String name) {
        Texture result = textureHashMap.get(name);
        if (result != null) {
            return result;
        }
        return new Texture("default.png");
    }
}
