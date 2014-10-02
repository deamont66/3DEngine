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

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.nulldevice.NullSoundDevice;
import de.lessvoid.nifty.renderer.lwjgl.input.LwjglInputSystem;
import de.lessvoid.nifty.renderer.lwjgl.render.LwjglRenderDevice;
import de.lessvoid.nifty.spi.time.impl.FastTimeProvider;
import deamont66.engine.rendering.Renderer;
import deamont66.engine.rendering.Window;

public abstract class Game {

        private Scene scene;
        private Nifty gui;
        private LwjglInputSystem inputSystem;
        private CoreEngine engine;
        private GameState currentState;

        /**
         * Change current gameState to given gameState class.
         * @param state new state
         */
        public void setGameState(final Class<? extends GameState> state) {
                try {
                        GameState newState = state.getConstructor(Game.class).newInstance(Game.this);
                        if (currentState != null) {
                                scene = getSceneObject(true);
                                engine.getRenderingEngine().clearLights();
                                currentState.cleanUp();
                        }
                        currentState = newState;
                        try {
                                currentState.init();
                        } catch (Exception e) {
                                throw e;
                        }
                } catch (Exception ex) {
                        throw new RuntimeException("Cannot change GameState to " + state.getName(), ex);
                }
        }

        /**
         * Inits Nifty GUI classes and loads default styles for it.
         * @see #getGui() 
         */
        protected void initGUI() {
                inputSystem = new LwjglInputSystem();
                try {
                        inputSystem.startup();
                } catch (Exception e) {
                        throw new RuntimeException(e);
                }
                gui = new Nifty(
                        new LwjglRenderDevice(),
                        new NullSoundDevice(),
                        inputSystem,
                        new FastTimeProvider());
                // load default styles
                gui.loadStyleFile("nifty-default-styles.xml");
                // load standard controls
                gui.loadControlFile("nifty-default-controls.xml");
        }

        protected void processInputAll(float delta) {
                getSceneObject().processInputAll(delta);
                processInput(delta);
                if (currentState != null) {
                        currentState.processInput(delta);
                }
        }

        protected void updateAll(float delta) {
                getSceneObject().updateAll(delta);
                if (gui != null) {
                        if (gui.update()) {
                                // nothing yet
                        }
                }
                update(delta);
                if (currentState != null) {
                        currentState.update(delta);
                }
        }

        protected void renderAll(Renderer renderer) {
                renderer.render(getSceneObject());
                if (gui != null) {
                        renderer.to2D(Window.getWidth(), Window.getHeight());
                        try {
                                gui.render(false);
                        } catch (Exception e) {
                        }
                        renderer.backTo3D();
                }
                render(renderer);
                if (currentState != null) {
                        currentState.render(renderer);
                }
        }

        public void cleanUp() {
                inputSystem.shutdown();
                if (currentState != null) {
                        currentState.cleanUp();
                }
        }

        protected abstract void init();

        protected abstract void processInput(float delta);

        protected abstract void render(Renderer renderer);

        protected abstract void update(float delta);

        protected void addToScene(Entity object) {
                getSceneObject().addChild(object);
        }

        /**
         * Sets {@link CoreEngine} for root entity.
         *
         * @param engine
         */
        public void setEngine(CoreEngine engine) {
                this.engine = engine;
                getSceneObject().setEngine(engine);
        }

        public Nifty getGui() {
                return gui;
        }

        public CoreEngine getEngine() {
                return engine;
        }

        private Scene getSceneObject() {
                return getSceneObject(scene == null);
        }

        private Scene getSceneObject(boolean reset) {
                if (reset) {
                        scene = new Scene();
                        scene.setEngine(engine);
                }
                return scene;
        }

        protected void setCamera(Entity camera) {
                scene.setCamera(camera);
        }
}
