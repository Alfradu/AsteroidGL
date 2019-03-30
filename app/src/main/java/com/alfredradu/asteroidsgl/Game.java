package com.alfredradu.asteroidsgl;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.AttributeSet;
import android.util.Log;

import com.alfredradu.asteroidsgl.utils.Jukebox;
import com.alfredradu.asteroidsgl.utils.Utils;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Game extends GLSurfaceView implements GLSurfaceView.Renderer {
    public static final String TAG = "Game";
    public static long SECOND_IN_NANOSECONDS = 1000000000;
    public static long MILLISECOND_IN_NANOSECONDS = 1000000;
    public static float NANOSECONDS_TO_MILLISECONDS = 1.0f / MILLISECOND_IN_NANOSECONDS;
    public static float NANOSECONDS_TO_SECONDS = 1.0f / SECOND_IN_NANOSECONDS;
    private static final float BG_COLOR[] = {0/255f, 0/255f, 0/255f, 1f};
    public static final String PREFS = "com.alfredradu.asteroidsgl.game";
    public static final String HIGHEST_SCORE = "highest_score";
    private static final int RESET_COUNT = 200;
    private SharedPreferences _prefs = null;
    private SharedPreferences.Editor _editor = null;
    private MainActivity _activity = null;
    public InputManager _inputs = new InputManager();
    private static Context _cont;
    private Player _player;
    private Flame _flame;
    private Text _fps;
    private Text _levelUp;
    private Text _menu;
    private Text _subMenu;
    private Text _score;
    private Text _highScore;
    private Text _charge;
    private Text _lives;
    private String menu;
    private String retry;
    private String start;
    private int _scoreNumber = 0;
    private int _highScoreNumber = 0;
    private int _resetCounter = RESET_COUNT;
    private int _levelUpCounter = RESET_COUNT;
    private int _currentLevel = 0;
    private Boolean _canMove = true;
    private Boolean _spawned = false;
    public Boolean _playerHit = false;
    private Boolean _isPlaying = false;
    private Boolean _leveling = false;
    private static final int BULLET_COUNT = (int)Bullet.TIME_TO_LIVE+1;
    Bullet[] _bullets = new Bullet[BULLET_COUNT];
    private static int STAR_COUNT = 100;
    private ArrayList<Star> _stars = new ArrayList<>();
    private ArrayList<Shrapnel> _shrapnel = new ArrayList<>();
    private ArrayList<Asteroid> _asteroids = new ArrayList<>();
    private ArrayList<Text> _texts = new ArrayList<>();
    static float WORLD_WIDTH = 160f;
    static float WORLD_HEIGHT = 90f;
    static float METERS_TO_SHOW_X = 160f;
    static float METERS_TO_SHOW_Y = 90f;
    static float  _halfWidth = 0;
    static float _halfHeight = 0;
    private float[] _viewportMatrix = new float[4*4];

    final double dt = 0.01;
    double accumulator = 0.0;
    double currentTime = System.nanoTime()*NANOSECONDS_TO_SECONDS;
    private int frames = 0;
    double lastTime = 0.0;
    public enum GameEvent {
        Shoot,
        Explosion,
        GameOver,
        LevelUp,
        Warp,
    }
    private Jukebox _jukebox = null;


    public static int getScreenWidth() { return Resources.getSystem().getDisplayMetrics().widthPixels; }
    public static int getScreenHeight() { return Resources.getSystem().getDisplayMetrics().heightPixels; }
    public Game(Context context) {
        super(context);
        init(context);
    }
    public Game(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public void init(Context context){
        _cont = context;
        GLEntity._game = this;
        setEGLContextClientVersion(2);
        setPreserveEGLContextOnPause(true);
        _prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        _editor = _prefs.edit();
        _activity = (MainActivity) _cont;
        _activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        _jukebox = new Jukebox(_activity, "bgmusic");
        final int TARGET_HEIGHT = 90;
        final int actualHeight = getScreenHeight();
        final float ratio = (TARGET_HEIGHT >= actualHeight) ? 1 : (float) TARGET_HEIGHT / actualHeight;
        WORLD_WIDTH = (int) (ratio * getScreenWidth());
        WORLD_HEIGHT = TARGET_HEIGHT;
        METERS_TO_SHOW_X = WORLD_WIDTH;
        METERS_TO_SHOW_Y = WORLD_HEIGHT;
        _halfWidth = WORLD_WIDTH/2;
        _halfHeight = WORLD_HEIGHT/2;
        menu = context.getString(R.string.menu);
        retry = context.getString(R.string.retry);
        start = context.getString(R.string.start);

        _fps = new Text("00", WORLD_WIDTH-24,8);
        _levelUp = new Text("", WORLD_WIDTH-31, 16);
        _score = new Text("Score 0000",8,8);
        _highScore = new Text ("Highscore 0000", 8, 16);
        _menu = new Text(menu,WORLD_WIDTH/3f,WORLD_HEIGHT/3);
        _subMenu = new Text(start,WORLD_WIDTH/3.5f,WORLD_HEIGHT/2.5f);
        _lives = new Text ("Lives ", 8,24);
        _charge = new Text ("charge ",WORLD_WIDTH/2.5f, WORLD_HEIGHT-8);
        _menu.setScale(0.6f);
        _score.setScale(0.4f);
        updateScore(0,_prefs.getInt(HIGHEST_SCORE, 0));

        _texts.add(_lives);
        _texts.add(_charge);
        _texts.add(_score);
        _texts.add(_highScore);
        _texts.add(_menu);
        _texts.add(_subMenu);
        _texts.add(_fps);
        _texts.add(_levelUp);
        for(int i = 0; i < BULLET_COUNT; i++){
            _bullets[i] = new Bullet();
        }
        Log.d(TAG, "Resolution: " + WORLD_WIDTH + " : " + WORLD_HEIGHT);
        setRenderer(this);
    }

    public void setControls(final InputManager input){
        _inputs = input;
    }

    public static Context getCont(){ return _cont; }

    public boolean maybeFireBullet(final GLEntity source){
        for(final Bullet b : _bullets) {
            if(b.isDead()) {
                onGameEvent(GameEvent.Shoot, null);
                b.fireFrom(source);
                return true;
            }
        }
        return false;
    }

    private void collisionDetection(){
        for(final Bullet b : _bullets) {
            if(b.isDead()){ continue; }
            for(final Asteroid a : _asteroids) {
                if(b.isColliding(a)){
                    if(a.isDead()){continue;}
                    b.onCollision(a);
                    a.onCollision(b);
                }
            }
        }
        for (final Asteroid a : _asteroids){
            if(a.isDead() || _player.isDead()){continue;}
            if(_player.isColliding(a)){
                _player.onCollision(a);
                a.onCollision(_player);
            }
            for (final Asteroid b : _asteroids){
                if(a.equals(b) || b.isDead()){continue;}
                if (a.isColliding(b) && a._type == 3 && b._type == 3){
                    a.onCollision(b);
                    b.onCollision(a);
                }
            }
        }
    }

    public void removeDeadEntities(){
        Asteroid temp;
        final int count = _asteroids.size();
        for(int i = count-1; i >= 0; i--){
            temp = _asteroids.get(i);
            if(temp.isDead()){
                int[] astVal = {temp._type, (int)temp._x, (int)temp._y};
                _asteroids.remove(i);
                switch (astVal[0]){
                    case 3:
                        spawnAsteroid(2,astVal[1], astVal[2]);
                        spawnAsteroid(2,astVal[1], astVal[2]);
                        spawnAsteroid(1,astVal[1], astVal[2]);
                        break;
                    case 2:
                        spawnAsteroid(1,astVal[1], astVal[2]);
                        spawnAsteroid(1,astVal[1], astVal[2]);
                        break;
                    default:
                        break;
                }
            }
        }
        for (int i = _shrapnel.size()-1; i >= 0; i--){
            if (!_shrapnel.get(i)._isAlive){
                _shrapnel.remove(i);
            }
        }
        if (_player.isDead()){
            _playerHit = true;
        }
    }

    public void spawnAsteroid(final int type, final int x, final int y){
        _asteroids.add(new Asteroid(x, y, Utils.between(3,11), type));
    }

    public void clearAsteroids(){
        _asteroids.clear();
    }

    public void levelUp(final int level){
        clearAsteroids();
        _currentLevel = level;
        onGameEvent(GameEvent.LevelUp, null);
        int x = 0;
        int y = 0;
        for (int i = 0; i < _currentLevel+2; i++){
            switch(Utils.between(0,3)){
                case 0:
                    x = -12;
                    y = Utils.between(0,(int)WORLD_HEIGHT);
                    break;
                case 1:
                    x = (int)WORLD_WIDTH+12;
                    y = Utils.between(0,(int)WORLD_HEIGHT);
                    break;
                case 2:
                    x = Utils.between(0,(int)WORLD_WIDTH);
                    y = -12;
                    break;
                case 3:
                    x = Utils.between(0,(int)WORLD_WIDTH);
                    y = (int)WORLD_HEIGHT+12;
                    break;
            }
            spawnAsteroid( 3, x, y);
        }
    }

    public void updateScore(final int score ){
        updateScore(_scoreNumber+score, _highScoreNumber);
    }
    public void updateScore(final int score, final int highScore){
        _scoreNumber = score;
        _highScoreNumber = highScore;
        _score.setString("Score " + formatScore(_scoreNumber));
        if (_scoreNumber> _highScoreNumber){
            _highScoreNumber = _scoreNumber;
        }
        _highScore.setString("HighScore " + formatScore(_highScoreNumber));
        _editor.putInt(HIGHEST_SCORE, _highScoreNumber);
        _editor.apply();
    }

    public String formatScore(final int score){
        String s;
        if (score<10){
            s = "000"+String.valueOf(score);
        } else if (score<100){
            s = "00"+String.valueOf(score);
        } else if (score<1000){
            s = "0"+String.valueOf(score);
        } else if (score<10000){
            s = String.valueOf(score);
        } else {
            s = "9999";
        }
        return s;
    }

    public void toggleHUD(String s){
        if (s.isEmpty()){
            _menu.setString(s);
            _subMenu.setString(s);
        } else {
            _menu.setString(menu);
            _subMenu.setString(s);
        }
    }

    public void warp() {
        onGameEvent(GameEvent.Warp, null);
        _player.setPos(Utils.between(_player._width, WORLD_WIDTH-_player._width),Utils.between(_player._height, WORLD_HEIGHT-_player._height));
    }
    public void updateCharge(int chargeLevel){
        _charge.setString("charge " + chargeLevel);
    }
    public void updateLives(int lives){
        _lives.setString("Lives " + lives);
    }
    public void spawnExplosion(final GLEntity entity, final int count){
        float theta = (float)(2*Math.PI/count);
        onGameEvent(GameEvent.Explosion, null);
        for (int i = 0; i < count; i++){
            _shrapnel.add(new Shrapnel(entity._x,entity._y));
            _shrapnel.get(_shrapnel.size()-1)._velX += (float)Math.sin(theta*i)*Utils.between(10,20);
            _shrapnel.get(_shrapnel.size()-1)._velY -= (float)Math.cos(theta*i)*Utils.between(10,20);
        }
    }

    private void reset() {
        _isPlaying = false;
        _playerHit = false;
        _player.reset();
        levelUp(1);
        toggleHUD(retry);
        updateScore(0,_highScoreNumber);
        _player.setPos(_halfWidth,_halfHeight);
        _resetCounter = RESET_COUNT;
        _canMove = true;

    }

    public void killPlayer(){
        _canMove = false;
        if (!_spawned){
            _player.setColors(new float[]{0,0,0,0});
            _spawned = true;
            spawnExplosion(_player,20);
            onGameEvent(GameEvent.GameOver, null);
        }
        if (_resetCounter < 0){
            reset();
            _spawned = false;
        }
        _resetCounter--;
    }

    public void checkGameOver(){
        if (_playerHit){
            killPlayer();
        } else if (_asteroids.size() < 1){
            levelUp(_currentLevel+1);
            _levelUp.setString("Level up");
            _leveling = true;
        }
    }

    public void onGameEvent( GameEvent gameEvent, GLEntity e){
        _jukebox.playSoundForGameEvent(gameEvent);
    }
    private void update(){
        final double newTime = System.nanoTime()*NANOSECONDS_TO_SECONDS;
        final double frameTime = newTime - currentTime;
        frames++;
        if (newTime - lastTime >= 1.00){
            _fps.setString("Fps " + String.valueOf(frames));
            lastTime = newTime;
            frames = 0;
        }
        currentTime = newTime;

        if (_isPlaying){
            accumulator += frameTime;
            while (accumulator >= dt){
                for(final Asteroid a : _asteroids){
                    if(a.isDead()){continue;}
                    a.update(dt);
                }
                for(final Bullet b : _bullets){
                    if(b.isDead()){ continue; }
                    b.update(dt);
                }
                for(final Shrapnel s : _shrapnel){
                    if(s.isDead()){continue;}
                    s.update(dt);
                }
                collisionDetection();
                removeDeadEntities();
                checkGameOver();
                if (_canMove){
                    _player.update(dt);
                    _flame.update(dt);
                }
                accumulator -= dt;
            }
        }
        if(_inputs._pressingA && !_isPlaying){
            toggleHUD("");
            _isPlaying = true;
        }
        if (_leveling){
            if (_levelUpCounter < 0){
                _levelUp.setString("");
                _leveling = false;
                _levelUpCounter = RESET_COUNT;
            }
            _levelUpCounter--;
        }
    }

    private void render(){
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        final int offset = 0;
        final float left = 0;
        final float right = METERS_TO_SHOW_X;
        final float bottom = METERS_TO_SHOW_Y;
        final float top = 0;
        final float near = 0f;
        final float far = 1f;
        Matrix.orthoM(_viewportMatrix, offset, left, right, bottom, top, near, far);
        for (final Star s : _stars){
            s.render(_viewportMatrix);
        }
        for (final Asteroid a : _asteroids){
            a.render(_viewportMatrix);
        }
        _player.render(_viewportMatrix);
        _flame.render(_viewportMatrix);
        for(final Text t : _texts){
            t.render(_viewportMatrix);
        }
        for(final Bullet b : _bullets){
            if(b.isDead()){ continue; }
            b.render(_viewportMatrix);
        }
        for (final Shrapnel s : _shrapnel){
            s.render(_viewportMatrix);
        }
    }

    public void onResume() {
        Log.d(TAG, "onResume");
        _inputs.onResume();
        _jukebox.resumeBgMusic();
    }

    public void onPause() {
        Log.d(TAG, "onPause");
        _inputs.onPause();
        _jukebox.pauseBgMusic();
    }

    public void onDestroy() {
        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onSurfaceCreated(final GL10 unused, final EGLConfig config) {
        GLManager.buildProgram();
        GLES20.glClearColor(BG_COLOR[0], BG_COLOR[1], BG_COLOR[2], BG_COLOR[3]);
        _player = new Player(_halfWidth,_halfHeight);
        _flame = new Flame(_player);
        for(int i = 0; i < STAR_COUNT; i++){
            _stars.add(new Star(Utils.nextInt((int)WORLD_WIDTH), Utils.nextInt((int)WORLD_HEIGHT)));
        }
        levelUp(1);
    }

    @Override
    public void onSurfaceChanged(final GL10 unused, final int width, final int height) {
        GLES20.glViewport(0, 0, width, height);
    }
    @Override
    public void onDrawFrame(final GL10 unused) {
        update();
        render();
    }

}
