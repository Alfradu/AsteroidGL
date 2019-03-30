package com.alfredradu.asteroidsgl;

import android.graphics.Color;
import android.opengl.GLES20;

public class Star extends GLEntity {
    private static Mesh m = null;

    public Star(final float x, final float y){
        super();
        _x = x;
        _y = y;
        _velR = 1f;
        _color[0] = Color.red(Color.WHITE) / 255f;
        _color[1] = Color.green(Color.WHITE) / 255f;
        _color[2] = Color.blue(Color.WHITE) / 255f;
        _color[3] = 1f;
        if(m == null) {
            final float[] vertices = {0, 0, 0};
            m = new Mesh(vertices, GLES20.GL_POINTS);
        }
        _mesh = m;
        _scale = 0.001f;
    }
}
