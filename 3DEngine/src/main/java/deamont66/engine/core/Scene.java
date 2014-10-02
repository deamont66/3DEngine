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
import deamont66.engine.rendering.Renderer;
import deamont66.engine.rendering.Shader;
import java.util.ArrayList;

/**
 *
 * @author JiriSimecek
 */
public class Scene {

        private final ArrayList<Entity> children;
        private CoreEngine engine;

        private Entity camera;

        public Scene() {
                this.children = new ArrayList<>();
                engine = null;
        }

        public void processInputAll(float delta) {
                processInput(delta);

                this.camera.processInput(delta);
                for (Entity child : children) {
                        child.processInputAll(delta);
                }
        }

        public void updateAll(float delta) {
                update(delta);

                this.camera.update(delta);
                for (Entity child : children) {
                        child.updateAll(delta);
                }
        }

        public void renderAll(Shader shader, Renderer renderer, Camera camera) {
                render(shader, renderer, camera);

                this.camera.render(shader, renderer, camera);
                for (Entity child : children) {
                        child.renderAll(shader, renderer, camera);
                }
        }

        public void addChild(Entity child) {
                children.add(child);
                child.setEngine(engine);
        }

        public ArrayList<Entity> getAllAttached() {
                ArrayList<Entity> result = new ArrayList<>();

                for (Entity child : children) {
                        result.addAll(child.getAllAttached());
                }

                return result;
        }

        public void setEngine(CoreEngine engine) {
                if (this.engine != engine) {
                        this.engine = engine;

                        if (this.camera != null) {
                                this.camera.setEngine(engine);
                        }
                        for (Entity child : children) {
                                child.setEngine(engine);
                        }
                }
        }

        public void setCamera(Entity camera) {
                this.camera = camera;
                camera.setEngine(engine);
        }

        public Entity getCamera() {
                return camera;
        }

        public void processInput(float delta) {
        }

        public void update(float delta) {
        }

        public void render(Shader shader, Renderer renderer, Camera camera) {
        }
}
