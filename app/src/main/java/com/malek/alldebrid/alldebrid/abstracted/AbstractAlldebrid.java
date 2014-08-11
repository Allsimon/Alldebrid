package com.malek.alldebrid.alldebrid.abstracted;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.malek.alldebrid.alldebrid.Link;
import com.malek.alldebrid.alldebrid.Torrent;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractAlldebrid implements AlldebridObservable {
    protected List<AlldebridObserver> mAlldebridObserver;
    protected SharedPreferences mPreferences;
    private final Context mContext;
    public static final String COOKIE = "cookie";
    public static final String TORRENT_NUMBERS = "torrentNumbers";
    public static final String LOGIN = "login";
    public static final String DATE = "date";
    public static final String PSEUDO = "pseudo";
    public static final String LAST_LOGIN = "lastLogin";
    public static final String TYPE = "type";
    public static final String PASSWORD = "password";
    public static final String DOWNLOAD_MANAGER = "downloadManager";
    public static final int LOGIN_FAILED = 0;
    public static final int LOGIN_SUCCESSFUL = 1;
    public static final int LINK_RESTRAIN_FAILED_UNKNOWN = 0;
    public static final int LINK_RESTRAIN_HOST_DOWN = 1;
    public static final int LINK_RESTRAIN_WRONG_LINK = 2;

    public AbstractAlldebrid(Context context) {
        this.mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.mContext = context;
        this.mAlldebridObserver = new ArrayList<AlldebridObserver>();
    }

    public static class SingletonHolder {
        public static AbstractAlldebrid instance;

        public static AbstractAlldebrid getInstance() {
            return instance;
        }
    }

    public static void init(Context context) {
    }

    public abstract void getTorrent();

    public abstract void getLimitedHostsAndDownloadInfos();

    public abstract void addTorrent(String data, boolean isMagnet,boolean splitTheFile);

    public abstract void removeTorrent(Torrent torrent);

    public abstract void unrestrainLink(String links);

    public abstract void login(String username, String password);

    public abstract void softLogin(String username, String password);

    public abstract boolean isLogged();

    protected Torrent[] parseTorrentsLink(String link) {
        if ("Invalid cookie.".equals(link))
            return null;
        String links[] = link.replace("[[", "").replace("]]", "")
                .split("\\],\\[");
        Torrent torrent[] = new Torrent[links.length];
        for (int i = 0; i < links.length; i++) {
            String test[] = links[i].split("\",\"");
            torrent[i] = new Torrent();
            torrent[i].setId(Integer.parseInt(test[1].replace("\"", "")));
            torrent[i].setName(test[3]);
            torrent[i].setStatus(test[4]);
            torrent[i].setDownloaded(test[5]);
            torrent[i].setWeight(test[6]);
            torrent[i].setSeederNumber(test[7]);
            torrent[i].setDownloadSpeed(test[8]);
            if (test[4].contains("finished"))
                torrent[i].setLink(test[10].replace(",;,", "\n"));
        }
        return torrent;
    }

    protected String getCookie() {
        return mPreferences.getString(COOKIE, "");
    }

    public String getTorrentNumbers() {
        return mPreferences.getString(TORRENT_NUMBERS, "");
    }

    protected String getCookieWithUID() {
        return "uid=" + mPreferences.getString(COOKIE, "");
    }


    protected String getLogin() {
        return mPreferences.getString(LOGIN, "apuoi$$12");
    }

    protected String getPassword() {
        return mPreferences.getString(PASSWORD, "apuoi$$12");
    }

    public void saveFile(final Link link) {
        new Thread() {
            public void run() {
                String adress = link.getLink();
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


    protected String parseByte(byte[] bytes) {
        try {
            return new String(bytes, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void registerObserver(AlldebridObserver observer) {
        mAlldebridObserver.add(observer);
    }

    @Override
    public void removeObserver(AlldebridObserver observer) {
        mAlldebridObserver.remove(observer);
    }

    @Override
    public void notifyObserverLinkRestrained(Link link) {
        for (AlldebridObserver observer : mAlldebridObserver) {
            observer.onLinkRestrained(link);
        }
    }

    @Override
    public void notifyObserverLinkRestrainFailed(int status) {
        for (AlldebridObserver observer : mAlldebridObserver) {
            observer.onLinkRestrainFailed(status);
        }
    }

    @Override
    public void notifyObserverTorrentAdded(Torrent torrent) {
        for (AlldebridObserver observer : mAlldebridObserver) {
            observer.onTorrentAdded(torrent);
        }
    }

    @Override
    public void notifyObserverTorrentRemoved(Torrent torrent) {
        for (AlldebridObserver observer : mAlldebridObserver) {
            observer.onTorrentRemoved(torrent);
        }
    }

    @Override
    public void notifyObserverSomethingBugged(int status, String text) {
        for (AlldebridObserver observer : mAlldebridObserver) {
            observer.onSomethingBugged(status, text);
        }
    }

    @Override
    public void notifyObserverLoginStatus(int status) {
        for (AlldebridObserver observer : mAlldebridObserver) {
            observer.onLogin(status);
        }
    }

    @Override
    public void notifyObserverTorrentListFetched(Torrent[] torrents) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(TORRENT_NUMBERS, torrents.length + "");
        editor.apply();
        for (AlldebridObserver observer : mAlldebridObserver) {
            observer.onTorrentFetched(torrents);
        }
    }

    public void notifyObserverLimitedHostsFetched(String[] limitedHosts) {
        for (AlldebridObserver observer : mAlldebridObserver) {
            observer.onLimitedHostsFetched(limitedHosts);
        }
    }

    public void notifyObserverDownloadInformationsFetched(String[] downloadedInfos) {
        for (AlldebridObserver observer : mAlldebridObserver) {
            observer.onDownloadInformationsFetched(downloadedInfos);
        }
    }


}
