package com.alfredradu.asteroidsgl;

import android.content.res.AssetManager;
import android.opengl.GLES20;
import android.util.Log;

import com.alfredradu.asteroidsgl.utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;

public class GLManager {
    public final static String TAG = "GLManager";
    private static final int OFFSET = 0;

    private static int glProgramHandle;
    private static int colorUniformHandle;
    private static int positionAttributeHandle;
    private static int MVPMatrixHandle;

    public static void checkGLError(final String func){
        int error;
        while((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR){
            Log.e(func, "glError " + error);
        }
    }

    private static int compileShader(final int type, final String shaderCode){
        assert(type == GLES20.GL_VERTEX_SHADER || type == GLES20.GL_FRAGMENT_SHADER);
        final int handle = GLES20.glCreateShader(type);
        GLES20.glShaderSource(handle, shaderCode);
        GLES20.glCompileShader(handle);
        Log.d(TAG, "Shader Compile Log: \n" + GLES20.glGetShaderInfoLog(handle));
        checkGLError("compileShader");
        return handle;
    }

    private static int linkShaders(final int vertexShader, final int fragmentShader){
        final int handle = GLES20.glCreateProgram();
        GLES20.glAttachShader(handle, vertexShader);
        GLES20.glAttachShader(handle, fragmentShader);
        GLES20.glLinkProgram(handle);
        Log.d(TAG, "Shader Link Log: \n" + GLES20.glGetProgramInfoLog(handle));
        checkGLError("linkShaders");
        return handle;
    }

    private static String getShaderCodeFromFile(String file){
        AssetManager am = Game.getCont().getAssets();
        InputStream is = null;
        try { is = am.open("shaders/"+file+".txt"); } catch (IOException e) { e.printStackTrace(); }
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        String s = "";
        try{
            while((line = br.readLine()) != null){
                s = s.concat(line+"\n");
            }
        } catch (Exception e){
            System.out.println(e);
        }
        return s;
    }

    public static void buildProgram(){
        final int vertex = compileShader(GLES20.GL_VERTEX_SHADER, getShaderCodeFromFile("vertex"));
        final int fragment = compileShader(GLES20.GL_FRAGMENT_SHADER, getShaderCodeFromFile("fragment"));
        glProgramHandle = linkShaders(vertex, fragment);
        GLES20.glDeleteShader(vertex);
        GLES20.glDeleteShader(fragment);
        positionAttributeHandle = GLES20.glGetAttribLocation(glProgramHandle, "position");
        colorUniformHandle = GLES20.glGetUniformLocation(glProgramHandle, "color");
        MVPMatrixHandle = GLES20.glGetUniformLocation(glProgramHandle, "modelViewProjection");
        GLES20.glUseProgram(glProgramHandle);
        GLES20.glLineWidth(5f);
        checkGLError("buildProgram");
    }

    private static void setModelViewProjection(final float[] modelViewMatrix) {
        final int COUNT = 1;
        final boolean TRANSPOSED = false;
        GLES20.glUniformMatrix4fv(MVPMatrixHandle, COUNT, TRANSPOSED, modelViewMatrix, OFFSET);
        checkGLError("setModelViewProjection");
    }

    public static void draw(final Mesh model, final float[] modelViewMatrix, final float[] color){
        setShaderColor(color);
        uploadMesh(model._vertexBuffer);
        setModelViewProjection(modelViewMatrix);
        drawMesh(model._drawMode, model._vertexCount);
    }

    private static void uploadMesh(final FloatBuffer vertexBuffer) {
        final boolean NORMALIZED = false;
        GLES20.glEnableVertexAttribArray(GLManager.positionAttributeHandle);
        GLES20.glVertexAttribPointer(GLManager.positionAttributeHandle, Mesh.COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, NORMALIZED, Mesh.VERTEX_STRIDE,
                vertexBuffer);
        checkGLError("uploadMesh");
    }

    private static void setShaderColor(final float[] color) {
        final int COUNT = 1;
        GLES20.glUniform4fv(GLManager.colorUniformHandle, COUNT, color, OFFSET);
        checkGLError("setShaderColor");
    }

    private static void drawMesh(final int drawMode, final int vertexCount) {
        Utils.require(drawMode == GLES20.GL_TRIANGLES
                || drawMode == GLES20.GL_LINES
                || drawMode == GLES20.GL_POINTS);
        GLES20.glDrawArrays(drawMode, OFFSET, vertexCount);
        GLES20.glDisableVertexAttribArray(GLManager.positionAttributeHandle);
        checkGLError("drawMesh");
    }
}
