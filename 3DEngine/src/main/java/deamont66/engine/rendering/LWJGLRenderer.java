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
import deamont66.engine.core.Entity;
import deamont66.engine.core.Transform;
import deamont66.engine.core.math.Matrix4f;
import deamont66.engine.core.math.Quaternion;
import deamont66.engine.core.math.Vector3f;
import static deamont66.engine.core.GlobalConstants.RENDER_LIGHT_RANGE;
import java.util.ArrayList;
import java.util.HashMap;
import static org.lwjgl.opengl.GL11.GL_BACK;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_CW;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_EQUAL;
import static org.lwjgl.opengl.GL11.GL_FRONT;
import static org.lwjgl.opengl.GL11.GL_LESS;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_VERSION;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glCullFace;
import static org.lwjgl.opengl.GL11.glDepthFunc;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glFrontFace;
import static org.lwjgl.opengl.GL11.glGetString;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL30.GL_RG32F;

/**
 *
 * @author JiriSimecek
 */
public class LWJGLRenderer extends Renderer {
    private static final int NUM_SHADOW_MAPS = 10;
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

    private final Transform m_planeTransform;
    private final Mesh      m_plane                 = new Mesh("plane.obj");
    
    private final Texture   m_tempTarget;
    private final Material  m_planeMaterial;
    private final Texture[] m_shadowMaps            = new Texture[NUM_SHADOW_MAPS];
    private final Texture[] m_shadowMapTempTargets  = new Texture[NUM_SHADOW_MAPS];
    
    private final Shader    m_defaultShader;
    private final Shader    m_shadowMapShader;
    private final Shader    m_nullFilter;
    private final Shader    m_gausBlurFilter;
    private final Shader    m_fxaaFilter; 
    private       Matrix4f  m_lightMatrix;
    
//    private Transform       m_altCameraTransform;
    private final Camera    m_altCamera;
    private Camera          m_mainCamera;
    
    private BaseLight       m_activeLight;
    private final ArrayList<BaseLight>      m_lights        = new ArrayList<>();
    private final HashMap<String, Integer>  m_samplerMap    = new HashMap<>();
    
    public LWJGLRenderer() {
        super();
                    
        setSamplerSlot("diffuse",    0);
        setSamplerSlot("normalMap",  1);
        setSamplerSlot("dispMap",    2);
        setSamplerSlot("shadowMap",  3);
        
        setSamplerSlot("filterTexture", 0);

        setVector3f("ambient", new Vector3f(0.2f, 0.2f, 0.2f));
        if (!Debug.ENABLE_LIGHTS) {
            setVector3f("ambient", new Vector3f(1f, 1f, 1f));
        }
        
        setFloat("fxaaSpanMax", 8.0f);
	setFloat("fxaaReduceMin", 1.0f/128.0f);
	setFloat("fxaaReduceMul", 1.0f/8.0f);
	setFloat("fxaaAspectDistortion", 150.0f);
        
        setTexture("displayTexture", new Texture(Window.getWidth(), Window.getHeight(), null, GL_TEXTURE_2D, GL_LINEAR, GL_RGBA, GL_RGBA, true, GL_COLOR_ATTACHMENT0));
        
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        glFrontFace(GL_CW);
        glCullFace(GL_BACK);
        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);
//        glEnable(GL_DEPTH_CLAMP);
	//glEnable(GL_MULTISAMPLE);
	//glEnable(GL_FRAMEBUFFER_SRGB);

        // glEnable(GL_TEXTURE_2D);
        m_tempTarget = new Texture(Window.getWidth(), Window.getHeight(), null, GL_TEXTURE_2D, GL_NEAREST, GL_RGBA, GL_RGBA, false, GL_COLOR_ATTACHMENT0);
        
        m_planeMaterial = new Material();
        m_planeMaterial.setTexture("diffuse", m_tempTarget);
        m_planeMaterial.setFloat("specularIntensity", 1);
        m_planeMaterial.setFloat("specularPower", 8);
        
        m_defaultShader     = new Shader("forward-ambient");
        m_shadowMapShader   = new Shader("shadowMapGenerator");
	m_nullFilter        = new Shader("filter-null");
	m_gausBlurFilter    = new Shader("filter-gausBlur7x1");
	m_fxaaFilter        = new Shader("filter-fxaa");
        
