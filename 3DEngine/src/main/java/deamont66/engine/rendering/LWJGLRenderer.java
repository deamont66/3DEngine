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
import deamont66.engine.components.PointLight;
import deamont66.engine.core.Debug;
import deamont66.engine.core.GameObject;
import deamont66.engine.core.Transform;
import deamont66.engine.core.math.Matrix4f;
import deamont66.engine.core.math.Vector3f;
import static deamont66.engine.core.GlobalConstants.RENDER_LIGHT_RANGE;
import java.util.ArrayList;
import java.util.HashMap;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_VERSION;
import static org.lwjgl.opengl.GL14.GL_DEPTH_COMPONENT16;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.GL_DEPTH_CLAMP;

public class LWJGLRenderer extends Renderer {

    private static final Matrix4f biasMatrix = new Matrix4f().initScale(0.5f, 0.5f, 0.5f).mul(new Matrix4f().initTranslation(1.0f, 1.0f, 1.0f));
    //Should construct a Matrix like this:
    //     x   y   z   w
    //x [ 0.5 0.0 0.0 0.5 ]
    //y [ 0.0 0.5 0.0 0.5 ]
    //z [ 0.0 0.0 0.5 0.5 ]
    //w [ 0.0 0.0 0.0 1.0 ]
    //
    //Note the 'w' column in this representation should be
    //the translation column!
    //
    //This matrix will convert 3D coordinates from the range (-1, 1) to the range (0, 1).

    private final HashMap<String, Integer> samplerMap;
    private final ArrayList<BaseLight> lights;
    private BaseLight activeLight;

    private final Shader forwardAmbientShader;
    private final Shader shadownMapShader;
//    private final Shader nullFilter;
//    private final Shader gausBlurFilter;

    private Matrix4f lightMatrix;
    private Camera mainCamera;

    private final Camera altCamera;
    private final GameObject altCameraObject;

    private final Mesh plane;
    private final Transform planeTransform;
    private final Material planeMaterial;

    private final Texture tempTarget;

    public LWJGLRenderer() {
        super();
        lights = new ArrayList<>();
        samplerMap = new HashMap<>();
        samplerMap.put("diffuse", 0);
        samplerMap.put("normalMap", 1);
        samplerMap.put("dispMap", 2);
        samplerMap.put("shadowMap", 3);
//        samplerMap.put("filterTexture", 4);

        setVector3f("ambient", new Vector3f(0.2f, 0.2f, 0.2f));
        if (!Debug.ENABLE_LIGHTS) {
            setVector3f("ambient", new Vector3f(1f, 1f, 1f));
        }
        setTexture("shadowMap", new Texture(1024, 1024, null, GL_TEXTURE_2D, GL_NEAREST, GL_DEPTH_COMPONENT16, GL_DEPTH_COMPONENT, true, GL_DEPTH_ATTACHMENT));
//        setTexture("shadowMapTempTarget", new Texture(1024, 1024, null, GL_TEXTURE_2D, GL_LINEAR, GL_RG32F, GL_RGBA, true, GL_COLOR_ATTACHMENT0));
        forwardAmbientShader = new Shader("forward-ambient");
        shadownMapShader = new Shader("shadowMapGenerator");
//        nullFilter = new Shader("filter-null");
//        gausBlurFilter = new Shader("filter-gausBlur7x1");

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        glFrontFace(GL_CW);
        glCullFace(GL_BACK);
        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);

        glEnable(GL_DEPTH_CLAMP);

        glEnable(GL_TEXTURE_2D);

        altCamera = new Camera(new Matrix4f().initIdentity());
        altCameraObject = new GameObject().addComponent(altCamera);
        altCamera.getParentTransform().rotate(new Vector3f(0, 1, 0), (float) Math.toRadians(180.0f));

        tempTarget = new Texture(Window.getWidth(), Window.getHeight(), null, GL_TEXTURE_2D, GL_NEAREST, GL_RGBA, GL_RGBA, false, GL_COLOR_ATTACHMENT0);

        planeMaterial = new Material();
        planeMaterial.setTexture("diffuse", tempTarget);
        planeMaterial.setFloat("specularIntensity", 1);
        planeMaterial.setFloat("specularPower", 8);

        plane = new Mesh("plane.obj");
        planeTransform = new Transform();
        planeTransform.rotate(new Vector3f(1, 0, 0), (float) Math.toRadians(90f));
        planeTransform.rotate(new Vector3f(0, 0, 1), (float) Math.toRadians(180f));
        planeTransform.setScale(1.0f);
    }

    @Override
    public void updateUniformStruct(Transform transform, Material material, Shader shader, String uniformName, String uniformType) {
        throw new IllegalArgumentException(uniformType + " is not a supported type in RenderingEngine");
    }

