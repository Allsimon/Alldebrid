package com.malek.alldebrid.alldebrid.ui.listener;

import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;

import com.malek.alldebrid.R;
import com.malek.alldebrid.alldebrid.API_Alldebrid;
import com.malek.alldebrid.alldebrid.Torrent;


public abstract class TorrentOnMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {
    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_remove_torrent:
                API_Alldebrid.getInstance().removeTorrent(getTorrent());
                break;
        }
        return true;
    }

    public abstract Torrent getTorrent();
}
