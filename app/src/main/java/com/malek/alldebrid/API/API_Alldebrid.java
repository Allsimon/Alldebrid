package com.malek.alldebrid.API;

import android.annotation.SuppressLint;
import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.malek.alldebrid.API.abstracted.AbstractDebrider;
import com.malek.alldebrid.API.abstracted.SingletonHolder;
import com.malek.alldebrid.API.pojo.Link;
import com.malek.alldebrid.API.pojo.Torrent;
import com.malek.alldebrid.API.utils.MyAsyncHttpResponseHandler;
import com.malek.alldebrid.API.utils.Utils;
import com.malek.alldebrid.API.utils.XMLHandler;
import com.malek.alldebrid.R;

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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.xml.parsers.SAXParserFactory;

public class API_Alldebrid extends AbstractDebrider {
    CookieStore mCookieStore;


    public void init(Context context) {
        super.init(context);
        mCookieStore = new PersistentCookieStore(context);
    }

    public void asyncGet(String URL, MyAsyncHttpResponseHandler asyncHttpResponseHandler) {
        AsyncHttpClient ahc = new AsyncHttpClient();
        ahc.addHeader("Cookie", SingletonHolder.SINGLETON.getPersister().getCookieWithUID());
        ahc.setCookieStore(mCookieStore);
        ahc.get(URL, asyncHttpResponseHandler);
    }


