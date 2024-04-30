package com.magneticnorth.steering.physics;

import com.badlogic.gdx.physics.box2d.*;

import static com.magneticnorth.steering.GDXSteeringTest.PIXELS_PER_METER;

public class PhysicsUtils {

    /**
     * Creates a rectangular body in the given world at the specified position and with the specified dimensions.
     *
     * @param world The world in which to create the body.
     * @param x The x-coordinate of the body's position.
     * @param y The y-coordinate of the body's position.
     * @param w The width of the body.
     * @param h The height of the body.
     * @param dynamic A boolean indicating whether the body should be dynamic (true) or static (false).
     * @return The created body.
     */
    public Body createRectBody(World world, float x, float y, float w, float h, BodyDef.BodyType type) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = type;
        bodyDef.position.set(x, y);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox((w / 2), h / 2);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.friction = 1.0f;

        Body body = world.createBody(bodyDef);
        body.createFixture(fixtureDef);
        body.setAngularDamping(1f);
        body.setLinearDamping(1f);

        shape.dispose();

        return body;
    }

    /**
     * Creates a rectangular body in the given world at the specified position and with the specified dimensions.
     *
     * @param world The world in which to create the body.
     * @param x The x-coordinate of the body's position.
     * @param y The y-coordinate of the body's position.
     * @param dynamic A boolean indicating whether the body should be dynamic (true) or static (false).
     * @return The created body.
     */
    public Body createCircleBody(World world, float x, float y, float radius, BodyDef.BodyType type) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = type;
        bodyDef.position.set(x, y);

        CircleShape shape = new CircleShape();
        shape.setRadius(0.2f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.friction = 1.0f;

        Body body = world.createBody(bodyDef);
        body.createFixture(fixtureDef);
        body.setAngularDamping(1f);
        body.setLinearDamping(1f);

        shape.dispose();

        return body;
    }
}
