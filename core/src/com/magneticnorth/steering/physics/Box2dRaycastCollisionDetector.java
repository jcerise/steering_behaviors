package com.magneticnorth.steering.physics;

import com.badlogic.gdx.ai.utils.Collision;
import com.badlogic.gdx.ai.utils.Ray;
import com.badlogic.gdx.ai.utils.RaycastCollisionDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

public class Box2dRaycastCollisionDetector implements RaycastCollisionDetector<Vector2> {

    private World world;
    private RaycastCallback callback;

    public Box2dRaycastCollisionDetector(World world) {
        this.world = world;
        this.callback = new RaycastCallback();
    }

    @Override
    public boolean collides(Ray<Vector2> ray) {
        callback.collided = false;
        if (!ray.start.equals(ray.end)){
            world.rayCast(callback, ray.start, ray.end);
            return callback.collided;
        } else {
            return false;
        }
    }

    @Override
    public boolean findCollision(Collision<Vector2> outputCollision, Ray<Vector2> inputRay) {
        callback.collided = false;
        if (!inputRay.start.equals(inputRay.end)) {
            world.rayCast(callback, inputRay.start, inputRay.end);
            if (callback.collided) {
                outputCollision.point.set(callback.point);
                outputCollision.normal.set(callback.normal);
            }
            return callback.collided;
        } else {
            return false;
        }
    }
}
