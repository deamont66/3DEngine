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

import deamont66.engine.components.Camera;
import deamont66.engine.components.DirectionalLight;
import deamont66.engine.components.PointLight;
import deamont66.engine.components.SpotLight;
import deamont66.engine.core.*;
import deamont66.engine.core.math.Matrix4f;
import deamont66.engine.core.math.Vector3f;
import deamont66.engine.rendering.resourceManagement.ShaderData;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.lwjgl.opengl.GL20.*;

public class Shader {

    private static final HashMap<String, ShaderData> loadedShaders = new HashMap<>();

    private final ShaderData m_shaderData;
    private final String m_fileName;

    public Shader() {
        this("basicShader");
    }

    public Shader(String fileName) {
        this.m_fileName = fileName;

        ShaderData oldResource = loadedShaders.get(fileName);

        if (oldResource != null) {
            m_shaderData = oldResource;
            m_shaderData.addReference();
        } else {
            m_shaderData = new ShaderData(fileName);
            loadedShaders.put(fileName, m_shaderData);

            if (Debug.DEBUG_ECHO) {
                System.out.println("Shader \"" + fileName + "\" was loaded. OpenGL ID of program is: " + m_shaderData.getProgram());
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        if(m_shaderData != null && m_shaderData.removeReference())
	{
		if(m_fileName.length() > 0)
			loadedShaders.remove(m_fileName);			
	}
    }
    
    public void bind() {
        glUseProgram(m_shaderData.getProgram());
    }

    public void updateUniforms(Transform transform, Material material, Renderer renderer, Camera camera) {
        Matrix4f worldMatrix = transform.getTransformation();
        Matrix4f projectedMatrix = camera.getViewProjection().mul(worldMatrix);

        for (int i = 0; i < m_shaderData.getUniformNames().size(); i++) {
            String uniformName = m_shaderData.getUniformNames().get(i);
            String uniformType = m_shaderData.getUniformTypes().get(i);

            if (uniformName.substring(0, 2).equals("R_")) {
                String unprefixedName = uniformName.substring(2, uniformName.length());

                if (unprefixedName.equals("lightMatrix")) {
                    setUniformMatrix4f(uniformName, renderer.getLightMatrix().mul(worldMatrix));
                } else if (uniformType.equals("sampler2D")) {
                    int samplerSlot = renderer.getSamplerSlot(unprefixedName);
                    renderer.getTexture(unprefixedName).bind(samplerSlot);
                    setUniformi(uniformName, samplerSlot);
                } else if (uniformType.equals("vec3")) {
                    setUniformVector3f(uniformName, renderer.getVector3f(unprefixedName));
                } else if (uniformType.equals("float")) {
                    setUniformf(uniformName, renderer.getFloat(unprefixedName));
                } else if (uniformType.equals("DirectionalLight")) {
                    setUniformDirectionalLight(uniformName, (DirectionalLight) renderer.getActiveLight());
                } else if (uniformType.equals("PointLight")) {
                    setUniformPointLight(uniformName, (PointLight) renderer.getActiveLight());
                } else if (uniformType.equals("SpotLight")) {
                    setUniformSpotLight(uniformName, (SpotLight) renderer.getActiveLight());
                } else {
                    renderer.updateUniformStruct(transform, material, this, uniformName, uniformType);
                }
            } else if (uniformType.equals("sampler2D")) {
                int samplerSlot = renderer.getSamplerSlot(uniformName);
                material.getTexture(uniformName).bind(samplerSlot);
                setUniformi(uniformName, samplerSlot);
            } else if (uniformName.substring(0, 2).equals("T_")) {
                if (uniformName.equals("T_MVP")) {
                    setUniformMatrix4f(uniformName, projectedMatrix);
                } else if (uniformName.equals("T_model")) {
                    setUniformMatrix4f(uniformName, worldMatrix);
                } else {
                    throw new IllegalArgumentException("Invalid Transform Uniform: " + uniformName);
                }
            } else if (uniformName.substring(0, 2).equals("C_")) {
                if (uniformName.equals("C_eyePos")) {
                    setUniformVector3f(uniformName, camera.getTransform().getTransformedPos());
                } else {
                    throw new IllegalArgumentException("Invalid Camera Uniform: " + uniformName);
                }
            } else {
                if (uniformType.equals("vec3")) {
                    setUniformVector3f(uniformName, material.getVector3f(uniformName));
                } else if (uniformType.equals("float")) {
                    setUniformf(uniformName, material.getFloat(uniformName));
                } else {
                    throw new IllegalArgumentException(uniformType + " is not supported by the Material class");
                }
            }
        }
    }

    public void setUniformi(String uniformName, int value) {
        glUniform1i(m_shaderData.getUniforms().get(uniformName), value);
    }

    public void setUniformf(String uniformName, float value) {
        glUniform1f(m_shaderData.getUniforms().get(uniformName), value);
    }

    public void setUniformVector3f(String uniformName, Vector3f value) {
        glUniform3f(m_shaderData.getUniforms().get(uniformName), value.getX(), value.getY(), value.getZ());
    }

    public void setUniformMatrix4f(String uniformName, Matrix4f value) {
        glUniformMatrix4(m_shaderData.getUniforms().get(uniformName), true, Util.createFlippedBuffer(value));
    }

    private void setUniformDirectionalLight(String uniformName, DirectionalLight directionalLight) {
        setUniformVector3f(uniformName + ".direction", directionalLight.getTransform().getTransformedRot().getForward());
        setUniformVector3f(uniformName + ".base.color", directionalLight.getColor());
        setUniformf(uniformName + ".base.intensity", directionalLight.getIntensity());
    }

    private void setUniformPointLight(String uniformName, PointLight pointLight) {
        setUniformVector3f(uniformName + ".base.color", pointLight.getColor());
        setUniformf(uniformName + ".base.intensity", pointLight.getIntensity());
        setUniformf(uniformName + ".atten.constant", pointLight.getAttenuation().getConstant());
        setUniformf(uniformName + ".atten.linear", pointLight.getAttenuation().getLinear());
        setUniformf(uniformName + ".atten.exponent", pointLight.getAttenuation().getExponent());
        setUniformVector3f(uniformName + ".position", pointLight.getTransform().getTransformedPos());
        setUniformf(uniformName + ".range", pointLight.getRange());
    }

    private void setUniformSpotLight(String uniformName, SpotLight spotLight) {
        setUniformVector3f(uniformName + ".pointLight.base.color", spotLight.getColor());
        setUniformf(uniformName + ".pointLight.base.intensity", spotLight.getIntensity());
        setUniformf(uniformName + ".pointLight.atten.constant", spotLight.getAttenuation().getConstant());
        setUniformf(uniformName + ".pointLight.atten.linear", spotLight.getAttenuation().getLinear());
        setUniformf(uniformName + ".pointLight.atten.exponent", spotLight.getAttenuation().getExponent());
        setUniformVector3f(uniformName + ".pointLight.position", spotLight.getTransform().getTransformedPos());
        setUniformf(uniformName + ".pointLight.range", spotLight.getRange());
        setUniformVector3f(uniformName + ".direction", spotLight.getTransform().getTransformedRot().getForward());
        setUniformf(uniformName + ".cutoff", spotLight.getCutoff());
    }

//--------------------------------------------------------------------------------
// Static Function Implementations
//--------------------------------------------------------------------------------
    public static void checkShaderError(int shader, int flag, boolean isProgram, String errorMessage) {
        int success = 0;

        if (isProgram) {
            success = glGetProgrami(shader, flag);
        } else {
            success = glGetShaderi(shader, flag);
        }

        if (success == 0) {
            String error;
            if (isProgram) {
                error = glGetProgramInfoLog(shader, 1024);
            } else {
                error = glGetShaderInfoLog(shader, 1024);
            }

            System.out.println(errorMessage + ": " + error);
        }
    }

    public static String loadShader(String fileName) {
        StringBuilder shaderSource = new StringBuilder();
        BufferedReader shaderReader;
        final String INCLUDE_DIRECTIVE = "#include";

        try {
            shaderReader = new BufferedReader(new InputStreamReader(Shader.class.getResourceAsStream("/res/shaders/" + fileName)));
            String line;

            while ((line = shaderReader.readLine()) != null) {
                if (line.startsWith(INCLUDE_DIRECTIVE)) {
                    shaderSource.append(loadShader(line.substring(INCLUDE_DIRECTIVE.length() + 2, line.length() - 1).trim()));
                } else {
                    shaderSource.append(line).append("\n");
                }
            }

            shaderReader.close();
        } catch (Exception e) {
            throw new RuntimeException(fileName + " cannot be found and loaded.");
        }

        return shaderSource.toString();
    }

    private static List<TypedData> findUniformStructComponents(String openingBraceToClosingBrace) {
        char[] charsToIgnore = new char[]{' ', '\n', '\t', '{'};
        int UNSIGNED_NEG_ONE = -1;

        List<TypedData> result = new ArrayList<>();
        String[] structLines = openingBraceToClosingBrace.split(";");

        for (int i = 0; i < structLines.length; i++) {
            int nameBegin = UNSIGNED_NEG_ONE;
            int nameEnd = UNSIGNED_NEG_ONE;

            for (int j = 0; j < structLines[i].length(); j++) {
                boolean isIgnoreableCharacter = false;

                for (int k = 0; k < charsToIgnore.length; k++) {
                    if (structLines[i].charAt(j) == charsToIgnore[k]) {
                        isIgnoreableCharacter = true;
                        break;
                    }
                }

                if (nameBegin == UNSIGNED_NEG_ONE && isIgnoreableCharacter == false) {
                    nameBegin = j;
                } else if (nameBegin != UNSIGNED_NEG_ONE && isIgnoreableCharacter) {
                    nameEnd = j;
                    break;
                }
            }

            if (nameBegin == UNSIGNED_NEG_ONE || nameEnd == UNSIGNED_NEG_ONE) {
                continue;
            }

            TypedData newData = new TypedData(structLines[i].substring(nameEnd + 1),
                    structLines[i].substring(nameBegin, nameEnd));

            result.add(newData);
        }

        return result;
    }

    public static String findUniformStructName(String structStartToOpeningBrace) {
        return structStartToOpeningBrace.split(" ")[0].split("\n")[0];
    }

    public static List<UniformStruct> findUniformStructs(String shaderText) {
        String STRUCT_KEY = "struct";
        List<UniformStruct> result = new ArrayList<>();

        int structLocation = shaderText.indexOf(STRUCT_KEY);
        while (structLocation != -1) {
            structLocation += STRUCT_KEY.length() + 1; //Ignore the struct keyword and space

            int braceOpening = shaderText.indexOf("{", structLocation);
            int braceClosing = shaderText.indexOf("}", braceOpening);

            UniformStruct newStruct = new UniformStruct(
                    findUniformStructName(shaderText.substring(structLocation, braceOpening)),
                    findUniformStructComponents(shaderText.substring(braceOpening, braceClosing)));

            result.add(newStruct);
            structLocation = shaderText.indexOf(STRUCT_KEY, structLocation);
        }

        return result;
    }

    public static class TypedData {

        private final String name;
        private final String type;

        private TypedData(String name, String type) {
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }
    }

    public static class UniformStruct {

        private final String name;
        private final List<TypedData> memberNames;

        private UniformStruct(String name, List<TypedData> memberNames) {
            this.name = name;
            this.memberNames = memberNames;
        }

        public List<TypedData> getMemberNames() {
            return memberNames;
        }

        public String getName() {
            return name;
        }
    }
}
