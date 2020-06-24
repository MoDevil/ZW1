package com.modev.zw1;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import com.modev.zw1.bean.Music;
import com.modev.zw1.bean.MusicUitl;
import com.modev.zw1.service.MusicService;

/**
 * Implementation of App Widget functionality.
 */
public class MusicAppWidget extends AppWidgetProvider {

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.music_app_widget);

        Log.d("选项", "到这里了Receive");
        if (MusicService.MUSIC_CHANGE.equals(intent.getAction())) {
            switch (intent.getIntExtra("method", -1)) {
                case MusicService.MUSIC_PLAY_STATUS_PAUSE:
                    rv.setImageViewResource(R.id.musicWg_bt_play, R.drawable.play);
                    break;
                case MusicService.MUSIC_PLAY_STATUS_PLAYING:
                    rv.setImageViewResource(R.id.musicWg_bt_play, R.drawable.pause);
                    break;
                case MusicService.MUSIC_PLAY_STATUS_STOP:
                    rv.setTextViewText(R.id.musicWg_tv_title, "未播放");
                    rv.setImageViewResource(R.id.musicWg_iv_cover, R.drawable.music);
                    rv.setImageViewResource(R.id.musicWg_bt_play, R.drawable.play);
                    break;
                case MusicService.MUSIC_PLAY_STATUS_PRE:
                    rv.setImageViewResource(R.id.musicWg_bt_play, R.drawable.pause);
                    break;
                case MusicService.MUSIC_PLAY_STATUS_NEXT:
                    rv.setImageViewResource(R.id.musicWg_bt_play, R.drawable.pause);
                    break;
                case MusicService.MUSIC_PLAY_STATUS_PLAY_ONE:
                    rv.setImageViewResource(R.id.musicWg_bt_play, R.drawable.pause);
                    break;
                case MusicService.MUSIC_SEEK_TO:
                    rv.setImageViewResource(R.id.musicWg_bt_play, R.drawable.pause);
                    break;
            }
            int musicId = intent.getIntExtra("musicID", -1);
            if (musicId != -1) {
                Music music = MusicUitl.getMusicList().get(musicId);
                rv.setTextViewText(R.id.musicWg_tv_title, music.getTitle());
                if (music.getCover() != null) {
                    rv.setImageViewBitmap(R.id.musicWg_iv_cover, music.getCover());
                }else{
                    rv.setImageViewResource(R.id.musicWg_iv_cover, R.drawable.music);
                }
            }
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName componentName = new ComponentName(context, MusicAppWidget.class);
        appWidgetManager.updateAppWidget(componentName, rv);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        super.onUpdate(context, appWidgetManager, appWidgetIds);
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.music_app_widget);

        Intent startAPP = new Intent(context, MainActivity.class);
        PendingIntent startAPPpending = PendingIntent.getActivity(context, 0, startAPP, 0);

        rv.setOnClickPendingIntent(R.id.musicWg_iv_cover, startAPPpending);
        rv.setOnClickPendingIntent(R.id.musicWg_bt_pre, getPendingIntent(context, MusicService.MUSIC_PRE));
        rv.setOnClickPendingIntent(R.id.musicWg_bt_play, getPendingIntent(context, MusicService.MUSIC_PLAYORPAUSE));
        rv.setOnClickPendingIntent(R.id.musicWg_bt_stop, getPendingIntent(context, MusicService.MUSIC_STOP));
        rv.setOnClickPendingIntent(R.id.musicWg_bt_nex, getPendingIntent(context, MusicService.MUSIC_NEXT));

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(new ComponentName(context, MusicAppWidget.class), rv);

    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    public PendingIntent getPendingIntent(Context context, int method) {
//        Intent intent = new Intent(context, MusicService.class);
//        Intent intent = new Intent();
//        intent.setAction(MusicService.MUSIC_ACTION);
        Intent intent = new Intent(MusicService.MUSIC_ACTION);
//        intent.setComponent(new ComponentName("com.modev.zw1.service", "com.modev.zw1.service.MusicService.MusicBroadReceiver"));
        intent.putExtra("method",method);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, method, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        return pendingIntent;
    }

}

