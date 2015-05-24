package com.malek.alldebrid.ui.adapter;


import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.malek.alldebrid.API.pojo.Torrent;
import com.malek.alldebrid.R;
import com.malek.alldebrid.ui.listener.TorrentOnMenuItemClickListener;

public class TorrentAdapter extends BaseAdapter {

    private final Context mContext;
    Torrent[] torrents;
    LayoutInflater inflater;

    public TorrentAdapter(Context context, Torrent[] torrents) {
        inflater = LayoutInflater.from(context);
        this.torrents = torrents;
        mContext = context;
    }

    public int getCount() {
        return torrents.length;
    }

    public Object getItem(int position) {
        return torrents[position];
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View cv, ViewGroup parent) {
        ViewHolder holder;
        final Torrent torrent = torrents[position];

        if (cv == null) {
            holder = new ViewHolder();
            cv = inflater.inflate(R.layout.adapter_links, parent, false);
            holder.tvName = getTextView(R.id.tv_name, cv);
            holder.tvWeight = getTextView(R.id.tv_weight, cv);
            holder.tvStatus = getTextView(R.id.tv_host, cv);
            holder.ibOverflow = (ImageButton) cv.findViewById(R.id.ibOverflow);
            cv.setTag(holder);
        } else {
            holder = (ViewHolder) cv.getTag();
        }
        holder.tvName.setText(torrent.getName());
        if ("finished".equals(torrent.getStatus())) {
            holder.tvWeight.setText(torrent.getWeight());
            String linkCount = torrent.getLinks().length + " ";
            if (torrent.getLinks().length > 1)
                linkCount += mContext.getResources().getString(R.string.links);
            else
                linkCount += mContext.getResources().getString(R.string.link);
            holder.tvStatus.setText(linkCount);
        } else {
            holder.tvWeight.setText(torrent.getDownloaded() + "/" + torrent.getWeight());
            String downloadStatus = torrent.getStatus() + " at " + torrent.getDownloadSpeed();
            if ("0".equals(torrent.getSeederNumber()) || "1".equals(torrent.getSeederNumber())) {
                downloadStatus += " (" + torrent.getSeederNumber() + " seeder)";
            } else {
                downloadStatus += " (" + torrent.getSeederNumber() + " seeders)";
            }
            holder.tvStatus.setText(downloadStatus);
        }
        holder.ibOverflow.setTag(R.id.ibOverflow, position);
        holder.ibOverflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup(view, torrent);
            }
        });
        return cv;
    }

    public void showPopup(View v, final Torrent torrent) {
        PopupMenu popup = new PopupMenu(mContext, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.action_torrent, popup.getMenu());
        popup.show();
        popup.setOnMenuItemClickListener(new TorrentOnMenuItemClickListener() {
            @Override
            public Torrent getTorrent() {
                return torrent;
            }
        });
    }

    private TextView getTextView(int id, View cv) {
        return (TextView) cv.findViewById(id);
    }

    private class ViewHolder {
        TextView tvName, tvWeight, tvStatus;
        ImageButton ibOverflow;
    }
}