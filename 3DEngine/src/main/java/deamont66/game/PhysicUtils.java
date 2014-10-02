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

import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.DefaultMotionState;
import deamont66.engine.core.Transform;
import deamont66.engine.core.math.Quaternion;
import deamont66.engine.core.math.Vector3f;

/**
 *
 * @author JiriSimecek
 */
public class PhysicUtils {
    
    public static RigidBody createRigidBody(float mass, Transform t, CollisionShape shape) {
        return createRigidBody(mass, Vector3f.zero(), t, shape);
    }
    
    public static RigidBody createRigidBody(float mass, Vector3f localInertia, Transform transform, CollisionShape shape) {
        if (mass != 0f) {
            shape.calculateLocalInertia(mass, localInertia);
        }
        
        com.bulletphysics.linearmath.Transform t = new com.bulletphysics.linearmath.Transform();
        t.setIdentity();
        t.setRotation(transform.getTransformedRot());
        t.origin.set(transform.getTransformedPos());

        // using motionstate is recommended, it provides interpolation capabilities, and only synchronizes 'active' objects
        DefaultMotionState myMotionState = new DefaultMotionState(t);
        RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(mass, myMotionState, shape, localInertia);
        return new RigidBody(rbInfo);
    }
    
    public static com.bulletphysics.linearmath.Transform convertTransform(Transform t) {
        com.bulletphysics.linearmath.Transform tr = new com.bulletphysics.linearmath.Transform();
        tr.setIdentity();
        tr.origin.set(t.getPos());
        tr.setRotation(t.getRot());
        return tr;
    }
    
    public static Transform convertTransform(com.bulletphysics.linearmath.Transform t) {
        Transform tr = new Transform();
        tr.setPos(new Vector3f(t.origin));
        tr.setRot((Quaternion) t.getRotation(new Quaternion()));
        return tr;
    }
}
