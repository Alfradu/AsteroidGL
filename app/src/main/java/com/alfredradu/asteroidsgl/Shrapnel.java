package com.alfredradu.asteroidsgl;

import android.graphics.Color;
import android.opengl.GLES20;

import com.alfredradu.asteroidsgl.utils.Utils;

public class Shrapnel extends GLEntity {
    final static float[] vertices = {0, 0, 0};
    private static Mesh m = new Mesh(vertices, GLES20.GL_POINTS);
    public float _ttl;

    public Shrapnel(final float x, final float y){
        super();
        _ttl = Utils.between(1f,1.5f);
        _x = x;
        _y = y;
        _color[0] = Color.red(Color.WHITE) / 255f;
        _color[1] = Color.green(Color.WHITE) / 255f;
        _color[2] = Color.blue(Color.WHITE) / 255f;
        _color[3] = 1f;
        _mesh = m;
        _scale = 0.1f;
    }

    @Override
    public void update(double dt) {
        if(_ttl < 0) {
            _isAlive = false;
        }
        _ttl -= dt;
        super.update(dt);
    }
}
