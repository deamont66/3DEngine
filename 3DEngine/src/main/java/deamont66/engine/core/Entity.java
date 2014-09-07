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

import deamont66.engine.components.Camera;
import deamont66.engine.components.EntityComponent;
import deamont66.engine.rendering.Renderer;
import deamont66.engine.rendering.Shader;
import java.util.ArrayList;

public class Entity {

    private final ArrayList<Entity> children;
    private final ArrayList<EntityComponent> components;
    private final Transform transform;
    private CoreEngine engine;

    public Entity() {
        children = new ArrayList<>();
        components = new ArrayList<>();
        transform = new Transform();
        engine = null;
    }

    public void addChild(Entity child) {
        children.add(child);
        child.setEngine(engine);
        child.getTransform().setParent(transform);
    }

    public Entity addComponent(EntityComponent component) {
        components.add(component);
        component.setParent(this);
        return this;
    }

    public void processInputAll(float delta) {
        processInput(delta);

        for (Entity child : children) {
            child.processInputAll(delta);
        }
    }

    public void updateAll(float delta) {
        update(delta);

        for (Entity child : children) {
            child.updateAll(delta);
        }
    }

    public void renderAll(Shader shader, Renderer renderer, Camera camera) {
        render(shader, renderer, camera);

        for (Entity child : children) {
            child.renderAll(shader, renderer, camera);
        }
    }

    public void processInput(float delta) {
        transform.update();

        for (EntityComponent component : components) {
            component.processInput(delta);
        }
    }

    public void update(float delta) {
        for (EntityComponent component : components) {
            component.update(delta);
        }
    }

    public void render(Shader shader, Renderer renderer, Camera camera) {
        for (EntityComponent component : components) {
            component.render(shader, renderer, camera);
        }
    }

    public ArrayList<Entity> getAllAttached() {
        ArrayList<Entity> result = new ArrayList<>();

        for (Entity child : children) {
            result.addAll(child.getAllAttached());
        }

        result.add(this);
        return result;
    }

    public Transform getTransform() {
        return transform;
    }

    public void setEngine(CoreEngine engine) {
        if (this.engine != engine) {
            this.engine = engine;

            for (EntityComponent component : components) {
                component.addToEngine(engine);
            }

            for (Entity child : children) {
                child.setEngine(engine);
            }
        }
    }
}
