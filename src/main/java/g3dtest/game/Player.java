package g3dtest.game;

import org.mini.g3d.core.EngineManager;
import org.mini.g3d.entity.Entity;
import org.mini.g3d.core.vector.Vector3f;

import org.mini.g3d.core.models.TexturedModel;
import org.mini.g3d.terrain.Terrain;
import org.mini.glfw.Glfw;
import static org.mini.glfw.Glfw.GLFW_KEY_A;
import static org.mini.glfw.Glfw.GLFW_KEY_D;
import static org.mini.glfw.Glfw.GLFW_KEY_S;
import static org.mini.glfw.Glfw.GLFW_KEY_W;
import static org.mini.glfw.Glfw.GLFW_PRESS;

public class Player extends Entity {

    private static final float RUN_SPEED = 20;
    private static final float TURN_SPEED = 160;
    private static final float GRAVITY = -50;
    private static final float JUMP_POWER = 30;

    private static final float TERRAIN_HEIGHT = 0;

    private float currentSpeed = 0;
    private float currentTurnSpeed = 0;
    private float upwardSpeed = 0;

    private boolean isInAir = false;

    public Player(TexturedModel model, Vector3f position, float rotX, float rotY, float rotZ, float scale) {
        super(model, position, rotX, rotY, rotZ, scale);
        super.increasePosition(0, 0, 0);
    }

    public void move(Terrain terrain) {
        //checkInputs();
        super.increaseRotation(0, currentTurnSpeed * EngineManager.getFrameTimeSeconds(), 0);
        float distance = currentSpeed * EngineManager.getFrameTimeSeconds();

        float dx = (float) (distance * Math.sin(Math.toRadians(super.getRotY())));
        float dz = (float) (distance * Math.cos(Math.toRadians(super.getRotY())));
        super.increasePosition(dx, 0, dz);

        upwardSpeed += GRAVITY * EngineManager.getFrameTimeSeconds();
        super.increasePosition(0, upwardSpeed * EngineManager.getFrameTimeSeconds(), 0);
        float terrainHeight = terrain.getHeightOfTerrain(super.getPosition().x, super.getPosition().z);
//		System.out.println("Position: " +this.getPosition().left + " " + this.getPosition().top + " " + this.getPosition().z);
        if (super.getPosition().y < terrainHeight) {
            upwardSpeed = 0;
            isInAir = false;
            super.getPosition().y = terrainHeight;
        }
    }

    private void jump() {
        if (!isInAir) {
            this.upwardSpeed = JUMP_POWER;
            isInAir = true;
        }
    }

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
}
