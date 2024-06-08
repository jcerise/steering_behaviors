package com.magneticnorth.steering.physics;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class SteeringAgent implements Steerable<Vector2> {

    private static final SteeringAcceleration<Vector2> steeringOutput =
            new SteeringAcceleration<Vector2>(new Vector2());

    Body body;
    Vector2 position;
    float orientation;
    Vector2 linearVelocity;
    float angularVelocity;
    float maxSpeed;
    boolean independentFacing;
    public SteeringBehavior<Vector2> steeringBehavior;
    private final float boundingRadius;
    boolean tagged;
    private float maxLinearSpeed, maxLinearAcceleration;
    private float maxAngularSpeed, maxAngularAcceleration;
    private float zeroLinearSpeedThreshold;
    public static final float PIXELS_TO_METERS = 1f / 64f;
    float scaledAngularSpeed = MathUtils.degreesToRadians * 60;

    public SteeringAgent (Body body, float boundingRadius) {
        this.body = body;
        this.position = this.body.getPosition();
        this.boundingRadius = boundingRadius;
        this.maxLinearSpeed = 25;
        this.maxLinearAcceleration = 50;
        this.maxAngularSpeed = 50f;
        this.maxAngularAcceleration = 50f;
        this.linearVelocity = new Vector2();
        this.independentFacing = true;
    }

    @Override
    public Vector2 getPosition() {
        return this.body.getPosition();
    }

    @Override
    public float getOrientation() {
        return this.orientation;
    }

    @Override
    public void setOrientation(float orientation) {
        this.orientation = orientation;
    }

    // Actual implementation depends on your coordinate system.
    // Here we assume the y-axis is pointing upwards.
    @Override
    public float vectorToAngle (Vector2 vector) {
        return (float)Math.atan2(-vector.x, vector.y);
    }

    // Actual implementation depends on your coordinate system.
    // Here we assume the y-axis is pointing upwards.
    @Override
    public Vector2 angleToVector (Vector2 outVector, float angle) {
        outVector.x = -(float)Math.sin(angle);
        outVector.y = (float)Math.cos(angle);
        return outVector;
    }

    @Override
    public Location<Vector2> newLocation() {
        return null;
    }

    public void update (float delta) {
        if (steeringBehavior != null) {
            // Calculate steering acceleration
            steeringBehavior.calculateSteering(steeringOutput);
            // Apply steering acceleration to move this agent
            applySteering(steeringOutput, delta);
        }
    }

    private void applySteering (SteeringAcceleration<Vector2> steering, float time) {
        if (!(steering == null)) {
            // Update position and linear velocity. Velocity is trimmed to maximum speed
            this.position.mulAdd(linearVelocity, time);
            this.linearVelocity = this.linearVelocity.mulAdd(steering.linear, time).limit(this.getMaxLinearSpeed()).scl(01.0f);

            // Apply angular velocity if necessary
            if (independentFacing) {
                this.angularVelocity = steering.angular * time;
                this.orientation += this.angularVelocity;
            } else {
                // For non-independent facing we have to align orientation to linear velocity
                float newOrientation = calculateOrientationFromLinearVelocity(this.body);
                if (newOrientation != this.orientation) {
                    this.angularVelocity = (newOrientation - this.orientation) * time;
                    this.orientation = newOrientation;
                }
            }

            body.applyForceToCenter(this.linearVelocity, true);
            body.applyTorque(this.angularVelocity, true);
        } else {
            body.setLinearDamping(10.5f);  // tweak value as needed
            body.setAngularDamping(10.5f);  // tweak value as needed
        }
    }

    @Override
    public Vector2 getLinearVelocity() {
        return this.linearVelocity;
    }

    @Override
    public float getAngularVelocity() {
        return this.angularVelocity;
    }

    @Override
    public float getBoundingRadius() {
        return this.boundingRadius;
    }

    @Override
    public boolean isTagged() {
        return this.tagged;
    }

    @Override
    public void setTagged(boolean tagged) {
        this.tagged = tagged;
    }

    @Override
    public float getZeroLinearSpeedThreshold() {
        return this.zeroLinearSpeedThreshold;
    }

    @Override
    public void setZeroLinearSpeedThreshold(float value) {
        this.zeroLinearSpeedThreshold = value;
    }

    @Override
    public float getMaxLinearSpeed() {
        return this.maxLinearSpeed;
    }

    @Override
    public void setMaxLinearSpeed(float maxLinearSpeed) {
        this.maxLinearSpeed = maxLinearSpeed;
    }

    @Override
    public float getMaxLinearAcceleration() {
        return this.maxLinearAcceleration;
    }

    @Override
    public void setMaxLinearAcceleration(float maxLinearAcceleration) {
        this.maxLinearAcceleration = maxLinearAcceleration;
    }

    @Override
    public float getMaxAngularSpeed() {
        return this.maxAngularSpeed;
    }

    @Override
    public void setMaxAngularSpeed(float maxAngularSpeed) {
        this.maxAngularSpeed = maxAngularSpeed;
    }

    @Override
    public float getMaxAngularAcceleration() {
        return this.maxAngularAcceleration;
    }

    @Override
    public void setMaxAngularAcceleration(float maxAngularAcceleration) {
        this.maxAngularAcceleration = maxAngularAcceleration;
    }

    public float calculateOrientationFromLinearVelocity(Body body) {
        Vector2 velocity = body.getLinearVelocity();
        float currentSpeedSquare = velocity.len2();

        // if speed is nearly zero, then we can't determine a direction.
        if(currentSpeedSquare < 0.0001f) {
            return body.getAngle();
        }

        return velocity.angleRad(); // angleRad() calculates the angle in radians.
    }

}
