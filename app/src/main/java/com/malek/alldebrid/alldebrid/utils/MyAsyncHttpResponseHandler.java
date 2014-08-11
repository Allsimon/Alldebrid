package com.malek.alldebrid.alldebrid.utils;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.malek.alldebrid.utils.Logg;

import org.apache.http.Header;

public abstract class MyAsyncHttpResponseHandler extends AsyncHttpResponseHandler {

    @Override
    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
        Logg.e(error);
    }

    @Override
    public void onProgress(int bytesWritten, int totalSize) {
    }
}
