package com.malek.alldebrid.API.utils;

import com.malek.alldebrid.API.pojo.Torrent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {
    private static final SimpleDateFormat format = new SimpleDateFormat("dd-MM-yy", Locale.US);

    /**
     * @return Approximation of the number of days between 2 dates
     */
    public static int daysBetween(Date d1, Date d2) {
        return (int) ((d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
    }

    public static Date parse(String date) {
        try {
            return format.parse(date);
        } catch (ParseException ignored) {
            ignored.printStackTrace();
            return new Date();
        }
    }

    public static String format(Date date) {
        return format.format(date);
    }

    public static Torrent[] parseTorrentsLink(String link) {
        if ("Invalid cookie.".equals(link))
            return null;
        String links[] = link.replace("[[", "").replace("]]", "")
                .split("\\],\\[");
        Torrent torrent[] = new Torrent[links.length];
        for (int i = 0; i < links.length; i++) {
            String test[] = links[i].split("\",\"");
            torrent[i] = new Torrent();
            torrent[i].setId(Integer.parseInt(test[1].replace("\"", "")));
            torrent[i].setName(test[3]);
            torrent[i].setStatus(test[4]);
            torrent[i].setDownloaded(test[5]);
            torrent[i].setWeight(test[6]);
            torrent[i].setSeederNumber(test[7]);
            torrent[i].setDownloadSpeed(test[8]);
            if (test[4].contains("finished"))
                torrent[i].setUnrestrainedLink(test[10].replace(",;,", "\n"));
        }
        return torrent;
    }

    public static String parseByte(byte[] bytes) {
        try {
            return new String(bytes, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
