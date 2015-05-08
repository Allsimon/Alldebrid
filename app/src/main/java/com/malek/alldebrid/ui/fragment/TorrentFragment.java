package com.malek.alldebrid.ui.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.dd.processbutton.iml.ActionProcessButton;
import com.malek.alldebrid.API.API_Alldebrid;
import com.malek.alldebrid.API.pojo.Link;
import com.malek.alldebrid.API.pojo.Torrent;
import com.malek.alldebrid.R;
import com.malek.alldebrid.ui.adapter.TorrentAdapter;
import com.malek.alldebrid.utils.Logg;
import com.nononsenseapps.filepicker.FilePickerActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;

@EFragment(R.layout.fragment_torrent)
public class TorrentFragment extends AlldebridFragment {
    @ViewById(R.id.lvTorrents)
    ListView lvTorrents;
    @ViewById(R.id.bAddTorrent)
    ActionProcessButton mButtonFilePicker;
    Torrent torrents[];
    TorrentAdapter torrentAdapter;
    Handler h = new Handler();
    int delay = 2000; //milliseconds
    int FILE_CODE = 69;

    @AfterViews
    public void afterView() {
        API_Alldebrid.getInstance().getTorrents();
        mButtonFilePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), FilePickerActivity.class)
//                Intent i = new Intent(Intent.ACTION_GET_CONTENT)
                        .setType("application/x-bittorrent")
                        .putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, true)
                        .putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false)
                        .putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);

                startActivityForResult(i, FILE_CODE);
            }
        });
        lvTorrents.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                LinkFragment linkFragment = new LinkFragment_();
                Bundle args = new Bundle();
                args.putString(LinkFragment.ARG_LINKS, torrents[position].getUnrestrainedLink());
                linkFragment.setArguments(args);
                mFragmentChanger.changeFragment(linkFragment);
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_CODE && resultCode == Activity.RESULT_OK) {
            if (data.getBooleanExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)) {
                // For JellyBean and above
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ClipData clip = data.getClipData();
                    if (clip != null) {
                        for (int i = 0; i < clip.getItemCount(); i++) {
                            Uri uri = clip.getItemAt(i).getUri();
                            API_Alldebrid.getInstance().addTorrent(uri.getPath(), false, true);
                        }
                    }
                    // For Ice Cream Sandwich
                } else {
                    ArrayList<String> paths = data.getStringArrayListExtra
                            (FilePickerActivity.EXTRA_PATHS);

                    if (paths != null) {
                        for (String path : paths) {
                            Uri uri = Uri.parse(path);
                            API_Alldebrid.getInstance().addTorrent(uri.getPath(), false, true);
                        }
                    }
                }

            } else {
                Uri uri = data.getData();
                API_Alldebrid.getInstance().addTorrent(uri.getPath(), false, true);

                Logg.e(uri.getPath());
            }
        }
    }

    private void refreshAdapter() {
        try {
            torrentAdapter = new TorrentAdapter(getActivity(), torrents);
            lvTorrents.setAdapter(torrentAdapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onStop() {
        super.onStop();
    }

    @Override
    public void onLinkRestrained(Link link) {

    }

    @Override
    public void onTorrentAdded(Torrent torrent) {
        API_Alldebrid.getInstance().getTorrents();
    }

    @Override
    public void onTorrentRemoved(Torrent torrent) {
        API_Alldebrid.getInstance().getTorrents();
    }

    @Override
    public void onSomethingBugged(int status, String text) {

    }

    @Override
    public void onLogin(int status) {

    }

    @Override
    public void onTorrentFetched(Torrent[] torrents) {
        this.torrents = torrents;
        boolean needRefresh = false;
        for (Torrent torrent : torrents) {
            if (!"finished".equals(torrent.getStatus()))
                needRefresh = true;
        }
        if (needRefresh)
            h.postDelayed(new Runnable() {
                public void run() {
                    API_Alldebrid.getInstance().getTorrents();
                }
            }, delay);
        refreshAdapter();
    }

    @Override
    public void onLimitedHostsFetched(String[] limitedHosts) {

    }

    @Override
    public void onDownloadInformationsFetched(String[] downloadedInfos) {

    }

    @Override
    public void onLinkRestrainFailed(Link link, String error) {

    }
}