	m_altCamera = new Camera(new Matrix4f().initIdentity());
        new Entity().addComponent(m_altCamera);
        m_altCamera.getTransform().setRot(new Quaternion(new Vector3f(0,1,0),(float) Math.toRadians(180.0)));

        m_planeTransform = new Transform();
        m_planeTransform.rotate(new Vector3f(1, 0, 0), (float) Math.toRadians(90f));
        m_planeTransform.rotate(new Vector3f(0, 0, 1), (float) Math.toRadians(180f));
        m_planeTransform.setScale(1.0f);
        
        for(int i = 0; i < NUM_SHADOW_MAPS; i++)
	{
		int shadowMapSize = 1 << (i + 1);
		m_shadowMaps[i] = new Texture(shadowMapSize, shadowMapSize, null, GL_TEXTURE_2D, GL_LINEAR, GL_RG32F, GL_RGBA, true, GL_COLOR_ATTACHMENT0);
		m_shadowMapTempTargets[i] = new Texture(shadowMapSize, shadowMapSize, null, GL_TEXTURE_2D, GL_LINEAR, GL_RG32F, GL_RGBA, true, GL_COLOR_ATTACHMENT0);
	}
	
	m_lightMatrix = new Matrix4f().initScale(0,0,0);
    }

    @Override
    public void updateUniformStruct(Transform transform, Material material, Shader shader, String uniformName, String uniformType) {
        throw new IllegalArgumentException(uniformType + " is not a supported type in RenderingEngine");
    }

    private void blurShadowMap(int shadowMapIndex, float blurAmount) {
        setVector3f("blurScale", new Vector3f(blurAmount/(m_shadowMaps[shadowMapIndex].getWidth()), 0.0f, 0.0f));
	applyFilter(m_gausBlurFilter, m_shadowMaps[shadowMapIndex], m_shadowMapTempTargets[shadowMapIndex]);
	
	setVector3f("blurScale", new Vector3f(0.0f, blurAmount/(m_shadowMaps[shadowMapIndex].getHeight()), 0.0f));
	applyFilter(m_gausBlurFilter, m_shadowMapTempTargets[shadowMapIndex], m_shadowMaps[shadowMapIndex]); 
    }

    private void applyFilter(Shader filter, Texture source, Texture dest) {
        if(source == dest) {
            System.err.println(getClass().getSimpleName() + "->applyFilter(): dest texture cannot equal to source");
            System.exit(0);
        }
        if (dest == null) {
            Window.bindAsRenderTarget();
        } else {
            dest.bindAsRenderTarget();
        }

        setTexture("filterTexture", source);

        m_altCamera.setProjection(new Matrix4f().initIdentity());
        m_altCamera.getTransform().setPos(new Vector3f(0, 0, 0));
        m_altCamera.getTransform().setRot(new Quaternion(new Vector3f(0, 1, 0), (float) Math.toRadians(180.0f)));

//        Camera temp = mainCamera;
//        mainCamera = altCamera;

        glClear(GL_DEPTH_BUFFER_BIT);
        filter.bind();
        filter.updateUniforms(m_planeTransform, m_planeMaterial, this, m_altCamera);
        m_plane.draw();

//        mainCamera = temp;
        setTexture("filterTexture", null);
    }

    @Override
    public void render(Entity object) {

//        getTexture("displayTexture").bindAsRenderTarget();
        Window.bindAsRenderTarget();
//		tempTarget.bindAsRenderTarget();

        glClearColor(0, 0, 0, 0);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        object.renderAll(m_defaultShader, this, m_mainCamera);

        if (Debug.ENABLE_LIGHTS) {

            for (BaseLight light : m_lights) {
                if (light.isActive()) {
                    m_activeLight = light;
                                        
                    if (light instanceof PointLight) {
                        Vector3f lightDirection = m_mainCamera.getTransform().getTransformedPos().sub(light.getTransform().getTransformedPos());
                        if (lightDirection.length() > RENDER_LIGHT_RANGE) {
                            continue;
                        }
                    }
                    
                    ShadowInfo shadowInfo = light.getShadowInfo();
                    
                    int shadowMapIndex = 0;
                    if(shadowInfo.getShadowMapSizeAsPowerOf2() != 0)
                            shadowMapIndex = shadowInfo.getShadowMapSizeAsPowerOf2() - 1;

                    if(shadowMapIndex < 0 || shadowMapIndex >= NUM_SHADOW_MAPS) {
                        throw new RuntimeException("Shadow map too large: " + (int) Math.pow(2, shadowInfo.getShadowMapSizeAsPowerOf2()));
                    }

                    setTexture("shadowMap", m_shadowMaps[shadowMapIndex]);
                    m_shadowMaps[shadowMapIndex].bindAsRenderTarget();
                    glClearColor(1.0f,1.0f,0.0f,0.0f);
                    glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);

                    if(shadowInfo.getShadowMapSizeAsPowerOf2() != 0) {
                        m_altCamera.setProjection(shadowInfo.getProjection());
                        m_altCamera.getTransform().setPos(m_activeLight.getTransform().getTransformedPos());
                        m_altCamera.getTransform().setRot(m_activeLight.getTransform().getTransformedRot());

                        m_lightMatrix = biasMatrix.mul(m_altCamera.getViewProjection());
                        setFloat("shadowVarianceMin", shadowInfo.getMinVariance());
                        setFloat("shadowLightBleedingReduction", shadowInfo.getLightBleedReductionAmount());

                        Camera temp = m_mainCamera;
                        m_mainCamera = m_altCamera;
                        if (shadowInfo.getFlipfaces()) {
                            glCullFace(GL_FRONT);
                        }
                        object.renderAll(m_shadowMapShader, this, m_altCamera);
                        if (shadowInfo.getFlipfaces()) {
                            glCullFace(GL_BACK);
                        }

                        m_mainCamera = temp;

                        float shadowSoftness = shadowInfo.getShadowSoftness();
			if(shadowSoftness != 0)
			{
                            blurShadowMap(shadowMapIndex, shadowSoftness);
			}
                    } else {
                        m_lightMatrix = new Matrix4f().initScale(0,0,0);
                        setFloat("shadowVarianceMin", 0.00002f);
                        setFloat("shadowLightBleedingReduction", 0.0f);
                    }   

//                    getTexture("displayTexture").bindAsRenderTarget();
                    Window.bindAsRenderTarget();

                    glEnable(GL_BLEND);
                    glBlendFunc(GL_ONE, GL_ONE);
                    glDepthMask(false);
                    glDepthFunc(GL_EQUAL);

                    object.renderAll(light.getShader(), this, m_mainCamera);

                    glDepthMask(true);
                    glDepthFunc(GL_LESS);
                    glDisable(GL_BLEND);
                }
            }
        }
        
