package com.magneticnorth.steering;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ai.GdxAI;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ScreenUtils;
import com.magneticnorth.steering.physics.PhysicsUtils;
import com.magneticnorth.steering.physics.SteeringAgent;
import com.magneticnorth.steering.physics.SteeringPresets;

public class GDXSteeringTest extends ApplicationAdapter {
	SpriteBatch batch;
	Texture img;
	private World physicsWorld;
	private Box2DDebugRenderer debugRenderer;
	private OrthographicCamera hudCamera;
	private OrthographicCamera physicsCamera;
	private static final float MAX_STEP_TIME = 1/60f;
	public static final float PIXELS_PER_METER = 33f;
	SteeringAgent primaryAgent;
	SteeringAgent targetAgent;
	Body primaryBody;
	Body targetBody;
	BitmapFont font;
	String currentBehavior = "None";
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		img = new Texture("badlogic.jpg");
		debugRenderer = new Box2DDebugRenderer();

		physicsWorld = new World(new Vector2(0, 0.0f), true);

		hudCamera = new OrthographicCamera();
		physicsCamera = new OrthographicCamera();

		hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		physicsCamera.setToOrtho(false, 20, 20 / (4f/3f));
		hudCamera.position.set(hudCamera.viewportWidth / 2.0f, hudCamera.viewportHeight / 2.0f, 1.0f);
		physicsCamera.position.set(physicsCamera.viewportWidth / 2f, physicsCamera.viewportHeight / 2f, 1.0f);
		physicsCamera.update();
		hudCamera.update();

		font = new BitmapFont();
		font.getData().setScale(1);

		primaryBody = new PhysicsUtils().createCircleBody(physicsWorld, physicsCamera.viewportWidth / 2f, physicsCamera.viewportHeight / 2f, 1f, BodyDef.BodyType.DynamicBody);
		targetBody = new PhysicsUtils().createCircleBody(physicsWorld, physicsCamera.viewportWidth / 2f, physicsCamera.viewportHeight / 2f, 1f, BodyDef.BodyType.DynamicBody);

		primaryAgent = new SteeringAgent(primaryBody, 5);
		targetAgent = new SteeringAgent(targetBody, 5);

		// Set the target to wander
		targetAgent.steeringBehavior = SteeringPresets.getWander(targetAgent);
	}

	@Override
	public void render () {
		ScreenUtils.clear(0, 0, 0, 1);
		physicsCamera.update();
		hudCamera.update();
		ShapeRenderer shapeRenderer = new ShapeRenderer();
		shapeRenderer.setProjectionMatrix(physicsCamera.combined);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
		shapeRenderer.setColor(Color.RED); // Change as needed
		shapeRenderer.rect(physicsCamera.position.x - (physicsCamera.viewportWidth / 2f),
				physicsCamera.position.y - (physicsCamera.viewportHeight / 2f),
				physicsCamera.viewportWidth,
				physicsCamera.viewportHeight);
		shapeRenderer.end();

		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
		shapeRenderer.setColor(Color.RED);
		shapeRenderer.rect(100, 100, 100, 100);
		shapeRenderer.end();

		batch.setProjectionMatrix(hudCamera.combined);
		batch.begin();
		font.draw(batch, "Steering Behaviors", 0, Gdx.graphics.getHeight());
		font.draw(batch, "Controls:", 0, Gdx.graphics.getHeight() - 14);
		font.draw(batch, "1: Wander", 0, Gdx.graphics.getHeight() - 28);
		font.draw(batch, "2: Arrive", 0, Gdx.graphics.getHeight() - 42);
		font.draw(batch, "3: Seek", 0, Gdx.graphics.getHeight() - 56);
		font.draw(batch, "4: Flee", 0, Gdx.graphics.getHeight() - 70);
		font.draw(batch, "5: Reach Orientation", 0, Gdx.graphics.getHeight() - 84);
		font.draw(batch, "6: Face", 0, Gdx.graphics.getHeight() - 98);
		font.draw(batch, "7: Pursue", 0, Gdx.graphics.getHeight() - 112);
		font.draw(batch, "8: Evade", 0, Gdx.graphics.getHeight() - 126);
		font.draw(batch, "9: None/Idle", 0, Gdx.graphics.getHeight() - 140);
		font.draw(batch, "Current Behavior: " + currentBehavior, 0, Gdx.graphics.getHeight() - 160);
		font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 0, Gdx.graphics.getHeight() - 180);
		batch.end();
		GdxAI.getTimepiece().update(Gdx.graphics.getDeltaTime());

		physicsWorld.step(MAX_STEP_TIME, 6, 2);

		primaryAgent.update(Gdx.graphics.getDeltaTime());
		targetAgent.update(Gdx.graphics.getDeltaTime());

		if (Gdx.input.justTouched()) {
			Vector3 touchPoint = new Vector3();
			touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			physicsCamera.unproject(touchPoint);
			System.out.println("Touch at: (" + touchPoint.x + ", " + touchPoint.y + ")");
			new PhysicsUtils().createRectBody(physicsWorld, touchPoint.x, touchPoint.y, 1, 1, BodyDef.BodyType.StaticBody);
		}

		if (Gdx.input.isKeyPressed(Input.Keys.NUM_1)) {
			primaryAgent.steeringBehavior = SteeringPresets.getWander(primaryAgent);
			currentBehavior = "Wander";
		}
		if (Gdx.input.isKeyPressed(Input.Keys.NUM_2)) {
			primaryAgent.steeringBehavior = SteeringPresets.getArrive(primaryAgent, targetAgent);
			currentBehavior = "Arrive";
		}
		if (Gdx.input.isKeyPressed(Input.Keys.NUM_3)) {
			primaryAgent.steeringBehavior = SteeringPresets.getSeek(primaryAgent, targetAgent);
			currentBehavior = "Seek";
		}
		if (Gdx.input.isKeyPressed(Input.Keys.NUM_4)) {
			primaryAgent.steeringBehavior = SteeringPresets.getFlee(primaryAgent, targetAgent);
			currentBehavior = "Flee";
		}
		if (Gdx.input.isKeyPressed(Input.Keys.NUM_5)) {
			primaryAgent.steeringBehavior = SteeringPresets.getReachOrientation(primaryAgent, targetAgent);
			currentBehavior = "Reach Orientation";
		}
		if (Gdx.input.isKeyPressed(Input.Keys.NUM_6)) {
			primaryAgent.steeringBehavior = SteeringPresets.getFace(primaryAgent, targetAgent);
			currentBehavior = "Face";
		}
		if (Gdx.input.isKeyPressed(Input.Keys.NUM_7)) {
			primaryAgent.steeringBehavior = SteeringPresets.getPursue(primaryAgent, targetAgent);
			currentBehavior = "Pursue";
		}
		if (Gdx.input.isKeyPressed(Input.Keys.NUM_8)) {
			primaryAgent.steeringBehavior = SteeringPresets.getEvade(primaryAgent, targetAgent);
			currentBehavior = "Evade";
		}
		if (Gdx.input.isKeyPressed(Input.Keys.NUM_9)) {
			primaryAgent.steeringBehavior = null;
			currentBehavior = "None/Idle";
		}

		debugRenderer.render(physicsWorld, physicsCamera.combined);
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		img.dispose();
	}
}
