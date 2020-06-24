package com.modev.zw1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.modev.zw1.adapter.MusicAdatper;
import com.modev.zw1.bean.Music;
import com.modev.zw1.bean.MusicUitl;
import com.modev.zw1.service.MusicService;

import java.util.List;

public class MusicListActivity extends AppCompatActivity {
    private List<Music> musicList;
    private MusicAdatper adatper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);

        ListView musiclv = findViewById(R.id.music_lv_musics);
        musiclv.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent();
                        intent.putExtra("musicId", position);
                        setResult(0x323, intent);
                        finish();
                    }
                }
        );

        musicList = MusicUitl.getMusicList();
        adatper = new MusicAdatper(MusicListActivity.this, R.layout.music_view, musicList);
        musiclv.setAdapter(adatper);

        Button asstesBtn = findViewById(R.id.music_bt_Assets);
        Button sdCardBtn = findViewById(R.id.music_bt_SDCard);
        Button internetBtn = findViewById(R.id.music_bt_Internet);

        MusicListAction listAction = new MusicListAction();
        asstesBtn.setOnClickListener(listAction);
        sdCardBtn.setOnClickListener(listAction);
        internetBtn.setOnClickListener(listAction);
    }
    /**
     * 切换音乐来源事件监听
     * */
    class MusicListAction implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.music_bt_Assets:
                    Log.d("选项", "Assets");
                    pushAction(Music.FROMASSETS);
                    break;
                case R.id.music_bt_SDCard:
                    Log.d("选项", "SDCard");
                    pushAction(Music.FROMSDCARD);
                    break;
                case R.id.music_bt_Internet:
                    Log.d("选项", "Internet");
                    pushAction(Music.FROMINTERNET);
                    break;
            }
            finish();
//            musicList = MusicUitl.getMusicList();
//            adatper.setMusicList(musicList);
        }
    }

    /**
     * 发送广播
     * */
    private void pushAction(String from) {
        Intent actionIntent = new Intent(MusicService.MUSIC_ACTION);
        actionIntent.putExtra("method",MusicService.MUSIC_LIST_CHANGE);
        actionIntent.putExtra("from", from);
        getApplicationContext().sendBroadcast(actionIntent);
    }
}
