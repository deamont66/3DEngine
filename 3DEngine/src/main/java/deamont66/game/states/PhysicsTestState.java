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

package deamont66.game.states;

import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.broadphase.Dispatcher;
import com.bulletphysics.collision.dispatch.CollisionConfiguration;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.CompoundShape;
import com.bulletphysics.collision.shapes.CylinderShapeX;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.dynamics.vehicle.DefaultVehicleRaycaster;
import com.bulletphysics.dynamics.vehicle.RaycastVehicle;
import com.bulletphysics.dynamics.vehicle.VehicleRaycaster;
import com.bulletphysics.dynamics.vehicle.VehicleTuning;
import com.bulletphysics.dynamics.vehicle.WheelInfo;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.MotionState;
import deamont66.engine.components.Camera;
import deamont66.engine.components.DirectionalLight;
import deamont66.engine.core.Entity;
import deamont66.engine.core.Game;
import deamont66.engine.core.GameState;
import deamont66.engine.core.Input;
import deamont66.engine.core.Transform;
import deamont66.engine.core.math.Quaternion;
import deamont66.engine.core.math.Vector3f;
import deamont66.engine.rendering.Material;
import deamont66.engine.rendering.Mesh;
import deamont66.engine.rendering.Renderer;
import deamont66.engine.rendering.Texture;
import deamont66.engine.rendering.Window;
import deamont66.game.componets.FreeLook;
import deamont66.game.componets.FreeMove;
import deamont66.game.entities.BoxEntity;
import deamont66.game.entities.LightEntity;
import deamont66.game.entities.MeshEntity;

/**
 *
 * @author JiriSimecek
 */
public class PhysicsTestState extends GameState {

    private Camera camera;

    private DynamicsWorld dynamicsWorld;

    private MeshEntity groundEntity;
    private MeshEntity boxEntity;
    private MeshEntity boxEntity2;
    private MeshEntity vehicleEntity;
    private MeshEntity[] wheelsEntities;

    private RigidBody groundBody;
    private RigidBody boxBody;
    private RigidBody boxBody2;
    private RigidBody vehicleRigidBody;

    private RaycastVehicle vehicle;

    private int cameraMode = 0;
    private float steering = 0f;
    
    public PhysicsTestState(Game game) {
        super(game);
    }

    @Override
    protected void init() {
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

        Material wheelMaterial = new Material();
        wheelMaterial.setTexture("diffuse", new Texture("wheel_1.png"));
        wheelMaterial.setFloat("specularIntensity", 1);
        wheelMaterial.setFloat("specularPower", 8);

        Material carMaterial = new Material();
        carMaterial.setTexture("diffuse", new Texture("car_1.jpg"));
        carMaterial.setFloat("specularIntensity", 1);
        carMaterial.setFloat("specularPower", 8);

        camera = new Camera((float) Math.toRadians(70.0f), (float) Window.getWidth() / (float) Window.getHeight(), 0.01f, 1000.0f);
        addToScene(new Entity().addComponent(new FreeLook(0.5f)).addComponent(new FreeMove(10.0f)).addComponent(camera));
        camera.getTransform().getPos().set(-5, 2, 5);
        camera.getTransform().setRot(new Quaternion(new Vector3f(0, 1, 0), (float) Math.toRadians(130)));

        groundEntity = new BoxEntity(new Vector3f(50f, 0.1f, 50f), oldBricksMaterial, new Vector3f(0, -5f, 0));
        addToScene(groundEntity);

        boxEntity = new BoxEntity(new Vector3f(1f, 1f, 1f), brickMaterial, new Vector3f(0, 2, 0));
        addToScene(boxEntity);

        boxEntity2 = new BoxEntity(new Vector3f(1f, 1f, 1f), brickMaterial, new Vector3f(0, 8, 0.5f));
        addToScene(boxEntity2);

        //        vehicleEntity = new BoxEntity(new Vector3f(1.f, 0.5f, 2.f), carMaterial, new Vector3f(0, 0, 0));       
        vehicleEntity = new MeshEntity(new Mesh("car_1.obj"), carMaterial, new Vector3f(0, 0, 0));
        addToScene(vehicleEntity);
        
        wheelsEntities = new MeshEntity[4];
        for (int i = 0; i < wheelsEntities.length; i++) {
            wheelsEntities[i] = new MeshEntity(new Mesh("wheel_1.obj"), carMaterial);
            addToScene(wheelsEntities[i]);
        }
        
        initPhysics(new Vector3f(0f, -10f, 0f));

        DirectionalLight directionalLight = new DirectionalLight(new Vector3f(0.5f, 0.5f, 0.5f), 0.6f, 5);
        addToScene(new LightEntity(directionalLight, new Vector3f(), new Quaternion(new Vector3f(1, 0, 0), (float) Math.toRadians(-45))));
    }

