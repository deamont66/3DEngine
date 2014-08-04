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

import deamont66.engine.core.math.Vector2f;
import deamont66.engine.core.math.Vector3f;

import java.util.ArrayList;

public class IndexedModel {

    private final ArrayList<Vector3f> positions;
    private final ArrayList<Vector2f> texCoords;
    private final ArrayList<Vector3f> normals;
    private final ArrayList<Vector3f> tangents;
    private final ArrayList<Integer> indices;

    public IndexedModel() {
        positions = new ArrayList<>();
        texCoords = new ArrayList<>();
        normals = new ArrayList<>();
        tangents = new ArrayList<>();
        indices = new ArrayList<>();
    }

    public void calcNormals() {
        normals.clear();
        for (Vector3f vector3f : positions) {
            normals.add(new Vector3f());
        }
        for (int i = 0; i < indices.size(); i += 3) {
            int i0 = indices.get(i);
            int i1 = indices.get(i + 1);
            int i2 = indices.get(i + 2);

            Vector3f v1 = positions.get(i1).sub(positions.get(i0));
            Vector3f v2 = positions.get(i2).sub(positions.get(i0));

            Vector3f normal = v1.cross(v2).normalized();

            normals.get(i0).set(normals.get(i0).add(normal));
            normals.get(i1).set(normals.get(i1).add(normal));
            normals.get(i2).set(normals.get(i2).add(normal));
        }

        for (int i = 0; i < normals.size(); i++) {
            normals.get(i).set(normals.get(i).normalized());
        }
    }

    public void calcTangents() {
        tangents.clear();
        for (Vector3f vector3f : positions) {
            tangents.add(new Vector3f());
        }
        for (int i = 0; i < indices.size(); i += 3) {
            int i0 = indices.get(i);
            int i1 = indices.get(i + 1);
            int i2 = indices.get(i + 2);

            Vector3f edge1 = positions.get(i1).sub(positions.get(i0));
            Vector3f edge2 = positions.get(i2).sub(positions.get(i0));

            float deltaU1 = texCoords.get(i1).getX() - texCoords.get(i0).getX();
            float deltaV1 = texCoords.get(i1).getY() - texCoords.get(i0).getY();
            float deltaU2 = texCoords.get(i2).getX() - texCoords.get(i0).getX();
            float deltaV2 = texCoords.get(i2).getY() - texCoords.get(i0).getY();

            float dividend = (deltaU1 * deltaV2 - deltaU2 * deltaV1);
            float f = dividend == 0 ? 0.0f : 1.0f / dividend;

            Vector3f tangent = new Vector3f(0, 0, 0);
            tangent.setX(f * (deltaV2 * edge1.getX() - deltaV1 * edge2.getX()));
            tangent.setY(f * (deltaV2 * edge1.getY() - deltaV1 * edge2.getY()));
            tangent.setZ(f * (deltaV2 * edge1.getZ() - deltaV1 * edge2.getZ()));

            tangents.get(i0).set(tangents.get(i0).add(tangent));
            tangents.get(i1).set(tangents.get(i1).add(tangent));
            tangents.get(i2).set(tangents.get(i2).add(tangent));
        }

        for (int i = 0; i < tangents.size(); i++) {
            tangents.get(i).set(tangents.get(i).normalized());
        }
    }

    public ArrayList<Vector3f> getPositions() {
        return positions;
    }

    public ArrayList<Vector2f> getTexCoords() {
        return texCoords;
    }

    public ArrayList<Vector3f> getNormals() {
        return normals;
    }

    public ArrayList<Vector3f> getTangents() {
        return tangents;
    }

    public ArrayList<Integer> getIndices() {
        return indices;
    }

    public boolean isValid() {
        return positions.size() == texCoords.size()
                && texCoords.size() == normals.size()
                && normals.size() == tangents.size();
    }
    
    public IndexedModel complete() {
        if(!isValid())
        {
            if(texCoords.size() != positions.size()) {
                for (int i = 0; i < positions.size(); i++) {
                    texCoords.clear();
                    texCoords.add(new Vector2f());
                }
            }
            if(normals.size() != positions.size()) {
                calcNormals();
            }
            if(tangents.size() != positions.size()) {
                calcTangents();
            }
        }
        return this;
    }

    public void addVertex(Vector3f vert) {
        positions.add(vert);
    }

    public void addTexCoord(Vector2f texCoord) {
        texCoords.add(texCoord);
    }

    public void addNormal(Vector3f normal) {
        normals.add(normal);
    }

    public void addTangent(Vector3f tangent) {
        tangents.add(tangent);
    }

    public void addFace(int vertIndex0, int vertIndex1, int vertIndex2) {
        indices.add(vertIndex0);
        indices.add(vertIndex1);
        indices.add(vertIndex2);
    }
}
