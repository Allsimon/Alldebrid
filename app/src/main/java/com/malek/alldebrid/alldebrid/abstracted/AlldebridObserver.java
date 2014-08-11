package com.malek.alldebrid.alldebrid.abstracted;

import com.malek.alldebrid.alldebrid.Link;
import com.malek.alldebrid.alldebrid.Torrent;

public interface AlldebridObserver {

    public void onLinkRestrained(Link link);

    public void onTorrentAdded(Torrent torrent);

    public void onTorrentRemoved(Torrent torrent);

    public void onSomethingBugged(int status, String text);

    public void onLogin(int status);

    public void onTorrentFetched(Torrent[] torrents);

    public void onLimitedHostsFetched(String[] limitedHosts);

    public void onDownloadInformationsFetched(String[] downloadedInfos);

    public void onLinkRestrainFailed(int status);


}
