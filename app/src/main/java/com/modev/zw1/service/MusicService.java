package com.modev.zw1.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.modev.zw1.MainActivity;
import com.modev.zw1.MusicAppWidget;
import com.modev.zw1.bean.Music;
import com.modev.zw1.bean.MusicUitl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicService extends Service {

    //控制指令
    public static final int MUSIC_PRE = 550;
    public static final int MUSIC_PLAYORPAUSE = 44541;
    public static final int MUSIC_NEXT = 3432;
    public static final int MUSIC_STOP = 4575643;
    public static final int MUSIC_SEEK_TO = 25475675;
    public static final int MUSIC_PLAY_ONE = 6345345;
    public static final int MUSIC_INQUIRY_STATUS = 342104;
    public static final int MUSIC_LIST_CHANGE = 323666;

    //广播
    public static final String MUSIC_ACTION = "com.modev.zw1.service.MusicService.ACTION";
    public static final String MUSIC_CHANGE = "com.modev.zw1.service.MusicService.CHANGE";

    //播放状态
    public static final int MUSIC_PLAY_STATUS_PLAYING = 9237432;
    public static final int MUSIC_PLAY_STATUS_STOP = 3543632;
    public static final int MUSIC_PLAY_STATUS_PAUSE = 24324234;
    public static final int MUSIC_PLAY_STATUS_PRE = 2899;
    public static final int MUSIC_PLAY_STATUS_NEXT = 66574;
    public static final int MUSIC_PLAY_STATUS_PLAY_ONE = 23245;

    private MediaPlayer mediaPlayer;        //定义MediaPlayer对象
    private List<Music> musicList;          //定义要播放的文件列表
    private int id;                         //定义播放文件的id
    private boolean isPlaying = false;

    private MusicBroadReceiver receiver;
    MusicUitl musicUitl;

    @Override
    public void onCreate() {
        super.onCreate();

        receiver = new MusicBroadReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MUSIC_ACTION);
        registerReceiver(receiver, intentFilter);

        musicUitl = new MusicUitl(getBaseContext());
        musicList = MusicUitl.getMusicList();
        initMediaPlayer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroy();
        unregisterReceiver(receiver);
        Log.d("选项", "服务已经销毁");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class MusicBroadReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d("选项", "MS收到MusicBroadReceiver");
            switch (intent.getIntExtra("method", -1)) {
                case MusicService.MUSIC_PRE:
                    Log.d("选项", "BroadcastReceiver到这里了MUSIC_PRE");
                    preMusic();
                    pushAction(MusicService.MUSIC_PLAY_STATUS_PRE, id);
                    break;
                case MusicService.MUSIC_PLAYORPAUSE:
                    Log.d("选项", "BroadcastReceiver到这里了MUSIC_PLAY");
                    playOrPause();
                    if (isPlaying) {
                        pushAction(MusicService.MUSIC_PLAY_STATUS_PLAYING, id);
                    }else {
                        pushAction(MusicService.MUSIC_PLAY_STATUS_PAUSE, -1);
                    }
                    break;
                case MusicService.MUSIC_NEXT:
                    Log.d("选项", "BroadcastReceiver到这里了MUSIC_NEXT");
                    nextMusic();
                    pushAction(MusicService.MUSIC_PLAY_STATUS_NEXT, id);
                    break;
                case MUSIC_PLAY_ONE:
                    Log.d("选项", "BroadcastReceiver到这里了MUSIC_PLAY_ONE");
                    int musicID = intent.getIntExtra("MusicID", 0);
                    playMusic(musicID);
                    pushAction(MusicService.MUSIC_PLAY_STATUS_PLAY_ONE, musicID);
                    break;
                case MUSIC_SEEK_TO:
                    Log.d("选项", "BroadcastReceiver到这里了MUSIC_SEEK_TO");
                    int time = intent.getIntExtra("time", 0);
                    seekTo(time);
                    pushAction(MusicService.MUSIC_SEEK_TO, -1);
                    break;
                case MUSIC_STOP:
                    stop();
                    pushAction(MusicService.MUSIC_PLAY_STATUS_STOP, -1);
                    break;
                case MUSIC_INQUIRY_STATUS:

                    if (isPlaying) {
                        pushActionWithTime(MusicService.MUSIC_PLAY_STATUS_PLAYING, id);
                    }else {
                        if (mediaPlayer != null) {
                            pushActionWithTime(MusicService.MUSIC_PLAY_STATUS_PAUSE, id);
                        }
                    }
                    break;
                case MUSIC_LIST_CHANGE:
                    String from = intent.getStringExtra("from");
                    musicList = musicUitl.changeMusicFrom(from, getApplicationContext());
                    id = 0;
//                    pushAction(MusicService.MUSIC_LIST_CHANGE, -1);
                    break;
                default:
                    break;
            }

        }
    }
    /**
     * 初始化音乐播放器
     * */
    public void initMediaPlayer() {

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(
                new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        if (id == musicList.size()-1) {
                            Toast.makeText(getApplicationContext(), "播放完毕，即将播放第一首！", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(getApplicationContext(), "播放完毕，即将播放下一首！", Toast.LENGTH_SHORT).show();
                        }

                        //下一首
                        nextMusic();
                        pushAction(MusicService.MUSIC_PLAY_STATUS_NEXT, id);
                    }
                }
        );

        if (Music.FROMASSETS.equals(musicList.get(0).getFrom())){
            playMusicFormAssets(0);
        }else if (Music.FROMINTERNET.equals(musicList.get(0).getFrom())) {
            playMusicFromInternet(0);
        }
    }
    /**
     * 将播放assets中的文件封装为方法
     * */
    public void playMusicFormAssets(int i) {
        try {
            AssetFileDescriptor file = getAssets().openFd("musics/"+musicList.get(i).getFilename());
            Log.d("选项", musicList.get(i).getFilename() +"\t 来自文件"+ i);
            mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void playMusicFromInternet(int i) {
        Log.d("选项", musicList.get(i).getFilename() +"\t 来自网络"+ i);
        try {
            mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(musicList.get(i).getFilename()));
            mediaPlayer.prepare();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    /**
     * 播放音乐
     * */
    public void playMusic(int i) {
        if (mediaPlayer != null) {
            mediaPlayer.reset();
        }
        if (Music.FROMASSETS.equals(musicList.get(i).getFrom())){
            playMusicFormAssets(i);
        }else if (Music.FROMINTERNET.equals(musicList.get(i).getFrom())) {
            playMusicFromInternet(i);
        }
        id = i;
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            isPlaying = true;
        }
    }

    /**
     * 上一首
     * */
    public void preMusic() {

        if (id > 0) {
            id--;
        }else {
            id = musicList.size()-1;
        }

        if (mediaPlayer != null) {
            mediaPlayer.reset();
        }

        if (Music.FROMASSETS.equals(musicList.get(id).getFrom())){
            playMusicFormAssets(id);
        }else if (Music.FROMINTERNET.equals(musicList.get(id).getFrom())) {
            playMusicFromInternet(id);
        }
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            isPlaying = true;
        }
    }

    /**
     * 下一首
     * */
    public void nextMusic() {

        if (id < musicList.size()-1) {
            id++;
        }else {
            id = 0;
        }
        if (mediaPlayer != null) {
            mediaPlayer.reset();
        }
        if (Music.FROMASSETS.equals(musicList.get(id).getFrom())){
            playMusicFormAssets(id);
        }else if (Music.FROMINTERNET.equals(musicList.get(id).getFrom())) {
            playMusicFromInternet(id);
        }
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            isPlaying = true;
        }

    }

    /**
     * 播放/暂停
     * */
    public void playOrPause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
        }else {
            mediaPlayer.start();
            isPlaying = true;
        }
    }

    /**
     * 停止播放
     * */
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            isPlaying = false;
            try {
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void seekTo(int time) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(time);
        }else {
            mediaPlayer.start();
            mediaPlayer.seekTo(time);
        }
    }
    /**
     * 销毁music对象
     * */
    public void destroy() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    public List<Music> getMusics() {
        return musicList;
    }

    public int getId() {
        return id;
    }

