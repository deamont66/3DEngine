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

import deamont66.engine.core.*;
import deamont66.engine.rendering.resourceManagement.TextureData;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.HashMap;
import javax.imageio.ImageIO;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;

public class Texture {

	private static final HashMap<String, TextureData> loadedTextures = new HashMap<>();
	private final TextureData resource;
	private final String fileName;

	public Texture(String fileName) {
		this(fileName, GL_TEXTURE_2D, GL_LINEAR_MIPMAP_LINEAR, GL_RGBA, GL_RGBA, false, GL_NONE);
	}

	public Texture(String fileName, int textureTarget) {
		this(fileName, textureTarget, GL_LINEAR_MIPMAP_LINEAR, GL_RGBA, GL_RGBA, false, GL_NONE);
	}

	public Texture(String fileName, int textureTarget, int filter) {
		this(fileName, textureTarget, filter, GL_RGBA, GL_RGBA, false, GL_NONE);
	}

	public Texture(String fileName, int textureTarget, int filter, int internalFormat) {
		this(fileName, textureTarget, filter, internalFormat, GL_RGBA, false, GL_NONE);
	}

	public Texture(String fileName, int textureTarget, int filter, int internalFormat, int format) {
		this(fileName, textureTarget, filter, internalFormat, format, false, GL_NONE);
	}

	public Texture(String fileName, int textureTarget, int filter, int internalFormat, int format, boolean clamp) {
		this(fileName, textureTarget, filter, internalFormat, format, clamp, GL_NONE);
	}

	public Texture(String fileName, int textureTarget, int filter, int internalFormat, int format, boolean clamp, int attachment) {
		this.fileName = fileName;
		TextureData oldResource = loadedTextures.get(fileName);

		if (oldResource != null) {
			resource = oldResource;
			resource.addReference();
		} else {
			resource = loadTexture(fileName, textureTarget, filter, internalFormat, format, clamp, attachment);
			loadedTextures.put(fileName, resource);
			if (Debug.DEBUG_ECHO) {
				System.out.println("Texture \"" + fileName + "\" was loaded.");
			}
		}
	}
//	Texture(int width = 0, int height = 0, unsigned char* data = 0, GLenum textureTarget = GL_TEXTURE_2D, GLfloat filter = GL_LINEAR_MIPMAP_LINEAR, GLenum internalFormat = GL_RGBA, GLenum format = GL_RGBA, bool clamp = false, GLenum attachment = GL_NONE);

	public Texture() {
		this(0, 0, null, GL_TEXTURE_2D, GL_LINEAR_MIPMAP_LINEAR, GL_RGBA, GL_RGBA, false, GL_NONE);
	}
	
	public Texture(int width, int height) {
		this(width, height, null, GL_TEXTURE_2D, GL_LINEAR_MIPMAP_LINEAR, GL_RGBA, GL_RGBA, false, GL_NONE);
	}
	
	public Texture(int width, int height, ByteBuffer data) {
		this(width, height, data, GL_TEXTURE_2D, GL_LINEAR_MIPMAP_LINEAR, GL_RGBA, GL_RGBA, false, GL_NONE);
	}
	
	public Texture(int width, int height, ByteBuffer data, int textureTarget) {
		this(width, height, data, textureTarget, GL_LINEAR_MIPMAP_LINEAR, GL_RGBA, GL_RGBA, false, GL_NONE);
	}
	
	public Texture(int width, int height, ByteBuffer data, int textureTarget, int filter) {
		this(width, height, data, textureTarget, filter, GL_RGBA, GL_RGBA, false, GL_NONE);
	}
	
	public Texture(int width, int height, ByteBuffer data, int textureTarget, int filter, int internalFormat) {
		this(width, height, data, textureTarget, filter, internalFormat, GL_RGBA, false, GL_NONE);
	}
	
	public Texture(int width, int height, ByteBuffer data, int textureTarget, int filter, int internalFormat, int format) {
		this(width, height, data, textureTarget, filter, internalFormat, format, false, GL_NONE);
	}
	
	public Texture(int width, int height, ByteBuffer data, int textureTarget, int filter, int internalFormat, int format,  boolean clamp) {
		this(width, height, data, textureTarget, filter, internalFormat, format, clamp, GL_NONE);
	}

	public Texture(int width, int height, ByteBuffer data, int textureTarget, int filter, int internalFormat, int format, boolean clamp, int attachment) {
		resource = new TextureData(textureTarget, width, height, new ByteBuffer[]{data}, new int[]{filter}, new int[]{internalFormat}, new int[]{format}, clamp, new int[] {attachment});
		fileName = "";
	}

	@Override
	protected void finalize() {
		if (resource.removeReference() && !fileName.isEmpty()) {
			loadedTextures.remove(fileName);
		}
	}

	public void bind() {
		bind(0);
	}

	public void bind(int samplerSlot) {
		assert (samplerSlot >= 0 && samplerSlot <= 31);
		glActiveTexture(GL_TEXTURE0 + samplerSlot);
		resource.bind(0);
	}

	public void bindAsRenderTarget() {
		resource.bindAsRenderTarget();
	}

	private TextureData loadTexture(String fileName, int textureTarget, int filter, int internalFormat, int format, boolean clamp, int attachment) {
		try {
			BufferedImage image = ImageIO.read(Texture.class.getResourceAsStream("/res/textures/" + fileName));
			int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());

			ByteBuffer buffer = Util.createByteBuffer(image.getHeight() * image.getWidth() * 4);
			boolean hasAlpha = image.getColorModel().hasAlpha();

			for (int y = 0; y < image.getHeight(); y++) {
				for (int x = 0; x < image.getWidth(); x++) {
					int pixel = pixels[y * image.getWidth() + x];

					buffer.put((byte) ((pixel >> 16) & 0xFF));
					buffer.put((byte) ((pixel >> 8) & 0xFF));
					buffer.put((byte) ((pixel) & 0xFF));
					if (hasAlpha) {
						buffer.put((byte) ((pixel >> 24) & 0xFF));
					} else {
						buffer.put((byte) (0xFF));
					}
				}
			}

			buffer.flip();

			return new TextureData(textureTarget, image.getWidth(), image.getHeight(), new ByteBuffer[]{buffer}, new int[]{filter}, new int[]{internalFormat}, new int[]{format}, clamp, new int[] {attachment});
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		return null;
	}

	public int getWidth() {
		return resource.getWidth();
	}

	public int getHeight() {
		return resource.getHeight();
	}
}
