package com.magneticnorth.steering;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.magneticnorth.steering.physics.PhysicsUtils;

public class GameState {

    Steerable<Vector2> currentTarget;
    OrthographicCamera worldCamera;
    OrthographicCamera physicsCamera;
    PhysicsUtils utils;

    public GameState(Steerable<Vector2> currentTarget, OrthographicCamera worldCamera, OrthographicCamera physicsCamera, PhysicsUtils utils) {
        this.currentTarget = currentTarget;
        this.worldCamera = worldCamera;
        this.physicsCamera = physicsCamera;
        this.utils = utils;
    }

    public Location<Vector2> getCurrentTarget() {
        return currentTarget;
    }

    public void setCurrentTarget(Steerable<Vector2> currentTarget) {
        this.currentTarget = currentTarget;
    }

    public OrthographicCamera getWorldCamera() {
        return worldCamera;
    }

    public void setWorldCamera(OrthographicCamera worldCamera) {
        this.worldCamera = worldCamera;
    }

    public OrthographicCamera getPhysicsCamera() {
        return physicsCamera;
    }

    public void setPhysicsCamera(OrthographicCamera physicsCamera) {
        this.physicsCamera = physicsCamera;
    }

    public PhysicsUtils getUtils() {
        return utils;
    }

    public void setUtils(PhysicsUtils utils) {
        this.utils = utils;
    }
}
