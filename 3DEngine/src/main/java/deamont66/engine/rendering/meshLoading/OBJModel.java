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

import deamont66.engine.core.Util;
import deamont66.engine.core.math.Vector2f;
import deamont66.engine.core.math.Vector3f;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class OBJModel {

    protected ArrayList<Vector3f> positions;
    protected ArrayList<Vector2f> texCoords;
    protected ArrayList<Vector3f> normals;
    protected ArrayList<OBJIndex> indices;
    protected boolean hasTexCoords;
    protected boolean hasNormals;

    /**
     * Generates empty OBJModel. Only for extetending purposes, we are
     * recomending to use {@link #OBJModel(java.lang.String) } constructor
     * instead.
     */
    public OBJModel() {
        positions = new ArrayList<>();
        texCoords = new ArrayList<>();
        normals = new ArrayList<>();
        indices = new ArrayList<>();
        hasTexCoords = false;
        hasNormals = false;
    }

    /**
     * Loads .obj model into game engine. Use {@link #toIndexedModel() } to get
     * optimazed model for rendering.
     *
     * @param fileName .obj file to load
     */
    public OBJModel(String fileName) {
        this();
        
        BufferedReader meshReader;

        try {
            meshReader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(fileName)));
            String line;

            while ((line = meshReader.readLine()) != null) {
                String[] tokens = line.split(" ");
                tokens = Util.removeEmptyStrings(tokens);

                if (tokens.length == 0 || tokens[0].equals("#")) {
                    //continue;
                } else if (tokens[0].equals("v")) {
                    positions.add(new Vector3f(Float.valueOf(tokens[1]),
                            Float.valueOf(tokens[2]),
                            Float.valueOf(tokens[3])));
                } else if (tokens[0].equals("vt")) {
                    texCoords.add(new Vector2f(Float.valueOf(tokens[1]),
                            Float.valueOf(tokens[2])));
                } else if (tokens[0].equals("vn")) {
                    normals.add(new Vector3f(Float.valueOf(tokens[1]),
                            Float.valueOf(tokens[2]),
                            Float.valueOf(tokens[3])));
                } else if (tokens[0].equals("f")) {
                    for (int i = 0; i < tokens.length - 3; i++) {
                        indices.add(parseOBJIndex(tokens[1]));
                        indices.add(parseOBJIndex(tokens[2 + i]));
                        indices.add(parseOBJIndex(tokens[3 + i]));
                    }
                }
            }

            meshReader.close();
        } catch (Exception e) {
            throw new RuntimeException("Error while loading OBJ file: " + fileName);

        }
    }

    public IndexedModel toIndexedModel() {
        IndexedModel result = new IndexedModel();
        IndexedModel normalModel = new IndexedModel();
        HashMap<OBJIndex, Integer> resultIndexMap = new HashMap<>();
        HashMap<Integer, Integer> normalIndexMap = new HashMap<>();
        HashMap<Integer, Integer> indexMap = new HashMap<>();

        for (OBJIndex currentIndex : indices) {
            Vector3f currentPosition = positions.get(currentIndex.vertexIndex);
            Vector2f currentTexCoord;
            Vector3f currentNormal;

            if (hasTexCoords) {
                currentTexCoord = texCoords.get(currentIndex.texCoordIndex);
            } else {
                currentTexCoord = new Vector2f(0, 0);
            }

            if (hasNormals) {
                currentNormal = normals.get(currentIndex.normalIndex);
            } else {
                currentNormal = new Vector3f(0, 0, 0);
            }

            Integer modelVertexIndex = resultIndexMap.get(currentIndex);

            if (modelVertexIndex == null) {
                modelVertexIndex = result.getPositions().size();
                resultIndexMap.put(currentIndex, modelVertexIndex);

                result.getPositions().add(currentPosition);
                result.getTexCoords().add(currentTexCoord);
                if (hasNormals) {
                    result.getNormals().add(currentNormal);
                }
                result.getTangents().add(new Vector3f(0, 0, 0));
            }

            Integer normalModelIndex = normalIndexMap.get(currentIndex.vertexIndex);

            if (normalModelIndex == null) {
                normalModelIndex = normalModel.getPositions().size();
                normalIndexMap.put(currentIndex.vertexIndex, normalModelIndex);

                normalModel.getPositions().add(currentPosition);
                normalModel.getTexCoords().add(currentTexCoord);
                normalModel.getNormals().add(currentNormal);
                normalModel.getTangents().add(new Vector3f(0, 0, 0));
            }

            result.getIndices().add(modelVertexIndex);
            normalModel.getIndices().add(normalModelIndex);
            indexMap.put(modelVertexIndex, normalModelIndex);
        }

        if (!hasNormals) {
            normalModel.calcNormals();

            for (int i = 0; i < result.getPositions().size(); i++) {
                result.getNormals().add(normalModel.getNormals().get(indexMap.get(i)));
            }
        }

        normalModel.calcTangents();

        for (int i = 0; i < result.getPositions().size(); i++) {
            result.getTangents().add(normalModel.getTangents().get(indexMap.get(i)));
        }

        return result;
    }

    private OBJIndex parseOBJIndex(String token) {
        String[] values = token.split("/");

        OBJIndex result = new OBJIndex();
        result.vertexIndex = Integer.parseInt(values[0]) - 1;

        if (values.length > 1) {
            if (!values[1].isEmpty()) {
                hasTexCoords = true;
                result.texCoordIndex = Integer.parseInt(values[1]) - 1;
            }
            if (values.length > 2) {
                if (!values[2].isEmpty()) {
                    hasNormals = true;
                    result.normalIndex = Integer.parseInt(values[2]) - 1;
                }
            }
        }

        return result;
    }
}
