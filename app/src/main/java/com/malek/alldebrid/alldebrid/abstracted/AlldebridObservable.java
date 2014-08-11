package com.malek.alldebrid.alldebrid.abstracted;

import com.malek.alldebrid.alldebrid.Link;
import com.malek.alldebrid.alldebrid.Torrent;

public interface AlldebridObservable {

    public void registerObserver(AlldebridObserver observer);

    public void removeObserver(AlldebridObserver observer);

    public void notifyObserverLinkRestrained(Link link);
    public void notifyObserverLinkRestrainFailed(int status);

    public void notifyObserverTorrentAdded(Torrent torrent);

    public void notifyObserverTorrentRemoved(Torrent torrent);

    public void notifyObserverSomethingBugged(int status, String text);

    public void notifyObserverLoginStatus(int status);

    public void notifyObserverTorrentListFetched(Torrent[] torrents);

    public void notifyObserverLimitedHostsFetched(String[] limitedHosts);

    public void notifyObserverDownloadInformationsFetched(String[] downloadedInfos);
}
