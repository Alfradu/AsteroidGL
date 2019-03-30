package com.alfredradu.asteroidsgl;

import com.alfredradu.asteroidsgl.utils.Utils;

public class Point3D {
    public float _x = 0.0f;
    public float _y = 0.0f;
    public float _z = 0.0f;

    public Point3D(){}
    public Point3D(final float x, final float y, final float z){
        set(x, y, z);
    }
    public Point3D(final float[] p){
        set(p);
    }

    public void set(final float x, final float y, final float z){
        _x = x;
        _y = y;
        _z = z;
    }

    public void set(final float[] p){
        Utils.require(p.length == 3);
        _x = p[0];
        _y = p[1];
        _z = p[2];
    }

    public final  float distanceSquared(Point3D that){
        final float dx = this._x-that._x;
        final float dy = this._y-that._y;
        final float dz = this._z-that._z;
        return dx*dx+dy*dy+dz*dz;
    }
    public final float distance(Point3D that){
        final float dx = this._x-that._x;
        final float dy = this._y-that._y;
        final float dz = this._z-that._z;
        return (float) Math.sqrt(dx*dx+dy*dy+dz*dz);
    }
    public final  float distanceL1(Point3D that){
        return(Math.abs(this._x-that._x) + Math.abs(this._y-that._y) + Math.abs(this._z-that._z));
    }
}
