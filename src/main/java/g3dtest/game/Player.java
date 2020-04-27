package g3dtest.game;

import org.mini.g3d.animation.AnimatedModel;
import org.mini.g3d.core.EngineManager;
import org.mini.g3d.core.gltf2.loader.data.GLTF;
import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.core.vector.Vector3f;
import org.mini.g3d.terrain.Terrain;
import org.mini.glfw.Glfw;

import java.util.ArrayList;

import static org.mini.glfw.Glfw.*;

public class Player extends AnimatedModel implements Cloneable {

    private static final float RUN_SPEED = 5f;
    private static final float TURN_SPEED = 160;
    private static final float GRAVITY = -50;
    private static final float JUMP_POWER = 3;
    private static final float TERRAIN_HEIGHT = 0;


    private float currentSpeed = 0;
    private float currentTurnSpeed = 0;
    private float upwardsSpeed = 0;

    private boolean isInAir = false;


    public Player(GLTF gltf, Vector3f position, float rotX, float rotY, float rotZ, float scale) {
        super(gltf);
        super.setPosition(position);
        super.setRotX(rotX);
        super.setRotY(rotY);
        super.setRotZ(rotZ);
        super.setScale(scale);
        super.increasePosition(0, 0, 0);
    }

//    public void update(Terrain terrain) {
//        //checkInputs();
//        super.increaseRotation(0, currentTurnSpeed * EngineManager.getFrameTimeSeconds(), 0);
//        float distance = currentSpeed * EngineManager.getFrameTimeSeconds();
//
//        float dx = (float) (distance * Math.sin(Math.toRadians(super.getRotY())));
//        float dz = (float) (distance * Math.cos(Math.toRadians(super.getRotY())));
//        super.increasePosition(dx, 0, dz);
//
//        upwardsSpeed += GRAVITY * EngineManager.getFrameTimeSeconds();
//        super.increasePosition(0, upwardsSpeed * EngineManager.getFrameTimeSeconds(), 0);
//        float terrainHeight = terrain.getHeightOfTerrain(super.getPosition().x, super.getPosition().z);
////		System.out.println("Position: " +this.getPosition().left + " " + this.getPosition().top + " " + this.getPosition().z);
//        if (super.getPosition().y < terrainHeight) {
//            upwardsSpeed = 0;
//            isInAir = false;
//            super.getPosition().y = terrainHeight;
//        }
//    }
//
//    public void jump() {
//        if (!isInAir) {
//            this.upwardsSpeed = JUMP_POWER;
//            isInAir = true;
//        }
//    }

    public void key(int key, int scancode, int action, int mods) {

        if (key == GLFW_KEY_W && (action == GLFW_PRESS || action == Glfw.GLFW_REPEAT)) {
            this.currentSpeed = RUN_SPEED;
        } else if (key == GLFW_KEY_S && (action == GLFW_PRESS || action == Glfw.GLFW_REPEAT)) {
            this.currentSpeed = -RUN_SPEED;
        } else {
            this.currentSpeed = 0;
        }

        if (key == GLFW_KEY_D && (action == GLFW_PRESS || action == Glfw.GLFW_REPEAT)) {
            this.currentTurnSpeed = -TURN_SPEED;
        } else if (key == GLFW_KEY_A && (action == GLFW_PRESS || action == Glfw.GLFW_REPEAT)) {
            this.currentTurnSpeed = TURN_SPEED;
        } else {
            this.currentTurnSpeed = 0;
        }

        if (key == Glfw.GLFW_KEY_SPACE && action == GLFW_PRESS) {
            jump();
        }
    }

    public void turnLeft() {
        this.currentTurnSpeed = -TURN_SPEED;
    }

    public void turnRight() {
        this.currentTurnSpeed = TURN_SPEED;
    }

    public void moveForward() {
        this.currentSpeed = RUN_SPEED;
    }

    public void moveBack() {
        this.currentSpeed = -RUN_SPEED;
    }

    public void moveStop() {
        this.currentSpeed = 0;
    }

    public void turnStop() {
        this.currentTurnSpeed = 0;
    }


    public void move(Terrain terrain) {
        if (currentTurnSpeed != 0) rotateAnimatedPlayer();
        if (currentSpeed != 0) {
            float distance = currentSpeed * EngineManager.getFrameTimeSeconds();
            float dx = (float) (distance * Math.sin(Math.toRadians(super.getRotY())));
            float dz = (float) (distance * Math.cos(Math.toRadians(super.getRotY())));
            super.increasePosition(dx, 0, dz);
            setClipIndex(1);
            upwardsSpeed += GRAVITY * EngineManager.getFrameTimeSeconds();
            super.increasePosition(0, upwardsSpeed * EngineManager.getFrameTimeSeconds(), 0);
            float terrainHeight = terrain.getHeightOfTerrain(super.getPosition().x, super.getPosition().z);
            if (super.getPosition().y < terrainHeight) {
                upwardsSpeed = 0;
                isInAir = false;
                super.getPosition().y = terrainHeight;
            }
        } else {
            setClipIndex(0);
        }
    }

    public void jump() {
        if (!isInAir) {
            this.upwardsSpeed = JUMP_POWER;
            isInAir = true;
            setClipIndex(0); // Dont want to model to look like its running in the air. Would have applied a jumping animation here.
        }
    }

    private void rotateAnimatedPlayer() {
        float turnSpeed = currentTurnSpeed * EngineManager.getFrameTimeSeconds();

        super.increaseRotation(0, turnSpeed, 0);
    }
//	private void checkInputs() {
//		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
//			this.currentSpeed = RUN_SPEED;
//		} else if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
//			this.currentSpeed = -RUN_SPEED;
//		} else {
//			this.currentSpeed = 0;
//		}
//	
//		if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
//			this.currentTurnSpeed = -TURN_SPEED;
//		} else if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
//			this.currentTurnSpeed = TURN_SPEED;
//		} else {
//			this.currentTurnSpeed = 0;
//		}
//		
//		if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
//			jump();
//		}
//		
//	}


    public Object clone() {
        try {
            Player p = (Player) super.clone();
            p.position = new Vector3f(position);
            p.transform = new Matrix4f(transform);
            p.animations = new ArrayList<>();
            p.animations.addAll(animations);
            return p;
        } catch (Exception e) {
        }
        return null;
    }

}
