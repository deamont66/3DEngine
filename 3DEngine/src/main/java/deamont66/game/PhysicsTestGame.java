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

import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.broadphase.Dispatcher;
import com.bulletphysics.collision.dispatch.CollisionConfiguration;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.StaticPlaneShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.DefaultMotionState;
import deamont66.engine.components.Camera;
import deamont66.engine.components.DirectionalLight;
import deamont66.engine.components.FreeLook;
import deamont66.engine.components.FreeMove;
import deamont66.engine.core.Entity;
import deamont66.engine.core.Game;
import deamont66.engine.core.Transform;
import deamont66.engine.core.math.Quaternion;
import deamont66.engine.core.math.Vector3f;
import deamont66.engine.rendering.Material;
import deamont66.engine.rendering.Mesh;
import deamont66.engine.rendering.Texture;
import deamont66.engine.rendering.Window;
import deamont66.game.entities.LightEntity;
import deamont66.game.entities.MeshEntity;
import org.lwjgl.opengl.Display;

/**
 *
 * @author JiriSimecek
 */
public class PhysicsTestGame extends Game {

    private Camera camera;

    private DynamicsWorld dynamicsWorld;
    private RigidBody groundBody;
    private Entity groundEntity;
    private MeshEntity boxEntity;
    private RigidBody boxBody;
    private RigidBody boxBody2;
    private MeshEntity boxEntity2;

    @Override
    public void init() {
        Window.setTitle("Physics test");

        Material oldBricksMaterial = new Material();
        oldBricksMaterial.setTexture("diffuse", new Texture("bricks.jpg"));
        oldBricksMaterial.setTexture("normalMap", new Texture("bricks_normal.jpg"));
        oldBricksMaterial.setTexture("dispMap", new Texture("bricks_disp.png"));
        oldBricksMaterial.setDipsMapParameters(0.02f, -0.5f);
        oldBricksMaterial.setFloat("specularIntensity", 1);
        oldBricksMaterial.setFloat("specularPower", 8);

        Material brickMaterial = new Material();
        brickMaterial.setTexture("diffuse", new Texture("bricks2.jpg"));
        brickMaterial.setTexture("normalMap", new Texture("bricks2_normal.jpg"));
        brickMaterial.setTexture("dispMap", new Texture("bricks2_disp.jpg"));
        brickMaterial.setFloat("specularIntensity", 1);
        brickMaterial.setFloat("specularPower", 8);

        Material plantMaterial = new Material();
        plantMaterial.setTexture("diffuse", new Texture("plant_2.bmp"));
        plantMaterial.setFloat("specularIntensity", 1);
        plantMaterial.setFloat("specularPower", 8);

        camera = new Camera((float) Math.toRadians(70.0f), (float) Window.getWidth() / (float) Window.getHeight(), 0.01f, 1000.0f);
        addToScene(new Entity().addComponent(new FreeLook(0.5f)).addComponent(new FreeMove(10.0f)).addComponent(camera));
        camera.getTransform().getPos().set(-5, 2, 5);
        camera.getTransform().setRot(new Quaternion(new Vector3f(0, 1, 0), (float) Math.toRadians(130)));

        groundEntity = new MeshEntity(new Mesh("plane4.obj"), oldBricksMaterial, new Vector3f(0, -5, 0), new Quaternion(), new Vector3f(2.5f, 2.5f, 2.5f));
        addToScene(groundEntity);
        
        boxEntity = new MeshEntity(new Mesh("cube.obj"), brickMaterial, new Vector3f(0, 2, 0));
        addToScene(boxEntity);
        
        boxEntity2 = new MeshEntity(new Mesh("cube.obj"), brickMaterial, new Vector3f(0, 8, 0.5f));
        addToScene(boxEntity2);
        
        initPhysics(new Vector3f(0f, -10f, 0f));
        
        DirectionalLight directionalLight = new DirectionalLight(new Vector3f(0.5f, 0.5f, 0.5f), 0.6f, 5);
		addToScene(new LightEntity(directionalLight, new Vector3f(), new Quaternion(new Vector3f(1, 0, 0), (float) Math.toRadians(-45))));

    }

    @Override
    public void update(float delta) {
        Window.setTitle(camera.getTransform().getTransformedPos().toString() + " - " + delta);

        updateEntityTransform(boxEntity.getTransform(), boxBody);
        updateEntityTransform(boxEntity2.getTransform(), boxBody2);
        
        dynamicsWorld.stepSimulation(delta);
    }

    private void initPhysics(Vector3f gravity) {
        CollisionConfiguration collisionConfiguration = new DefaultCollisionConfiguration();

        // use the default collision dispatcher. For parallel processing you can use a diffent dispatcher (see Extras/BulletMultiThreaded)
        Dispatcher dispatcher = new CollisionDispatcher(collisionConfiguration);

        BroadphaseInterface broadphase = new DbvtBroadphase();

        // the default constraint solver. For parallel processing you can use a different solver (see Extras/BulletMultiThreaded)
        SequentialImpulseConstraintSolver solver = new SequentialImpulseConstraintSolver();

        dynamicsWorld = new DiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);

        dynamicsWorld.setGravity(gravity);

        // create a few basic rigid bodies
//        CollisionShape groundShape = new BoxShape(new Vector3f(50f, 1f, 50f));
        
        CollisionShape groundShape = new StaticPlaneShape(new Vector3f(0, 1, 0), 0);
        groundBody = createRigidBody(0, Vector3f.zeros(), groundEntity.getTransform(), groundShape);
        dynamicsWorld.addRigidBody(groundBody);
        
        CollisionShape boxShape = new BoxShape(new Vector3f(1, 1, 1));
        boxBody = createRigidBody(1f, Vector3f.zeros(), boxEntity.getTransform(), boxShape);
        dynamicsWorld.addRigidBody(boxBody);
        
        boxBody2 = createRigidBody(1f, Vector3f.zeros(), boxEntity2.getTransform(), boxShape);
        dynamicsWorld.addRigidBody(boxBody2);
        
    }

    private RigidBody createRigidBody(float mass, Vector3f localInertia, Transform transform, CollisionShape shape) {
        if (mass != 0f) {
            shape.calculateLocalInertia(mass, localInertia);
        }
        
        com.bulletphysics.linearmath.Transform t = new com.bulletphysics.linearmath.Transform();
        t.setRotation(transform.getTransformedRot());
        t.origin.set(transform.getTransformedPos());

        // using motionstate is recommended, it provides interpolation capabilities, and only synchronizes 'active' objects
        DefaultMotionState myMotionState = new DefaultMotionState(t);
        RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(mass, myMotionState, shape, localInertia);
        return new RigidBody(rbInfo);
    }
    
    private void updateEntityTransform(Transform entityTransform, RigidBody body) {
        entityTransform.setPos((Vector3f) body.getCenterOfMassPosition(new Vector3f()));
        entityTransform.setRot((Quaternion) body.getOrientation(new Quaternion()));
    }

}
