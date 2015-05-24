package com.malek.alldebrid.API.abstracted;

import android.content.Context;

import com.malek.alldebrid.API.API_Alldebrid;
import com.malek.alldebrid.API.persistence.PreferencePersister;

public enum SingletonHolder {
    SINGLETON;
    private AbstractDebrider debrider = new API_Alldebrid();
    private AbstractPersister persister = new PreferencePersister();

    public AbstractDebrider getDebrider() {
        return debrider;
    }

    public AbstractPersister getPersister() {
        return persister;
    }

    public void init(Context context) {
        debrider.init(context);
        persister.init(context);
    }
}