    @Override
    protected void processInput(float delta) {
        if(Input.getKeyUp(Input.KEY_R)) {
            changeGameState(PhysicsTestState.class);
        }
        
        if (Input.getKeyDown(Input.KEY_C)) {
            cameraMode += 1;
            cameraMode %= 3;
            if(cameraMode == 0) {
                camera.getTransform().getRot().set(0, 0, 0, 1);
            }
        }

        float increment = 0.01f;
        float clamp = 0.5f;
        if (Input.getKey(Input.KEY_LEFT)) {
            steering -= increment;
        } else if (Input.getKey(Input.KEY_RIGHT)) {
            steering += increment;
        } else {
            if (steering > 0) {
                steering -= increment;
            } else if (steering < 0) {
                steering += increment;
            }
        }
        if (steering > clamp) {
            steering = clamp;
        } else if (steering < -clamp) {
            steering = -clamp;
        }

        if (Input.getKey(Input.KEY_UP)) {
            vehicle.applyEngineForce(3000, 0);
            vehicle.applyEngineForce(3000, 1);
        } else if (Input.getKey(Input.KEY_DOWN)) {
            vehicle.applyEngineForce(-3000, 0);
            vehicle.applyEngineForce(-3000, 1);
        } else {
            vehicle.applyEngineForce(0, 0);
            vehicle.applyEngineForce(0, 1);
        }

        if (Input.getKey(Input.KEY_SPACE)) {
            vehicle.setBrake(1000, 2);
            vehicle.setBrake(1000, 3);
        } else {
            vehicle.setBrake(0, 2);
            vehicle.setBrake(0, 3);
        }
    }

    @Override
    protected void update(float delta) {
        com.bulletphysics.linearmath.Transform chassisWorldTransform = vehicle.getChassisWorldTransform(new com.bulletphysics.linearmath.Transform());
        vehicleEntity.updateTransform(chassisWorldTransform, new Vector3f(0, 0.75f, 0));
        for (int i = 0; i < vehicle.getNumWheels(); i++) {
            Quaternion offset = Quaternion.noRotation();
            if (i == 1 || i == 2) {
                offset = new Quaternion(new Vector3f(0, 0, 1), (float) Math.toRadians(180));
            }
            wheelsEntities[i].updateTransform(vehicle.getWheelTransformWS(i, new com.bulletphysics.linearmath.Transform()), new Vector3f(0, 0, 0), offset);
        }

        boxEntity.updateTransform(boxBody.getWorldTransform(new com.bulletphysics.linearmath.Transform()));
        boxEntity2.updateTransform(boxBody2.getWorldTransform(new com.bulletphysics.linearmath.Transform()));

        if (cameraMode == 1 || cameraMode == 2) {
            Transform tr = convertTransform(chassisWorldTransform);
            Quaternion newRot = camera.getTransform().getLookAtRotation(tr.getTransformedPos(),
                    new Vector3f(0, 1, 0));
            if (cameraMode == 2) {
                camera.getTransform().setPos(new Vector3f(chassisWorldTransform.origin).sub(tr.getRot().getForward().mul(10f)).add(new Vector3f(0, 8, 0)));
            }
            camera.getTransform().setRot(camera.getTransform().getRot().nlerp(newRot, delta * 5.0f, true));
        }

        vehicle.setSteeringValue(steering, 0);
        vehicle.setSteeringValue(steering, 1);

        dynamicsWorld.stepSimulation(delta);
    }

