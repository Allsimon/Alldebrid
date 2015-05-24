package com.malek.alldebrid;

import android.widget.TextView;

import com.malek.alldebrid.API.abstracted.AbstractDebrider;
import com.malek.alldebrid.API.abstracted.DebridObserver;
import com.malek.alldebrid.API.abstracted.SingletonHolder;
import com.malek.alldebrid.API.pojo.Account;
import com.malek.alldebrid.API.pojo.Link;
import com.malek.alldebrid.API.pojo.Torrent;
import com.malek.alldebrid.ui.adapter.NavDrawerItem;
import com.malek.alldebrid.ui.fragment.DebridFragment;
import com.malek.alldebrid.ui.fragment.LinkFragment_;
import com.malek.alldebrid.ui.fragment.LoginFragment_;
import com.malek.alldebrid.ui.fragment.PreferenceFragment;
import com.malek.alldebrid.ui.fragment.TorrentFragment_;
import com.malek.alldebrid.utils.Logg;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EActivity;

import java.util.ArrayList;


@EActivity
public class MyActivity extends AbstractActivity implements DebridFragment.FragmentChanger, DebridObserver {


    @AfterInject
    public void addCallbacks() {
        SingletonHolder.SINGLETON.init(this);
        SingletonHolder.SINGLETON.getDebrider().registerObserver(this);
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
        mNavDrawerItems.add(new NavDrawerItem(getResources().getString(R.string.torrent), R.drawable.ic_torrent, SingletonHolder.SINGLETON.getPersister().getTorrentsAmount()) {
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
        mNavDrawerItems.add(new NavDrawerItem(getResources().getString(R.string.account), R.drawable.ic_account, SingletonHolder.SINGLETON.getPersister().getAccount().daysLeft()) {
            @Override
            public void onClick() {
                changeFragment(new LoginFragment_());
            }
        });
        refreshHeader();
        refreshNavDrawer();
    }

    private void refreshHeader() {
        Account account = SingletonHolder.SINGLETON.getPersister().getAccount();
        ((TextView) findViewById(R.id.tv_dl_username)).setText(account.getUsername());
        ((TextView) findViewById(R.id.tv_dl_secondary)).setText(account.getEmailAdress());
    }

    @Override
    public void initFragment() {
        if (SingletonHolder.SINGLETON.getDebrider().isLogged())
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
        if (AbstractDebrider.LOGIN_SUCCESSFUL == status) {
            changeFragment(new LinkFragment_());
            prepareListData();
        }
    }

    @Override
    public void onTorrentFetched(Torrent[] torrents) {
        SingletonHolder.SINGLETON.getPersister().persistTorrentsAmount(torrents.length);
        // Refresh the torrent counter in the nav drawer
        prepareListData();
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
