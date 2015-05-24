package com.malek.alldebrid.API.abstracted;

import android.content.Context;

import com.malek.alldebrid.API.pojo.Account;

public interface AbstractPersister {
    public static final String COOKIE = "cookie";
    public static final String TORRENT_NUMBERS = "torrentNumbers";
    public static final String LOGIN = "login";
    public static final String EMAIL = "email";
    public static final String EXPIRATION_DATE = "date";
    public static final String USERNAME = "pseudo";
    public static final String LAST_LOGIN = "lastLogin";
    public static final String TYPE = "type";
    public static final String PASSWORD = "password";

    public void persist(Account account);

    public void persistLogin(String username, String password);

    public Account getAccount();

    public void init(Context context);

    public String getCookie();

    public int getTorrentsAmount();

    public void persistTorrentsAmount(int torrentsAmount);

    public String getCookieWithUID();

    public void clear();
}
