package com.modev.zw1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.modev.zw1.bean.Music;
import com.modev.zw1.bean.MusicUitl;
import com.modev.zw1.service.MusicService;

public class MainActivity extends AppCompatActivity {
    private MainMusicReceiver receiver;
    private ImageView coverIV;
    private TextView titleTV;
    private TextView artistTV;
    private SeekBar musicProgress;
    private ImageButton preBtn;
    private ImageButton plOrpaBtn;
    private ImageButton nexBtn;
    private ImageButton stpBtn;
    private ImageButton lisBtn;

    public Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0x887) {
                musicProgress.setProgress(musicProgress.getProgress()+1000);
//                Log.d("选项", "循环"+musicProgress.getProgress());
                if (musicProgress.getProgress() < musicProgress.getMax()) {
                    handler.sendEmptyMessageDelayed(0x887, 1000);
                }
            }
        }
    };

    private Intent serviceIn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        serviceIn = new Intent(MainActivity.this, MusicService.class);
        startService(serviceIn);
        init();

        receiver = new MainMusicReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicService.MUSIC_CHANGE);
        registerReceiver(receiver, intentFilter);

        pushAction(MusicService.MUSIC_INQUIRY_STATUS);
//        update = new UIprogress();
//        new Thread(update).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pushAction(MusicService.MUSIC_STOP);
        stopService(serviceIn);
        unregisterReceiver(receiver);

    }

    public void init() {

        coverIV = findViewById(R.id.main_iv_cover);
        titleTV = findViewById(R.id.main_tv_title);
        artistTV = findViewById(R.id.main_tv_artist);

        //对几个按钮初始化
        preBtn = findViewById(R.id.main_btn_pre);
        plOrpaBtn = findViewById(R.id.main_btn_playOrPause);
        nexBtn = findViewById(R.id.main_btn_nex);
        stpBtn = findViewById(R.id.main_btn_stop);
        lisBtn = findViewById(R.id.main_btn_list);

        MusicButtonActionListener musicListener = new MusicButtonActionListener();
        preBtn.setOnClickListener(musicListener);
        plOrpaBtn.setOnClickListener(musicListener);
        nexBtn.setOnClickListener(musicListener);
        stpBtn.setOnClickListener(musicListener);
        lisBtn.setOnClickListener(musicListener);

        //初始化seekBar
        musicProgress = findViewById(R.id.main_sb_progress);
        musicProgress.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            Intent intent = new Intent(MusicService.MUSIC_ACTION);
                            intent.putExtra("method", MusicService.MUSIC_SEEK_TO);
                            intent.putExtra("time", progress);
                            getApplicationContext().sendBroadcast(intent);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                }
        );
    }

//    private Runnable updateUI = new Runnable() {
//        private int inc = 0;
//
//        @Override
//        public void run() {
//            handler = new Handler(){
//                @Override
//                public void handleMessage(@NonNull Message msg) {
//                    super.handleMessage(msg);
//                }
//            };
//            int p = musicProgress.getProgress();
//            if (p < musicProgress.getMax()) {
//                musicProgress.setProgress(p + inc);
//            }
//        }
//    };

    /**
     * 发送广播
     * */
    private void pushAction(int method) {
        Intent actionIntent = new Intent(MusicService.MUSIC_ACTION);
        actionIntent.putExtra("method",method);
        getApplicationContext().sendBroadcast(actionIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("选项", "到这里了：onActivityResult，播放第1首音乐");

        if (requestCode == 0x999 && resultCode == 0x323) {
            int musicId = data.getIntExtra("musicId", -1);
            if (musicId != -1) {
                Intent actionIntent = new Intent(MusicService.MUSIC_ACTION);
                actionIntent.putExtra("method",MusicService.MUSIC_PLAY_ONE);
                actionIntent.putExtra("MusicID", musicId);
                getApplicationContext().sendBroadcast(actionIntent);
            }
        }
    }

    /**
     * 按钮的事件监听
     * */
    private class MusicButtonActionListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.main_btn_pre:
                    Log.d("选项", "main_btn_pre");
                    pushAction(MusicService.MUSIC_PRE);
                    break;
                case R.id.main_btn_playOrPause:
                    Log.d("选项", "main_btn_playOrPause");
                    pushAction(MusicService.MUSIC_PLAYORPAUSE);
                    break;
                case R.id.main_btn_stop:
                    Log.d("选项", "main_btn_stop");
                    pushAction(MusicService.MUSIC_STOP);
                    break;
                case R.id.main_btn_nex:
                    Log.d("选项", "main_btn_nex");
                    pushAction(MusicService.MUSIC_NEXT);
                    break;
                case R.id.main_btn_list:
                    Log.d("选项", "main_btn_list");
                    Intent intent = new Intent(MainActivity.this, MusicListActivity.class);
                    startActivityForResult(intent, 0x999);
                    break;
            }
        }
    }

    /**
     * 主界面的消息接收方法
     * */
    private class MainMusicReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("选项", "MainMusicReceiver"+"\t " + intent.getIntExtra("musicID", -1) + "\t " +intent.getIntExtra("method", -1));
            if (MusicService.MUSIC_CHANGE.equals(intent.getAction())) {

                int musicId = intent.getIntExtra("musicID", -1);
                int currentPosition = intent.getIntExtra("currentPosition", -1);
                if (musicId != -1) {
                    Music music = MusicUitl.getMusicList().get(musicId);
                    titleTV.setText(music.getTitle());
                    artistTV.setText(music.getArtist());
                    musicProgress.setMax(music.getLength());
                    Log.d("选项", "音乐长度1\t "+music.getLength());
                    Log.d("选项", "音乐长度2\t "+musicProgress.getMax());

                    if (music.getCover() != null) {
                        coverIV.setImageBitmap(music.getCover());
                    }else {
                        coverIV.setImageResource(R.drawable.music);
                    }
                }
                switch (intent.getIntExtra("method", -1)) {
                    case MusicService.MUSIC_PLAY_STATUS_PAUSE:
                        plOrpaBtn.setImageResource(R.drawable.play);
                        handler.removeMessages(0x887);
                        if (currentPosition != -1) {
                            musicProgress.setProgress(currentPosition);
                        }
                        break;
                    case MusicService.MUSIC_PLAY_STATUS_PLAYING:
                        plOrpaBtn.setImageResource(R.drawable.pause);
                        handler.sendEmptyMessageDelayed(0x887, 1000);
                        if (currentPosition != -1) {
                            musicProgress.setProgress(currentPosition);
                        }
                        break;
                    case MusicService.MUSIC_PLAY_STATUS_STOP:
                        plOrpaBtn.setImageResource(R.drawable.play);
                        musicProgress.setProgress(0);
                        handler.removeMessages(0x887);
                        break;
                    case MusicService.MUSIC_PLAY_STATUS_PRE:
                        musicProgress.setProgress(0);
                        handler.sendEmptyMessageDelayed(0x887, 1000);
                        break;
                    case MusicService.MUSIC_PLAY_STATUS_NEXT:
                        musicProgress.setProgress(0);
                        handler.sendEmptyMessageDelayed(0x887, 1000);
                        break;
                    case MusicService.MUSIC_PLAY_STATUS_PLAY_ONE:
                        plOrpaBtn.setImageResource(R.drawable.pause);
                        musicProgress.setProgress(0);
                        handler.sendEmptyMessageDelayed(0x887, 1000);
                        break;
                    case MusicService.MUSIC_SEEK_TO:
                        plOrpaBtn.setImageResource(R.drawable.pause);
                        break;
                }
            }
        }
    }
}
