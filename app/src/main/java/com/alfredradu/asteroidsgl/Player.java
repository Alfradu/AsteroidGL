package com.alfredradu.asteroidsgl;

import android.opengl.GLES20;

import com.alfredradu.asteroidsgl.utils.Utils;

public class Player extends GLEntity {
    private static final String TAG = "Player";
    private static final float ROTATION_VELOCITY = 360f;
    private static final float THRUST = 4f;
    private static final float DRAG = 0.99f;
    private static final int TIMER = 80;
    final int LIVES = 3;
    private static int _lives;
    private boolean _invincible = false;
    private int _resetCounter = 0;
    private Boolean _hasShot = false;
    private Boolean _boosting = false;
    public int _cooldown = 100;

    public Player(float x, float y){
        super();
        _lives = LIVES;
        _game.updateLives(_lives);
        _game.updateCharge(_cooldown);
        _x = x;
        _y = y;
        _width = 8f;
        _height = 10f;
        _scale = 0.4f;
        float vertices[] = {
                0.0f, 0.5f, 0.0f,
                -0.5f, -0.5f, 0.0f,
                0.5f, -0.5f, 0.0f
        };
        _mesh = new Mesh(vertices, GLES20.GL_TRIANGLES);
        _mesh.setWidthHeight(_width, _height);
        _mesh.flipY();
    }

    public boolean isColliding(final GLEntity that){
        return areBoundingSpheresOverlapping(this, that);
    }

    @Override
    public void onCollision(final GLEntity that) {
        setVel(that._velX,that._velY);
        if (!_invincible){
            _invincible = true;
            _lives--;
            _game.updateLives(_lives);
            _resetCounter = TIMER;
        }
        if (_lives < 1) {
            _boosting = false;
            _isAlive = false;
        }
    }
    public void setPos(float x, float y){
        _x = x;
        _y = y;
    }

    public void setVel(float vx, float vy){
        _velX = vx;
        _velY = vy;
    }

    private void flash(){
        if (_resetCounter%20==0){
            setColors(1, 1, 1, 1);
        } else {
            setColors(1.0f, 0, 1,1);
        }
    }

    public Boolean isBoosting(){
        return _boosting;
    }

    public void reset(){
        _lives = LIVES;
        _game.updateLives(_lives);
        _cooldown = 100;
        _game.updateCharge(_cooldown);
        _isAlive = true;
        setVel(0,0);
        _rotation = 0f;
        setColors(1, 1, 1, 1);
    }

    @Override
    public void update(double dt) {
        _rotation += (dt*ROTATION_VELOCITY) * _game._inputs._horizontalFactor;
        if(_game._inputs._pressingB){
            _boosting = true;
            final float theta = _rotation*(float)Utils.TO_RAD;
            _velX += (float)Math.sin(theta) * THRUST;
            _velY -= (float)Math.cos(theta) * THRUST;
        } else {
            _boosting = false;
        }
        _velX *= DRAG;
        _velY *= DRAG;

        if(_game._inputs._pressingA && !_hasShot){
            if(_game.maybeFireBullet(this)){ _hasShot = true; }
        }else if (!_game._inputs._pressingA) {
            _hasShot = false;
        }

        if(_game._inputs._pressingC && _cooldown > 99){
            _game.warp();
            _cooldown = 0;
        }
        if (_cooldown < 100){
            _cooldown++;
            _game.updateCharge(_cooldown);
        }

        if (_invincible) {
            _resetCounter--;
            if (_resetCounter%10==0){
                flash();
            }
            if (_resetCounter < 0) {
                _invincible = false;
                _resetCounter = 0;
            }
        }
        super.update(dt);
    }

    @Override
    public void render(float[] viewportMatrix) {
        super.render(viewportMatrix);
    }
}
