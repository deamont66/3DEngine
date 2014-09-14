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
package deamont66.engine.rendering.meshLoading;

import com.obj.Face;
import com.obj.Group;
import com.obj.TextureCoordinate;
import com.obj.Vertex;
import com.obj.WavefrontObject;
import deamont66.engine.core.math.Vector2f;
import deamont66.engine.core.math.Vector3f;

/**
 *
 * @author JiriSimecek
 */
public class NewOBJModel extends OBJModel {

    public NewOBJModel(String fileName) {
        super();

        WavefrontObject obj = new WavefrontObject(fileName);

        Group group = obj.getGroups().get(0);

        for (Vertex ver : group.vertices) {
            positions.add(new Vector3f(ver.getX(), ver.getY(), ver.getZ()));
        }
        for (TextureCoordinate texCoord : group.texcoords) {
            texCoords.add(new Vector2f(texCoord.getU(), texCoord.getV()));
        }
        for (Vertex normal : group.normals) {
            normals.add(new Vector3f(normal.getX(), normal.getY(), normal.getZ()));
        }
        for (Face f : group.getFaces()) {
            int[] idx = f.vertIndices;
            int[] nidx = f.normIndices;
            int[] tidx = f.texIndices;

            if (f.getType() == Face.GL_TRIANGLES) {
                OBJIndex index = new OBJIndex();
                OBJIndex index2 = new OBJIndex();
                OBJIndex index3 = new OBJIndex();

                index.vertexIndex = idx[0];
                index2.vertexIndex = idx[1];
                index3.vertexIndex = idx[2];

                index.normalIndex = nidx[0];
                index2.normalIndex = nidx[1];
                index3.normalIndex = nidx[2];

                index.texCoordIndex = tidx[0];
                index2.texCoordIndex = tidx[1];
                index3.texCoordIndex = tidx[2];

                indices.add(index);
                indices.add(index2);
                indices.add(index3);
            } else if (f.getType() == Face.GL_QUADS) {
                OBJIndex index = new OBJIndex();
                OBJIndex index2 = new OBJIndex();
                OBJIndex index3 = new OBJIndex();

                OBJIndex index_2 = new OBJIndex();
                OBJIndex index2_2 = new OBJIndex();
                OBJIndex index3_2 = new OBJIndex();

                index.vertexIndex = idx[0];
                index2.vertexIndex = idx[1];
                index3.vertexIndex = idx[2];

                index.normalIndex = nidx[0];
                index2.normalIndex = nidx[1];
                index3.normalIndex = nidx[2];

                index.texCoordIndex = tidx[0];
                index2.texCoordIndex = tidx[1];
                index3.texCoordIndex = tidx[2];

                index_2.vertexIndex = idx[0];
                index2_2.vertexIndex = idx[2];
                index3_2.vertexIndex = idx[3];

                index_2.normalIndex = nidx[0];
                index2_2.normalIndex = nidx[2];
                index3_2.normalIndex = nidx[3];

                index_2.texCoordIndex = tidx[0];
                index2_2.texCoordIndex = tidx[2];
                index3_2.texCoordIndex = tidx[3];

                indices.add(index);
                indices.add(index2);
                indices.add(index3);
                
                indices.add(index_2);
                indices.add(index2_2);
                indices.add(index3_2);
            }
        }
    }
}
