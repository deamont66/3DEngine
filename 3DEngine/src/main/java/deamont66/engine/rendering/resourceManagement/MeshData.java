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
import deamont66.engine.core.math.Vector2f;
import deamont66.engine.core.math.Vector3f;
import deamont66.engine.rendering.meshLoading.IndexedModel;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class MeshData extends ReferenceCounter
{
        private static final int POSITION_VB    = 0;
	private static final int TEXCOORD_VB    = 1;
	private static final int NORMAL_VB      = 2;
	private static final int TANGENT_VB     = 3;
        private static final int INDEX_VB       = 4;
        private static final int NUM_BUFFERS    = 5;
                
                
        private final int vertexArrayObject;
        private final int[] vertexArrayBuffers;
	private final int size;
	

	public MeshData(IndexedModel model)
	{
                vertexArrayBuffers = new int[NUM_BUFFERS];
                vertexArrayObject = glGenVertexArrays();
                glBindVertexArray(vertexArrayObject);
                
                vertexArrayBuffers[POSITION_VB] = glGenBuffers();
                vertexArrayBuffers[TEXCOORD_VB] = glGenBuffers();
                vertexArrayBuffers[NORMAL_VB] = glGenBuffers();
                vertexArrayBuffers[TANGENT_VB] = glGenBuffers();
                vertexArrayBuffers[INDEX_VB] = glGenBuffers();
                
                glBindBuffer(GL_ARRAY_BUFFER, vertexArrayBuffers[POSITION_VB]);
                glBufferData(GL_ARRAY_BUFFER, Util.createFlippedBuffer(model.getPositions().toArray(new Vector3f[model.getPositions().size()])), GL_STATIC_DRAW);

                glEnableVertexAttribArray(0);
                glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

                glBindBuffer(GL_ARRAY_BUFFER, vertexArrayBuffers[TEXCOORD_VB]);
                glBufferData(GL_ARRAY_BUFFER, Util.createFlippedBuffer(model.getTexCoords().toArray(new Vector2f[model.getTexCoords().size()])), GL_STATIC_DRAW);

                glEnableVertexAttribArray(1);
                glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

                glBindBuffer(GL_ARRAY_BUFFER, vertexArrayBuffers[NORMAL_VB]);
                glBufferData(GL_ARRAY_BUFFER, Util.createFlippedBuffer(model.getNormals().toArray(new Vector3f[model.getNormals().size()])), GL_STATIC_DRAW);

                glEnableVertexAttribArray(2);
                glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);

                glBindBuffer(GL_ARRAY_BUFFER, vertexArrayBuffers[TANGENT_VB]);
                glBufferData(GL_ARRAY_BUFFER, Util.createFlippedBuffer(model.getTangents().toArray(new Vector3f[model.getTangents().size()])), GL_STATIC_DRAW);

                glEnableVertexAttribArray(3);
                glVertexAttribPointer(3, 3, GL_FLOAT, false, 0, 0);

                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vertexArrayBuffers[INDEX_VB]);
                glBufferData(GL_ELEMENT_ARRAY_BUFFER, Util.createFlippedBuffer(Util.toIntArray(model.getIndices().toArray(new Integer[model.getIndices().size()]))), GL_STATIC_DRAW);
                
		this.size = model.getIndices().size();
	}

	@Override
	protected void finalize() 
	{
                glDeleteBuffers(vertexArrayBuffers[POSITION_VB]);
                glDeleteBuffers(vertexArrayBuffers[TEXCOORD_VB]);
                glDeleteBuffers(vertexArrayBuffers[NORMAL_VB]);
                glDeleteBuffers(vertexArrayBuffers[TANGENT_VB]);
                glDeleteBuffers(vertexArrayBuffers[INDEX_VB]);
                glDeleteVertexArrays(vertexArrayObject);
	}

	public int getSize() {
		return size;
	}

        public void draw() {
                glBindVertexArray(vertexArrayObject);
		glDrawElements(GL_TRIANGLES, size, GL_UNSIGNED_INT, 0);
        }
}
