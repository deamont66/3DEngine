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

package deamont66.game;

import deamont66.engine.components.MeshRenderer;
import deamont66.engine.core.GameObject;
import deamont66.engine.core.math.Quaternion;
import deamont66.engine.core.math.Vector3f;
import deamont66.engine.rendering.Material;
import deamont66.engine.rendering.Mesh;

/**
 *
 * @author JiriSimecek
 */
public class MeshObject extends GameObject {
    
    
    public MeshObject(Mesh mesh, Material material) {
        this(mesh, material, null, null, null);
    }
    
    public MeshObject(Mesh mesh, Material material, Vector3f pos) {
        this(mesh, material, pos, null, null);
    }
    
    public MeshObject(Mesh mesh, Material material, Vector3f pos, Quaternion rot) {
        this(mesh, material, pos, rot, null);
    }
    
    public MeshObject(Mesh mesh, Material material, Vector3f pos, Quaternion rot, Vector3f scale) {
        super();
        addMesh(mesh, material, pos, rot, scale);
    }
    
    private void addMesh(Mesh mesh, Material material, Vector3f pos, Quaternion rot, Vector3f scale) {
        addComponent(new MeshRenderer(mesh, material));
        if(pos != null)
            getTransform().getPos().set(pos);
        if(rot != null)
            getTransform().getRot().set(rot);
        if(scale != null)
            getTransform().getScale().set(scale);
    }
}