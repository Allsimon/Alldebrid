package com.malek.alldebrid;

import android.widget.TextView;

import com.malek.alldebrid.API.API_Alldebrid;
import com.malek.alldebrid.API.abstracted.AbstractAlldebrid;
import com.malek.alldebrid.API.abstracted.AlldebridObserver;
import com.malek.alldebrid.API.pojo.Account;
import com.malek.alldebrid.API.pojo.Link;
import com.malek.alldebrid.API.pojo.Torrent;
import com.malek.alldebrid.ui.adapter.NavDrawerItem;
import com.malek.alldebrid.ui.fragment.AlldebridFragment;
import com.malek.alldebrid.ui.fragment.LinkFragment_;
import com.malek.alldebrid.ui.fragment.LoginFragment_;
import com.malek.alldebrid.ui.fragment.PreferenceFragment;
import com.malek.alldebrid.ui.fragment.TorrentFragment_;
import com.malek.alldebrid.utils.Logg;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EActivity;

import java.util.ArrayList;


@EActivity
public class MyActivity extends AbstractActivity implements AlldebridFragment.FragmentChanger, AlldebridObserver {


    @AfterInject
    public void addCallbacks() {
        API_Alldebrid.init(this);
        API_Alldebrid.getInstance().registerObserver(this);
    }


    @Override
    public void prepareListData() {
        mNavDrawerItems = new ArrayList<>();
        mNavDrawerItems.add(new NavDrawerItem(getResources().getString(R.string.debridLinks), R.drawable.ic_download) {
            @Override
            public void onClick() {
                changeFragment(new LinkFragment_());
            }
        });
        mNavDrawerItems.add(new NavDrawerItem(getResources().getString(R.string.torrent), R.drawable.ic_torrent, API_Alldebrid.getInstance().getTorrentNumbers()) {
            public void onClick() {
                changeFragment(new TorrentFragment_());
            }
        });
        mNavDrawerItems.add(new NavDrawerItem(getResources().getString(R.string.setting), R.drawable.ic_action_settings) {
            @Override
            public void onClick() {
                changeFragment(new PreferenceFragment());
            }
        });
        mNavDrawerItems.add(new NavDrawerItem(getResources().getString(R.string.account), R.drawable.ic_account, API_Alldebrid.getInstance().getAccountInfo().daysLeft()) {
            @Override
            public void onClick() {
                changeFragment(new LoginFragment_());
            }
        });
        refreshHeader();
        refreshNavDrawer();
    }

    private void refreshHeader() {
        Account account = API_Alldebrid.getInstance().getAccountInfo();
        ((TextView) findViewById(R.id.tv_dl_username)).setText(account.getUsername());
        ((TextView) findViewById(R.id.tv_dl_secondary)).setText(account.getEmailAdress());
    }

    @Override
    public void initFragment() {
        if (API_Alldebrid.getInstance().isLogged())
            changeFragment(new LinkFragment_());
        else
            changeFragment(new LoginFragment_());
    }

    @Override
    public void onLinkRestrained(Link link) {

    }

    @Override
    public void onTorrentAdded(Torrent torrent) {
        prepareListData();
    }

    @Override
    public void onTorrentRemoved(Torrent torrent) {

    }

    @Override
    public void onSomethingBugged(int status, String text) {
        Logg.e(text);
    }

    @Override
    public void onLogin(int status) {
        if (AbstractAlldebrid.LOGIN_SUCCESSFUL == status) {
            changeFragment(new LinkFragment_());
            prepareListData();
        }
    }

    @Override
    public void onTorrentFetched(Torrent[] torrents) {

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
