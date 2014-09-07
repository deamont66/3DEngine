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

import deamont66.engine.core.Debug;
import deamont66.engine.rendering.Shader;
import deamont66.engine.rendering.Shader.UniformStruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.GL_MAJOR_VERSION;
import static org.lwjgl.opengl.GL30.GL_MINOR_VERSION;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;

public class ShaderData extends ReferenceCounter {

    private static int s_supportedOpenGLLevel;
    private static String s_glslVersion;
    private final int m_program;

    private HashMap<String, Integer> m_uniformMap;
    private ArrayList<String> m_uniformNames;
    private ArrayList<Integer> m_shaders;
    private ArrayList<String> m_uniformTypes;

    public ShaderData(String fileName) {
        m_uniformMap = new HashMap<>();
        m_uniformNames = new ArrayList<>();
        m_shaders = new ArrayList<>();
        m_uniformTypes = new ArrayList<>();
                
        String actualFileName = fileName;
        if (!Debug.ENABLE_SHADERS) {
            actualFileName = "nullShader";
        }

        m_program = glCreateProgram();

        if (m_program == 0) {
            System.err.println("Error creating shader program");
            System.exit(1);
        }

        if (s_supportedOpenGLLevel == 0) {
            int majorVersion = glGetInteger(GL_MAJOR_VERSION);
            int minorVersion = glGetInteger(GL_MINOR_VERSION);

            s_supportedOpenGLLevel = majorVersion * 100 + minorVersion * 10;

            if (s_supportedOpenGLLevel >= 330) {
                s_glslVersion = s_supportedOpenGLLevel + "";
            } else if (s_supportedOpenGLLevel >= 320) {
                s_glslVersion = "150";
            } else if (s_supportedOpenGLLevel >= 310) {
                s_glslVersion = "140";
            } else if (s_supportedOpenGLLevel >= 300) {
                s_glslVersion = "130";
            } else if (s_supportedOpenGLLevel >= 210) {
                s_glslVersion = "120";
            } else if (s_supportedOpenGLLevel >= 200) {
                s_glslVersion = "110";
            } else {
                System.err.println("Error: OpenGL Version " + majorVersion + ". " + minorVersion + " does not support shaders.\n");
                System.exit(1);
            }
        }

        s_glslVersion = "150";
        
        String shaderText = Shader.loadShader(actualFileName + ".glsl");

        String vertexShaderText = "#version " + s_glslVersion + "\n#define VS_BUILD\n#define GLSL_VERSION " + s_glslVersion + "\n" + shaderText;
        String fragmentShaderText = "#version " + s_glslVersion + "\n#define FS_BUILD\n#define GLSL_VERSION " + s_glslVersion + "\n" + shaderText;

        addVertexShader(vertexShaderText);
        addFragmentShader(fragmentShaderText);

        String attributeKeyword = "attribute";
        addAllAttributes(vertexShaderText, attributeKeyword);

        compileShader();

        addShaderUniforms(shaderText);

    }

    @Override
    protected void finalize() {
        for (int it : m_shaders) {
            glDetachShader(m_program, it);
            glDeleteShader(it);
        }
        glDeleteBuffers(m_program);
    }

    public int getProgram() {
        return m_program;
    }

    public HashMap<String, Integer> getUniforms() {
        return m_uniformMap;
    }

    public ArrayList<String> getUniformNames() {
        return m_uniformNames;
    }

    public ArrayList<String> getUniformTypes() {
        return m_uniformTypes;
    }

    public ArrayList<Integer> getShaders() {
        return m_shaders;
    }

    private void addVertexShader(String text) {
        addProgram(text, GL_VERTEX_SHADER);
    }

    private void addGeometryShader(String text) {
        addProgram(text, GL_GEOMETRY_SHADER);
    }

    private void addFragmentShader(String text) {
        addProgram(text, GL_FRAGMENT_SHADER);
    }