    @Override
    protected void render(Renderer renderer) {
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
        CollisionShape groundShape = new BoxShape(new Vector3f(50f, 0.1f, 50f));
//        CollisionShape groundShape = new StaticPlaneShape(new Vector3f(0, 1, 0), 0);
        groundBody = createRigidBody(0, Vector3f.zeros(), groundEntity.getTransform(), groundShape);
        dynamicsWorld.addRigidBody(groundBody);

        CollisionShape boxShape = new BoxShape(new Vector3f(1, 1, 1));
        boxBody = createRigidBody(100f, Vector3f.zeros(), boxEntity.getTransform(), boxShape);
        dynamicsWorld.addRigidBody(boxBody);

        boxBody2 = createRigidBody(100f, Vector3f.zeros(), boxEntity2.getTransform(), boxShape);
        dynamicsWorld.addRigidBody(boxBody2);

        ///////////////////////////////////////////////////////////////////////
        //              Vehicle Setup
        ///////////////////////////////////////////////////////////////////////
        float vehicleMass = 800;
        float wheelRadius = 0.5f;
        float wheelWidth = 0.2f;
        float wheelFriction = 3;//1e30f;
        float suspensionStiffness = 40.f;
        float suspensionDamping = 2.3f;
        float suspensionCompression = 8.4f;
        float rollInfluence = 0.1f;//1.0f;

        float suspensionRestLength = 0.6f;
        VehicleTuning vehicleTuning = new VehicleTuning();
        int CUBE_HALF_EXTENT = 1;
        Vector3f wheelDirectionCS0 = new Vector3f(0, -1, 0);
        Vector3f wheelAxleCS = new Vector3f(-1, 0, 0);

        CollisionShape vehicleChassisShape = new BoxShape(new Vector3f(1.f, 0.5f, 2.f));
        CompoundShape vehicleBody = new CompoundShape();

        Transform localTrans = new Transform();
        localTrans.getPos().set(0, 1, 0);
        vehicleBody.addChildShape(convertTransform(localTrans), vehicleChassisShape);

        localTrans.getPos().set(3, 0, 0);
        MotionState vehicleMotionState = new DefaultMotionState(convertTransform(localTrans));

        Vector3f vehicleInertia = new Vector3f(0, 0, 0);
        vehicleBody.calculateLocalInertia(vehicleMass, vehicleInertia);
        RigidBodyConstructionInfo vehicleRigidBodyCI = new RigidBodyConstructionInfo(vehicleMass, vehicleMotionState, vehicleBody, vehicleInertia);

        vehicleRigidBody = new RigidBody(vehicleRigidBodyCI);
        dynamicsWorld.addRigidBody(vehicleRigidBody);

        CollisionShape wheelShape = new CylinderShapeX(new Vector3f(wheelWidth, wheelRadius, wheelRadius));
        {
            VehicleRaycaster vehicleRayCaster = new DefaultVehicleRaycaster(dynamicsWorld);
            vehicle = new RaycastVehicle(vehicleTuning, vehicleRigidBody, vehicleRayCaster);

            // never deactivate vehicle
            vehicleRigidBody.setActivationState(CollisionObject.DISABLE_DEACTIVATION);
            dynamicsWorld.addVehicle(vehicle);

            float connectionHeight = 1f;
            boolean isFrontWheel = true;

            vehicle.setCoordinateSystem(0, 1, 2); // 0, 1, 2

            // add wheels
            // front left
            Vector3f connectionPointCS0 = new Vector3f(CUBE_HALF_EXTENT - (0.7f * wheelWidth), connectionHeight, 2 * CUBE_HALF_EXTENT - wheelRadius - 0.25f);
            vehicle.addWheel(connectionPointCS0, wheelDirectionCS0, wheelAxleCS, suspensionRestLength, wheelRadius, vehicleTuning, isFrontWheel);
            // front right
            connectionPointCS0 = new Vector3f(-CUBE_HALF_EXTENT + (0.7f * wheelWidth), connectionHeight, 2 * CUBE_HALF_EXTENT - wheelRadius - 0.25f);
            vehicle.addWheel(connectionPointCS0, wheelDirectionCS0, wheelAxleCS, suspensionRestLength, wheelRadius, vehicleTuning, isFrontWheel);
            isFrontWheel = false;
            // rear right
            connectionPointCS0 = new Vector3f(-CUBE_HALF_EXTENT + (0.7f * wheelWidth), connectionHeight, -2 * CUBE_HALF_EXTENT + wheelRadius + 0.25f);
            vehicle.addWheel(connectionPointCS0, wheelDirectionCS0, wheelAxleCS, suspensionRestLength, wheelRadius, vehicleTuning, isFrontWheel);
            // rear left
            connectionPointCS0 = new Vector3f(CUBE_HALF_EXTENT - (0.7f * wheelWidth), connectionHeight, -2 * CUBE_HALF_EXTENT + wheelRadius + 0.25f);
            vehicle.addWheel(connectionPointCS0, wheelDirectionCS0, wheelAxleCS, suspensionRestLength, wheelRadius, vehicleTuning, isFrontWheel);

            for (int i = 0; i < vehicle.getNumWheels(); i++) {
                WheelInfo wheel = vehicle.getWheelInfo(i);
                wheel.suspensionStiffness = suspensionStiffness;
                wheel.wheelsDampingRelaxation = suspensionDamping;
                wheel.wheelsDampingCompression = suspensionCompression;
                wheel.frictionSlip = wheelFriction;
                wheel.rollInfluence = rollInfluence;
            }
        }
    }

    private RigidBody createRigidBody(float mass, Transform t, CollisionShape shape) {
        return createRigidBody(mass, Vector3f.zeros(), t, shape);
    }

    private RigidBody createRigidBody(float mass, Vector3f localInertia, Transform transform, CollisionShape shape) {
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

    private com.bulletphysics.linearmath.Transform convertTransform(Transform t) {
        com.bulletphysics.linearmath.Transform tr = new com.bulletphysics.linearmath.Transform();
        tr.setIdentity();
        tr.origin.set(t.getPos());
        tr.setRotation(t.getRot());
        return tr;
    }

    private Transform convertTransform(com.bulletphysics.linearmath.Transform t) {
        Transform tr = new Transform();
        tr.setPos(new Vector3f(t.origin));
        tr.setRot((Quaternion) t.getRotation(new Quaternion()));
        return tr;
    }
    
}
