package com.malek.alldebrid.alldebrid;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.malek.alldebrid.alldebrid.abstracted.AbstractAlldebrid;
import com.malek.alldebrid.alldebrid.utils.MyAsyncHttpResponseHandler;
import com.malek.alldebrid.alldebrid.utils.XMLHandler;

import org.apache.http.Header;
import org.apache.http.client.CookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.File;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class API_Alldebrid extends AbstractAlldebrid {
    CookieStore mCookieStore;

    private API_Alldebrid(Context context) {
        super(context);
        mCookieStore = new PersistentCookieStore(context);
    }

    public static void init(Context context) {
        SingletonHolder.instance = new API_Alldebrid(context);
    }

    public static AbstractAlldebrid getInstance() {
        return SingletonHolder.getInstance();
    }


    @Override
    public void getTorrent() {
        String URL = "http://www.alldebrid.com/api/torrent.php?json2=true";
        AsyncHttpClient ahc = new AsyncHttpClient();
        ahc.addHeader("Cookie", getCookieWithUID());
        ahc.get(URL, new MyAsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = parseByte(responseBody);
                Torrent[] torrents = parseTorrentsLink(response);
                notifyObserverTorrentListFetched(torrents);
            }
        });
    }

    @Override
    public void getLimitedHostsAndDownloadInfos() {
        String URL = "http://www.alldebrid.com/service/";
        try {
            AsyncHttpClient ahc = new AsyncHttpClient();
            ahc.setCookieStore(mCookieStore);
            ahc.get(URL, new MyAsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = parseByte(responseBody);
                    Document doc = Jsoup.parse(response);
                    Elements elements = doc.select("#downloaders_right_col > li");
                    notifyObserverLimitedHostsFetched(getTextFromElements(elements));
                    elements = doc.select(".stats_informations > li");
                    notifyObserverDownloadInformationsFetched(getTextFromElements(elements));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String[] getTextFromElements(Elements elements) {
        String[] texts = new String[elements.size()];
        int i = 0;
        for (Element element : elements) {
            texts[i] = element.text();
            i++;
        }
        return texts;
    }

    @Override
    public void addTorrent(String data, boolean isMagnet, boolean splitTheFile) {
        try {
            String URL = "http://upload.alldebrid.com/uploadtorrent.php";
            AsyncHttpClient ahc = new AsyncHttpClient();
            RequestParams rp = new RequestParams();
            rp.put("uid", getCookie());
            rp.put("domain", "http://www.alldebrid.fr/torrent/");
            if (splitTheFile) {
                rp.put("splitfile", "1");
            }
            if (isMagnet) {
                rp.put("magnet", data);
                rp.put("uploadedfile", "");
            } else {
                File file = new File(data);
                if (file.length() / 1024 >= 500)
                    return;
                rp.put("magnet", "");
                rp.put("uploadedfile", file);
            }
            ahc.post(URL, rp, new MyAsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    notifyObserverTorrentAdded(null);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeTorrent(final Torrent torrent) {
        String URL = "http://www.alldebrid.com/torrent/?action=remove&id="
                + torrent.getId();
        AsyncHttpClient ahc = new AsyncHttpClient();
        ahc.addHeader("Cookie", getCookieWithUID());
        ahc.get(URL, new MyAsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                notifyObserverTorrentRemoved(torrent);
            }
        });
    }

    @Override
    public void unrestrainLink(String link) {
        String URL = "http://www.alldebrid.com/service.php?json=true&link="
                + link;
        try {

            AsyncHttpClient ahc = new AsyncHttpClient();
            ahc.addHeader("Cookie", getCookieWithUID());
            ahc.get(URL, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = parseByte(responseBody);
                    Link link = new Link();
                    try {
                        JSONObject jObj = new JSONObject(response);
                        link.setLink(jObj.getString("link"));
                        link.setName(jObj.getString("filename"));
                        link.setHost(jObj.getString("host"));
                        if (!jObj.isNull("filesize")) {
                            link.setWeight(jObj.getString("filesize"));
                        }
                        if (!jObj.isNull("error")) {
                            String error = jObj.getString("error");
                            if ("This link isn't valid or not supported.".equals(error)) {
                                notifyObserverLinkRestrainFailed(LINK_RESTRAIN_WRONG_LINK);
                            } else if ("".equals(error)) {
                                notifyObserverLinkRestrained(link);
                            } else {
                                notifyObserverLinkRestrainFailed(LINK_RESTRAIN_FAILED_UNKNOWN);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        notifyObserverLinkRestrainFailed(LINK_RESTRAIN_WRONG_LINK);

                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    notifyObserverLinkRestrainFailed(LINK_RESTRAIN_FAILED_UNKNOWN);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            notifyObserverLinkRestrainFailed(LINK_RESTRAIN_HOST_DOWN);
        }
    }

    @Override
    public void login(String username, String password) {
        String URL = "http://www.alldebrid.com/api.php?action=info_user&login="
                + username + "&pw=" + password;
        AsyncHttpClient ahc = new AsyncHttpClient();
        RequestParams rp = new RequestParams();
        ahc.get(URL, rp, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = parseByte(responseBody);
                if (response.startsWith("too mutch error")) {
                    notifyObserverLoginStatus(LOGIN_FAILED);
                }
                parseLogin(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                notifyObserverLoginStatus(LOGIN_FAILED);
            }
        });
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(LOGIN, username);
        editor.putString(PASSWORD, password);
        editor.apply();
    }

    public void softLogin(String username, String password) {
        String last_login = mPreferences.getString(LAST_LOGIN, "");
        if (last_login.length() > 1) {
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yy");
            try {
                Date lastLogin = format.parse(last_login);
                if (daysBetween(lastLogin, new Date()) > 7)
                    login(username, password);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            login(username, password);
        }
    }

    @Override
    public boolean isLogged() {
        String cookie = mPreferences.getString(COOKIE, "");
        return cookie.length() > 2;
    }

    public int daysBetween(Date d1, Date d2) {
        return (int) ((d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
    }

    public void addCookie() {
        BasicClientCookie newCookie = new BasicClientCookie("uid", getCookie());
        newCookie.setVersion(1);
        newCookie.setDomain("www.alldebrid.com");
        newCookie.setPath("/");
        mCookieStore.addCookie(newCookie);
        newCookie = new BasicClientCookie("Cookie", getCookieWithUID());
        newCookie.setVersion(1);
        newCookie.setDomain("www.alldebrid.com");
        newCookie.setPath("/");
        mCookieStore.addCookie(newCookie);
    }

    @SuppressLint("CommitPrefEdits")
    private void parseLogin(String response) {
        SAXParserFactory saxPF = SAXParserFactory.newInstance();
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yy");
        SAXParser saxP;
        try {
            saxP = saxPF.newSAXParser();
            XMLReader xmlR = saxP.getXMLReader();
            XMLHandler myXMLHandler = new XMLHandler();
            xmlR.setContentHandler(myXMLHandler);
            InputSource is2 = new InputSource();
            is2.setCharacterStream(new StringReader(response));
            xmlR.parse(is2);
            String cookie = XMLHandler.getXMLData().getCookie();
            String pseudo = XMLHandler.getXMLData().getPseudo();
            String date = XMLHandler.getXMLData().getDate();
            String type = XMLHandler.getXMLData().getType();
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putString(COOKIE, cookie);
            editor.putString(PSEUDO, pseudo);
            editor.putString(DATE, date);
            editor.putString(TYPE, type);
            editor.putString(LAST_LOGIN, format.format(new Date()));
            editor.commit();
            addCookie();
            notifyObserverLoginStatus(LOGIN_SUCCESSFUL);
        } catch (Exception e) {
            if (response.startsWith("flood")) {
                notifyObserverLoginStatus(LOGIN_FAILED);
            }
        }
    }
}
