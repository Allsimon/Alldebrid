package com.malek.alldebrid.API.abstracted;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.malek.alldebrid.API.pojo.Link;
import com.malek.alldebrid.API.pojo.Torrent;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDebrider implements DebridObservable {
    public static final String DOWNLOAD_MANAGER = "downloadManager";
    public static final int LOGIN_FAILED = 0;
    public static final int LOGIN_SUCCESSFUL = 1;
    protected List<DebridObserver> mDebridObserver;
    protected SharedPreferences mPreferences;
    protected Context mContext;

    public void init(Context context) {
        this.mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.mContext = context;
        this.mDebridObserver = new ArrayList<>();
    }

    public abstract void getTorrents();

    public abstract void getLimitedHostsAndDownloadInfos();

    public abstract void addTorrent(String data, boolean isMagnet, boolean splitTheFile);

    public abstract void removeTorrent(Torrent torrent);

    public abstract void unrestrainLink(String links);

    public abstract void login(String username, String password);

    public abstract void softLogin(String username, String password);

    public void saveFile(final Link link) {
        new Thread() {
            public void run() {
                String adress = link.getUnrestrainedLink();
                if (mPreferences.getBoolean(DOWNLOAD_MANAGER, false)) {
                    DownloadManager dm = (DownloadManager) mContext
                            .getSystemService(Context.DOWNLOAD_SERVICE);
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(adress));
                    request.setTitle(link.getName());
                    request.setDestinationInExternalPublicDir(
                            Environment.DIRECTORY_DOWNLOADS, link.getName());
                    dm.enqueue(request);

                } else {
                    Uri uri = Uri.parse(adress);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(uri);
                    mContext.startActivity(intent);
                }
            }
        }.run();
    }


    public abstract boolean isLogged();

    @Override
    public void registerObserver(DebridObserver observer) {
        mDebridObserver.add(observer);
    }

    @Override
    public void removeObserver(DebridObserver observer) {
        mDebridObserver.remove(observer);
    }

    @Override
    public void notifyObserverLinkRestrained(Link link) {
        for (DebridObserver observer : mDebridObserver) {
            observer.onLinkRestrained(link);
        }
    }

    @Override
    public void notifyObserverLinkRestrainFailed(Link link, String error) {
        for (DebridObserver observer : mDebridObserver) {
            observer.onLinkRestrainFailed(link, error);
        }
    }

    @Override
    public void notifyObserverTorrentAdded(Torrent torrent) {
        for (DebridObserver observer : mDebridObserver) {
            observer.onTorrentAdded(torrent);
        }
    }

    @Override
    public void notifyObserverTorrentRemoved(Torrent torrent) {
        for (DebridObserver observer : mDebridObserver) {
            observer.onTorrentRemoved(torrent);
        }
    }

    @Override
    public void notifyObserverSomethingBugged(int status, String text) {
        for (DebridObserver observer : mDebridObserver) {
            observer.onSomethingBugged(status, text);
        }
    }

    @Override
    public void notifyObserverLoginStatus(int status) {
        for (DebridObserver observer : mDebridObserver) {
            observer.onLogin(status);
        }
    }

    @Override
    public void notifyObserverTorrentListFetched(Torrent[] torrents) {
        if (torrents != null) {
            for (DebridObserver observer : mDebridObserver) {
                observer.onTorrentFetched(torrents);
            }
        }
    }

    public void notifyObserverLimitedHostsFetched(String[] limitedHosts) {
        for (DebridObserver observer : mDebridObserver) {
            observer.onLimitedHostsFetched(limitedHosts);
        }
    }

    public void notifyObserverDownloadInformationsFetched(String[] downloadedInfos) {
        for (DebridObserver observer : mDebridObserver) {
            observer.onDownloadInformationsFetched(downloadedInfos);
        }
    }

}