    private void addProgram(String text, int type) {
        int shader = glCreateShader(type);

        if (shader == 0) {
            System.err.println("Error creating shader type " + type);
            System.exit(1);
        }

        glShaderSource(shader, text);
        glCompileShader(shader);

        int success = glGetShaderi(shader, GL_COMPILE_STATUS);
        if (success == 0) {
            String infoLog = glGetShaderInfoLog(shader, 1024);
            System.err.println("Error compiling shader type " + shader + ": '" + infoLog + "'");
            System.exit(1);
        }

        glAttachShader(m_program, shader);
        m_shaders.add(shader);
    }

    private void addAllAttributes(String vertexShaderText, String attributeKeyword) {
        int currentAttribLocation = 0;
        int attributeLocation = vertexShaderText.indexOf(attributeKeyword);
        while (attributeLocation != -1) {
            boolean isCommented = false;
            int lastLineEnd = vertexShaderText.lastIndexOf("\n", attributeLocation);

            if (lastLineEnd != -1) {
                String potentialCommentSection = vertexShaderText.substring(lastLineEnd, attributeLocation);

                //Potential false positives are both in comments, and in macros.
                isCommented = (potentialCommentSection.contains("//") || potentialCommentSection.contains("#"));
            }

            if (!isCommented) {
                int begin = attributeLocation + attributeKeyword.length();
                int end = vertexShaderText.indexOf(";", begin);

                String attributeLine = vertexShaderText.substring(begin + 1, end);

                begin = attributeLine.indexOf(" ");
                String attributeName = attributeLine.substring(begin + 1);

                glBindAttribLocation(m_program, currentAttribLocation, attributeName);
                currentAttribLocation++;
            }
            attributeLocation = vertexShaderText.indexOf(attributeKeyword, attributeLocation + attributeKeyword.length());
        }
    }

    private void addShaderUniforms(String shaderText) {
        String UNIFORM_KEY = "uniform";

        List<UniformStruct> structs = Shader.findUniformStructs(shaderText);

        int uniformLocation = shaderText.indexOf(UNIFORM_KEY);
        while (uniformLocation >= 0) {
            boolean isCommented = false;
            int lastLineEnd = shaderText.lastIndexOf("\n", uniformLocation);

            if (lastLineEnd != -1) {
                String potentialCommentSection = shaderText.substring(lastLineEnd, uniformLocation);
                isCommented = potentialCommentSection.contains("//");
            }

            if (!isCommented) {
                int begin = uniformLocation + UNIFORM_KEY.length();
                int end = shaderText.indexOf(";", begin);

                String uniformLine = shaderText.substring(begin + 1, end);

                begin = uniformLine.indexOf(" ");
                String uniformName = uniformLine.substring(begin + 1);
                String uniformType = uniformLine.substring(0, begin);

                m_uniformNames.add(uniformName);
                m_uniformTypes.add(uniformType);
                addUniform(uniformName, uniformType, structs);
            }
            uniformLocation = shaderText.indexOf(UNIFORM_KEY, uniformLocation + UNIFORM_KEY.length());
        }
    }

    private void addUniform(String uniformName, String uniformType, List<UniformStruct> structs) {
        boolean addThis = true;

        for (int i = 0; i < structs.size(); i++) {
            if (structs.get(i).getName().equals(uniformType)) {
                addThis = false;
                for (int j = 0; j < structs.get(i).getMemberNames().size(); j++) {
                    addUniform(uniformName + "." + structs.get(i).getMemberNames().get(j).getName(), structs.get(i).getMemberNames().get(j).getType(), structs);
                }
            }
        }

        if (!addThis) {
            return;
        }

        int location = glGetUniformLocation(m_program, uniformName);

        assert (location != GL_INVALID_VALUE);

        m_uniformMap.put(uniformName, location);
    }

    private void compileShader() {
        glLinkProgram(m_program);
        Shader.checkShaderError(m_program, GL_LINK_STATUS, true, "Error linking shader program");

        glValidateProgram(m_program);
        Shader.checkShaderError(m_program, GL_VALIDATE_STATUS, true, "Invalid shader program");
    }
}
