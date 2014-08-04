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

package deamont66.engine.core;

import deamont66.engine.components.GameComponent;
import deamont66.engine.rendering.RenderingEngine;
import deamont66.engine.rendering.Shader;

import java.util.ArrayList;

public class GameObject
{
	private final ArrayList<GameObject> children;
	private final ArrayList<GameComponent> components;
	private final Transform transform;
	private CoreEngine engine;

	public GameObject()
	{
		children = new ArrayList<>();
		components = new ArrayList<>();
		transform = new Transform();
		engine = null;
	}

	public void addChild(GameObject child)
	{
		children.add(child);
		child.setEngine(engine);
		child.getTransform().setParent(transform);
	}

	public GameObject addComponent(GameComponent component)
	{
		components.add(component);
		component.setParent(this);

		return this;
	}

	public void inputAll(float delta)
	{
		input(delta);

		for(GameObject child : children)
			child.inputAll(delta);
	}

	public void updateAll(float delta)
	{
		update(delta);

		for(GameObject child : children)
			child.updateAll(delta);
	}

	public void renderAll(Shader shader, RenderingEngine renderingEngine)
	{
		render(shader, renderingEngine);

		for(GameObject child : children)
			child.renderAll(shader, renderingEngine);
	}

	public void input(float delta)
	{
		transform.update();

		for(GameComponent component : components)
			component.input(delta);
	}

	public void update(float delta)
	{
		for(GameComponent component : components)
			component.update(delta);
	}

	public void render(Shader shader, RenderingEngine renderingEngine)
	{
		for(GameComponent component : components)
			component.render(shader, renderingEngine);
	}

	public ArrayList<GameObject> getAllAttached()
	{
		ArrayList<GameObject> result = new ArrayList<>();

		for(GameObject child : children)
			result.addAll(child.getAllAttached());

		result.add(this);
		return result;
	}

	public Transform getTransform()
	{
		return transform;
	}

	public void setEngine(CoreEngine engine)
	{
		if(this.engine != engine)
		{
			this.engine = engine;

			for(GameComponent component : components)
				component.addToEngine(engine);

			for(GameObject child : children)
				child.setEngine(engine);
		}
	}
}