    @Override
    public void getTorrents() {
        asyncGet("http://www.alldebrid.com/api/torrent.php?json2=true",
                new MyAsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        String response = Utils.parseByte(responseBody);
                        Torrent[] torrents = Utils.parseTorrentsLink(response);
                        notifyObserverTorrentListFetched(torrents);
                    }
                });
    }

    @Override
    public void getLimitedHostsAndDownloadInfos() {
        asyncGet("http://www.alldebrid.com/service/",
                new MyAsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        String response = Utils.parseByte(responseBody);
                        Document doc = Jsoup.parse(response);
                        Elements elements = doc.select("#downloaders_right_col > li");
                        notifyObserverLimitedHostsFetched(getTextFromElements(elements));
                        elements = doc.select(".stats_informations > li");
                        notifyObserverDownloadInformationsFetched(getTextFromElements(elements));
                    }
                });
    }


    @Override
    public void addTorrent(String data, boolean isMagnet, boolean splitTheFile) {
        try {
            String URL = "http://upload.alldebrid.com/uploadtorrent.php";
            AsyncHttpClient ahc = new AsyncHttpClient();
            RequestParams rp = new RequestParams();
            rp.put("uid", SingletonHolder.SINGLETON.getPersister().getCookie());
            rp.put("domain", "http://www.alldebrid.fr/torrent/");
            if (splitTheFile) {
                rp.put("splitfile", "1");
            }
            if (isMagnet) {
                rp.put("magnet", data);
                rp.put("uploadedfile", "");
            } else {
                File file = new File(data);
                if (file.length() / 1024 >= 800)
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
        asyncGet("http://www.alldebrid.com/torrent/?action=remove&id=" + torrent.getId(),
                new MyAsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        notifyObserverTorrentRemoved(torrent);
                    }
                });
    }

    @Override
    public void unrestrainLink(String url) {
        final String URL = "http://www.alldebrid.com/service.php?json=true&jd=true&link="
                + url;
        final Link link = new Link();
        link.setOriginalLink(url);
        try {

            AsyncHttpClient ahc = new AsyncHttpClient();
            ahc.addHeader("Cookie", SingletonHolder.SINGLETON.getPersister().getCookieWithUID());
            ahc.get(URL, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = Utils.parseByte(responseBody);
                    try {
                        JSONObject jObj = new JSONObject(response);
                        link.setUnrestrainedLink(jObj.getString("link"));
                        link.setName(jObj.getString("filename"));
                        link.setHost(jObj.getString("host"));
                        if (!jObj.isNull("filesize")) {
                            link.setWeight(jObj.getString("filesize"));
                        }
                        if (!jObj.isNull("error")) {
                            String error = jObj.getString("error");
                            switch (error) {
                                case "":
                                    notifyObserverLinkRestrained(link);
                                    break;
                                default:
                                    notifyObserverLinkRestrainFailed(link.bugged(), getErrorMessage(error));
                                    break;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        notifyObserverLinkRestrainFailed(link.bugged(), e.getMessage());
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    notifyObserverLinkRestrainFailed(link.bugged(), error.getMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            notifyObserverLinkRestrainFailed(link.bugged(), e.getMessage());
        }
    }

    @Override
    public void login(String username, String password) {
        asyncGet("http://www.alldebrid.com/api.php?action=info_user&login=" + username + "&pw=" + password,
                new MyAsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        parseLogin(responseBody);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        notifyObserverLoginStatus(LOGIN_FAILED);
                    }
                });
        SingletonHolder.SINGLETON.getPersister().persistLogin(username, password);
    }

    public void softLogin(String username, String password) {
        Date lastLogin = SingletonHolder.SINGLETON.getPersister().getAccount().getLastLogin();
        if (lastLogin != null) {
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yy", Locale.US);
            if (Utils.daysBetween(lastLogin, new Date()) > 7)
                login(username, password);
        } else {
            login(username, password);
        }
    }

    @Override
    public boolean isLogged() {
        // Username and password length must be !=0 and he should have logged in in last 7 days
        return SingletonHolder.SINGLETON.getPersister().getAccount().getUsername().length() != 0 &&
                SingletonHolder.SINGLETON.getPersister().getAccount().getPassword().length() != 0 &&
                Utils.daysBetween(SingletonHolder.SINGLETON.getPersister().getAccount().getLastLogin(), new Date()) < 7;
    }


    public void addCookie() {
        BasicClientCookie newCookie = new BasicClientCookie("uid", SingletonHolder.SINGLETON.getPersister().getCookie());
        newCookie.setVersion(1);
        newCookie.setDomain("www.alldebrid.com");
        newCookie.setPath("/");
        mCookieStore.addCookie(newCookie);
        newCookie = new BasicClientCookie("Cookie", SingletonHolder.SINGLETON.getPersister().getCookieWithUID());
        newCookie.setVersion(1);
        newCookie.setDomain("www.alldebrid.com");
        newCookie.setPath("/");
        mCookieStore.addCookie(newCookie);
    }

    @SuppressLint("CommitPrefEdits")
    private void parseLogin(byte[] bytes) {
        String response = Utils.parseByte(bytes);
        if (response.startsWith("too mutch error"))
            notifyObserverLoginStatus(LOGIN_FAILED);
        else {
            try {
                XMLReader xmlR = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
                xmlR.setContentHandler(new XMLHandler());
                InputSource is2 = new InputSource();
                is2.setCharacterStream(new StringReader(response));
                xmlR.parse(is2);

                SingletonHolder.SINGLETON.getPersister().persist(XMLHandler.getAccount());

                addCookie();
                notifyObserverLoginStatus(LOGIN_SUCCESSFUL);
            } catch (Exception e) {
                notifyObserverLoginStatus(LOGIN_FAILED);
                e.printStackTrace();
            }
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

    public String getErrorMessage(String error) {
        switch (error) {
            case "premium":
                return mContext.getResources().getString(R.string.error_account_not_premium);
            case "ipnotallowed":
                return mContext.getResources().getString(R.string.error_ip_not_allowed);
            case "hostmaintenance":
                return mContext.getResources().getString(R.string.error_host_maintenance);
            case "noserveravalible":
                return mContext.getResources().getString(R.string.error_no_server_available);
            case "notraffic(1024/1024)":
                return mContext.getResources().getString(R.string.error_no_traffic);
            case "downonhoster":
                return mContext.getResources().getString(R.string.error_down_on_hoster);
            case "hosternotsupported":
                return mContext.getResources().getString(R.string.error_hoster_not_supported);
            case "maxsimudown(5/5)":
                return mContext.getResources().getString(R.string.error_max_simu_down);
            default:
                return mContext.getResources().getString(R.string.error_unknown);
        }
    }
}
