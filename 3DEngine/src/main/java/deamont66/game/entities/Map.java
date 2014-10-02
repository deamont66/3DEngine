/*
 * Copyright (c) 2012 - 2014, JiĹ™Ă­ Ĺ imeÄŤek
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
package deamont66.game.entities;

import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.dynamics.DynamicsWorld;
import deamont66.engine.components.Camera;
import deamont66.engine.core.CoreEngine;
import deamont66.engine.core.Entity;
import deamont66.engine.core.Transform;
import deamont66.engine.core.math.Vector3f;
import deamont66.engine.rendering.Material;
import deamont66.engine.rendering.Mesh;
import deamont66.engine.rendering.Renderer;
import deamont66.engine.rendering.Shader;
import deamont66.engine.rendering.Texture;
import deamont66.game.entities.physics.DynamicEntity;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JiriSimecek
 */
public class Map extends Entity {

    private static final int MAP_SIZE = 40;
    private DynamicsWorld dynamicsWorld;

    private final DynamicEntity[][] ground;
    private final List<DynamicEntity> entities = new ArrayList<>();

    public Map() {
        this.ground = new DynamicEntity[MAP_SIZE / 20][MAP_SIZE / 20];
        Transform transform = new Transform();
        Material roadTestMaterial = new Material();
        roadTestMaterial.setTexture("diffuse", new Texture("roads/road-asphalt.png"));
        roadTestMaterial.setTexture("normalMap", new Texture("roads/road-asphalt_normal.png"));
        for (int y = 0; y < ground.length; y++) {
            for (int x = 0; x < ground[y].length; x++) {
                transform.setPos(new Vector3f(20 * x, -5, 20 * y));
                ground[y][x] = new DynamicEntity(new Mesh("roads/road-flat.obj"), new BoxShape(new Vector3f(10f, 0.5f, 10f)), roadTestMaterial, transform);
                entities.add(ground[y][x]);
                transform.reset();
            }
        }
    }
    

    public void setDynamicsWorld(DynamicsWorld dynamicsWorld) {
        this.dynamicsWorld = dynamicsWorld;
        for (DynamicEntity e : entities) {
            e.addToWorld(dynamicsWorld);
        }
    }
    
    @Override
    public void setEngine(CoreEngine engine) {
//        super.setEngine(engine);
//        
//        for (DynamicEntity e : entities) {
//            e.setEngine(engine);
//        }
    }

    @Override
    public void render(Shader shader, Renderer renderer, Camera camera) {
        super.render(shader, renderer, camera);

        for (Entity e : entities) {
            e.renderAll(shader, renderer, camera);
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        
        for (Entity e : entities) {
            e.updateAll(delta);
        }
    }
    
    

    private class MapGroundData implements Serializable {

        int x, y;
        int groundID;

        public MapGroundData() {
        }

        public MapGroundData(int x, int y, int groundID) {
            this.x = x;
            this.y = y;
            this.groundID = groundID;
        }
    }

    private class MapEntityData implements Serializable {

        float x, y;
        String name;
        int damage;
        boolean isDynamic;

        public MapEntityData() {
        }

        public MapEntityData(float x, float y, String name, int damage) {
            this.x = x;
            this.y = y;
            this.name = name;
            this.damage = damage;
        }
    }
}
