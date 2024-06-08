package com.magneticnorth.steering.physics;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.behaviors.*;
import com.badlogic.gdx.ai.steer.utils.rays.RayConfigurationBase;
import com.badlogic.gdx.ai.utils.RaycastCollisionDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class SteeringPresets {
    public static final float PIXELS_TO_METERS = 1f / 64f;
    public static Wander<Vector2> getWander(Steerable steerable){
        return new Wander<Vector2>(steerable)
                .setWanderOffset(50f) // distance away from entity to set target
                .setWanderOrientation(0f) // the initial orientation
                .setWanderRadius(15f) // size of target
                .setWanderRate(MathUtils.PI2 * 4)
                .setFaceEnabled(true);
    }

    public static Seek<Vector2> getSeek(Steerable steerable, Steerable<Vector2> target) {
        return new Seek<Vector2>(steerable, target);
    }

    public static Arrive<Vector2> getArrive(Steerable steerable, Steerable<Vector2> target) {
        return new Arrive<Vector2>(steerable, target);
    }

    public static Flee<Vector2> getFlee(Steerable steerable, Steerable<Vector2> target) {
        return new Flee<Vector2>(steerable, target);
    }

    public static ReachOrientation<Vector2> getReachOrientation(Steerable steerable, Steerable<Vector2> target) {
        return new ReachOrientation<>(steerable, target);
    }

    public static Face<Vector2> getFace(Steerable steerable, Steerable<Vector2> target) {
        return new Face<>(steerable, target);
    }

    public static Pursue<Vector2> getPursue(Steerable steerable, Steerable<Vector2> target) {
        return new Pursue<>(steerable, target);
    }

    public static Evade<Vector2> getEvade(Steerable steerable, Steerable<Vector2> target) {
        return new Evade<>(steerable, target);
    }

    public static RaycastObstacleAvoidance<Vector2> getObstacleAvoidance(Steerable steerable, RayConfigurationBase<Vector2> rayConfig, RaycastCollisionDetector collisionDetector, int distance) {
        return new RaycastObstacleAvoidance<>(steerable,  rayConfig, collisionDetector, distance);
    }

    public static PrioritySteering<Vector2> getPrioritySteering(Steerable steerable, float threshold, Steerable<Vector2> target, RayConfigurationBase<Vector2> rayConfig, RaycastCollisionDetector collisionDetector, int distance) {
        PrioritySteering<Vector2> pSteer = new PrioritySteering<Vector2>(steerable, threshold);
        Seek<Vector2> seek = getSeek(steerable, target);
        RaycastObstacleAvoidance<Vector2> obsAvoid = getObstacleAvoidance(steerable, rayConfig, collisionDetector, distance);
        pSteer.add(obsAvoid);
        pSteer.add(seek);
        return pSteer;
    }

    public static BlendedSteering<Vector2> getBlendedSteering(Steerable steerable, Steerable<Vector2> target, RayConfigurationBase<Vector2> rayConfig, RaycastCollisionDetector collisionDetector, int distance) {
        BlendedSteering<Vector2> pSteer = new BlendedSteering<Vector2>(steerable);
        Seek<Vector2> seek = getSeek(steerable, target);
        RaycastObstacleAvoidance<Vector2> obsAvoid = getObstacleAvoidance(steerable, rayConfig, collisionDetector, distance);
        pSteer.add(obsAvoid, 1);
        pSteer.add(seek, 0.2f);
        return pSteer;
    }
}
