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

import deamont66.engine.core.math.Vector2f;
import deamont66.engine.core.NativesLoader;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public class Window {

    private static boolean fullscreen;
    private static int width;
    private static int height;

    public static void createWindow(int width, int height, boolean fulscreen, String title) {
        NativesLoader.loadNatives();
        setTitle(title);
        try {
            setDisplayMode(width, height, fullscreen);
            Display.create();
            Keyboard.create();
            Mouse.create();
        } catch (LWJGLException e) {
            e.printStackTrace();
        }
    }

    public static void render() {
        Display.update();
    }

    /**
     * Tries to set up display mode for lwjgl display by StateManager settings.
     *
     * @param newWidth
     * @param newHeight
     * @param newFullscreen
     * @throws LWJGLException when is something wrong with Display setting.
     */
    public static void setDisplayMode(int newWidth, int newHeight, boolean newFullscreen) throws LWJGLException {
        fullscreen = newFullscreen;
        width = newWidth;
        height = newHeight;
        if (fullscreen) {
            for (DisplayMode mode : Display.getAvailableDisplayModes()) {
                if (mode.getHeight() == height && mode.getWidth() == width && mode.isFullscreenCapable()) {
                    setDisplayModePrivate(width, height, fullscreen);
                    return;
                }
            }
            throw new LWJGLException("This resolution cannot be set up for fullscreen, try other one.");
        } else {
            if (width > 0 && height > 0) {
                setDisplayModePrivate(width, height, fullscreen);
                return;
            }
            throw new LWJGLException("This resolution cannot be set up. It has to be bigger then zero.");
        }
    }

    /**
     * Set the display mode to be used
     *
     * @param width The width of the display required
     * @param height The height of the display required
     * @param fullscreen True if we want fullscreen mode
     * @throws org.lwjgl.LWJGLException
     */
    private static void setDisplayModePrivate(int width, int height, boolean fullscreen) throws LWJGLException {
        // return if requested DisplayMode is already set
        if ((Display.getDisplayMode().getWidth() == width)
                && (Display.getDisplayMode().getHeight() == height)
                && (Display.isFullscreen() == fullscreen)) {
            return;
        }

        DisplayMode targetDisplayMode = null;

        if (fullscreen) {
            DisplayMode[] modes = Display.getAvailableDisplayModes();
            int freq = 0;
            for (DisplayMode current : modes) {
                if ((current.getWidth() == width) && (current.getHeight() == height)) {
                    if ((targetDisplayMode == null) || (current.getFrequency() >= freq)) {
                        if ((targetDisplayMode == null) || (current.getBitsPerPixel() > targetDisplayMode.getBitsPerPixel())) {
                            targetDisplayMode = current;
                            freq = targetDisplayMode.getFrequency();
                        }
                    }

                    // if we've found a match for bpp and frequence against the 
                    // original display mode then it's probably best to go for this one
                    // since it's most likely compatible with the monitor
                    if ((current.getBitsPerPixel() == Display.getDesktopDisplayMode().getBitsPerPixel())
                            && (current.getFrequency() == Display.getDesktopDisplayMode().getFrequency())) {
                        targetDisplayMode = current;
                        break;
                    }
                }
            }
        } else {
            targetDisplayMode = new DisplayMode(width, height);
        }

        if (targetDisplayMode == null) {
            throw new LWJGLException("Failed to find value mode: " + width + "x" + height + " fs=" + fullscreen);
        }

        Display.setDisplayMode(targetDisplayMode);
        Display.setFullscreen(fullscreen);
    }

    public static void dispose() {
        Display.destroy();
        Keyboard.destroy();
        Mouse.destroy();
    }

    public static boolean isCloseRequested() {
        return Display.isCloseRequested();
    }

    public static int getWidth() {
        return Display.getDisplayMode().getWidth();
    }

    public static int getHeight() {
        return Display.getDisplayMode().getHeight();
    }

    public static String getTitle() {
        return Display.getTitle();
    }
    
    public static void setTitle(String title) {
        Display.setTitle(title);
    }

    public static boolean isFullscreen() {
        return Display.isFullscreen();
    }

    public Vector2f getCenter() {
        return new Vector2f(getWidth() / 2, getHeight() / 2);
    }
    
    public static void setVSyncEnabled(boolean enabled) {
        Display.setVSyncEnabled(enabled);
    }
    
    public static void bindAsRenderTarget() {
        glBindTexture(GL_TEXTURE_2D, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, width, height);
    }
}