//    public int getMusicProcess() {
//        if (mediaPlayer != null) {
//            return mediaPlayer.getCurrentPosition();
//        }else {
//            return 0;
//        }
//    }

    private void pushAction(int status, int musicId) {
        Intent actionToWidget = new Intent(getApplicationContext(), MusicAppWidget.class);
        actionToWidget.setAction(MusicService.MUSIC_CHANGE);
        actionToWidget.putExtra("method",status);
        actionToWidget.putExtra("musicID", musicId);

//        actionIntent.putExtra("title", music.getTitle());
//        actionIntent.putExtra("artist", music.getArtist());
//        actionIntent.putExtra("cover", music.getCover());
//        actionIntent.putExtra("albumTitle", music.getAlbumTitle());

        getApplicationContext().sendBroadcast(actionToWidget);

        Intent actionToMain = new Intent(MusicService.MUSIC_CHANGE);
//        actionToMain.setAction();
        actionToMain.putExtra("method", status);
        actionToMain.putExtra("musicID", musicId);
        getApplicationContext().sendBroadcast(actionToMain);
    }

    public void pushActionWithTime(int status, int musicId){
        Intent actionToWidget = new Intent(getApplicationContext(), MusicAppWidget.class);
        actionToWidget.setAction(MusicService.MUSIC_CHANGE);
        actionToWidget.putExtra("method",status);
        actionToWidget.putExtra("musicID", musicId);
        getApplicationContext().sendBroadcast(actionToWidget);

        Intent actionToMain = new Intent(MusicService.MUSIC_CHANGE);
        actionToMain.putExtra("method", status);
        actionToMain.putExtra("musicID", musicId);
        actionToMain.putExtra("currentPosition", mediaPlayer.getCurrentPosition());
        getApplicationContext().sendBroadcast(actionToMain);
    }
}
