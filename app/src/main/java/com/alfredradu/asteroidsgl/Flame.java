package com.alfredradu.asteroidsgl;

import android.opengl.GLES20;

public class Flame extends GLEntity {
    private static final float TO_RADIANS = (float)Math.PI/180.0f;
    private Player _player;
    private float theta = 0f;

    public Flame(GLEntity player){
        _player = (Player)player;
        _x = _player._x;
        _y = _player._y;
        _width = 8f;
        _height = 8f;
        _scale = 0.3f;
        final float[] vertices = {
                -0.25f, 0.5f, 0.0f,
                -0.5f, 0.0f, 0.0f,
                -0.5f, 0.0f, 0.0f,
                -0.25f, 0.0f, 0.0f,
                -0.25f, 0.0f, 0.0f,
                0, -0.25f, 0.0f,
                0, -0.25f, 0.0f,
                0.25f, 0.0f, 0.0f,
                0.25f, 0.0f, 0.0f,
                0.5f, 0.0f, 0.0f,
                0.5f, 0.0f, 0.0f,
                0.25f, 0.5f, 0.0f,
                0.25f, 0.5f, 0.0f,
                -0.25f, 0.5f, 0.0f
        };
        _mesh = new Mesh(vertices, GLES20.GL_LINES);
        _mesh.setWidthHeight(_width, _height);
        _mesh.flipX();
    }

    @Override
    public void update(double dt) {
        theta = _player._rotation*TO_RADIANS;
        _x = _player._x - (float)Math.sin(theta) * (_player._width*0.3f);
        _y = _player._y + (float)Math.cos(theta) * (_player._height*0.3f);
        _rotation = _player._rotation-180;
        super.update(dt);
    }

    @Override
    public void render(final float[] viewportMatrix){
        if (_player.isBoosting()){
            super.render(viewportMatrix);
        }
    }
}
