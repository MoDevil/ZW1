package com.modev.zw1.bean;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MusicUitl {
    private static List<Music> musicList;

    public static List<Music> getMusicList() {
        return musicList;
    }

    public MusicUitl(Context context) {
        musicList = getMusicsFromAssets(context);
//        musicList = getMusicsFromInternet();

    }
    public void setMusicList(List<Music> musicList) {
        this.musicList = musicList;
    }
    public List<Music> changeMusicFrom(String from, Context context) {
        if (Music.FROMASSETS.equals(from)) {
            musicList = getMusicsFromAssets(context);
        }else if (Music.FROMINTERNET.equals(from)) {
            musicList = getMusicsFromInternet();
        }else if (Music.FROMSDCARD.equals(from)) {

        }else {

        }

        return musicList;
    }
    /**
     * 从assets文件夹获取音乐文件，并将其封装为Music对象
     * @param context
     * @return 封装过的Music对象列表
     * */
    public List<Music> getMusicsFromAssets(Context context) {

        AssetManager assetManager = context.getAssets();
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();

        String[] fileNames = null;
        List<Music> musicList = new ArrayList<>();

        try {
            //获取文件列表
            fileNames = assetManager.list("musics");

            //通过文件列表获得具体的文件，然后读取其信息，将其转换为Music对象
            for (String fileName : fileNames) {
                Music music = new Music();
                AssetFileDescriptor file = context.getAssets().openFd("musics/"+fileName);
                mediaMetadataRetriever.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());

                music.setFilename(fileName);
                music.setFrom(Music.FROMASSETS);
                music.setTitle(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
                music.setArtist(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
                music.setAlbumTitle(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
                music.setLength(Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.	METADATA_KEY_DURATION)));
                byte[] artwork = mediaMetadataRetriever.getEmbeddedPicture();;

                if (artwork != null) {
                    music.setCover(BitmapFactory.decodeByteArray(artwork, 0, artwork.length));
                }

                musicList.add(music);
            }
        }catch (IOException e) {
            e.printStackTrace();
        }

        return musicList;
    }

    public List<Music> getMusicsFromInternet() {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        List<Music> musicList = new ArrayList<>();

        for (String url : list) {
            Music music = new Music();
//            mediaMetadataRetriever.setDataSource(context, Uri.parse(url));
            mediaMetadataRetriever.setDataSource(url, new HashMap<String, String>());
            music.setFrom(Music.FROMINTERNET);
            music.setFilename(url);
//            music.setTitle(url);
            music.setTitle(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
            music.setArtist(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
            music.setAlbumTitle(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
            music.setLength(Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.	METADATA_KEY_DURATION)));
            byte[] artwork = mediaMetadataRetriever.getEmbeddedPicture();;

            if (artwork != null) {
                music.setCover(BitmapFactory.decodeByteArray(artwork, 0, artwork.length));
            }

            musicList.add(music);
        }
        return musicList;
    }

    private String[] list = {
            "http://music.163.com/song/media/outer/url?id=458548653.mp3",
            "http://music.163.com/song/media/outer/url?id=324376.mp3",
            "http://music.163.com/song/media/outer/url?id=1436207562.mp3",
            "http://music.163.com/song/media/outer/url?id=31445554.mp3",
            "http://music.163.com/song/media/outer/url?id=1387600215.mp3",
            "http://music.163.com/song/media/outer/url?id=1412259763.mp3",
            "http://music.163.com/song/media/outer/url?id=1433939296.mp3"
    };
}
