package com.malek.alldebrid.API.abstracted;


import com.malek.alldebrid.API.pojo.Link;
import com.malek.alldebrid.API.pojo.Torrent;

public interface DebridObserver {

    public void onLinkRestrained(Link link);

    public void onTorrentAdded(Torrent torrent);

    public void onTorrentRemoved(Torrent torrent);

    public void onSomethingBugged(int status, String text);

    public void onLogin(int status);

    public void onTorrentFetched(Torrent[] torrents);

    public void onLimitedHostsFetched(String[] limitedHosts);

    public void onDownloadInformationsFetched(String[] downloadedInfos);

    public void onLinkRestrainFailed(Link link, String error);


}
