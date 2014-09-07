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
package deamont66.engine.rendering.resourceManagement;

import deamont66.engine.core.Util;
import java.nio.ByteBuffer;
import org.lwjgl.opengl.EXTFramebufferObject;
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class TextureData extends ReferenceCounter {

    private final int textureTarget;
    private final int textureIDs[];
    private int frameBuffer;
    private int renderBuffer;
    private final int width;
    private final int height;

    public TextureData(int textureTarget, int width, int height, ByteBuffer[] buffers, int[] filters, int[] internalFormat, int[] format, boolean clamp, int[] attachments) {
        this.textureTarget = textureTarget;
        textureIDs = new int[buffers.length];
        this.width = width;
        this.height = height;
        this.frameBuffer = 0;
        initTextures(buffers, filters, internalFormat, format, clamp);
        intiRenderTarget(attachments);
    }

    private void initTextures(ByteBuffer[] buffers, int[] filters, int[] internalFormat, int[] format, boolean clamp) {
        for (int i = 0; i < buffers.length; i++) {
            textureIDs[i] = glGenTextures();
            glBindTexture(textureTarget, textureIDs[i]);

            glTexParameteri(textureTarget, GL_TEXTURE_MIN_FILTER, filters[i]);
            glTexParameteri(textureTarget, GL_TEXTURE_MAG_FILTER, filters[i]);

            if (clamp) {
                glTexParameterf(textureTarget, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
                glTexParameterf(textureTarget, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            }

            glTexImage2D(textureTarget, 0, internalFormat[i], width, height, 0, format[i], GL_UNSIGNED_BYTE, buffers[i]);

            if (filters[i] == GL_NEAREST_MIPMAP_NEAREST
                    || filters[i] == GL_NEAREST_MIPMAP_LINEAR
                    || filters[i] == GL_LINEAR_MIPMAP_NEAREST
                    || filters[i] == GL_LINEAR_MIPMAP_LINEAR) {
                glGenerateMipmap(textureTarget);
                float maxAnisotropy = glGetFloat(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT);
                glTexParameterf(textureTarget, GL_TEXTURE_MAX_ANISOTROPY_EXT, Util.Clamp(0.0f, 8.0f, maxAnisotropy));
            } else {
                glTexParameteri(textureTarget, GL_TEXTURE_BASE_LEVEL, 0);
                glTexParameteri(textureTarget, GL_TEXTURE_MAX_LEVEL, 0);
            }
        }
    }

    private void intiRenderTarget(int[] attachments) {
        if (attachments == null) {
            return;
        }

        int[] drawBuffers = new int[32];
        assert (attachments.length <= 32);

        boolean hasDepth = false;
        for (int i = 0; i < attachments.length; i++) {
            if (attachments[i] == GL_DEPTH_ATTACHMENT) {
                drawBuffers[i] = GL_NONE;
                hasDepth = true;
            } else {
                drawBuffers[i] = attachments[i];
            }

            if (attachments[i] == GL_NONE) {
                continue;
            }

            if (frameBuffer == 0) {
                frameBuffer = glGenFramebuffers();
                glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);
            }
            glFramebufferTexture2D(GL_FRAMEBUFFER, attachments[i], textureTarget, textureIDs[i], 0);
        }

        if (frameBuffer == 0) {
            return;
        }

        if (!hasDepth) {
            renderBuffer = glGenRenderbuffers();
            glBindRenderbuffer(GL_RENDERBUFFER, renderBuffer);
            glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, width, height);
            glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, renderBuffer);
        }

        for (int i = 0; i < drawBuffers.length; i++) {
            glDrawBuffers(drawBuffers[i]);
        }
        
//        glDrawBuffer(GL_NONE);
//	glReadBuffer(GL_NONE);
        
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            new Exception("Framebuffer creations failure. Error Code: " + glCheckFramebufferStatus(GL_FRAMEBUFFER)).printStackTrace();
            System.exit(1);
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    @Override
    protected void finalize() {
        for (int id : textureIDs) {
            glDeleteBuffers(id);
        }
        if (frameBuffer != 0) {
            glDeleteFramebuffers(frameBuffer);
        }
    }

    public void bind(int textureNum) {
        glBindTexture(GL_TEXTURE_2D, textureIDs[textureNum]);
    }

    public void bindAsRenderTarget() {
        glBindTexture(GL_TEXTURE_2D, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);
        glViewport(0, 0, width, height);
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }
}
