package com.malek.alldebrid;

import android.os.Bundle;
import android.widget.Toast;

import com.malek.alldebrid.alldebrid.API_Alldebrid;
import com.malek.alldebrid.alldebrid.Link;
import com.malek.alldebrid.alldebrid.Torrent;
import com.malek.alldebrid.alldebrid.abstracted.AbstractAlldebrid;
import com.malek.alldebrid.alldebrid.abstracted.AlldebridObserver;
import com.malek.alldebrid.alldebrid.ui.AlldebridFragment;
import com.malek.alldebrid.ui.ActionBarDrawerActivity;
import com.malek.alldebrid.ui.NavDrawerItem;

import java.util.ArrayList;

public class MyActivity extends ActionBarDrawerActivity implements AlldebridObserver, AlldebridFragment.FragmentChanger {
    private static final String LAST_FRAGMENT = "last_fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        API_Alldebrid.init(this);
        API_Alldebrid.getInstance().registerObserver(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void prepareListData() {
        mNavDrawerItems = new ArrayList<NavDrawerItem>();
        if (API_Alldebrid.getInstance().isLogged()) {
            mNavDrawerItems.add(new NavDrawerItem(getResources().getString(R.string.debridLinks), R.drawable.ic_download));
            mNavDrawerItems.add(new NavDrawerItem(getResources().getString(R.string.torrent), R.drawable.ic_torrent, true, API_Alldebrid.getInstance().getTorrentNumbers()));
            mNavDrawerItems.add(new NavDrawerItem(getResources().getString(R.string.setting), R.drawable.ic_action_settings));
            mNavDrawerItems.add(new NavDrawerItem(getResources().getString(R.string.account), R.drawable.ic_account));
        }
    }

    @Override
    public void initFragment() {
        if (API_Alldebrid.getInstance().isLogged()) {
            changeFragment(new LinkFragment());
        } else {
            changeFragment(new LoginFragment());
        }
    }

    @Override
    public void onNavDrawerClickListener(int groupPosition) {
        switch (groupPosition) {
            case 0:
                changeFragment(new LinkFragment());
                break;
            case 1:
                changeFragment(new TorrentFragment());
                break;
            case 2:
                try {
                    changeFragment(new PreferenceFragment());
                } catch (NoClassDefFoundError e) {
                    Toast.makeText(this, "Sorry, this doesn't work for Android <4  :'(", Toast.LENGTH_SHORT).show();
                }
                break;
            case 3:
                changeFragment(new LoginFragment());
                break;
        }
        mDrawerLayout.closeDrawer(mDrawerList);
    }


    @Override
    public void onLinkRestrained(Link link) {

    }

    @Override
    public void onTorrentAdded(Torrent torrent) {

    }

    @Override
    public void onTorrentRemoved(Torrent torrent) {

    }

    @Override
    public void onSomethingBugged(int status, String text) {

    }

    @Override
    public void onLogin(int status) {
        if (status == AbstractAlldebrid.LOGIN_SUCCESSFUL) {
            prepareListData();
            refreshNavDrawer();
            changeFragment(new LinkFragment());
        } else {
        }
    }

    @Override
    public void onTorrentFetched(Torrent[] torrents) {
        mNavDrawerItems.get(1).setCount(torrents.length);
        refreshNavDrawer();
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
