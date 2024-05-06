package com.magneticnorth.steering.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.magneticnorth.steering.GameState;
import com.magneticnorth.steering.physics.PhysicsUtils;
import com.magneticnorth.steering.physics.PointLocation;

public class SteeringInputProcessor implements InputProcessor {

    GameState gs;

    public SteeringInputProcessor(GameState gs) {
        this.gs = gs;
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        OrthographicCamera camera = gs.getPhysicsCamera();
        PhysicsUtils utils = gs.getUtils();
        if (button == Input.Buttons.RIGHT) {
            Vector3 touchPoint = new Vector3();
            touchPoint.set(screenX, screenY, 0);
            camera.unproject(touchPoint);
            utils.createCircleBody(touchPoint.x, touchPoint.y, 1, BodyDef.BodyType.StaticBody);
        } else if (button == Input.Buttons.LEFT) {
            Vector3 touchPoint = new Vector3();
            touchPoint.set(screenX, screenY, 0);
            camera.unproject(touchPoint);
            gs.setCurrentTarget(new PointLocation(new Vector2(touchPoint.x, touchPoint.y)));
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }
}
