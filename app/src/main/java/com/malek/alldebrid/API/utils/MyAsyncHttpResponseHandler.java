package com.malek.alldebrid.API.utils;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.malek.alldebrid.API.abstracted.SingletonHolder;
import com.malek.alldebrid.utils.Logg;

import org.apache.http.Header;

public abstract class MyAsyncHttpResponseHandler extends AsyncHttpResponseHandler {

    @Override
    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
        Logg.e(error);
        SingletonHolder.SINGLETON.getDebrider().notifyObserverSomethingBugged(statusCode, error.getMessage());
    }

    @Override
    public void onProgress(long bytesWritten, long totalSize) {
    }
}