//        float displayTextureAspect = (float)getTexture("displayTexture").getWidth()/(float)getTexture("displayTexture").getHeight();
//	float displayTextureHeightAdditive = displayTextureAspect * getFloat("fxaaAspectDistortion");
//	setVector3f("inverseFilterTextureSize", new Vector3f(1.0f/(float)getTexture("displayTexture").getWidth(), 
//	                                                 1.0f/((float)getTexture("displayTexture").getHeight() + displayTextureHeightAdditive), 0.0f));
//	
//	applyFilter(m_fxaaFilter, getTexture("displayTexture"), null);

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
    public String getRendererVersion() {
        return glGetString(GL_VERSION);
    }

    @Override
    public void addLight(BaseLight light) {
        m_lights.add(light);
    }

    @Override
    public int getSamplerSlot(String samplerName) {
        return m_samplerMap.get(samplerName);
    }
    
    private void setSamplerSlot(String textureName, int slot) {
        m_samplerMap.put(textureName, slot);
    }

    @Override
    public BaseLight getActiveLight() {
        return m_activeLight;
    }

    @Override
    public Camera getMainCamera() {
        return m_mainCamera;
    }

    @Override
    public void setMainCamera(Camera mainCamera) {
        this.m_mainCamera = mainCamera;
    }

    @Override
    public Matrix4f getLightMatrix() {
        return m_lightMatrix;
    }
}
