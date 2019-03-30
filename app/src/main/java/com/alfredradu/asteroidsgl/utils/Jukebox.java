package com.alfredradu.asteroidsgl.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.util.Log;

import com.alfredradu.asteroidsgl.Game;

import java.io.IOException;
import java.util.HashMap;

public class Jukebox {
    private static final String TAG = "Jukebox";
    private String _musicFile;
    private SoundPool _soundPool = null;
    private static final int MAX_STREAMS = 3;
    private HashMap<Game.GameEvent, Integer> _soundsMap;
    private static final String SOUNDS_PREF_KEY = "sounds_pref_key";
    private static final String MUSIC_PREF_KEY = "music_pref_key";
    private MediaPlayer _bgPlayer = null;
    private float DEFAULT_SFX_VOLUME = 0.4f;
    private float DEFAULT_MUSIC_VOLUME = 0.6f;
    private Context _context = null;

    public Jukebox(Context context, String music) {
        _musicFile = music;
        _context = context;
        loadIfNeeded();
    }

    public void newSong(String music){
        _musicFile = music;
        _bgPlayer.reset();
        loadMusic();
        _bgPlayer.start();
    }

    private void loadIfNeeded(){
        loadSounds();
        loadMusic();
    }

    private void createSoundPool() {
        AudioAttributes attr = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        _soundPool = new SoundPool.Builder()
                .setAudioAttributes(attr)
                .setMaxStreams(MAX_STREAMS)
                .build();
    }

    private void loadEventSound(final Game.GameEvent event, final String fileName){
        try {
            AssetFileDescriptor afd = _context.getAssets().openFd(fileName);
            int soundId = _soundPool.load(afd, 1);
            _soundsMap.put(event, soundId);
        } catch( IOException e){
            Log.e(TAG, "loadEventsound: error loading sound " + e.toString());
        }
    }

    private void loadSounds(){
        createSoundPool();
        _soundsMap = new HashMap<Game.GameEvent, Integer>();
        loadEventSound(Game.GameEvent.Shoot, "sounds/shoot.wav");
        loadEventSound(Game.GameEvent.Explosion, "sounds/explode.wav");
        loadEventSound(Game.GameEvent.GameOver, "sounds/hit.wav");
        loadEventSound(Game.GameEvent.LevelUp, "sounds/levelup.wav");
        loadEventSound(Game.GameEvent.Warp, "sounds/warp.wav");
    }

    private void unloadSounds(){
        if (_soundPool != null){
            _soundPool.release();
            _soundPool = null;
            _soundsMap.clear();
        }
    }

    public void playSoundForGameEvent(Game.GameEvent event){
        final float leftVolume = DEFAULT_SFX_VOLUME;
        final float rightVolume = DEFAULT_SFX_VOLUME;
        final int priority = 1;
        final int loop = 0; //-1 loop forever, 0 play once
        final float rate = 1.0f;
        final Integer soundID = _soundsMap.get(event);
        if(soundID != null){
            _soundPool.play(soundID, leftVolume, rightVolume, priority, loop, rate);
        }
    }

    private void loadMusic(){
        try{
            _bgPlayer = new MediaPlayer();
            AssetFileDescriptor afd = _context
                    .getAssets().openFd("music/"+_musicFile+".mp3");
            _bgPlayer.setDataSource(
                    afd.getFileDescriptor(),
                    afd.getStartOffset(),
                    afd.getLength());
            _bgPlayer.setLooping(true);
            _bgPlayer.setVolume(DEFAULT_MUSIC_VOLUME, DEFAULT_MUSIC_VOLUME);
            _bgPlayer.prepare();
        }catch(IOException e){
            _bgPlayer = null;
            Log.e(TAG, e.toString());
        }
    }

    private void unloadMusic(){
        if(_bgPlayer != null) {
            _bgPlayer.stop();
            _bgPlayer.release();
        }
    }

    public void pauseBgMusic(){
        _bgPlayer.pause();
    }
    public void resumeBgMusic(){
        _bgPlayer.start();
    }
}
