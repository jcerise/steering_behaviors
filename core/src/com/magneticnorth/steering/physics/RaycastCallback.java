package com.magneticnorth.steering.physics;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;

public class RaycastCallback implements RayCastCallback {
    Vector2 point = new Vector2();
    Vector2 normal = new Vector2();
    boolean collided;

    @Override
    public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
        this.point.set(point);
        this.normal.set(normal);
        collided = true;
        return fraction;
    }
}
