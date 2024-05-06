package com.magneticnorth.steering;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ai.GdxAI;
import com.badlogic.gdx.ai.steer.behaviors.*;
import com.badlogic.gdx.ai.steer.utils.RayConfiguration;
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
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
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
	Box2dRaycastCollisionDetector collisionDetector;
	PhysicsUtils physicsUtils;
	SteeringInputProcessor inputProcessor;
	Location<Vector2> currentTarget;
	GameState gs;
	Viewport physicsViewport;
	Viewport uiViewport;
	String rayType = "Parallel";
	RayConfigurationBase<Vector2> rayConfig;

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

		hudCamera = new OrthographicCamera();
		physicsCamera = new OrthographicCamera();

		physicsViewport = new FitViewport(20, 20 / (16f/9f), physicsCamera);
		uiViewport = new FitViewport(1920, 1080, hudCamera);

		hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		physicsCamera.setToOrtho(false, 20, 20 / (16f/9f));
		hudCamera.position.set(hudCamera.viewportWidth / 2.0f, hudCamera.viewportHeight / 2.0f, 1.0f);
		physicsCamera.position.set(physicsCamera.viewportWidth / 2f, physicsCamera.viewportHeight / 2f, 1.0f);
		physicsCamera.update();
		hudCamera.update();

		physicsUtils = new PhysicsUtils(physicsWorld, physicsCamera);

		// Collision detector using Box2D
		collisionDetector = new Box2dRaycastCollisionDetector(physicsWorld);

		font = new BitmapFont();
		font.getData().setScale(1);

		primaryBody = physicsUtils.createCircleBody(physicsCamera.viewportWidth / 2f, physicsCamera.viewportHeight / 2f, 0.2f, BodyDef.BodyType.DynamicBody);
		targetBody = physicsUtils.createCircleBody(physicsCamera.viewportWidth / 2f, physicsCamera.viewportHeight / 2f, 0.2f, BodyDef.BodyType.DynamicBody);

		primaryAgent = new SteeringAgent(primaryBody, 5);
		targetAgent = new SteeringAgent(targetBody, 5);

		// Set the target to wander
		targetAgent.steeringBehavior = SteeringPresets.getWander(targetAgent);

		gs = new GameState(targetAgent, hudCamera, physicsCamera, physicsUtils);

		inputProcessor = new SteeringInputProcessor(gs);
		Gdx.input.setInputProcessor(inputProcessor);

		rayConfig = new ParallelSideRayConfiguration<>(primaryAgent, 2, 0.2f);
	}

	@Override
	public void render () {
		ScreenUtils.clear(0, 0, 0, 1);
		physicsCamera.update();
		hudCamera.update();

		physicsViewport.apply();
		uiViewport.apply();

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
		font.draw(batch, "P: Seek + Obstacle Avoidance", 0, Gdx.graphics.getHeight() - 160);
		font.draw(batch, "Current Behavior: " + currentBehavior, 0, Gdx.graphics.getHeight() - 180);
		font.draw(batch, "Current Ray Type: " + rayType, 0, Gdx.graphics.getHeight() - 200);
		font.draw(batch, "Target: (" + gs.currentTarget.getPosition().x + ", " + gs.currentTarget.getPosition().y + ")", 0, Gdx.graphics.getHeight() - 220);
		font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 0, Gdx.graphics.getHeight() - 240);
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
		debugRenderer.render(physicsWorld, physicsCamera.combined);
	}

	public void resize(int width, int height) {
		physicsViewport.update(width, height, true);
		uiViewport.update(width, height, true);
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		img.dispose();
	}
}
