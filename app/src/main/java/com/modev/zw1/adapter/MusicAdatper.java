package com.modev.zw1.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.modev.zw1.R;
import com.modev.zw1.bean.Music;

import org.w3c.dom.Text;

import java.util.List;

public class MusicAdatper extends ArrayAdapter<Music> {
    private int resID;
    public MusicAdatper(@NonNull Context context, int resource, List<Music> list) {
        super(context, resource, list);
        resID = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Music music = getItem(position);
        View view;
        ViewHolder viewHolder;

        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resID, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.cover = view.findViewById(R.id.musicV_iv_cover);
            viewHolder.title = view.findViewById(R.id.musicV_tv_title);
            viewHolder.artist = view.findViewById(R.id.musicV_tv_artist);
            view.setTag(viewHolder);
        }else {
            view = convertView;
            viewHolder = (ViewHolder)view.getTag();
        }

        if (music.getCover() == null){
            viewHolder.cover.setImageResource(R.drawable.music);
        }else {
            viewHolder.cover.setImageBitmap(music.getCover());
        }
        viewHolder.title.setText(music.getTitle());
        viewHolder.artist.setText(music.getArtist());

        return view;
    }

    class ViewHolder{
        ImageView cover;
        TextView title;
        TextView artist;
    }

    public void setMusicList(List<Music> musicList) {
        super.clear();
        super.addAll(musicList);
        super.notifyDataSetChanged();
    }
}
