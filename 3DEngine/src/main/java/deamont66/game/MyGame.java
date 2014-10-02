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

import de.lessvoid.nifty.controls.Label;
import deamont66.engine.core.Game;
import deamont66.engine.core.Input;
import deamont66.engine.rendering.Renderer;
import deamont66.engine.rendering.Window;
import deamont66.game.states.PhysicsTestState;
import org.lwjgl.LWJGLException;

/**
 *
 * @author JiriSimecek
 */
public class MyGame extends Game {

      @Override
      public void init() {
            Window.setTitle("Physics test");
            initGUI();
            getGui().fromXml("test.xml", "ingame");

            setGameState(PhysicsTestState.class);
      }

      @Override
      public void processInput(float delta) {
            if (Input.getKeyDown(Input.KEY_F11)) {
                  try {
                        Window.setDisplayMode(Window.getWidth(), Window.getHeight(), !Window.isFullscreen());
                  } catch (LWJGLException ex) {
                        System.err.println("Error while changing window state: " + ex.getLocalizedMessage());
                  }
            }
      }

      @Override
      public void update(float delta) {
            Label label = getGui().getCurrentScreen().findNiftyControl("fpsLabel", Label.class);
            label.setText(getEngine().getFps() + " FPS");

      }

      @Override
      protected void render(Renderer renderer) {
      }
}
