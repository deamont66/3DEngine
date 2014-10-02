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

import deamont66.engine.rendering.Renderer;
import deamont66.engine.rendering.Window;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CoreEngine {

        /**
         * Tells engine to render scene only if atleast one update processed for last frame.
         * Caps framerate to updaterate.
         */
        private static final boolean RENDER_ONLY_WHILE_UPDATED = false;
        
        private boolean isRunning;

        private Game game;
        private Class<? extends Game> gameClass;

        private Class<? extends Renderer> rendererClass;
        private Renderer renderer;

        private final int width;
        private final int height;
        private final double updateTime;        // update time in sec
        private final double fps_cap;           // time to render in sec
        private final boolean vsync;

        private int fps;                        // current fps value, default is framecap value

        private final DebugTimer renderTimer = new DebugTimer("Render");
        private final DebugTimer windowSyncTimer = new DebugTimer("Sync");
        private final DebugTimer updateTimer = new DebugTimer("Update");
        private final DebugTimer allTimer = new DebugTimer("Loop");

        public CoreEngine() {
                this(640, 480, 60, 120, false);
        }

        public CoreEngine(int width, int height, double updatesPerSecond, double frame_cap, boolean vsync) {
                this.isRunning = false;
                this.width = width;
                this.height = height;
                this.fps = (int) frame_cap;
                this.fps_cap = 1.0 / frame_cap;
                this.updateTime = 1.0 / updatesPerSecond;
                this.vsync = vsync;
        }

        public void createWindow() {
                createWindow("");
        }

        public void createWindow(String title) {
                if (gameClass == null || rendererClass == null) {
                        throw new IllegalStateException("Game or Renderer not set.");
                }
                Window.createWindow(width, height, false, title);
                Window.setVSyncEnabled(vsync);
                try {
                        game = gameClass.newInstance();
                        game.setEngine(this);
                        renderer = rendererClass.newInstance();
                } catch (InstantiationException | IllegalAccessException ex) {
                        Logger.getLogger(CoreEngine.class.getName()).log(Level.SEVERE, null, ex);
                        System.exit(1);
                }
                if (Debug.DEBUG_ECHO) {
                        System.out.println("Renderer version: " + renderer.getRendererVersion());
                }
        }

        public void start() {
                if (isRunning) {
                        return;
                }

                run();
        }

        public void stop() {
                if (!isRunning) {
                        return;
                }

                isRunning = false;
        }

        private void run() {
                isRunning = true;

                int frames = 0;
                double frameCounter = 0;

                game.init();

                double lastTime = Time.getTime();
                double unprocessedTime = 0;
                double renderTime = 0;

                while (isRunning) {
                        boolean render = !RENDER_ONLY_WHILE_UPDATED;

                        double startTime = Time.getTime();
                        double passedTime = startTime - lastTime;
                        lastTime = startTime;

                        unprocessedTime += passedTime;
                        renderTime += passedTime;
                        frameCounter += passedTime;

                        while (unprocessedTime > updateTime) {
                                render = true;

                                unprocessedTime -= updateTime;

                                if (Window.isCloseRequested()) {
                                        stop();
                                }

                                updateTimer.start();
                                game.processInputAll((float) updateTime);
                                game.updateAll((float) updateTime);
                                Input.update();
                                updateTimer.stop();

                                if (frameCounter >= 1.0) {
                                        fps = frames;
                                        frames = 0;
                                        frameCounter = 0;
                                        System.out.println("FPS: " + fps + "");
                                }
                        }

                        // game was updated so we want to re-render our scene
                        if (render) {
                                if (renderTime > fps_cap) {
                                        renderTimer.start();
                                        game.renderAll(renderer);
                                        windowSyncTimer.start();
                                        Window.render();
                                        windowSyncTimer.stop();
                                        renderTimer.stop();
                                        frames++;

                                        renderTime -= fps_cap;
                                }
                        } else {
                                try {
                                        Thread.sleep(1);
                                } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                }
                        }
                }

                cleanUp();
        }

        private void cleanUp() {
                game.cleanUp();
                Window.dispose();
        }

        public Renderer getRenderingEngine() {
                return renderer;
        }

        public int getFps() {
                return fps;
        }

        public void setGame(Class<? extends Game> aClass) {
                gameClass = aClass;
        }

        public void setRenderer(Class<? extends Renderer> aClass) {
                rendererClass = aClass;
        }
}
