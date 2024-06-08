package com.magneticnorth.steering;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ai.GdxAI;
import com.badlogic.gdx.ai.steer.behaviors.*;
import com.badlogic.gdx.ai.steer.utils.rays.CentralRayWithWhiskersConfiguration;
import com.badlogic.gdx.ai.steer.utils.rays.ParallelSideRayConfiguration;
import com.badlogic.gdx.ai.steer.utils.rays.RayConfigurationBase;
import com.badlogic.gdx.ai.steer.utils.rays.SingleRayConfiguration;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.magneticnorth.steering.input.SteeringInputProcessor;
import com.magneticnorth.steering.physics.Box2dRaycastCollisionDetector;
import com.magneticnorth.steering.physics.PhysicsUtils;
import com.magneticnorth.steering.physics.SteeringAgent;
import com.magneticnorth.steering.physics.SteeringPresets;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class GDXSteeringTest extends ApplicationAdapter {
	SpriteBatch batch;
	Texture img;
	private World physicsWorld;
	private Box2DDebugRenderer debugRenderer;
	private OrthographicCamera gameCamera;
	private static final float MAX_STEP_TIME = 1/60f;
	public static final float PIXELS_PER_METER = 10f;
	SteeringAgent primaryAgent;
	SteeringAgent targetAgent;
	Body primaryBody;
	Body targetBody;
	BitmapFont font;
	String currentBehavior = "None";
	Box2dRaycastCollisionDetector collisionDetector;
	PhysicsUtils physicsUtils;
	SteeringInputProcessor inputProcessor;
	Location<Vector2> currentTarget;
	GameState gs;
	Viewport physicsViewport;
	Viewport gameViewport;
	String rayType = "Parallel";
	RayConfigurationBase<Vector2> rayConfig;
	public final float WORLD_WIDTH = 600;
	public final float WORLD_HEIGHT = 400;

	private static final Set<Class<?>> classesWithSetTarget;

	static {
		classesWithSetTarget = new HashSet<>();
		classesWithSetTarget.add(Arrive.class);
		classesWithSetTarget.add(Seek.class);
		classesWithSetTarget.add(Wander.class);
		classesWithSetTarget.add(Flee.class);
		classesWithSetTarget.add(ReachOrientation.class);
		classesWithSetTarget.add(Face.class);
		classesWithSetTarget.add(Pursue.class);
		classesWithSetTarget.add(Evade.class);
	}

	@Override
	public void create () {
		batch = new SpriteBatch();
		img = new Texture("badlogic.jpg");
			debugRenderer = new Box2DDebugRenderer();

		physicsWorld = new World(new Vector2(0, 0.0f), true);

		gameCamera = new OrthographicCamera();
		gameViewport = new FillViewport(WORLD_WIDTH, WORLD_HEIGHT, gameCamera);
		gameCamera.position.set(WORLD_WIDTH/ 2.0f, WORLD_HEIGHT / 2.0f, 0.0f);
		gameCamera.update();

		physicsUtils = new PhysicsUtils(physicsWorld, gameCamera);

		// Collision detector using Box2D
		collisionDetector = new Box2dRaycastCollisionDetector(physicsWorld);

		font = new BitmapFont();
		font.getData().setScale(0.5f);

		primaryBody = physicsUtils.createCircleBody(WORLD_WIDTH / 2.0f, WORLD_HEIGHT / 2.0f, 10.0f, BodyDef.BodyType.DynamicBody);
		targetBody = physicsUtils.createCircleBody(WORLD_WIDTH / 2.0f, WORLD_HEIGHT / 2.0f, 10.0f, BodyDef.BodyType.DynamicBody);

		primaryAgent = new SteeringAgent(primaryBody, 50);
		targetAgent = new SteeringAgent(targetBody, 50);

		// Set the target to wander
		targetAgent.steeringBehavior = SteeringPresets.getWander(targetAgent);

		gs = new GameState(targetAgent, gameCamera, gameCamera, physicsUtils);

		inputProcessor = new SteeringInputProcessor(gs);
		Gdx.input.setInputProcessor(inputProcessor);

		rayConfig = new ParallelSideRayConfiguration<>(primaryAgent, 2, 0.2f);
	}

	@Override
	public void render () {
		ScreenUtils.clear(0, 0, 0, 1);
		gameCamera.update();

		gameViewport.apply();

		batch.setProjectionMatrix(gameCamera.combined);
		batch.begin();
		font.draw(batch, "Steering Behaviors",0, WORLD_HEIGHT - 32);
		font.draw(batch, "Controls:", 0, WORLD_HEIGHT - 42);
		font.draw(batch, "1: Wander", 10, WORLD_HEIGHT - 52);
		font.draw(batch, "2: Arrive", 10, WORLD_HEIGHT - 62);
		font.draw(batch, "3: Seek", 10, WORLD_HEIGHT - 72);
		font.draw(batch, "4: Flee", 10, WORLD_HEIGHT - 82);
		font.draw(batch, "5: Reach Orientation", 10, WORLD_HEIGHT - 92);
		font.draw(batch, "6: Face", 10, WORLD_HEIGHT - 102);
		font.draw(batch, "7: Pursue", 10, WORLD_HEIGHT - 112);
		font.draw(batch, "8: Evade", 10, WORLD_HEIGHT - 122);
		font.draw(batch, "9: None/Idle", 10, WORLD_HEIGHT - 132);
		font.draw(batch, "P: Seek + Obstacle Avoidance", 10, WORLD_HEIGHT - 142);
		font.draw(batch, "Current Behavior: " + currentBehavior, 0, WORLD_HEIGHT - 152);
		font.draw(batch, "Current Ray Type: " + rayType, 0, WORLD_HEIGHT - 162);
		font.draw(batch, "Target: (" + gs.currentTarget.getPosition().x + ", " + gs.currentTarget.getPosition().y + ")", 0, WORLD_HEIGHT - 172);
		font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 0, WORLD_HEIGHT - 182);
		batch.end();
		GdxAI.getTimepiece().update(Gdx.graphics.getDeltaTime());

		physicsWorld.step(MAX_STEP_TIME, 6, 2);

		primaryAgent.update(Gdx.graphics.getDeltaTime());
		targetAgent.update(Gdx.graphics.getDeltaTime());

		RayConfigurationBase<Vector2> rayConfig = new SingleRayConfiguration<>(primaryAgent, 2);

		if (Gdx.input.isKeyPressed(Input.Keys.NUM_1)) {
			primaryAgent.steeringBehavior = SteeringPresets.getWander(primaryAgent);
			currentBehavior = "Wander";
		}
		if (Gdx.input.isKeyPressed(Input.Keys.NUM_2)) {
			primaryAgent.steeringBehavior = SteeringPresets.getArrive(primaryAgent, gs.currentTarget);
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
		if (Gdx.input.isKeyPressed(Input.Keys.NUM_0)) {
			primaryAgent.steeringBehavior = SteeringPresets.getObstacleAvoidance(primaryAgent, rayConfig, collisionDetector, 5);
			currentBehavior = "Obstacle Avoidance";
		}
		if (Gdx.input.isKeyPressed(Input.Keys.C)) {
			primaryAgent.steeringBehavior = SteeringPresets.getPrioritySteering(primaryAgent, 0.001f, gs.currentTarget, rayConfig, collisionDetector, 2);
			currentBehavior = "Seek with Obstacle Avoidance";
		}

		if (Gdx.input.isKeyPressed(Input.Keys.S)) {
			rayConfig = new SingleRayConfiguration<>(primaryAgent, 2);
			rayType = "Single";
		}
		if (Gdx.input.isKeyPressed(Input.Keys.P)) {
			rayConfig = new ParallelSideRayConfiguration<>(primaryAgent, 2, 0.2f);
			rayType = "Parallel";
		}
		if (Gdx.input.isKeyPressed(Input.Keys.W)) {
			rayConfig = new CentralRayWithWhiskersConfiguration<>(primaryAgent, 2, 0.2f, 5);
			rayType = "Central with Whiskers, whisker angle: 5 degrees";
		}


		// Update the current target
		if (primaryAgent.steeringBehavior != null) {
			if (classesWithSetTarget.contains(primaryAgent.steeringBehavior.getClass())) {
				try {
					Method setTargetMethod = primaryAgent.steeringBehavior.getClass().getMethod("setTarget", Location.class);
					setTargetMethod.invoke(primaryAgent.steeringBehavior, gs.currentTarget);
				} catch (NoSuchMethodException e) {
					System.out.println("Method not found");
				} catch (IllegalAccessException e) {
					System.out.println("Illegal access");
				} catch (InvocationTargetException e) {
					System.out.println("Invocation error");
				}
			}
		}
		debugRenderer.render(physicsWorld, gameCamera.combined);
	}

	public void resize(int width, int height) {
		gameViewport.update(width, height, true);
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		img.dispose();
	}
}
