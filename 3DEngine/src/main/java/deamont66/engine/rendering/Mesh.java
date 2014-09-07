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

import deamont66.engine.core.math.Vector3f;
import deamont66.engine.rendering.meshLoading.IndexedModel;
import deamont66.engine.rendering.meshLoading.OBJModel;
import deamont66.engine.rendering.resourceManagement.MeshData;
import java.util.HashMap;

public class Mesh {

    private static final HashMap<String, MeshData> loadedModels = new HashMap<>();
    private MeshData resource;
    private String fileName;

    public Mesh(String fileName) {
        this.fileName = fileName;
        MeshData oldResource = loadedModels.get(fileName);

        if (oldResource != null) {
            resource = oldResource;
            resource.addReference();
        } else {
            loadMesh(fileName);
            loadedModels.put(fileName, resource);
        }
    }

    public Mesh(String meshName, IndexedModel model) {
        this.fileName = meshName;
        MeshData oldResource = loadedModels.get(fileName);
        if (oldResource != null) {
            resource = oldResource;
            resource.addReference();
        } else {
            resource = new MeshData(model);
            loadedModels.put(fileName, resource);
        }
    }

    @Deprecated
    public Mesh(Vertex[] vertices, int[] indices) {
        this(vertices, indices, false);
    }

    @Deprecated
    public Mesh(Vertex[] vertices, int[] indices, boolean calcNormals) {
        addVertices(vertices, indices, calcNormals);
    }

    @Override
    protected void finalize() {
        if (resource.removeReference() && !fileName.isEmpty()) {
            loadedModels.remove(fileName);
        }
    }

    private void addVertices(Vertex[] vertices, int[] indices, boolean calcNormals) {
        if (calcNormals) {
            calcNormals(vertices, indices);
        }

        IndexedModel model = new IndexedModel();
        for (Vertex vertex : vertices) {
            model.addNormal(vertex.getNormal());
            model.addVertex(vertex.getPos());
            model.addTangent(vertex.getTangent());
            model.addTexCoord(vertex.getTexCoord());
        }

        for (int i = 0; i < indices.length; i += 3) {
            model.addFace(indices[i], indices[i + 1], indices[i + 2]);
        }

        resource = new MeshData(model);
    }

    public void draw() {
        resource.draw();
    }

    private void calcNormals(Vertex[] vertices, int[] indices) {
        for (int i = 0; i < indices.length; i += 3) {
            int i0 = indices[i];
            int i1 = indices[i + 1];
            int i2 = indices[i + 2];

            Vector3f v1 = vertices[i1].getPos().sub(vertices[i0].getPos());
            Vector3f v2 = vertices[i2].getPos().sub(vertices[i0].getPos());

            Vector3f normal = v1.cross(v2).normalized();

            vertices[i0].setNormal(vertices[i0].getNormal().add(normal));
            vertices[i1].setNormal(vertices[i1].getNormal().add(normal));
            vertices[i2].setNormal(vertices[i2].getNormal().add(normal));
        }

        for (int i = 0; i < vertices.length; i++) {
            vertices[i].setNormal(vertices[i].getNormal().normalized());
        }
    }

    private Mesh loadMesh(String fileName) {
        String[] splitArray = fileName.split("\\.");
        String ext = splitArray[splitArray.length - 1];

        if (!ext.equals("obj")) {
            System.err.println("Error: '" + ext + "' file format not supported for mesh data.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        OBJModel test = new OBJModel("/res/models/" + fileName);
        IndexedModel model = test.toIndexedModel();
        model.calcNormals();
        model.calcTangents();

        resource = new MeshData(model);

        return this;
    }
}
