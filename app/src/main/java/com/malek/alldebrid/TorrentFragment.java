package com.malek.alldebrid;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.dd.processbutton.iml.ActionProcessButton;
import com.ipaulpro.afilechooser.utils.FileUtils;
import com.malek.alldebrid.alldebrid.API_Alldebrid;
import com.malek.alldebrid.alldebrid.Link;
import com.malek.alldebrid.alldebrid.Torrent;
import com.malek.alldebrid.alldebrid.ui.AlldebridFragment;
import com.malek.alldebrid.ui.adapter.TorrentAdapter;

public class TorrentFragment extends AlldebridFragment {
    ListView lvTorrents;
    Torrent torrents[];
    TorrentAdapter torrentAdapter;
    ActionProcessButton mButtonFilePicker;
    private final int RESULT_OK = -1;
    private final int TORRENT_PICKER = 1;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_torrent, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        lvTorrents = (ListView) view.findViewById(R.id.lvTorrents);
        mButtonFilePicker = (ActionProcessButton) view.findViewById(R.id.bAddTorrent);
        API_Alldebrid.getInstance().getTorrent();
        mButtonFilePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent getContentIntent = FileUtils.createGetContentIntent().setType("application/x-bittorrent");
                Intent intent = Intent.createChooser(getContentIntent, "Select a file");
                startActivityForResult(intent, TORRENT_PICKER);
            }
        });
        lvTorrents.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                LinkFragment linkFragment = new LinkFragment();
                Bundle args = new Bundle();
                args.putString(LinkFragment.ARG_LINKS, torrents[position].getLink());
                linkFragment.setArguments(args);
                mFragmentChanger.changeFragment(linkFragment);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TORRENT_PICKER:
                if (resultCode == RESULT_OK) {
                    final Uri uri = data.getData();
                    String path = FileUtils.getPath(getActivity(), uri);
                    API_Alldebrid.getInstance().addTorrent(path, false, true);
                }
                break;
        }
    }

    private void refreshAdapter() {
        try {
            torrentAdapter = new TorrentAdapter(getActivity(), torrents);
            lvTorrents.setAdapter(torrentAdapter);
        } catch (Exception e) {
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
        API_Alldebrid.getInstance().getTorrent();
    }

    @Override
    public void onTorrentRemoved(Torrent torrent) {
        API_Alldebrid.getInstance().getTorrent();
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
        refreshAdapter();
    }

    @Override
    public void onLimitedHostsFetched(String[] limitedHosts) {

    }

    @Override
    public void onDownloadInformationsFetched(String[] downloadedInfos) {

    }

    @Override
    public void onLinkRestrainFailed(int status) {

    }
}