//    private void blurShadowMap(Texture shadowMap, float blurAmount) {
//        setVector3f("blurScale", new Vector3f(1.0f / (shadowMap.getWidth() * blurAmount), 0.0f, 0.0f));
//        applyFilter(gausBlurFilter, shadowMap, getTexture("shadowMapTempTarget"));
//
//        setVector3f("blurScale", new Vector3f(0.0f, 1.0f / (shadowMap.getHeight() * blurAmount), 0.0f));
//        applyFilter(gausBlurFilter, getTexture("shadowMapTempTarget"), shadowMap);
//    }
//
//    private void applyFilter(Shader filter, Texture source, Texture dest) {
//        assert (source != dest);
//        if (dest == null) {
//            Window.bindAsRenderTarget();
//        } else {
//            dest.bindAsRenderTarget();
//        }
//
//        setTexture("filterTexture", source);
//
//        altCamera.setProjection(new Matrix4f().initIdentity());
//        altCamera.getTransform().setPos(new Vector3f(0, 0, 0));
//        altCamera.getTransform().setRot(new Quaternion(new Vector3f(0, 1, 0), (float) Math.toRadians(180.0f)));
//
//        Camera temp = mainCamera;
//        mainCamera = altCamera;
//
//        glClear(GL_DEPTH_BUFFER_BIT);
//        filter.bind();
//        filter.updateUniforms(planeTransform, planeMaterial, this);
//        plane.draw();
//
//        mainCamera = temp;
//        setTexture("filterTexture", null);
//    }

    @Override
    public void render(GameObject object) {
        Window.bindAsRenderTarget();
//		tempTarget.bindAsRenderTarget();

        glClearColor(0, 0, 0, 1);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        object.renderAll(forwardAmbientShader, this);

        if (Debug.ENABLE_LIGHTS) {

            for (BaseLight light : lights) {
                if (light.isActive()) {
                    activeLight = light;
                    if (light instanceof PointLight) {
                        Vector3f lightDirection = mainCamera.getParentTransform().getTransformedPos().sub(light.getParentTransform().getTransformedPos());
                        if (lightDirection.length() > RENDER_LIGHT_RANGE) {
                            continue;
                        }
                    }
                    getTexture("shadowMap").bindAsRenderTarget();
                    glClear(GL_DEPTH_BUFFER_BIT);

                    ShadowInfo shadowInfo = light.getShadowInfo();

                    if (shadowInfo != null && Debug.ENABLE_SHADOWS) {
                        altCamera.setProjection(shadowInfo.getProjection());
                        altCamera.getParentTransform().setPos(activeLight.getParentTransform().getTransformedPos());
                        altCamera.getParentTransform().setRot(activeLight.getParentTransform().getTransformedRot());

                        lightMatrix = biasMatrix.mul(altCamera.getViewProjection());
                        setVector3f("shadowTexelSize", new Vector3f(1.0f/1024.0f, 1.0f/1024.0f, 0.0f));
 			setFloat("shadowBias", shadowInfo.getBias()/1024.0f);
                        //setFloat("shadowVarianceMin", shadowInfo.getMinVariance());
                        //setFloat("shadowLightBleedingReduction", shadowInfo.getLightBleedReductionAmount());

                        Camera temp = mainCamera;
                        mainCamera = altCamera;
                        if (shadowInfo.getFlipfaces()) {
                            glCullFace(GL_FRONT);
                        }
                        object.renderAll(shadownMapShader, this);
                        if (shadowInfo.getFlipfaces()) {
                            glCullFace(GL_BACK);
                        }

                        mainCamera = temp;

//                        blurShadowMap(getTexture("shadowMap"), shadowInfo.getShadowSoftness());
                    }

                    Window.bindAsRenderTarget();

                    glEnable(GL_BLEND);
                    glBlendFunc(GL_ONE, GL_ONE);
                    glDepthMask(false);
                    glDepthFunc(GL_EQUAL);

                    activeLight = light;
                    object.renderAll(light.getShader(), this);

                    glDepthFunc(GL_LESS);
                    glDepthMask(true);
                    glDisable(GL_BLEND);
                }
            }

        }

//		temp render to texture
//		Window.bindAsRenderTarget();
//
//		Camera temp = mainCamera;
//		mainCamera = altCamera;
//		glClearColor(0, 0, 1f, 1);
//		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
//		forwardAmbient.bind();
//		forwardAmbient.updateUniforms(planeTransform, planeMaterial, this);
//		plane.draw();
//
//		mainCamera = temp;
    }

    @Override
    public String getRenderVersion() {
        return glGetString(GL_VERSION);
    }

    @Override
    public void addLight(BaseLight light) {
        lights.add(light);
    }

    @Override
    public int getSamplerSlot(String samplerName) {
        return samplerMap.get(samplerName);
    }

    @Override
    public BaseLight getActiveLight() {
        return activeLight;
    }

    @Override
    public Camera getMainCamera() {
        return mainCamera;
    }

    @Override
    public void setMainCamera(Camera mainCamera) {
        this.mainCamera = mainCamera;
    }

    @Override
    public Matrix4f getLightMatrix() {
        return lightMatrix;
    }

}
