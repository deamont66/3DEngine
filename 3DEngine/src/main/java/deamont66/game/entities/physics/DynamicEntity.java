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
package deamont66.game.entities.physics;

import com.bulletphysics.collision.shapes.BvhTriangleMeshShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.IndexedMesh;
import com.bulletphysics.collision.shapes.TriangleIndexVertexArray;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import deamont66.engine.components.MeshRenderer;
import deamont66.engine.core.Entity;
import deamont66.engine.core.Transform;
import deamont66.engine.core.math.Quaternion;
import deamont66.engine.core.math.Vector3f;
import deamont66.engine.rendering.Material;
import deamont66.engine.rendering.Mesh;
import deamont66.engine.rendering.meshLoading.IndexedModel;
import deamont66.game.PhysicUtils;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * @author JiriSimecek
 */
public class DynamicEntity extends Entity {
    
    private final RigidBody body;
    private final float mass;

    /**
     * Creates non-dynamic collidable object. Same as setting mass of object to zero.
     * @param modelName
     * @param model
     * @param material
     * @param initialTransform 
     */
    public DynamicEntity(String modelName, IndexedModel model, Material material, Transform initialTransform) {
        this(0, modelName, model, material, initialTransform);
    }

    /**
     * Creates non-dynamic collidable object. Same as setting mass of object to zero.
     * @param mesh
     * @param shape
     * @param material
     * @param initialTransform 
     */
    public DynamicEntity(Mesh mesh, CollisionShape shape, Material material, Transform initialTransform) {
        this(0, mesh, shape, material, initialTransform);
    }
    
    public DynamicEntity(float mass, String modelName, IndexedModel model, Material material, Transform initialTransform) {
        super();
        if(!model.complete().isValid()) {
            throw new RuntimeException("Model is not valid and cannot be converted to jBullet's IndexedMesh");
        }
        IndexedMesh mesh = new IndexedMesh();
        mesh.numTriangles = model.getIndices().size() / 3;
        mesh.numVertices = model.getPositions().size();
        mesh.triangleIndexStride = 3 * 4;
        mesh.vertexStride = 3 * 4;
        
        mesh.triangleIndexBase = ByteBuffer.allocateDirect(model.getIndices().size() * 4).order(ByteOrder.nativeOrder());
        for (int i = 0; i < model.getIndices().size(); i++) {
            mesh.triangleIndexBase.putInt(model.getIndices().get(i));
        }
        
        mesh.vertexBase = ByteBuffer.allocateDirect(model.getPositions().size() * 3 * 4).order(ByteOrder.nativeOrder());
        for (int i = 0; i < model.getPositions().size(); i++) {
            mesh.vertexBase.putFloat(model.getPositions().get(i).x);
            mesh.vertexBase.putFloat(model.getPositions().get(i).y);
            mesh.vertexBase.putFloat(model.getPositions().get(i).z);
        }
        
        TriangleIndexVertexArray vertexArray = new TriangleIndexVertexArray();
        vertexArray.addIndexedMesh(mesh);
        
        BvhTriangleMeshShape meshShape = new BvhTriangleMeshShape(vertexArray, true);
        
        getTransform().setPos(initialTransform.getPos());
        getTransform().setRot(initialTransform.getRot());
        getTransform().setScale(1f);
        
        addComponent(new MeshRenderer(new Mesh(modelName, model), material));
        
        this.body = PhysicUtils.createRigidBody(mass, initialTransform, meshShape);
        this.mass = mass;
    }
    
    public DynamicEntity(float mass, Mesh mesh, CollisionShape shape, Material material, Transform initialTransform) {
        super();
        
        getTransform().setPos(initialTransform.getPos());
        getTransform().setRot(initialTransform.getRot());
        getTransform().setScale(1f);
        
        addComponent(new MeshRenderer(mesh, material));
        
        this.body = PhysicUtils.createRigidBody(mass, initialTransform, shape);
        this.mass = mass;
    }
    
    public void addToWorld(DynamicsWorld world) {
        world.addRigidBody(body);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if(!body.isStaticObject())
            updateTransform(body.getWorldTransform(new com.bulletphysics.linearmath.Transform()));
    }
    
    /**
     * Overloads {@link #updateTransform(com.bulletphysics.linearmath.Transform, deamont66.engine.core.math.Vector3f, deamont66.engine.core.math.Quaternion) }
     * @param sourceTransform 
     */
    protected void updateTransform(com.bulletphysics.linearmath.Transform sourceTransform) {
        updateTransform(sourceTransform, Vector3f.zero(), Quaternion.identity());
    }
    
    /**
     * Overloads {@link #updateTransform(com.bulletphysics.linearmath.Transform, deamont66.engine.core.math.Vector3f, deamont66.engine.core.math.Quaternion) }
     * @param sourceTransform
     * @param offset 
     */
    protected void updateTransform(com.bulletphysics.linearmath.Transform sourceTransform, Vector3f offset) {
        updateTransform(sourceTransform, offset, Quaternion.identity());
    }
    
    /**
     * Updates entity transform from jBullet transform object. Usualy updates entity position to physics body position.
     * @param sourceTransform jBullet transform object
     * @param offsetPos position offset between entity and jBullet transform
     * @param offsetRot rotation offset between entity and jBullet transform
     */
    protected void updateTransform(com.bulletphysics.linearmath.Transform sourceTransform, Vector3f offsetPos, Quaternion offsetRot) {
        getTransform().setPos(new Vector3f(sourceTransform.origin).add(offsetPos));
        Quaternion rotation = (Quaternion) sourceTransform.getRotation(new Quaternion());
        rotation.mul(offsetRot);
        getTransform().setRot(rotation);
    }

    public RigidBody getRigridBody() {
        return body;
    }
}
