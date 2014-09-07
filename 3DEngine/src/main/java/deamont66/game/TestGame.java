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

import deamont66.game.entities.MeshEntity;
import deamont66.game.entities.LightEntity;
import deamont66.engine.components.*;
import deamont66.engine.core.*;
import deamont66.engine.core.math.Quaternion;
import deamont66.engine.core.math.Vector3f;
import deamont66.engine.rendering.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.LWJGLException;

public class TestGame extends Game {

	private LightEntity flashLightObject;
	private Renderer renderingEngine;

	@Override
	public void init() {
                Window.setTitle("3D Engine Test");
//  --------    Materials:    -----------------------------------------------------    
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

//  --------    GameObjects:    -----------------------------------------------------    
		Camera camera = new Camera((float) Math.toRadians(70.0f), (float) Window.getWidth() / (float) Window.getHeight(), 0.01f, 1000.0f);
		addToScene(new Entity().addComponent(new FreeLook(0.5f)).addComponent(new FreeMove(10.0f)).addComponent(camera));
		camera.getTransform().getPos().set(-10, 0, 10);
		camera.getTransform().setRot(new Quaternion(new Vector3f(0, 1, 0), (float) Math.toRadians(130)));

		addToScene(new MeshEntity(new Mesh("plane4.obj"), oldBricksMaterial, new Vector3f(0, -1, 0), new Quaternion(), new Vector3f(2.5f, 2.5f, 2.5f)));

		Quaternion wall4Rot = new Quaternion(new Vector3f(1, 0, 0), (float) Math.toRadians(90));
		wall4Rot.mul(new Quaternion(new Vector3f(0, 1, 0), (float) Math.toRadians(90)));
		addToScene(new MeshEntity(new Mesh("plane4.obj"), brickMaterial, new Vector3f(0, 8f, -8f), wall4Rot));

		addToScene(new MeshEntity(new Mesh("plant.obj"), plantMaterial, new Vector3f(0, -1, 5)));

//		GameObject[] angryMonkeys = new GameObject[5];
//		for (int i = 0; i < angryMonkeys.length; i++) {
//			angryMonkeys[i] = new MeshObject(new Mesh("monkey3.obj"), oldBricksMaterial, new Vector3f(6, 3, 6 - i * 3)).addComponent(new LookAtComponent(camera));
//			addToScene(angryMonkeys[i]);
//		}
                
                addToScene(new MeshEntity(new Mesh("monkey3.obj"), oldBricksMaterial, new Vector3f(6, 3, 6 - 0 * 3)).addComponent(new LookAtComponent(camera)));

		addToScene(new MeshEntity(new Mesh("plane.obj"), oldBricksMaterial, new Vector3f(-5, 2, 0)));

		addToScene(new MeshEntity(new Mesh("cube.obj"), brickMaterial, new Vector3f(3, 0, 0), new Quaternion(new Vector3f(0, 1, 0), (float) Math.toRadians(30))));

//  --------    LIGHTING:    -----------------------------------------------------    
		DirectionalLight directionalLight = new DirectionalLight(new Vector3f(0.5f, 0.5f, 0.5f), 0.6f, 5);
		addToScene(new LightEntity(directionalLight, new Vector3f(), new Quaternion(new Vector3f(1, 0, 0), (float) Math.toRadians(-45))));

		PointLight pointLight = new PointLight(new Vector3f(0, 1, 0), 0.4f,
			new Attenuation(0, 0, 1));
		addToScene(new LightEntity(pointLight));

		SpotLight spotLight = new SpotLight(new Vector3f(0f, 1f, 1f), 3f,
			new Attenuation(0, 0, 0.1f), 0.9f);
		addToScene(new LightEntity(spotLight, new Vector3f(-3, 0, 5), new Quaternion(new Vector3f(0, 1, 0), (float) Math.toRadians(90))));

		SpotLight flashLight = new SpotLight(new Vector3f(255 / 255f, 201 / 255f, 103 / 255f), 2f,
			new Attenuation(0, 0, 0.1f), 0.9f);
		flashLightObject = new LightEntity(flashLight, new Vector3f(5, 0, 5), new Quaternion(new Vector3f(0, 1, 0), (float) Math.toRadians(90)));
		flashLightObject.setActive(false);
		addToScene(flashLightObject);
	}

	@Override
	public void update(float delta) {
		super.update(delta);
		if (renderingEngine != null && flashLightObject.isActive()) {
			flashLightObject.getTransform().setPos(renderingEngine.getMainCamera().getTransform().getTransformedPos());
			flashLightObject.getTransform().setRot(renderingEngine.getMainCamera().getTransform().getRot());
		}
	}

	@Override
	public void processInput(float delta) {
		super.processInput(delta);
		if (Input.getKeyDown(Input.KEY_F)) {
			flashLightObject.setActive(!flashLightObject.isActive());
		}
		if (Input.getKeyDown(Input.KEY_F11)) {
			try {
				Window.setDisplayMode(Window.getWidth(), Window.getHeight(), !Window.isFullscreen());
			} catch (LWJGLException ex) {
				Logger.getLogger(TestGame.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	@Override
	public void render(Renderer renderingEngine) {
		super.render(renderingEngine);
		this.renderingEngine = renderingEngine;
	}

}
