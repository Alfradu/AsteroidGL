package com.alfredradu.asteroidsgl;

import android.opengl.GLES20;

public class Bullet extends GLEntity {
    final static float[] vertices = {0, 0, 0};
    private static Mesh BULLET_MESH = new Mesh(vertices, GLES20.GL_POINTS);
    private static final float TO_RADIANS = (float)Math.PI/180.0f;
    private static final float SPEED = 120f;
    public static final float TIME_TO_LIVE = 3.0f; //seconds

    public float _ttl = 0;
    public Bullet() {
        setColors(1, 0, 1, 1);
        _mesh = BULLET_MESH; //all bullets use the exact same mesh
    }

    public void fireFrom(GLEntity source){
        final float theta = source._rotation*TO_RADIANS;
        _x = source._x + (float)Math.sin(theta) * (source._width*0.5f);
        _y = source._y - (float)Math.cos(theta) * (source._height*0.5f);
        _velX = source._velX;
        _velY = source._velY;
        _velX += (float)Math.sin(theta) * SPEED;
        _velY -= (float)Math.cos(theta) * SPEED;
        _ttl = TIME_TO_LIVE;
    }

    @Override
    public void onCollision(final GLEntity that) {
        _ttl = 0;
    }

    @Override
    public boolean isDead(){
        return _ttl < 1;
    }

    @Override
    public void update(double dt) {
        if(_ttl > 0) {
            _ttl -= dt;
            super.update(dt);
        }
    }

    @Override
    public void render(final float[] viewportMatrix){
        if(_ttl > 0) {
            super.render(viewportMatrix);
        }
    }
}