package com.malek.alldebrid.API.persistence;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.malek.alldebrid.API.abstracted.AbstractPersister;
import com.malek.alldebrid.API.pojo.Account;
import com.malek.alldebrid.API.utils.Utils;

public class PreferencePersister implements AbstractPersister {
    protected SharedPreferences mPreferences;


    @Override
    public void persist(Account account) {
        SharedPreferences.Editor editor = mPreferences.edit();
        put(COOKIE, account.getCookie(), editor);
        put(USERNAME, account.getUsername(), editor);
        put(EXPIRATION_DATE, Utils.format(account.getExpirationDate()), editor);
        put(TYPE, account.getType(), editor);
        put(EMAIL, account.getEmailAdress(), editor);
        put(PASSWORD, account.getPassword(), editor);
        put(LAST_LOGIN, Utils.format(account.getLastLogin()), editor);
        editor.apply();
    }

    public void persistLogin(String username, String password) {
        Account account = getAccount();
        account.setUsername(username);
        account.setPassword(password);
        persist(account);
    }

    @Override
    public Account getAccount() {
        Account account = new Account();
        account.setCookie(get(COOKIE));
        account.setUsername(get(USERNAME));
        account.setExpirationDate(Utils.parse(get(EXPIRATION_DATE)));
        account.setType(get(TYPE));
        account.setEmailAdress(get(EMAIL));
        account.setPassword(get(PASSWORD));
        account.setLastLogin(Utils.parse(get(LAST_LOGIN)));
        return account;
    }

    private String get(String preferences) {
        return mPreferences.getString(preferences, "");
    }

    private void put(String preferences, String values, SharedPreferences.Editor editor) {
        if (values != null && values.length() != 0)
            editor.putString(preferences, values);
    }

    @Override
    public void init(Context context) {
        this.mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String getCookie() {
        return mPreferences.getString(COOKIE, "");
    }

    public String getCookieWithUID() {
        return "uid=" + mPreferences.getString(COOKIE, "");
    }

    public String getLogin() {
        return mPreferences.getString(LOGIN, "apuoi$$12");
    }

    public int getTorrentsAmount() {
        return mPreferences.getInt(TORRENT_NUMBERS, 0);
    }

    public String getPassword() {
        return mPreferences.getString(PASSWORD, "apuoi$$12");
    }

    @Override
    public void clear() {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public void persistTorrentsAmount(int torrentsAmount) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(TORRENT_NUMBERS, torrentsAmount);
        editor.apply();
    }

}
