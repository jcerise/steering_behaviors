package com.magneticnorth.steering.physics;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.behaviors.*;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class SteeringPresets {
    public static final float PIXELS_TO_METERS = 1f / 64f;
    public static Wander<Vector2> getWander(Steerable steerable){
        return new Wander<Vector2>(steerable)
                .setWanderOffset(1f) // distance away from entity to set target
                .setWanderOrientation(0f) // the initial orientation
                .setWanderRadius(5f) // size of target
                .setWanderRate(MathUtils.PI2 * 4)
                .setFaceEnabled(true);
    }

    public static Seek<Vector2> getSeek(Steerable steerable, Steerable target) {
        return new Seek<Vector2>(steerable, target);
    }

    public static Arrive<Vector2> getArrive(Steerable steerable, Steerable target) {
        return new Arrive<Vector2>(steerable, target);
    }

    public static Flee<Vector2> getFlee(Steerable steerable, Steerable target) {
        return new Flee<Vector2>(steerable, target);
    }

    public static ReachOrientation<Vector2> getReachOrientation(Steerable steerable, Steerable target) {
        return new ReachOrientation<>(steerable, target);
    }

    public static Face<Vector2> getFace(Steerable steerable, Steerable target) {
        return new Face<>(steerable, target);
    }

    public static Pursue<Vector2> getPursue(Steerable steerable, Steerable target) {
        return new Pursue<>(steerable, target);
    }

    public static Evade<Vector2> getEvade(Steerable steerable, Steerable target) {
        return new Evade<>(steerable, target);
    }
}
