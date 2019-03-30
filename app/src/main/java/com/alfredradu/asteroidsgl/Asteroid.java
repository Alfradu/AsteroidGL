package com.alfredradu.asteroidsgl;

import android.opengl.GLES20;

import com.alfredradu.asteroidsgl.utils.Utils;

public class Asteroid extends GLEntity {
    private static final float MAX_VEL = 4f;
    private static final float MIN_VEL = -4f;
    private float EXTRA_VEL;
    private int _lives;
    public int _type;
    private int _points;

    public Asteroid(final float x, final float y, int points, int type){
        _type = type;
        switch (_type){
            case 3:
                if(points < 5){ points = 5; }
                EXTRA_VEL = 2;
                _width = 12;
                _lives = 4;
                _points = 1;
                break;
            case 2:
                if(points < 5){ points = 5; }
                EXTRA_VEL = 3;
                _width = 9;
                _lives = 2;
                _points = 3;
                break;
            default:
                if(points < 3){ points = 3; }
                EXTRA_VEL = 5;
                _width = 5;
                _type = 1;
                _lives = 1;
                _points = 4;
                break;
        }
        _x = x;
        _y = y;
        _height = _width;
        _velX = Utils.between(MIN_VEL*EXTRA_VEL, MAX_VEL*EXTRA_VEL);
        _velY = Utils.between(MIN_VEL*EXTRA_VEL, MAX_VEL*EXTRA_VEL);
        _velR = Utils.between(-1.5f, 1.5f);
        final double radius = _width*0.5f;
        final float[] verts = Mesh.generateLinePolygon(points, radius);
        _mesh = new Mesh(verts, GLES20.GL_LINES);
        _mesh.setWidthHeight(_width, _height);
    }

    @Override
    public void onCollision(final GLEntity that) {
        _lives--;
        if (_lives < 1) {
            if (that instanceof Bullet){
                _game.updateScore(_points);
            }
            _isAlive = false;
            _game.spawnExplosion(this, 20);
        }
    }
}
