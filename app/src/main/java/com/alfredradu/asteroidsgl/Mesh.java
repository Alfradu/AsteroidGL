package com.alfredradu.asteroidsgl;

import android.opengl.GLES20;

import com.alfredradu.asteroidsgl.utils.Utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Mesh {
    private static final String TAG = "Mesh";
    public static final int SIZE_OF_FLOAT = Float.SIZE/Byte.SIZE; //32bit/8bit = 4 bytes
    public static final int COORDS_PER_VERTEX = 3; //X, Y, Z
    public static final int VERTEX_STRIDE = COORDS_PER_VERTEX * SIZE_OF_FLOAT;
    public static final int X = 0;
    public static final int Y = 1;
    public static final int Z = 2;

    public FloatBuffer _vertexBuffer = null;
    public int _vertexCount = 0;
    public int _drawMode = GLES20.GL_TRIANGLES;
    public float _width = 0f;
    public float _height = 0f;
    public float _depth = 0f;
    public float _radius = 0f;
    public Point3D _min = new Point3D();
    public Point3D _max = new Point3D();

    public Mesh(final float[] geometry){
        init(geometry, GLES20.GL_TRIANGLES);
    }
    public Mesh(final float[] geometry, final int drawMode){
        init(geometry, drawMode);
    }
    private void init(final float[] geometry, final int drawMode){
        setVertices(geometry);
        setDrawmode(drawMode);
    }
    public void scale(final double factor) { scale(factor, factor, factor); }
    public void scaleX(final double factor){ scale(factor, 1.0, 1.0); }
    public void scaleY(final double factor){ scale(1.0, factor, 1.0); }
    public void scaleZ(final double factor){ scale(1.0, 1.0, factor); }
    public void flipX(){ scaleX(-1.0); }
    public void flipY(){ scaleY(-1.0); }
    public void flipZ(){ scaleZ(-1.0); }

    public void flip(final int axis){
        Utils.require(axis == X || axis == Y || axis == Z);
        _vertexBuffer.position(0);
        for(int i = 0; i < _vertexCount; i++){
            final int index = i*COORDS_PER_VERTEX + axis;
            final float invertedCoordinate = _vertexBuffer.get(index) * -1;
            _vertexBuffer.put(index, invertedCoordinate);
        }
        updateBounds();
    }

    public void scale(final double xFactor, final double yFactor, final double zFactor){
        for(int i = 0; i < _vertexCount*COORDS_PER_VERTEX; i+=COORDS_PER_VERTEX) {
            _vertexBuffer.put(i+X, (float)(_vertexBuffer.get(i+X) * xFactor));
            _vertexBuffer.put(i+Y, (float)(_vertexBuffer.get(i+Y) * yFactor));
            _vertexBuffer.put(i+Z, (float)(_vertexBuffer.get(i+Z) * zFactor));
        }
        updateBounds();
    }

    public void setWidthHeight(final double w, final double h){
        normalize();  //a normalized mesh is centered at [0,0] and ranges from [-1,1]
        scale(w*0.5, h*0.5, 1.0); //meaning we now scale from the center, so *0.5 (radius)
        Utils.require(Math.abs(w-_width) < Float.MIN_NORMAL && Math.abs(h-_height) < Float.MIN_NORMAL, "incorrect width / height after scaling!");
    }

    public void setDrawmode(int drawMode){
        Utils.require(drawMode == GLES20.GL_TRIANGLES
                || drawMode == GLES20.GL_LINES
                || drawMode == GLES20.GL_POINTS);
        _drawMode = drawMode;
    }

    public void setVertices(final float[] geometry){
        // create a floating point buffer from a ByteBuffer
        _vertexBuffer = ByteBuffer.allocateDirect(geometry.length * SIZE_OF_FLOAT)
                .order(ByteOrder.nativeOrder()) // use the device hardware's native byte order
                .asFloatBuffer();
        _vertexBuffer.put(geometry); //add the coordinates to the FloatBuffer
        _vertexBuffer.position(0); // set the buffer to read the first coordinate
        _vertexCount = geometry.length / COORDS_PER_VERTEX;
        updateBounds();
        //normalize();
    }

    public void updateBounds(){
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, minZ = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;
        for(int i = 0; i < _vertexCount*COORDS_PER_VERTEX; i+=COORDS_PER_VERTEX) {
            final float x = _vertexBuffer.get(i+X);
            final float y = _vertexBuffer.get(i+Y);
            final float z = _vertexBuffer.get(i+Z);
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            minZ = Math.min(minZ, z);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
            maxZ = Math.max(maxZ, z);
        }
        _min.set(minX, minY, minZ);
        _max.set(maxX, maxY, maxZ);
        _width = maxX - minX;
        _height = maxY - minY;
        _depth = maxZ - minZ;
        _radius = Math.max(Math.max(_width, _height), _depth) * 0.5f;
    }

    public float left() {
        return _min._x;
    }
    public float right() {
        return _max._x;
    }
    public float top() {
        return _min._y;
    }
    public float bottom() {
        return _max._y;
    }
    public float centerX() { return _min._x + (_width * 0.5f); }
    public float centerY() {
        return _min._y + (_height * 0.5f);
    }

    public void normalize() {
        final double inverseW = (_width  == 0.0) ? 0.0 : 1/_width;
        final double inverseH = (_height == 0.0) ? 0.0 : 1/_height;
        final double inverseD = (_depth  == 0.0) ? 0.0 : 1/_depth;
        for (int i = 0; i < _vertexCount * COORDS_PER_VERTEX; i += COORDS_PER_VERTEX) {
            final double dx = _vertexBuffer.get(i + X) - _min._x; //"d" for "delta" or "difference"
            final double dy = _vertexBuffer.get(i + Y) - _min._y;
            final double dz = _vertexBuffer.get(i + Z) - _min._z;
            final double xNorm = 2.0 * (dx * inverseW) - 1.0; //(dx * inverseW) is equivalent to (dx / _width)
            final double yNorm = 2.0 * (dy * inverseH) - 1.0; //but avoids the risk of division-by-zero.
            final double zNorm = 2.0 * (dz * inverseD) - 1.0;
            _vertexBuffer.put(i+X, (float)xNorm);
            _vertexBuffer.put(i+Y, (float)yNorm);
            _vertexBuffer.put(i+Z, (float)zNorm);
        }
        updateBounds();
        Utils.require(_width <= 2.0f, "x-axis is out of range!");
        Utils.require(_height <= 2.0f, "y-axis is out of range!");
        Utils.require(_depth <= 2.0f, "z-axis is out of range!");
        Utils.expect((_min._x >= -1.0f && _max._x <= 1.0f), TAG, "normalized x["+_min._x+", "+_max._x+"] expected x[-1.0, 1.0]");
        Utils.expect((_min._y >= -1.0f && _max._y <= 1.0f), TAG, "normalized y["+_min._y+", "+_max._y+"] expected y[-1.0, 1.0]");
        Utils.expect((_min._z >= -1.0f && _max._z <= 1.0f), TAG, "normalized z["+_min._z+", "+_max._z+"] expected z[-1.0, 1.0]");
    }

    public static float[] generateLinePolygon(final int numPoints, final double radius) {
        Utils.require(numPoints > 2, "a polygon requires at least 3 points.");
        final int numVerts = numPoints * 2; //we render lines, and each line requires 2 points
        final float[] verts = new float[numVerts * Mesh.COORDS_PER_VERTEX];
        double step = 2.0 * Math.PI / numPoints;
        int i = 0, point = 0;
        while (point < numPoints) { //generate verts on circle, 2 per point
            double theta = point * step;
            verts[i++] = (float) (Math.cos(theta) * radius); //X
            verts[i++] = (float) (Math.sin(theta) * radius); //Y
            verts[i++] = 0f;                                //Z
            point++;
            theta = point * step;
            verts[i++] = (float) (Math.cos(theta) * radius); //X
            verts[i++] = (float) (Math.sin(theta) * radius); //Y
            verts[i++] = 0f;                                 //Z
        }
        return verts;
    }

    public void rotateX(final double theta) {    rotate(X, theta); }
    public void rotateY(final double theta) {    rotate(Y, theta); }
    public void rotateZ(final double theta) {    rotate(Z, theta); }
    private void rotate(final int axis, final double theta) {
        Utils.require(axis == X || axis == Y || axis == Z);
        final double sinTheta = Math.sin(theta);
        final double cosTheta = Math.cos(theta);
        for (int i = 0; i < _vertexCount * COORDS_PER_VERTEX; i += COORDS_PER_VERTEX) {
            final double x = _vertexBuffer.get(i + X);
            final double y = _vertexBuffer.get(i + Y);
            final double z = _vertexBuffer.get(i + Z);
            if (axis == Z) {
                _vertexBuffer.put(i + X, (float) (x * cosTheta - y * sinTheta));
                _vertexBuffer.put(i + Y, (float) (y * cosTheta + x * sinTheta));
            } else if (axis == Y) {
                _vertexBuffer.put(i + X, (float) (x * cosTheta - z * sinTheta));
                _vertexBuffer.put(i + Z, (float) (z * cosTheta + x * sinTheta));
            } else if (axis == X) {
                _vertexBuffer.put(i + Y, (float) (y * cosTheta - z * sinTheta));
                _vertexBuffer.put(i + Z, (float) (z * cosTheta + y * sinTheta));
            }
        }
        updateBounds();
    }
}