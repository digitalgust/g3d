package org.mini.g3d.animation;

import org.mini.g3d.core.EngineManager;
import org.mini.g3d.terrain.Terrain;

public class AnimatedPlayer extends AnimatedModel {

    private static final float RUN_SPEED = 20;
    private static final float TURN_SPEED = 160;
    private static final float GRAVITY = -50;
    private static final float JUMP_POWER = 30;
    private static final float TERRAIN_HEIGHT = 0;

    Animation currentAnimation = null;

    private float currentSpeed = 0;
    private float currentTurnSpeed = 0;
    private float upwardsSpeed = 0;

    private boolean isInAir = false;

    private Animation runAnimation;
    private Animation standingAnimation;

    /**
     * Constructor for Animated Player
     *
     * @param model
     * @param run
     */
    public AnimatedPlayer(AnimatedModel model, Animation run) {
        super(model.getModel().getRawModel(), model.getModel().getTexture(), model.getRootJoint(), model.getJointCount(), model.getPosition(), model.getRotX(), model.getRotY(), model.getRotX(), model.getScale());
        this.runAnimation = run;
    }

    public void move(Terrain terrain) {
        rotateAnimatedPlayer();
        float distance = currentSpeed * EngineManager.getFrameTimeSeconds();
        float dx = (float) (distance * Math.sin(Math.toRadians(super.getRotY())));
        float dz = (float) (distance * Math.cos(Math.toRadians(super.getRotY())));
        super.increasePosition(dx, 0, dz);
        upwardsSpeed += GRAVITY * EngineManager.getFrameTimeSeconds();
        super.increasePosition(0, upwardsSpeed * EngineManager.getFrameTimeSeconds(), 0);
        float terrainHeight = terrain.getHeightOfTerrain(super.getPosition().x, super.getPosition().z);

        if (super.getPosition().y < terrainHeight) {
            upwardsSpeed = 0;
            isInAir = false;
            super.getPosition().y = terrainHeight;
            setAnimation(runAnimation);
        }
    }

    public void jump() {
        if (!isInAir) {
            this.upwardsSpeed = JUMP_POWER;
            isInAir = true;
            setAnimation(null); // Dont want to model to look like its running in the air. Would have applied a jumping animation here.
        }
    }

    private void setAnimation(Animation animation) {
        if (currentAnimation != animation) {
            currentAnimation = animation;
            doAnimation(animation);
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

    /**
     * Rotates the camera along with the animated model. Was thinking that only
     * calling the to super class would rotate the animated model aswell. But it
     * only rotated the camera. So have to rotate the model aswell. And now they
     * both rotate correctly when the user sends inputs
     */
    private void rotateAnimatedPlayer() {
        float turnSpeed = currentTurnSpeed * EngineManager.getFrameTimeSeconds();

        super.increaseRotation(0, turnSpeed, 0);
    }

    /**
     * Tells the superclass to animated the model to do animation
     */
    public void doAnimation(Animation animation) {
        super.doAnimation(animation);
    }

    /**
     * Tells the superclass to animated the model to update the pose
     */
    public void update() {
        super.update();
    }

}
